package com.gyc98.opensource.polyglotpress.service.translate.impl;

import com.alibaba.cloud.ai.dashscope.agent.DashScopeAgent;
import com.alibaba.cloud.ai.dashscope.agent.DashScopeAgentOptions;
import com.alibaba.cloud.ai.dashscope.api.DashScopeAgentApi;
import com.alibaba.fastjson2.JSON;
import com.gyc98.opensource.polyglotpress.model.SubtitleItem;
import com.gyc98.opensource.polyglotpress.model.SubtitleModel;
import com.gyc98.opensource.polyglotpress.service.translate.AiTranslateService;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiTranslateServiceImpl implements AiTranslateService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AiTranslateServiceImpl.class);

    private final DashScopeAgent agent;

    public AiTranslateServiceImpl(DashScopeAgentApi dashscopeAgentApi) {
        this.agent = new DashScopeAgent(dashscopeAgentApi);
    }

    @Value("${spring.ai.dashscope.agent.app-id}")
    private String appId;
    @Override
    public void translateSubtitle(SubtitleModel subtitleModel) {
        for (SubtitleItem subtitleItem : subtitleModel.getItems()) {
            // 1. 构建请求
            TranslatePrompt prompt = new TranslatePrompt();
            prompt.setStartTime(subtitleItem.getStartTime());
            prompt.setEndTime(subtitleItem.getEndTime());
            prompt.setOriginalText(subtitleItem.getOriginalText());
            prompt.setTargetLocale(subtitleModel.getTargetLocale());
            String message = JSON.toJSONString(prompt);

            // 2. 调用ai
            String output = invoke(message);
            TranslateOutput translateOutput = JSON.parseObject(output, TranslateOutput.class);

            // 3. 设置翻译
            subtitleItem.setTranslatedText(translateOutput.getTranslatedText());
        }
    }

    private String invoke(String message) {
        LOGGER.info("invoke ai message: {}", message);
        ChatResponse response = agent.call(new Prompt(message, DashScopeAgentOptions.builder().withAppId(appId).build()));
        if (response == null || response.getResult() == null) {
            LOGGER.error("chat response is null");
            return "chat response is null";
        }

        AssistantMessage app_output = response.getResult().getOutput();
        String content = app_output.getText();

        DashScopeAgentApi.DashScopeAgentResponse.DashScopeAgentResponseOutput output = (DashScopeAgentApi.DashScopeAgentResponse.DashScopeAgentResponseOutput) app_output.getMetadata().get("output");
        List<DashScopeAgentApi.DashScopeAgentResponse.DashScopeAgentResponseOutput.DashScopeAgentResponseOutputDocReference> docReferences = output.docReferences();
        List<DashScopeAgentApi.DashScopeAgentResponse.DashScopeAgentResponseOutput.DashScopeAgentResponseOutputThoughts> thoughts = output.thoughts();

        LOGGER.info("content:\n{}\n\n", content);

        if (docReferences != null && !docReferences.isEmpty()) {
            for (DashScopeAgentApi.DashScopeAgentResponse.DashScopeAgentResponseOutput.DashScopeAgentResponseOutputDocReference docReference : docReferences) {
                LOGGER.info("{}\n\n", docReference);
            }
        }

        if (thoughts != null && !thoughts.isEmpty()) {
            for (DashScopeAgentApi.DashScopeAgentResponse.DashScopeAgentResponseOutput.DashScopeAgentResponseOutputThoughts thought : thoughts) {
                LOGGER.info("{}\n\n", thought);
            }
        }

        return content;
    }

    @Data
    private class TranslatePrompt {
        private String startTime;
        private String endTime;
        private String originalText;
        private String targetLocale;
    }

    @Data
    private class TranslateOutput {
        private String translatedText;
    }
}

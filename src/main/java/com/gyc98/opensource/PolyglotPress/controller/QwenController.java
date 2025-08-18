package com.gyc98.opensource.PolyglotPress.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class QwenController {

    // 使用 SLF4J 创建日志记录器
    private static final Logger log = LoggerFactory.getLogger(QwenController.class);

    private final ChatClient chatClient;

    public QwenController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        log.info("收到用户消息: {}", message);  // 使用 SLF4J 打印日志
        try {
            String response = chatClient.call(message);
            log.debug("模型返回: {}", response);
            return response;
        } catch (Exception e) {
            log.error("调用 AI 服务失败", e);
            return "抱歉，服务暂时不可用。";
        }
    }

    // 其他方法...
}
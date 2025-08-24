package com.gyc98.opensource.polyglotpress.controller;


import com.alibaba.fastjson2.JSON;
import com.gyc98.opensource.polyglotpress.model.SubtitleModel;
import com.gyc98.opensource.polyglotpress.service.translate.AiTranslateService;
import com.gyc98.opensource.polyglotpress.service.video.SubtitleExtractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/video")
public class SubtitleExtractController {
    @Autowired
    private SubtitleExtractService subtitleExtractService;

    @Autowired
    private AiTranslateService aiTranslateService;

    @PostMapping("/extraceSubtitle")
    public String extraceSubtitle(@RequestParam String videoPath) {
        List<SubtitleModel> res = subtitleExtractService.extractSubtitleFromVideo(videoPath);
        return JSON.toJSONString(res);
    }

    @PostMapping("/extraceSubtitleAndTranslate")
    public String extraceSubtitleAndTranslate(@RequestParam String videoPath, @RequestParam int subtitleIndex, String targetLang) {
        List<SubtitleModel> res = subtitleExtractService.extractSubtitleFromVideo(videoPath);
        SubtitleModel subtitleModel = res.get(subtitleIndex);
        subtitleModel.setTargetLocale(targetLang);

        // 翻译
        String id = aiTranslateService.asyncTranslateSubtitle(subtitleModel, videoPath);

        // 下次带着id来查询
        return id;
    }

    @PostMapping("/query")
    public String extraceSubtitleAndTranslate(@RequestParam String id) {
        // 翻译
        SubtitleModel subtitleModel = aiTranslateService.query(id);

        return JSON.toJSONString(subtitleModel);
    }
}

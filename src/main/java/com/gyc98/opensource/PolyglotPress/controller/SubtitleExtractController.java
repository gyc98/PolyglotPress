package com.gyc98.opensource.polyglotpress.controller;


import com.alibaba.fastjson2.JSON;
import com.gyc98.opensource.polyglotpress.service.video.SubtitleExtractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/video")
public class SubtitleExtractController {
    @Autowired
    private SubtitleExtractService subtitleExtractService;

    @PostMapping("/extraceSubtitle")
    public String call(@RequestParam String videoPath) {
        Object res = subtitleExtractService.extractSubtitleFromVideo(videoPath);
        return JSON.toJSONString(res);
    }
}

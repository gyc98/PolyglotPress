package com.gyc98.opensource.polyglotpress.model;

import lombok.Data;

@Data
public class SubtitleItem {
    /**
     * 本条字幕的起始时间
     */
    private String startTime;

    /**
     * 本条字幕的结束时间
     */
    private String endTime;

    /**
     * 原始语言字幕文本
     */
    private String originalText;

    /**
     * 翻译后的字幕文本
     */
    private String translatedText;
}

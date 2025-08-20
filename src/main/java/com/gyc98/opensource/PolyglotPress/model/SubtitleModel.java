package com.gyc98.opensource.polyglotpress.model;

import lombok.Data;

import java.util.List;
import java.util.Locale;

@Data
public class SubtitleModel {
    /**
     * 按照时间排序的字幕
     */
    private List<SubtitleItem> items;

    /**
     * 原始字幕语言
     */
    private String originLocale;

    /**
     * 目标字幕语言
     */
    private String targetLocale;
}

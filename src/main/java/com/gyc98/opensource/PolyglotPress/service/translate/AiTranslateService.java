package com.gyc98.opensource.polyglotpress.service.translate;

import com.gyc98.opensource.polyglotpress.model.SubtitleModel;

import java.util.Locale;

public interface AiTranslateService {
    String asyncTranslateSubtitle(SubtitleModel subtitleModel, String videoPath);

    SubtitleModel query(String id);
}

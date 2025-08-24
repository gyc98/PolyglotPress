package com.gyc98.opensource.polyglotpress.service.video;

import com.gyc98.opensource.polyglotpress.model.SubtitleModel;

import java.util.List;

public interface SubtitleExtractService {
    /**
     * 从视频文件中提取字幕
     *
     * @param videoPath
     * @return
     */
    List<SubtitleModel> extractSubtitleFromVideo(String videoPath);

    /**
     * 将字幕提取到srt文件中
     * @param subtitleModel
     * @return
     */
    String exportSubtitleToFile(SubtitleModel model, String videoPath);
}

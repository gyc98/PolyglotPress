package com.gyc98.opensource.polyglotpress.service.video.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import com.gyc98.opensource.polyglotpress.model.SubtitleItem;
import com.gyc98.opensource.polyglotpress.model.SubtitleModel;
import com.gyc98.opensource.polyglotpress.service.video.SubtitleExtractService;
import com.gyc98.opensource.polyglotpress.util.ProcessUtil;
import com.gyc98.opensource.polyglotpress.util.SubtitlePathGenerator;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class SubtitleExtractServiceImpl implements SubtitleExtractService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubtitleExtractServiceImpl.class);

    @Override
    public List<SubtitleModel> extractSubtitleFromVideo(String videoPath) {
        // 1. 先解析出来有哪些字幕类型
        Streams streams = fetchSubtitleStreamMetaData(videoPath);

        // 2. 逐个提取字幕
        List<SubtitleModel> subtitleModels = new ArrayList<>();
        for (Streams.StreamInfo streamInfo : streams.getStreams()) {
            // 随机生成字幕路径
            String subtitlePath = SubtitlePathGenerator.generateSubtitlePath(videoPath, streamInfo.getIndex());
            // 提取字幕到文件
            extractSubtitleToFile(videoPath, streamInfo.getIndex(), subtitlePath);
            // 将字幕读取出来
            SubtitleModel subtitleModel = readSubtitleFromFile(subtitlePath);
            subtitleModels.add(subtitleModel);
        }

        return subtitleModels;
    }

    private Streams fetchSubtitleStreamMetaData(String mkvPath) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        // 构建 ffprobe 命令
        processBuilder.command(
                "ffprobe",
                "-v", "error",           // 只显示错误信息
                "-show_streams",         // 显示流信息
                "-select_streams", "s",  // 仅选择字幕流 (s)
                "-of", "json",           // 输出格式为 JSON
                mkvPath            // 输入文件
        );

        String output = ProcessUtil.runProcess(processBuilder);
        return JSON.parseObject(output, Streams.class);
    }

    private void extractSubtitleToFile(String mkvPath, int streamIndex, String srtPath) {
        LOGGER.info("extrace subtitle to file, mkvPath: {}, , streamIndex: {}, , srtPath: {}",
                mkvPath, streamIndex, srtPath);
        // ffmpeg -i test.mkv -map 0:2 -scodec copy eng.srt
        ProcessBuilder processBuilder = new ProcessBuilder();
        // 构建 ffprobe 命令
        processBuilder.command(
                "ffmpeg",
                "-i", mkvPath,           // 输入文件
                "-map", "0:" + streamIndex,      // 指定流
                "-scodec", "copy",  // 复制
                srtPath // 输出字幕文件
        );
        String output = ProcessUtil.runProcess(processBuilder);
        LOGGER.info("extrace subtitle to file, output: {}", output);
    }

    private SubtitleModel readSubtitleFromFile(String srtPath) {
        SubtitleModel model = new SubtitleModel();
        List<SubtitleItem> items = new ArrayList<>();

        // 可根据实际需求设置语言，这里作为示例
        model.setOriginLocale(Locale.ENGLISH);
        model.setTargetLocale(Locale.SIMPLIFIED_CHINESE);

        // 匹配 SRT 时间轴格式：00:00:20,000 --> 00:00:24,400
        Pattern timePattern = Pattern.compile("\\d{2}:\\d{2}:\\d{2},\\d{3}\\s*-->\\s*\\d{2}:\\d{2}:\\d{2},\\d{3}");

        File file = new File(srtPath);
        if (!file.exists() || !file.canRead()) {
            System.err.println("字幕文件不存在或无法读取: " + srtPath);
            model.setItems(items);
            return model;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            SubtitleItem currentItem = null;
            boolean expectIndex = true; // 下一行应为序号

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // 空行表示一个字幕块结束
                if (line.isEmpty()) {
                    if (currentItem != null) {
                        items.add(currentItem);
                        currentItem = null;
                    }
                    expectIndex = true;
                    continue;
                }

                if (expectIndex) {
                    // 期望是数字序号
                    if (line.matches("\\d+")) {
                        expectIndex = false;
                        currentItem = new SubtitleItem();
                    }
                    // 如果不是数字，跳过或忽略（兼容性处理）
                } else if (currentItem != null && currentItem.getStartTime() == null) {
                    // 当前 item 存在且尚未设置时间，尝试解析时间行
                    if (timePattern.matcher(line).find()) {
                        // 分割开始和结束时间
                        String[] parts = line.split("-->");
                        if (parts.length >= 2) {
                            String start = parts[0].trim();  // 例如 "00:00:20,000"
                            String end = parts[1].trim();    // 例如 "00:00:24,400"

                            currentItem.setStartTime(start);
                            currentItem.setEndTime(end);
                        }
                    } else {
                        // 时间格式不匹配，丢弃当前项
                        currentItem = null;
                        expectIndex = true;
                    }
                } else if (currentItem != null) {
                    // 收集字幕文本（支持多行）
                    if (currentItem.getOriginalText() == null) {
                        currentItem.setOriginalText(line);
                    } else {
                        currentItem.setOriginalText(currentItem.getOriginalText() + "\n" + line);
                    }
                }
            }

            // 处理文件末尾没有空行的情况
            if (currentItem != null) {
                items.add(currentItem);
            }

        } catch (IOException e) {
            LOGGER.error("read subtitle error", e);
        }

        model.setItems(items);
        return model;
    }

    @Data
    private class Streams {
        private List<StreamInfo> streams;

        @Data
        private class StreamInfo {
            private int index;

            @JSONField(name = "codec_name")
            private String codecName;

            @JSONField(name = "codec_type")
            private String codecType;

            private StreamTag tags;

            @Data
            private class StreamTag {
                private String language;
            }
        }
    }

}

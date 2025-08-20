package com.gyc98.opensource.polyglotpress.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Random;

public class SubtitlePathGenerator {

    private static final Random RANDOM = new SecureRandom();
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * 生成指定长度的随机字符串
     */
    private static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    /**
     * 去掉文件名最后一个点（.）之后的扩展名部分
     */
    private static String removeExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName; // 没有扩展名则原样返回
    }

    /**
     * 根据输入文件路径，生成字幕输出路径（在同一目录下）
     *
     * @param videoFilePath 输入视频文件的完整路径（任意扩展名）
     * @param number        用户提供的数字
     * @param randomString  可选随机字符串（传 null 则自动生成）
     * @return 生成的 .srt 字幕文件完整路径
     */
    public static String generateSubtitlePath(String videoFilePath, int number) {
        if (videoFilePath == null || videoFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("输入文件路径不能为空");
        }

        File inputFile = new File(videoFilePath);
        String inputDir = inputFile.getParent(); // 获取父目录
        String fileName = inputFile.getName();

        // 去除扩展名
        String baseName = removeExtension(fileName);

        // 生成随机字符串
        String randomString = generateRandomString(8);

        // 构造新文件名：baseName_number_randomString.srt
        String subtitleFileName = baseName + "_" + number + "_" + randomString + ".srt";

        // 拼接完整路径：和原文件在同一目录
        Path outputPath = Paths.get(inputDir, subtitleFileName);
        return outputPath.toString();
    }
}
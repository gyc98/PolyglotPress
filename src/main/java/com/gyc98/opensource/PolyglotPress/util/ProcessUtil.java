package com.gyc98.opensource.polyglotpress.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProcessUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessUtil.class);

    public static String runProcess(ProcessBuilder processBuilder) {
        try {
            // 启动进程
            Process process = processBuilder.start();

            // 读取命令的标准输出 (stdout)
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
            reader.close();

            // 读取命令的错误输出 (stderr)，用于调试
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder errorOutput = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append(System.lineSeparator());
            }
            errorReader.close();

            // 等待命令执行完成
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                // 命令成功执行，返回 JSON 输出
                // trim() 去除末尾可能的换行符
                return output.toString().trim();
            } else {
                // 命令执行失败
                LOGGER.error("command exited with code " + exitCode);
                if (errorOutput.length() > 0) {
                    LOGGER.error("command exited with error: " + errorOutput);
                }
                return null;
            }

        } catch (IOException e) {
            LOGGER.error("command run exception", e);
            return null;
        } catch (InterruptedException e) {
            LOGGER.error("command run interrupted", e);
            Thread.currentThread().interrupt(); // 重新设置中断状态
            return null;
        }
    }
}

package com.luodaijun.imserver.utils;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created with IntelliJ IDEA.
 * User: luodaijun
 */
public class ExecUtil {
    private final static Logger logger = Logger.getLogger(ExecUtil.class);

    public static String executeShellResult(String shellCommand) {
        StringBuffer stringBuffer = new StringBuffer();
        BufferedReader bufferedReader = null;

        try {
            Process pid = null;
            String[] cmd = {"/bin/sh", "-c", shellCommand};
            // 执行Shell命令
            pid = Runtime.getRuntime().exec(cmd);
            if (pid != null) {
                // bufferedReader用于读取Shell的输出内容
                bufferedReader = new BufferedReader(new InputStreamReader(pid.getInputStream()), 1024);
                pid.waitFor();
            }
            String line = null;
            // 读取Shell的输出内容，并添加到stringBuffer中
            while (bufferedReader != null
                    && (line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line).append("\n");
            }
        } catch (Exception ioe) {
            logger.error("执行Shell命令时发生异常：", ioe);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {

                }
            }
        }
        return stringBuffer.toString();
    }

    public static void executeShell(String shellCommand) {
        try {
            Process pid = null;
            String[] cmd = {"/bin/sh", "-c", shellCommand};
            // 执行Shell命令
            pid = Runtime.getRuntime().exec(cmd);
        } catch (Exception ioe) {
            logger.error("执行Shell命令时发生异常：", ioe);
        }
    }
}

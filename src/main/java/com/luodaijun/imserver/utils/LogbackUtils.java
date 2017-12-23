package com.luodaijun.imserver.utils;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by luodaijun on 2015-03-26.
 * 说明:
 */
public class LogbackUtils {
    public static void initLogback(File logFile) {
        if (!logFile.exists()) {
            System.err.println("logback.xml not found,path:" + logFile.getAbsolutePath());
        } else {
            try {
                System.err.println("load logback.xml,path:" + logFile.getAbsolutePath());
                LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
                loggerContext.reset();
                JoranConfigurator joranConfigurator = new JoranConfigurator();
                joranConfigurator.setContext(loggerContext);
                joranConfigurator.doConfigure(logFile);
            } catch (JoranException e) {
                System.err.println("logback.xml load error");
            }
        }
    }
}

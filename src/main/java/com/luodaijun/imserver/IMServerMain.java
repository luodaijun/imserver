package com.luodaijun.imserver;

import com.luodaijun.imserver.core.NettyIMServer;
import com.luodaijun.imserver.utils.LogbackUtils;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by luodaijun on 2017/6/24.
 */
public class IMServerMain {
    public static void main(String[] args) throws Exception {
        initLogback();

        final NettyIMServer imServer = new NettyIMServer();


        imServer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                imServer.shutdown();
            }
        }));

        TimeUnit.HOURS.sleep(Integer.MAX_VALUE);
    }

    private static void initLogback() {
        File logFile = new File(System.getProperty("user.dir"), "conf/logback.xml");
        if (!logFile.exists()) {
            logFile = new File(System.getProperty("user.dir"), "src/main/conf/logback.xml");
        }

        LogbackUtils.initLogback(logFile);
    }
}

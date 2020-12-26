package com.luodaijun.imserver.utils;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

import java.io.File;

/**
 * Created by luodaijun on 2015-04-02.
 * 说明:
 */
public class IMConfig {
    private final static Logger logger = Logger.getLogger(IMConfig.class);

    private static XMLConfiguration configuration;

    static {
        load();
    }

    private static void load() {
        configuration = new XMLConfiguration();
        try {
            File logFile = new File(System.getProperty("user.dir"), "conf/im.conf.xml");
            if (!logFile.exists()) {
                logFile = new File(System.getProperty("user.dir"), "src/main/conf/im.conf.xml");
            }

            System.err.println("load im config from file " + logFile.getCanonicalPath());

            configuration.load(logFile);
        } catch (Exception e) {
            logger.error("load config error", e);
            System.exit(0);
        }
    }

    public final static class IM_SERVER {
        /**
         * 明文协议端口
         */
        public final static int PORT = configuration.getInt("server.port", 80);

        /**
         * TLS加密协议端口
         */
        public final static int SSL_PORT = configuration.getInt("server.sslPort", -1);

        /**
         * KEEPALIVE保持时间
         */
        public final static int KEEPALIVE_TIMEOUT_SECONDS = configuration.getInt("server.keepalived_timeout", 65);


        /**
         * I/O线程池大小
         */
        public final static int IO_THREAD_COUNT = configuration.getInt("server.ioThread", 20);


        /**
         * Worker线程池大小
         */
        public final static int WORK_THREAD_COUNT = configuration.getInt("server.workThread", 200);


        /**
         * 绑定本机IP
         */
        public static final String BIND_IP = configuration.getString("server.bindIp", "0.0.0.0");

        /**
         * 是否使用Native transports实现
         */
        public static final boolean USE_NATIVE_TRANSPORTS = configuration.getBoolean("server.useNativeTransports", false);
    }
}

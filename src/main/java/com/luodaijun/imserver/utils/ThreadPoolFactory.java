package com.luodaijun.imserver.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Created by luodaijun on 2015-02-16.
 * 说明:线程池工厂类
 */
public class ThreadPoolFactory {
    private static Logger logger = LoggerFactory.getLogger(ThreadPoolFactory.class);

    public static ThreadPoolExecutor newFixedThreadPool(final String threadGroupName, final int nThreads, final int queueLength) {
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(queueLength), new ThreadFactory() {
            private volatile int index = 1;
            //private final long unique = System.nanoTime();

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName(threadGroupName + "_" + index);
                index++;
                return thread;
            }
        });
        return threadPool;
    }

    public static ThreadFactory newThreadFactory(final String threadGroupName) {
        return new ThreadFactory() {
            private volatile int index = 1;
            //private final long unique = System.nanoTime();

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName(threadGroupName + "_" + index);
                index++;
                return thread;
            }
        };
    }

    public static ScheduledExecutorService newTimer(final String threadName) {
        return Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName(threadName);
                thread.setDaemon(true);
                return thread;
            }
        });
    }
}

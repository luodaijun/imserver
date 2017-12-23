package com.luodaijun.imserver.core.handler;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.luodaijun.imserver.utils.IMLog;
import io.netty.channel.Channel;

import java.util.concurrent.TimeUnit;

/**
 * Created by luodaijun on 2017/7/16.
 */
public class SessionCache {
    private static SessionCache ourInstance = new SessionCache();

    public static SessionCache getInstance() {
        return ourInstance;
    }

    private SessionCache() {
    }


    private Cache<String, Channel> channelCache = CacheBuilder.newBuilder().expireAfterAccess(6, TimeUnit.HOURS).maximumSize(50000).removalListener(new RemovalListener<String, Channel>() {
        @Override
        public void onRemoval(RemovalNotification<String, Channel> removalNotification) {
            IMLog.IM_LOG.warn("userId[" + removalNotification.getKey() + "] expired, remove session.");
        }
    }).build();

    public void put(String userId, Channel channel) {
        if (userId == null) {
            return;
        }

        channelCache.put(userId, channel);
    }

    public void remove(String userId) {
        if (userId == null) {
            return;
        }
        channelCache.invalidate(userId);
    }

    public Channel get(String userId) {
        if (userId == null) {
            return null;
        }

        return channelCache.getIfPresent(userId);
    }
}

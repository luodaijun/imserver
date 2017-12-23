package com.luodaijun.imserver.store;

import com.luodaijun.imserver.core.bean.Message;

import java.util.List;

/**
 * Created by luodaijun on 2017/7/16.
 */
public interface MessageStore {
    int save(Message message);

    List<Message> findOffline(String toUserId);

    List<Message> queryHistoryMessage(String fromUserId, String toUserId, int pageNo, int pageSize);

    void updateReceiveTime(List<Message> messageList);
}

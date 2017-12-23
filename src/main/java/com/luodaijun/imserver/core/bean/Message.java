package com.luodaijun.imserver.core.bean;

import java.io.Serializable;

/**
 * Created by luodaijun on 2017/7/16.
 */
public class Message implements Serializable {
    private Long id;
    private String fromUserId;
    private String fromUserName;
    private String toUserId;
    private String content;
    private Long sendTime;
    private Long receiveTime;

    public Message(){

    }

    public Message(String fromUserId, String fromUserName, String toUserId, String content, Long sendTime, Long receiveTime) {
        this.fromUserId = fromUserId;
        this.fromUserName = fromUserName;
        this.toUserId = toUserId;
        this.content = content;
        this.sendTime = sendTime;
        this.receiveTime = receiveTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getSendTime() {
        return sendTime;
    }

    public void setSendTime(Long sendTime) {
        this.sendTime = sendTime;
    }

    public Long getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(Long receiveTime) {
        this.receiveTime = receiveTime;
    }


    @Override
    public String toString() {
        return "Message{" +
                "fromUserId='" + fromUserId + '\'' +
                ", fromUserName='" + fromUserName + '\'' +
                ", toUserId='" + toUserId + '\'' +
                ", content='" + content + '\'' +
                ", sendTime=" + sendTime +
                ", receiveTime=" + receiveTime +
                '}';
    }
}

package com.luodaijun.imserver.core.bean;

/**
 * Created by luodaijun on 2017/7/16.
 */
public class UniResponse {
    private String command;
    private int rtnCode;
    private String rtnMsg;
    private Object data;

    public UniResponse(String command, int rtnCode, String rtnMsg, Object data) {
        this.command = command;
        this.rtnCode = rtnCode;
        this.rtnMsg = rtnMsg;
        this.data = data;
    }


    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getRtnCode() {
        return rtnCode;
    }

    public void setRtnCode(int rtnCode) {
        this.rtnCode = rtnCode;
    }

    public String getRtnMsg() {
        return rtnMsg;
    }

    public void setRtnMsg(String rtnMsg) {
        this.rtnMsg = rtnMsg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }


    @Override
    public String toString() {
        return "UniResponse{" +
                "command='" + command + '\'' +
                ", rtnCode=" + rtnCode +
                ", rtnMsg='" + rtnMsg + '\'' +
                ", data=" + data +
                '}';
    }
}

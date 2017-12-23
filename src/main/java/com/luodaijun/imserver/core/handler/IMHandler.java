package com.luodaijun.imserver.core.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.luodaijun.imserver.core.bean.Message;
import com.luodaijun.imserver.core.bean.UniResponse;
import com.luodaijun.imserver.store.MessageStore;
import com.luodaijun.imserver.store.sqlite.SqliteMessageStore;
import com.luodaijun.imserver.utils.IMLog;
import com.luodaijun.imserver.utils.JacksonUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang.StringUtils;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by luodaijun on 2015-08-18.
 * 说明:
 */
public class IMHandler extends SimpleChannelInboundHandler<String> {
    private volatile String loginUserId;
    private volatile String loginUserName;

    /**
     * flex跨域响应策略文件
     */
    private final static String flashPolicy = "<?xml version=\"1.0\"?><cross-domain-policy><site-control permitted-cross-domain-policies=\"all\"/><allow-access-from domain=\"*\" to-ports=\"*\"/></cross-domain-policy>\0";

    /**
     * flex客户端请求跨域策略
     */
    private final static String flashPolicyRequest = "<policy-file-request/>";


    /**
     * 消息存储引擎
     */
    private static MessageStore messageStore = new SqliteMessageStore();


    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        final String ip = address.getAddress().getHostAddress();
        final int port = address.getPort();

        if (IMLog.IM_LOG.isDebugEnabled()) {
            IMLog.IM_LOG.debug("client[" + ip + ":" + port + "] connected");
        }

        ctx.channel().closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (IMLog.IM_LOG.isDebugEnabled()) {
                    IMLog.IM_LOG.debug("client[" + ip + ":" + port + "] closed,isSuccess=" + future.isSuccess());
                }

                SessionCache.getInstance().remove(IMHandler.this.loginUserId);
            }
        });


    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, final String request)
            throws Exception {

        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        final String ip = address.getAddress().getHostAddress();
        final int port = address.getPort();

        if (IMLog.IM_LOG.isDebugEnabled() && !request.contains("checkOnline")) {
            IMLog.IM_LOG.debug("receive client[" + ip + ":" + port + "] request:" + request);
        }

        if (StringUtils.isBlank(request)) {
            return;
        }

        if (request.contains(flashPolicyRequest)) {
            if (IMLog.IM_LOG.isDebugEnabled()) {
                IMLog.IM_LOG.debug("write flashPolicy to client[" + ip + ":" + port + "]");
            }

            ctx.writeAndFlush(Unpooled.copiedBuffer(flashPolicy, CharsetUtil.UTF_8));
        } else {
            Map<String, Object> requestData = null;
            try {
                requestData = JacksonUtils.objectMapper.readValue(request, Map.class);
            } catch (Exception e) {
                IMLog.IM_LOG.error("parse request json error,request=" + request, e);
            }

            try {
                exeCommand(ctx, requestData);
            } catch (Exception e) {
                IMLog.IM_LOG.error("exec command error", e);
            }
        }
    }

    private void exeCommand(ChannelHandlerContext ctx, Map<String, Object> requestData) {
        String command = (String) requestData.get("command");

        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        final String ip = address.getAddress().getHostAddress();
        final int port = address.getPort();

        //命令字段为空, 直接关闭连接
        if (StringUtils.isEmpty(command)) {
            IMLog.IM_LOG.warn("parameter[command] is empty,close connection[" + ip + ":" + port + "],request data=" + requestData);
            ctx.close();
            return;
        }

        //如果没有登录，直接关闭连接
        if (this.loginUserId == null && !"login".equals(command) && !"checkOnline".equals(command)) {
            IMLog.IM_LOG.warn("client[" + ip + ":" + port + "] is not login,close it!");
            ctx.close();
            return;
        }

        switch (command) {
            case "login":
                login(ctx, requestData);
                break;
            case "checkOnline":
                checkOnline(ctx, requestData);
                break;
            case "logout":
                logout(ctx, requestData);
                break;
            case "send":
                send(ctx, requestData);
                break;
            case "receiveOffline":
                receiveOffline(ctx);
                break;
            case "sendOnline":
                break;
            case "queryHistoryMessage":
                queryHistoryMessage(ctx, requestData);
                break;
            default:
                IMLog.IM_LOG.error("unsupported command[" + command + "] from[" + ip + ":" + port + "]!");
                return;
        }

    }

    /**
     * 登录
     *
     * @param ctx
     * @param requestData
     */

    private void login(ChannelHandlerContext ctx, Map<String, Object> requestData) {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        final String ip = address.getAddress().getHostAddress();
        final int port = address.getPort();

        this.loginUserId = (String) requestData.get("userId");
        this.loginUserName = (String) requestData.get("userName");

        String password = (String) requestData.get("password");


        if (StringUtils.isEmpty(this.loginUserId)) {
            IMLog.IM_LOG.warn("parameter[userId] is empty,close connection[" + ip + ":" + port + "]");
            ctx.close();
            return;
        }


        if (IMLog.IM_LOG.isDebugEnabled()) {
            IMLog.IM_LOG.debug("user[" + this.loginUserName + "(id=" + this.loginUserId + ")] login from [" + ip + ":" + port + "]");
        }

        SessionCache.getInstance().put(this.loginUserId, ctx.channel());

        UniResponse uniResponse = new UniResponse("login", 0, "login success", null);
        writeResponse(ctx.channel(), uniResponse);


        //receiveOffline(ctx);

    }


    /**
     * 心跳
     *
     * @param ctx
     * @param requestData
     */
    private void checkOnline(ChannelHandlerContext ctx, Map<String, Object> requestData) {
        UniResponse uniResponse = new UniResponse("checkOnline", 0, null, System.currentTimeMillis());
        writeResponse(ctx.channel(), uniResponse);
    }


    /**
     * 注销
     *
     * @param ctx
     * @param requestData
     */
    private void logout(ChannelHandlerContext ctx, Map<String, Object> requestData) {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        final String ip = address.getAddress().getHostAddress();
        final int port = address.getPort();

        SessionCache.getInstance().remove(this.loginUserId);

        ctx.close();

        if (IMLog.IM_LOG.isDebugEnabled()) {
            IMLog.IM_LOG.debug("user[" + this.loginUserId + "] logout from[" + ip + ":" + port + "]");
        }
    }


    /**
     * 发送点对点消息
     *
     * @param ctx
     * @param requestData
     */
    private void send(ChannelHandlerContext ctx, Map<String, Object> requestData) {
        String receiveUserId = (String) requestData.get("userId");
        String content = (String) requestData.get("content");
        Object msgType = requestData.get("msgType");

        if (IMLog.IM_LOG.isDebugEnabled()) {
            IMLog.IM_LOG.debug("send message to:" + receiveUserId + ", content:" + content);
        }

        long now = System.currentTimeMillis();

        Message message = new Message(loginUserId, loginUserName, receiveUserId, content, now, now);


        Channel receiveUserChannel = SessionCache.getInstance().get(receiveUserId);
        if (receiveUserChannel == null) {
            if (IMLog.IM_LOG.isDebugEnabled()) {
                IMLog.IM_LOG.debug("user[" + receiveUserId + "] offline,save offline message");
            }

            message.setReceiveTime(null);
        } else {
            Map data = new HashMap();
            data.put("fromUserId", this.loginUserId);
            data.put("fromUserName", this.loginUserName);
            data.put("msgType", msgType);
            data.put("content", content);
            data.put("sendTime", System.currentTimeMillis());

            UniResponse uniResponse = new UniResponse("send", 0, null, data);
            writeResponse(receiveUserChannel, uniResponse);
        }

        messageStore.save(message);
    }


    /**
     * 返回离线消息
     *
     * @param ctx
     */
    private void receiveOffline(ChannelHandlerContext ctx) {
        List<Message> messageList = messageStore.findOffline(loginUserId);

        UniResponse uniResponse = new UniResponse("receiveOffline", 0, null, messageList);

        writeResponse(ctx.channel(), uniResponse);

        //更新接收时间
        messageStore.updateReceiveTime(messageList);
    }

    /**
     * 读取历史消息
     *
     * @param ctx
     * @param requestData
     */
    private void queryHistoryMessage(ChannelHandlerContext ctx, Map<String, Object> requestData) {
        String toUserId = (String) requestData.get("userId");
        Integer pageSize = (Integer) requestData.get("pageSize");
        Integer pageNo = (Integer) requestData.get("pageNo");

        List<Message> messageList = messageStore.queryHistoryMessage(this.loginUserId, toUserId, pageNo, pageSize);

        Map data = new HashMap();
        data.put("queryUserId", this.loginUserId);
        data.put("messages", messageList);

        UniResponse uniResponse = new UniResponse("queryHistoryMessage", 0, null, data);
        writeResponse(ctx.channel(), uniResponse);

    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.ALL_IDLE) {
                InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
                String ip = address.getAddress().getHostAddress();
                int port = address.getPort();
                IMLog.IM_LOG.warn("client[" + ip + ":" + port + "] timeout,close channel");

                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        if (cause instanceof java.io.IOException) {//客户端主动关闭
            InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
            String ip = address.getAddress().getHostAddress();
            int port = address.getPort();
            IMLog.IM_LOG.warn("client[" + ip + ":" + port + "] force close,close channel");

            ctx.close();
        }
    }

    private void writeResponse(Channel channel, UniResponse response) {
        try {
            channel.write(Unpooled.copiedBuffer(JacksonUtils.objectMapper.writeValueAsBytes(response)));
            channel.writeAndFlush(Unpooled.copiedBuffer("\r\n".getBytes()));
        } catch (JsonProcessingException e) {
            IMLog.IM_LOG.error("response to json error,response obj=" + response, e);
        }
    }

}
package com.luodaijun.imserver.store.sqlite;

import com.luodaijun.imserver.core.bean.Message;
import com.luodaijun.imserver.store.MessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by luodaijun on 2017/7/16.
 */
public class SqliteMessageStore implements MessageStore {
    private final static Logger logger = LoggerFactory.getLogger(SqliteMessageStore.class);

    /**
     * 本地Sqlite数据连接
     */
    private static Connection sqliteConnection;

    static {
        init();
    }


    public static void init() {
        try {
            File dbFile = new File(System.getProperty("user.dir") + "/message.db");
            if (!dbFile.getParentFile().exists()) {
                dbFile.getParentFile().mkdirs();
            }

            if (!dbFile.exists()) {
                dbFile.createNewFile();
            }

            Class.forName("org.sqlite.JDBC");
            sqliteConnection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

            createTable();
        } catch (Exception e) {
            logger.warn("init sqlite db error", e);
        }
    }

    /**
     * 创建本地数据库表
     */
    private static void createTable() {

        Statement smt = null;
        try {
            smt = sqliteConnection.createStatement();
            smt.executeUpdate("create table if not exists t_message (fromUserId TEXT,fromUserName TEXT,toUserId TEXT,content TEXT,sendTime INTEGER,receiveTime INTEGER,id INTEGER  primary key AutoIncrement )");
        } catch (SQLException e) {
            logger.warn("create table t_message error", e);
        } finally {
            try {
                smt.close();
            } catch (SQLException e) {
                //do nothing
            }
        }
    }


    @Override
    public int save(Message message) {
        if (message == null) {
            return 0;
        }

        PreparedStatement psmt = null;
        try {
            psmt = sqliteConnection.prepareStatement("insert into t_message(fromUserId,fromUserName,toUserId,content,sendTime,receiveTime) values(?,?,?,?,?,?)");

            psmt.setString(1, message.getFromUserId());
            psmt.setString(2, message.getFromUserName());
            psmt.setString(3, message.getToUserId());
            psmt.setString(4, message.getContent());
            psmt.setLong(5, message.getSendTime());

            if (message.getReceiveTime() == null) {
                psmt.setNull(6, Types.INTEGER);
            } else {
                psmt.setLong(6, message.getReceiveTime());
            }

            if (logger.isDebugEnabled()) {
                logger.debug("insert message:" + message);
            }

            return psmt.executeUpdate();
        } catch (SQLException e) {
            logger.warn("insert into t_message error", e);
        } finally {
            try {
                psmt.close();
            } catch (SQLException e) {
                //do nothing
            }
        }


        return 0;
    }


    @Override
    public List<Message> findOffline(String toUserId) {
        return query("select * from t_message where toUserId='" + toUserId + "' and receiveTime is null");
    }

    @Override
    public List<Message> queryHistoryMessage(String fromUserId, String toUserId, int pageNo, int pageSize) {
        return query("select * from t_message where (toUserId='" + toUserId + "' and fromUserId='" + fromUserId + "') or (toUserId='" + fromUserId + "' and fromUserId='" + toUserId + "') order by  id desc limit " + pageSize + " offset " + (pageNo - 1) * pageSize);
    }

    @Override
    public void updateReceiveTime(List<Message> messageList) {
        if (messageList == null || messageList.isEmpty()) {
            return;
        }

        StringBuilder idStr = new StringBuilder();
        for (int i = 0; i < messageList.size(); i++) {
            Message message = messageList.get(i);
            if (i > 0) {
                idStr.append(",");
            }

            idStr.append(message.getId());
        }

        String sql = "update t_message set receiveTime=" + System.currentTimeMillis() + " where id in (" + idStr + ")";

        Statement smt = null;
        try {
            smt = sqliteConnection.createStatement();
            smt.executeUpdate(sql);
        } catch (SQLException e) {
            logger.warn("execute sql error,sql=" + sql, e);
        } finally {

            if (smt != null) {
                try {
                    smt.close();
                } catch (SQLException e) {
                    //do nothing
                }
            }
        }
    }


    private static List<Message> query(String sql) {
        List<Message> messages = new ArrayList<Message>();

        Statement smt = null;
        ResultSet resultSet = null;
        try {
            smt = sqliteConnection.createStatement();
            resultSet = smt.executeQuery(sql);
            while (resultSet.next()) {
                Message message = new Message();
                message.setId(resultSet.getLong("id"));
                message.setFromUserId(resultSet.getString("fromUserId"));
                message.setFromUserName(resultSet.getString("fromUserName"));
                message.setToUserId(resultSet.getString("toUserId"));
                message.setContent(resultSet.getString("content"));
                message.setSendTime(resultSet.getLong("sendTime"));
                message.setReceiveTime(resultSet.getLong("receiveTime"));

                messages.add(message);
            }
        } catch (SQLException e) {
            logger.warn("execute sql error,sql=" + sql, e);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    //do nothing
                }
            }

            if (smt != null) {
                try {
                    smt.close();
                } catch (SQLException e) {
                    //do nothing
                }
            }
        }

        return messages;
    }
}

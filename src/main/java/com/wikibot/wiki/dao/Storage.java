package com.wikibot.wiki.dao;

import com.wikibot.wiki.config.StorageConf;
import com.wikibot.wiki.wiki.WikiNote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class Storage {

    @Autowired
    private StorageConf storageConf;

    public boolean createWikiUser(long userId, String len) {
        String query = "insert into wiki_user(user_id, len) values (?, ?);";
        try (Connection connection = storageConf.getCon()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setLong(1, userId);
            preparedStatement.setString(2, len);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getUserLen(long userId) {
        String len = "";
        String query = "select len from wiki_user where user_id = ?;";
        try (Connection connection = storageConf.getCon()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setLong(1, userId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                len = rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return len;
    }

    public boolean setLen(long userId, String len) {
        String query = "update wiki_user set len = ? where user_id = ?;";
        try (Connection connection = storageConf.getCon()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, len);
            preparedStatement.setLong(2, userId);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean setMsgUser(long userId, String text, long time) {
        String query = "insert into user_msg(user_id, msg_text, msg_time) values (?, ?, ?);";
        try (Connection connection = storageConf.getCon()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setLong(1, userId);
            preparedStatement.setString(2, text);
            preparedStatement.setLong(3, time);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public long getMsgId(String msg_text, long user_id, long msg_time){
        long msg_id = 0;
        String query = "select msg_id from user_msg where msg_text=? and user_id=? and msg_time=?;";
        try (Connection connection = storageConf.getCon()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, msg_text);
            preparedStatement.setLong(2, user_id);
            preparedStatement.setLong(3, msg_time);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                msg_id = rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return msg_id;
    }

    public long getWikiAnswerId(String title, String disc, String url, long msg_id){
        long answer_id = 0;
        String query = "select answer_id from wiki_answer where title=? and disc=? and url=? and msg_id=?;";
        try (Connection connection = storageConf.getCon()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, disc);
            preparedStatement.setString(3, url);
            preparedStatement.setLong(4, msg_id);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                answer_id = rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return answer_id;
    }

    public WikiNote getWikiAnswer(long answerId){
        WikiNote wikiNote = new WikiNote();
        String query = "select * from wiki_answer where answer_id=?;";
        try (Connection connection = storageConf.getCon()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setLong(1, answerId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                wikiNote.setName(rs.getString(2));
                wikiNote.setDisc(rs.getString(3));
                wikiNote.setLink(rs.getString(4));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wikiNote;
    }

    public boolean setWikiAnswer(String title, String disc, String url, long msgId){
        String query = "insert into wiki_answer(title, disc, url, msg_id) values (?, ?, ?, ?);";
        try (Connection connection = storageConf.getCon()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, disc);
            preparedStatement.setString(3, url);
            preparedStatement.setLong(4, msgId);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

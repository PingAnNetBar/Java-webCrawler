package com.github.JavaWebCrawler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.*;

public class JdbcCrawlerDao implements CrawlerDao {

    private final Connection connection;
    private static final String ROOT = "root";
    private static final String PASSWORD = "123456";

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public JdbcCrawlerDao() {
        try {
            this.connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/news?characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai", ROOT, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //getNextLink方法并不是所有实现都要需要遵守的协议，流程中并没有用到它，只是辅助getNextLinkThenDelete方法，故设置为JdbcCrawlerDao的private方法
    private String getNextLink(String sql) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return resultSet.getString(1);
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return null;
    }
    @Override
    public String getNextLinkThenDelete() throws SQLException {
        String link = getNextLink("select link from LINKS_TO_BE_PROCESSED limit 1");
        if (link != null) {
            updateDatabase(link, "delete from LINKS_TO_BE_PROCESSED where link = ?");
        }
        return link;
    }

    public void updateDatabase(String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }
    @Override
    public void insertNewsIntoDataBase(String title, String url, String content) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("insert into news (title,url,content,created_at,modified_at) values(?,?,?,now(),now())")) {
            statement.setString(1, title);
            statement.setString(2, url);
            statement.setString(3, content);
            statement.executeUpdate();
        }
    }
    @Override
    public boolean isProcessed(String link) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement("select link from LINKS_ALREADY_PROCESSED where link = ?")) {
            statement.setString(1, link);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return true;
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return false;
    }

    @Override
    public void insertLinkAlreadyProcessed(String link) {

    }

    @Override
    public void insertLinkToBeProcessed(String href) {

    }
}

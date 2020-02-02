package com.github.JavaWebCrawler;

import java.sql.SQLException;


public interface CrawlerDao {
    String getNextLink(String sql) throws SQLException;

    String getNextLinkThenDelete() throws SQLException;

    void updateDatabase(String link, String sql) throws SQLException;

    void insertNewsIntoDataBase(String title, String url, String content) throws SQLException;

    boolean isProcessed(String link) throws SQLException;
}

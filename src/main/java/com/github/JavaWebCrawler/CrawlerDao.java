package com.github.JavaWebCrawler;

import java.sql.SQLException;


public interface CrawlerDao {

    String getNextLinkThenDelete() throws SQLException;

    void insertNewsIntoDataBase(String title, String url, String content) throws SQLException;

    boolean isDistincted(String title) throws SQLException;

    void insertLinkAlreadyProcessed(String link) throws SQLException;

    void insertLinkToBeProcessed(String href) throws SQLException;
}

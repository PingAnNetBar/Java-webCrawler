package com.github.JavaWebCrawler;

import java.sql.SQLException;


public interface CrawlerDao {

    String getNextLinkThenDelete() throws SQLException;

    void insertNewsIntoDataBase(String title, String url, String content) throws SQLException;

    boolean isProcessed(String link) throws SQLException;

    void insertLinkAlreadyProcessed(String link);

    void insertLinkToBeProcessed(String href);
}

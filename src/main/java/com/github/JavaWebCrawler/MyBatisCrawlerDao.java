package com.github.JavaWebCrawler;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MyBatisCrawlerDao implements CrawlerDao {

    private SqlSessionFactory sqlSessionFactory;

    public MyBatisCrawlerDao() {
        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized String getNextLinkThenDelete() throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            String url = session.selectOne("com.github.JavaWebCrawler.MyMapper.selectNextAvailableLink");
            if (url != null) {
                session.delete("com.github.JavaWebCrawler.MyMapper.deleteLink", url);
            }
            return url;
        }
    }


    @Override
    public void insertNewsIntoDataBase(String title, String url, String content) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.JavaWebCrawler.MyMapper.insertNews", new News(title, content, url));
        }
    }

    @Override
    public synchronized boolean isDistincted(String title) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            int count = session.selectOne("com.github.JavaWebCrawler.MyMapper.selectDistincted", title);
            if (count>1){
                return true;
            }
        }
        return false;
    }


    @Override
    public void insertLinkAlreadyProcessed(String link) {
        Map<String, Object> param = new HashMap<>();
        param.put("tableName", "LINKS_ALREADY_PROCESSED");
        param.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.JavaWebCrawler.MyMapper.insertLink", param);
        }
    }

    @Override
    public void insertLinkToBeProcessed(String link) {
        Map<String, Object> param = new HashMap<>();
        param.put("tableName", "LINKS_TO_BE_PROCESSED");
        param.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.JavaWebCrawler.MyMapper.insertLink", param);
        }
    }
}

package com.github.JavaWebCrawler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;


public class Crawler extends Thread {

    private CrawlerDao dao;

    public Crawler(CrawlerDao dao) {
        this.dao = dao;
    }

    public void run() {
        try {
            String link;
            while ((link = dao.getNextLinkThenDelete()) != null) {
//                if (dao.isProcessed(link)) {
//                    continue;
//                }
                if (IsInterestingLink(link)) {
                    Document doc = httpGetAndParseHtml(link);
                    findAllaTagAndStoreIntoDatabase(doc);
                    StoreItInDataBaseIfItIsNecessary(doc, link);
                    dao.insertLinkAlreadyProcessed(link);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void findAllaTagAndStoreIntoDatabase(Document doc) throws SQLException {
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");

            if (href.startsWith("//")) {
                href = "https:" + href;
            }
            if (!href.toLowerCase().startsWith("javascript")) {
                dao.insertLinkToBeProcessed(href);
            }
        }
    }


    private void StoreItInDataBaseIfItIsNecessary(Document doc, String link) throws SQLException {

        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags
            ) {
                String title = articleTags.get(0).child(0).text();
                System.out.println(title);
                //let us use method to solve this problem first,then，bring it to the interface
                if (isDistincted(title)) {
                    continue;
                }
                String content = articleTag.select("p").stream().map(Element::text).collect(Collectors.joining("\n"));
                dao.insertNewsIntoDataBase(title, link, content);
            }
        }
    }

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    private static boolean isDistincted(String title) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/news?characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai", "root", "123456");
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement("select title from NEWS where title = ?");
            statement.setString(1, title);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return true;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return false;
    }

    private static Document httpGetAndParseHtml(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        System.out.println(link);

        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36");
        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            HttpEntity entity1 = response1.getEntity();
            String html = EntityUtils.toString(entity1);
            return Jsoup.parse(html);
        }
    }

    private static boolean IsInterestingLink(String link) {
        return (IsNewPage(link) || IsEqualPage(link)) && IsNotLoginPage(link) && IsNotIllegalPage(link);
    }

    private static boolean IsEqualPage(String link) {
        return "https://sina.cn".equals(link);
    }

    private static boolean IsNewPage(String link) {
        return link.contains("news.sina.cn") /*|| link.contains("nba.sina.cn")*/;
    }

    private static boolean IsSinaPage(String link) {
        return link.contains("sina.cn");
    }

    private static boolean IsNotLoginPage(String link) {
        return !link.contains("passport.sina.cn");
    }

    private static boolean IsNotIllegalPage(String link) {
        return !link.contains("k=");
    }


}

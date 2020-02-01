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


@SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
public class Main {
    public static void main(String[] args) throws IOException, SQLException {

        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/news?characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai", "root", "123456");
        while (true) {

            List<String> linkPool = getUrlFromDataBase(connection, "select link from LINKS_TO_BE_PROCESSED");
            if (linkPool.isEmpty()) {
                break;
            }
            String link = linkPool.remove(linkPool.size() - 1);
            insertLinkIntoDataBase(connection, link, "delete from LINKS_TO_BE_PROCESSED where link = ?");

            if (isProcessed(connection, link)) {
                continue;
            }

            if (IsInterestingLink(link)) {
                Document doc = httpGetAndParseHtml(link);
                findAllaTagAndStoreIntoDatabase(connection, doc);
                StoreItInDataBaseIfItIsNecessary(doc);
                insertLinkIntoDataBase(connection, link, "insert into LINKS_ALREADY_PROCESSED (link) values (?)");
            }
        }
    }

    private static void findAllaTagAndStoreIntoDatabase(Connection connection, Document doc) throws SQLException {
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");
            insertLinkIntoDataBase(connection, href, "insert into LINKS_TO_BE_PROCESSED (link) values (?)");
        }
    }


    private static boolean isProcessed(Connection connection, String link) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement("select link from LINKS_ALREADY_PROCESSED where link = ?")) {
            statement.setString(1, link);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return true;
            }
        }finally {
            if (resultSet!=null){
                resultSet.close();
            }
        }
        return false;
    }

    private static void insertLinkIntoDataBase(Connection connection, String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    private static ArrayList<String> getUrlFromDataBase(Connection connection, String sql) throws SQLException {
        ArrayList<String> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                result.add(resultSet.getString(1));
            }
        }
        return result;
    }

    private static void StoreItInDataBaseIfItIsNecessary(Document doc) {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags
            ) {
                String title = articleTags.get(0).child(0).text();
                System.out.println(title);
            }
        }
    }

    private static Document httpGetAndParseHtml(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        System.out.println(link);
        if (link.startsWith("//")) {
            link = "https:" + link;
            System.out.println(link);
        }
        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36");
        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {

            //System.out.println(response1.getStatusLine());
            HttpEntity entity1 = response1.getEntity();
            String html = EntityUtils.toString(entity1);
            return Jsoup.parse(html);
        }
    }

    private static boolean IsInterestingLink(String link) {
        return IsSinaPage(link) && IsNotLoginPage(link) && IsNotIllegalPage(link) && IsNewPage(link) || IsEqualPage(link);
    }

    private static boolean IsEqualPage(String link) {
        return "https://sina.cn".equals(link);
    }

    private static boolean IsNewPage(String link) {
        return link.contains("news.sina.cn");
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

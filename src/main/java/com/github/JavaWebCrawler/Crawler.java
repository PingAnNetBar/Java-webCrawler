package com.github.JavaWebCrawler;

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


public class Crawler {

    private CrawlerDao dao = new JdbcCrawlerDao();

    public void run() throws SQLException, IOException {

        String link;
        while ((link = dao.getNextLinkThenDelete()) != null) {

            if (dao.isProcessed(link)) {
                continue;
            }
            if (IsInterestingLink(link)) {
                Document doc = httpGetAndParseHtml(link);
                findAllaTagAndStoreIntoDatabase(doc);
                StoreItInDataBaseIfItIsNecessary(doc, link);
                dao.updateDatabase(link, "insert into LINKS_ALREADY_PROCESSED (link) values (?)");
            }
        }
    }

    public static void main(String[] args) throws IOException, SQLException {
        new Crawler().run();
    }

    private void findAllaTagAndStoreIntoDatabase(Document doc) throws SQLException {
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");

            if (href.startsWith("//")) {
                href = "https:" + href;
            }
            if (!href.toLowerCase().startsWith("javascript")) {
                dao.updateDatabase(href, "insert into LINKS_TO_BE_PROCESSED (link) values (?)");
            }
        }
    }


    private void StoreItInDataBaseIfItIsNecessary(Document doc, String link) throws SQLException {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags
            ) {
                String title = articleTags.get(0).child(0).text();
                String content = articleTag.select("p").stream().map(Element::text).collect(Collectors.joining("\n"));
                dao.insertNewsIntoDataBase(title, link, content);
            }
        }
    }

    private static Document httpGetAndParseHtml(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        System.out.println(link);

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
        return (IsNewPage(link) || IsEqualPage(link)) && IsNotLoginPage(link) && IsNotIllegalPage(link);
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

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


public class Crawler extends Thread {

    private CrawlerDao dao;

    public Crawler(CrawlerDao dao) {
        this.dao = dao;
    }

    public void run() {
        try {
            String link;
            while ((link = dao.getNextLinkThenDelete()) != null) {
                if (Filter.IsInterestingLink(link)) {
                    Document doc = httpGetAndParseHtml(link);
                    findAllaTagAndStoreIntoDatabase(doc);
                    storeItInDataBaseIfItIsNecessary(doc, link);
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


    private void storeItInDataBaseIfItIsNecessary(Document doc, String link) throws SQLException {

        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags
            ) {
                String title = articleTags.get(0).child(0).text();
                System.out.println(title);

                if (dao.isDistincted(title)) {
                    continue;
                }
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
            HttpEntity entity1 = response1.getEntity();
            String html = EntityUtils.toString(entity1);
            return Jsoup.parse(html);
        }
    }



}

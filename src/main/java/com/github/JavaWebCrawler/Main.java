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
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {

        List<String> linkPool = new ArrayList<>();
        Set<String> processedLinkPool = new HashSet<>();
        linkPool.add("https://sina.cn");

        while (true) {
            if (linkPool.isEmpty()) {
                break;
            }
            String link = linkPool.remove(linkPool.size() - 1);

            if (processedLinkPool.contains(link)) {
                continue;
            }

            if (IsInterestingLink(link)) {

                Document doc = httpGetAndParseHtml(link);
                ArrayList<Element> links = doc.select("a");
                for (Element aTag : links
                ) {
                    linkPool.add(aTag.attr("href"));
                }
                StoreItInDataBaseIfItIsNecessary(doc);
                processedLinkPool.add(link);
            }
        }
    }

    private static void StoreItInDataBaseIfItIsNecessary(Document doc){
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

            System.out.println(response1.getStatusLine());
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


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

import javax.print.Doc;
import java.io.IOException;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {

        //创建一个linkPool
        //创建一个processedlinkPool
        List<String> linkPool = new ArrayList<>();
        Set<String> processedLinkPool = new HashSet<>();
        linkPool.add("https://sina.cn");

        while (true){
            if (linkPool.isEmpty()) {
                break;
            }

            String link = linkPool.remove(linkPool.size()-1);

            if (processedLinkPool.contains(link)){
                continue;
            }

            if (link.contains("sina.cn") && !link.contains("passport.sina.cn") && !link.contains("k=") && link.contains("news.sina.cn") || "https://sina.cn".equals(link)) {
                //That is our target that we want to crawl.
                CloseableHttpClient httpclient = HttpClients.createDefault();

                System.out.println(link);
                if (link.startsWith("//")){
                    link = "https:"+ link;
                    System.out.println(link);
                }
                HttpGet httpGet = new HttpGet(link);
                httpGet.addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36");
                try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {

                    System.out.println(response1.getStatusLine());
                    HttpEntity entity1 = response1.getEntity();
                    String html = EntityUtils.toString(entity1);
                    Document doc = Jsoup.parse(html);
                    //take every a<> from target
                    ArrayList<Element> links = doc.select("a");
                    //traversal every href and put them to the linkPool
                    for (Element aTag: links
                    ) {
                        linkPool.add(aTag.attr("href"));
                    }

                    ArrayList<Element> articleTags = doc.select("article");
                    if (!articleTags.isEmpty()) {
                        for (Element articleTag: articleTags
                        ) {
                            String title = articleTags.get(0).child(0).text();
                            System.out.println(title);
                        }
                    }
                }
                processedLinkPool.add(link);
            }else{

            }
        }

    }
}

package com.github.JavaWebCrawler;

public class Main {

    //let me do this

    public static void main(String[] args) {
        CrawlerDao dao = new MyBatisCrawlerDao();
        for (int i = 0; i < 4; i++) {
            new Crawler(dao).start();
        }
    }
}


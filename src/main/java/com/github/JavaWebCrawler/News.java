package com.github.JavaWebCrawler;

import java.time.Instant;

public class News {


    private Integer id;
    private String title;
    private String content;
    private String url;
    private Instant createdAt;

    public News() {
    }

    public News(String title, String content, String url) {
        this.title = title;
        this.content = content;
        this.url = url;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}

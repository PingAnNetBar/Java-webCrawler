package com.github.JavaWebCrawler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.http.HttpHost;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElasticSearchDataDao {
    @SuppressFBWarnings("DLS_DEAD_LOCAL_STORE")
    public static void main(String[] args) throws IOException {

        SqlSessionFactory sqlSessionFactory;
        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //List<News> newsFromMysql = getDataFromNews(sqlSessionFactory);

        //InsertIntoEs(newsFromMysql);
        //SearchKeyWordFromES();
    }

    private static void insertIntoEs(List<News> newsFromMysql) throws IOException {
        try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")))) {
            for (News news : newsFromMysql
            ) {
                IndexRequest request = new IndexRequest("news");
                Map<String, Object> map = new HashMap<>();
                map.put("title", news.getTitle());
                map.put("content", news.getContent());
                map.put("url", news.getUrl());
                map.put("createdAt", news.getCreatedAt());
                request.source(map, XContentType.JSON);
                IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
                System.out.println(indexResponse.status().getStatus());
            }
        }
    }

    @SuppressFBWarnings("DM_DEFAULT_ENCODING")
    private static void searchKeyWordFromES() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Please input a search keyword:");
        String keyword = reader.readLine();

        try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")))) {
            SearchRequest request = new SearchRequest("news");
            request.source(new SearchSourceBuilder().query(new MultiMatchQueryBuilder(keyword, "title", "content")));

            SearchResponse result = client.search(request, RequestOptions.DEFAULT);
            result.getHits().forEach(hit -> System.out.println(hit.getSourceAsString()));
        }
    }

    private static List<News> getDataFromNews(SqlSessionFactory sqlSessionFactory) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return session.selectList("com.github.JavaWebCrawler.MockMapper.selectNews");
        }
    }
}

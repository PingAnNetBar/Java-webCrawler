package com.github.JavaWebCrawler;

public class Filter {

    public static boolean IsInterestingLink(String link) {
        return (isNewPage(link) || isEqualPage(link)) && isNotLoginPage(link) && isNotIllegalPage(link);
    }

    public static boolean isEqualPage(String link) {
        return "https://sina.cn".equals(link);
    }

    public static boolean isNewPage(String link) {
        return link.contains("news.sina.cn");
    }

    public static boolean isNotLoginPage(String link) {
        return !link.contains("passport.sina.cn");
    }

    public static boolean isNotIllegalPage(String link) {
        return !link.contains("k=");
    }
}

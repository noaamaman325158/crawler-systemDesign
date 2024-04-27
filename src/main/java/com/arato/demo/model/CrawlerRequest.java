package com.arato.demo.model;

public class CrawlerRequest {

    // Source url of the traverse of the DFS algorithm.
    String url;
    //Here we have our base cases for the DFS algorithm:
    // Distance from the source url
    // Amount of seconds till stop
    // Amount of url's till stop
    Integer maxDistance;
    Integer maxSeconds;
    Integer maxUrls;


    // Getter's and Setter's
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(Integer maxDistance) {
        this.maxDistance = maxDistance;
    }

    public Integer getMaxSeconds() {
        return maxSeconds;
    }

    public void setMaxSeconds(Integer maxSeconds) {
        this.maxSeconds = maxSeconds;
    }

    public Integer getMaxUrls() {
        return maxUrls;
    }

    public void setMaxUrls(Integer maxUrls) {
        this.maxUrls = maxUrls;
    }
}

package com.arato.demo.model;


public class CrawlStatus {
    // The current distance from the source url
    int distance;
    // The start time when we start the process to know when we get the stop time
    long startTime;
    // All the base cases that we want to handle during the traversing
    StopReason stopReason;
    long lastModified;
    // Here we track the number of teh web pages we're visiting during the traversing
    long numPages = 0;


    // Builder Design pattern implementation
    public static CrawlStatus of(int distance, long startTime, int numPages, StopReason stopReason) {
        CrawlStatus res = new CrawlStatus();
        res.distance = distance;
        res.startTime =  startTime;
        res.lastModified = System.currentTimeMillis();
        res.stopReason = stopReason;
        res.numPages = numPages;
        return res;
    }

    public int getDistance() {
        return distance;
    }

    public long getLastModified() {
        return lastModified;
    }

    public long getStartTime() {
        return startTime;
    }

    public StopReason getStopReason() {
        return stopReason;
    }

    public long getNumPages() {
        return numPages;
    }

    public void setNumPages(long numPages) {
        this.numPages = numPages;
    }
}

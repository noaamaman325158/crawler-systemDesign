package com.arato.demo.crawler;


import com.arato.demo.model.CrawlStatus;
import com.arato.demo.model.CrawlerRecord;
import com.arato.demo.model.CrawlerRequest;
import com.arato.demo.model.StopReason;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

// The main functionality centralized here
@Service
public class Crawler {

    // init some logger instance that helps to follow the algorithm promotion
    protected final Log logger = LogFactory.getLog(getClass());
    // init some static size for the queue auxiliary data structure
    public static final int MAX_CAPACITY = 100000;
    private Set<String> visitedUrls = new HashSet<>();
    // This is an in memory queue that functions as auxiliary memory for the DF algorithm.
    // In full implementation I use message queue like Kafka.
    private BlockingQueue<CrawlerRecord> queue = new ArrayBlockingQueue<CrawlerRecord>(MAX_CAPACITY);

    private int curDistance = 0;
    private long startTime = 0;

    // init some StopReason enum that contains all the base cases for the algorithm.
    private StopReason stopReason;

    public CrawlStatus crawl(String crawlId, CrawlerRequest crawlerRequest) throws InterruptedException, IOException {
        String rootDirectory = System.getProperty("user.dir") + "/crawledPages";
        String sessionDirectory = new File(rootDirectory, crawlId).getAbsolutePath();
        new File(sessionDirectory).mkdirs();  // Ensure the session directory exists

        visitedUrls.clear();
        queue.clear();

        curDistance = 0;
        startTime = System.currentTimeMillis();

        stopReason = null;
        queue.put(CrawlerRecord.of(crawlId, crawlerRequest));
        while (!queue.isEmpty() && getStopReason(queue.peek()) == null) {
            CrawlerRecord rec = queue.poll();

            logger.info("crawling url: " + rec.getUrl());

            Document webPageContent = Jsoup.connect(rec.getUrl()).get();
            savePageLocally(webPageContent, sessionDirectory);

            List<String> innerUrls = extractWebPageUrls(rec.getBaseUrl(), webPageContent);
            addUrlsToQueue(rec, innerUrls, rec.getDistance() + 1);
        }

        stopReason = queue.isEmpty() ? null : getStopReason(queue.peek());
        return CrawlStatus.of(curDistance, startTime, visitedUrls.size(), stopReason);
    }

    private void savePageLocally(Document webPageContent, String sessionDirectory) throws IOException {
        String fileName = UUID.randomUUID().toString() + ".html";
        File file = new File(sessionDirectory, fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(webPageContent.outerHtml());
        }
    }





    private StopReason getStopReason(CrawlerRecord rec) {
        if (rec.getDistance() == rec.getMaxDistance() +1) return StopReason.maxDistance;
        if (visitedUrls.size() >= rec.getMaxUrls()) return StopReason.maxUrls;
        if (System.currentTimeMillis() >= rec.getMaxTime()) return StopReason.timeout;
        return null;
    }


    private void addUrlsToQueue(CrawlerRecord rec, List<String> urls, int distance) throws InterruptedException {
        logger.info(">> adding urls to queue: distance->" + distance + " amount->" + urls.size());
        curDistance = distance;
        for (String url : urls) {
            if (!visitedUrls.contains(url)) {
                visitedUrls.add(url);
                queue.put(CrawlerRecord.of(rec).withUrl(url).withIncDistance()) ;
            }
        }
    }

    private List<String> extractWebPageUrls(String baseUrl, Document webPageContent) {
        List<String> links = webPageContent.select("a[href]")
                .eachAttr("abs:href")
                .stream()
                .filter(url -> url.startsWith(baseUrl))
                .collect(Collectors.toList());
        logger.info(">> extracted->" + links.size() + " links");

        return links;
    }


}

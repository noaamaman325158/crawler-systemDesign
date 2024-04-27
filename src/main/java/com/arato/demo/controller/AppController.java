package com.arato.demo.controller;


import com.arato.demo.crawler.Crawler;
import com.arato.demo.model.CrawlStatus;
import com.arato.demo.model.CrawlStatusOut;
import com.arato.demo.model.CrawlerRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class AppController {

    private static final int ID_LENGTH = 6;
    private Random random = new Random();
    @Autowired
    Crawler crawler;

    @RequestMapping(value = "/crawl", method = RequestMethod.POST)
    public CrawlStatusOut crawl(@RequestBody CrawlerRequest request) throws IOException, InterruptedException {
        String crawlId = generateCrawlId();
        if (!request.getUrl().startsWith("http")) {
            request.setUrl("https://" + request.getUrl());
        }
        CrawlStatus res = crawler.crawl(crawlId, request);
        return CrawlStatusOut.of(res);
    }

    private String generateCrawlId() {
        String charPool = "ABCDEFHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < ID_LENGTH; i++) {
            res.append(charPool.charAt(random.nextInt(charPool.length())));
        }
        return res.toString();
    }

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String hello(@RequestParam String hello) {
        return "hello";
    }
}
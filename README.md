# CrawlerTask
# Table of Contents
1. [CrawlerTask Overview](#crawlerTask)
2. [Complexity Analysis of BFS](#complexity-analysis-of-bfs)
   - [Time Complexity](#time-complexity)
   - [Space Complexity](#space-complexity)
3. [How to Run It](#how-to-run-it)
4. [Potential Bottlenecks](#potential-bottlenecks)
   - [Scalability](#scalability)
   - [Concurrency](#concurrency)
   - [Network I/O and Latency](#network-io-and-latency)
   - [Memory Consumption](#memory-consumption)
5. [System Components](#system-components)
   - [Crawler Engine](#crawler-engine)
   - [URL Queue Management](#url-queue-management)
   - [Downloader Web Pages](#downloader-web-pages)
   - [Data Storage](#data-storage)
   - [Link Extractor](#link-extractor)
6. [Converting to a Multi-Computer System](#converting-to-a-multi-computer-system)
   - [Technologies Level Diagram](#technologies-level-diagram)
   - [Global Variables to Redis Data](#global-variables-to-redis-data)
   - [In-Memory Queue to Kafka Messages Queue](#in-memory-queue-to-kafka-messages-queue)
   - [Local Content to Blob Store or Elasticsearch](#local-content-to-blob-store-or-elasticsearch)

This crawler is implemented using a graph data structure, employing the Breadth First Search (BFS) algorithm to navigate through web pages.


## Complexity Analysis of BFS

### Time Complexity
The time complexity of BFS in this context is \(O(V + E)\), where:
- \(V\) represents the number of web pages (vertices) in the graph.
- \(E\) represents the number of hyperlinks (edges) connecting these web pages.

### Space Complexity
The space complexity is \(O(V)\), considering:
- In a web crawling scenario, we primarily store and manage the URLs (vertices) to ensure each page is visited once. While edges (links between pages) are traversed, they do not accumulate in memory in the same way vertices do because they are processed immediately and then discarded.

## How to run it

To test the system, you can use the Swagger interface provided.

1)git clone git@github.com:noaamaman325158/crawlerTask.git
```bash
git clone git@github.com:noaamaman325158/crawlerTask.git
```
2)Navigate to the project directory:
```bash
cd crawlerTask
```
3)Run the application. This step assumes you have a way to execute DemoApplication.java, typically through an IDE or a command line instruction (ensure you have built the application if necessary).

4)Open your favorite web browser and go to the following link to access the Swagger UI:
```bash
http://localhost:8080/swagger-ui.html
```
5)In the Swagger UI, locate the app-controller to find all the available endpoints.

6)Navigate to the /api/crawl endpoint within the app-controller. Here you can execute a POST request to test the crawling functionality.
app-controller

Additionally, you can use Postman to test the API by sending a POST request to http://localhost:8080/api/crawl.

Therefore, it's more accurate to consider the number of vertices (\(V\)) for space complexity in the context of BFS for web crawling, where each unique URL represents a vertex in the graph.

![Untitled Diagram drawio](https://github.com/noaamaman325158/crawlerTask/assets/126208613/bb827458-63cf-4232-9982-7b02fa7d1962)


## Potetntial Bottlenecks
### Scalability
The current single-threaded implementation is not optimized for high-volume data handling, limiting its ability to scale effectively with increased load.

### Concurrency
Utilizing a single-threaded model restricts the processing speed. Adopting a multi-threaded or asynchronous approach can improve efficiency but requires careful management of shared resources like visitedUrls and queue.

### Network I/O and Latency
Crawling performance is significantly affected by network latency and bandwidth constraints, necessitating optimizations in network interactions and potential distributed crawling strategies.

### Memory Consumption
Managing large sets of URLs in memory can lead to high memory usage, impacting performance. Efficient data structures and persistent storage mechanisms are essential for large-scale crawls.


## System Components In Our Current System


### Crawler Engine
```java
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
```
### Url Queue Managment
```java
    private BlockingQueue<CrawlerRecord> queue = new ArrayBlockingQueue<CrawlerRecord>(MAX_CAPACITY);
```
### Downloader web pages
```java
Document webPageContent = Jsoup.connect(rec.getUrl()).get();
savePageLocally(webPageContent, sessionDirectory);
```
### Data storage 
```java
private void savePageLocally(Document webPageContent, String sessionDirectory) throws IOException {
        String fileName = UUID.randomUUID().toString() + ".html";
        File file = new File(sessionDirectory, fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(webPageContent.outerHtml());
        }
    }
```
### Link Extractor(Utilize the Scraping Technique)
```java
private List<String> extractWebPageUrls(String baseUrl, Document webPageContent) {
        List<String> links = webPageContent.select("a[href]")
                .eachAttr("abs:href")
                .stream()
                .filter(url -> url.startsWith(baseUrl))
                .collect(Collectors.toList());
        logger.info(">> extracted->" + links.size() + " links");

        return links;
    }
```



## Convert this local system to multi computers system 
### Diagram For the technologies Level
![image](https://github.com/noaamaman325158/crawlerTask/assets/126208613/8b7712cf-adac-4666-accc-fd8d1d0ee817)
![image](https://github.com/noaamaman325158/crawlerTask/assets/126208613/15ef05fc-d0c1-4048-9163-82d5a1292138)

or convert our simple implementation that meant to run on single machine and handle the tasks in single-thread approach we need to devide our system for several logical parts that will be services in some system architecture of microservices.

### Convert global varaibles in code to redis data
```java

    protected final Log logger = LogFactory.getLog(getClass());
    public static final int MAX_CAPACITY = 100000;

    // THe visitedUrls, the current distance from the source url  and the things that realted to the tracking of the crawler
    //private Set<String> visitedUrls = new HashSet<>();
    //private int curDistance = 0;
    //private long startTime = 0;

    private BlockingQueue<CrawlerRecord> queue = new ArrayBlockingQueue<CrawlerRecord>(MAX_CAPACITY);
    private StopReason stopReason;
    }
```

### Convert the simple in memory queue in our code to be in a Kafka Messages Queue
```java

    protected final Log logger = LogFactory.getLog(getClass());
    public static final int MAX_CAPACITY = 100000;

    // THe visitedUrls, the current distance from the source url  and the things that realted to the tracking of the crawler
    //private Set<String> visitedUrls = new HashSet<>();
    //private int curDistance = 0;
    //private long startTime = 0;

    // Convert the static queue data structures to be messages queue in Kafka technolegy
    //private BlockingQueue<CrawlerRecord> queue = new ArrayBlockingQueue<CrawlerRecord>(MAX_CAPACITY);
    private StopReason stopReason;

```

### The local save content in our pages will be saved for a blob store or to elastic search text db 
```java
public CrawlStatus crawl(String crawlId, CrawlerRequest crawlerRequest) throws InterruptedException, IOException {
        String rootDirectory = System.getProperty("user.dir") + "/crawledPages";
        String sessionDirectory = new File(rootDirectory, crawlId).getAbsolutePath();
        new File(sessionDirectory).mkdirs();  

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
            // Instead of using the following service function - use some service function for save the page object in bucket os save in text-based database like Elastic Search
            //savePageLocally(webPageContent, sessionDirectory);

            List<String> innerUrls = extractWebPageUrls(rec.getBaseUrl(), webPageContent);
            addUrlsToQueue(rec, innerUrls, rec.getDistance() + 1);
        }

        stopReason = queue.isEmpty() ? null : getStopReason(queue.peek());
        return CrawlStatus.of(curDistance, startTime, visitedUrls.size(), stopReason);
    }


```




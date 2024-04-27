# CrawlerTask

This crawler is implemented using a graph data structure, employing the Breadth First Search (BFS) algorithm to navigate through web pages.

## Complexity Analysis of BFS

### Time Complexity
The time complexity of BFS in this context is \(O(V + E)\), where:
- \(V\) represents the number of web pages (vertices) in the graph.
- \(E\) represents the number of hyperlinks (edges) connecting these web pages.

### Space Complexity
The space complexity is \(O(V)\), considering:
- In a web crawling scenario, we primarily store and manage the URLs (vertices) to ensure each page is visited once. While edges (links between pages) are traversed, they do not accumulate in memory in the same way vertices do because they are processed immediately and then discarded.

Therefore, it's more accurate to consider the number of vertices (\(V\)) for space complexity in the context of BFS for web crawling, where each unique URL represents a vertex in the graph.

![Untitled Diagram drawio](https://github.com/noaamaman325158/crawlerTask/assets/126208613/bb827458-63cf-4232-9982-7b02fa7d1962)

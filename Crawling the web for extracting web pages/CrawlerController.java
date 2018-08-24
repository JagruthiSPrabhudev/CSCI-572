import java.io.*;
import java.util.*;

import edu.uci.ics.crawler4j.crawler.*;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.*;

public class CrawlerController {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		CrawlConfig config = new CrawlConfig();
		CrawlerHandler state = new CrawlerHandler();
		String StorageFolder = "/path_to_crawl/crawl";
		int numberofCrawlers = 8;
		config.setCrawlStorageFolder(StorageFolder);
		config.setMaxDepthOfCrawling(16);
		config.setMaxPagesToFetch(10);
		config.setPolitenessDelay(250);
		config.setIncludeBinaryContentInCrawling(true);
		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer); 
		controller.addSeed("website to be crawled");
        controller.start(MyCrawler.class, numberofCrawlers);
        List<Object> crawlerdata=controller.getCrawlersLocalData();
        for (Object localData : crawlerdata) {
        	CrawlerHandler crawlerstate = (CrawlerHandler) localData;
            state.attemptUrls.addAll(crawlerstate.attemptUrls);
            state.visitedUrls.addAll(crawlerstate.visitedUrls);
            state.discoveredUrls.addAll(crawlerstate.discoveredUrls);  
        }
        fetchcsv(state);
        visitcsv(state);
        urlscsv(state);
        statsfile(state);
	}
	
	public static void fetchcsv(CrawlerHandler state) throws Exception{
		FileWriter writer = new FileWriter("fetch_c-span.csv");
		writer.append("URL,HTTP STATUS CODE");
		writer.append('\n');
		for (UrlInfo info : state.attemptUrls) {
            writer.append(info.url + "," + info.statusCode + "\n");
        }
		writer.flush();
		writer.close();
    }
	
	public static void visitcsv(CrawlerHandler state) throws Exception{
		FileWriter writer = new FileWriter("visit_c-span.csv");
		writer.append("URL,Size of Dowloaded File in Bytes,# of OutLinks,Content Type\n");
		for (UrlInfo info : state.visitedUrls) {
            if (info.type != "unknown") {
                writer.append(info.url + "," + info.size + "," + info.outgoingUrls.size() + "," + info.type + "\n");
            }
        }
		writer.flush();
		writer.close();
    }
	
	public static void urlscsv(CrawlerHandler state) throws Exception{
		FileWriter writer = new FileWriter("urls_c-span.csv");
		writer.append("URL,Validity\n");
		for (UrlInfo info : state.discoveredUrls) {
            writer.append(info.url + "," + info.type + "\n");
        }
		writer.flush();
		writer.close();
    }
	
	public static void statsfile(CrawlerHandler state) throws Exception{
		int failedUrlsCount = 0;
	    int abortedUrlsCount = 0;
		FileWriter writer = new FileWriter("CrawlReport_C-Span.txt");
		writer.append("");
		writer.append("Web site crawled:");
		writer.append("Fetch Statistics\n================\n");
        writer.append("# fetches attempted: " + state.attemptUrls.size() + "\n");
        writer.append("# fetches succeeded: " + state.visitedUrls.size() + "\n");
       
        for (UrlInfo info : state.attemptUrls) {
            if (info.statusCode >= 300 && info.statusCode < 400) {
                abortedUrlsCount++;
            } else if (info.statusCode != 200) { //error ??
                failedUrlsCount++;
            }
        }

        writer.append("# fetches failed or aborted: " + (failedUrlsCount + abortedUrlsCount) + "\n\n");
        
        HashSet<String> hashSet = new HashSet<String>();
        int uniqueUrls = 0;
        int okUrls = 0;
        int outUrls = 0;
        writer.append("Outgoing URLs\n==============\n");
        writer.append("Total URLs extracted: " + state.discoveredUrls.size() + "\n");
        for (UrlInfo info : state.discoveredUrls) {
            if (!hashSet.contains(info.url)) {
                hashSet.add(info.url);
                uniqueUrls++;
                if (info.type.equals("OK")) {
                    okUrls++;
                } 
                else {
                    outUrls++;
                }
            }
        }
        writer.append("# unique URLs extracted: " + uniqueUrls + "\n");
        writer.append("# unique URLs within News Site: " + okUrls + "\n");
        writer.append("# unique URLs outside News Site: " + outUrls + "\n\n");
        
        // Status Code
        writer.append("Status Codes:\n=============\n");
        HashMap<Integer, Integer> hashMap = new HashMap<Integer, Integer>();
        for (UrlInfo info : state.attemptUrls) {
            if (hashMap.containsKey(info.statusCode)) {
                hashMap.put(info.statusCode, hashMap.get(info.statusCode) + 1);
            } else {
                hashMap.put(info.statusCode, 1);
            }
        }
        HashMap<Integer, String> statusCodeMapping = new HashMap<Integer, String>();
        statusCodeMapping.put(200, "OK");
        statusCodeMapping.put(301, "Moved Permanently");
        statusCodeMapping.put(302, "Found");
        statusCodeMapping.put(401, "Unauthorized");
        statusCodeMapping.put(403, "Forbidden:");
        statusCodeMapping.put(404, "Not Found:");
        statusCodeMapping.put(405, "Method Not Allowed");
        statusCodeMapping.put(500, "Internal Server Error");

        for (Integer key : hashMap.keySet()) {
        	if(hashMap.get(key)!=0)
        		writer.append(key + " " + statusCodeMapping.get(key) + ": " + hashMap.get(key) + "\n");
        }
        writer.append("\n");
        writer.append("File Sizes:\n===========\n");
        int oneK = 0;
        int tenK = 0;
        int hundredK = 0;
        int oneM = 0;
        int other = 0;
        for (UrlInfo info : state.visitedUrls) {
            if (info.size < 1024) {
                oneK++;
            } else if (info.size < 10240) {
                tenK++;
            } else if (info.size < 102400) {
                hundredK++;
            } else if (info.size < 1024 * 1024) {
                oneM++;
            } else {
                other++;
            }
        }
        writer.append("< 1KB: " + oneK + "\n");
        writer.append("1KB ~ <10KB: " + tenK + "\n");
        writer.append("10KB ~ <100KB: " + hundredK + "\n");
        writer.append("100KB ~ <1MB: " + oneM + "\n");
        writer.append(">= 1MB: " + other + "\n");
        writer.append("\n");
        
     // Content Types
        HashMap<String, Integer> hashMap1 = new HashMap<String, Integer>();
        writer.append("Content Types:\n==============\n");
        for (UrlInfo info : state.visitedUrls) {
            /*if (info.type.equals("unknown")) {
                continue;
            }*/
            if (hashMap1.containsKey(info.type)) {
                hashMap1.put(info.type, hashMap1.get(info.type) + 1);
            } else {
                hashMap1.put(info.type, 1);
            }
        }
        for (String key : hashMap1.keySet()) {
            writer.append("" + key + ": " + hashMap1.get(key) + "\n");
        }
        writer.append("\n");

		writer.flush();
		writer.close();
    }
}

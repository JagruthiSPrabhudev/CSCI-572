import java.util.ArrayList;

public class CrawlerHandler {
	ArrayList<UrlInfo> attemptUrls;
    ArrayList<UrlInfo> visitedUrls;
    ArrayList<UrlInfo> discoveredUrls;

    public CrawlerHandler() {
        attemptUrls = new ArrayList<UrlInfo>();
        visitedUrls = new ArrayList<UrlInfo>();
        discoveredUrls = new ArrayList<UrlInfo>();
    }
}

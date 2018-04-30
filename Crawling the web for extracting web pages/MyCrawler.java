import java.util.*;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class MyCrawler extends WebCrawler{
	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js"
            + "|wav|avi|mov|mpeg|mpg|ram|m4v|wma|wmv|mid|txt" + "|mp2|mp3|mp4|zip|rar|gz|exe))$");
	CrawlerHandler state = new CrawlerHandler();
	@Override
	protected void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
		// TODO Auto-generated method stub
		state.attemptUrls.add(new UrlInfo(webUrl.getURL(), statusCode));
	}
	
	@Override
    public Object getMyLocalData() {
        return state;
    }

	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		// TODO Auto-generated method stub
		 String href = url.getURL().toLowerCase().replaceAll(",", "-");
		 String valid = "";
		 if((href.startsWith("https://www.c-span.org/") || href.startsWith("http://www.c-span.org/")))
			    valid="OK";
		 else
			    valid = "N_OK";
		 state.discoveredUrls.add(new UrlInfo(href, valid));
		 return (!FILTERS.matcher(href).matches() && (href.startsWith("https://www.c-span.org/") || href.startsWith("http://www.c-span.org/")));
		
	}

	@Override
	public void visit(Page page) {
		// TODO Auto-generated method stub
		String url = page.getWebURL().getURL().replaceAll(",", "-");
		String contentType = page.getContentType().split(";")[0];
        ArrayList<String> outgoingUrls = new ArrayList<String>();
        UrlInfo urlInfo;
        if (contentType.equals("text/html")) { // html
            if (page.getParseData() instanceof HtmlParseData) {
                HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
                Set<WebURL> links = htmlParseData.getOutgoingUrls();
                for (WebURL link : links) {
                    outgoingUrls.add(link.getURL());
                }
                urlInfo = new UrlInfo(url, page.getContentData().length, outgoingUrls, "text/html");
                state.visitedUrls.add(urlInfo);
            } else {
                urlInfo = new UrlInfo(url, page.getContentData().length, outgoingUrls, "text/html");
                state.visitedUrls.add(urlInfo);
            }
        } else if (contentType.equals("application/msword")) { // doc
            urlInfo = new UrlInfo(url, page.getContentData().length, outgoingUrls, "application/msword");
            state.visitedUrls.add(urlInfo);
        } else if (contentType.equals("application/pdf")) { // pdf
            urlInfo = new UrlInfo(url, page.getContentData().length, outgoingUrls, "application/pdf");
            state.visitedUrls.add(urlInfo);
        } else if (contentType.contains("image/")) { // images
            urlInfo = new UrlInfo(url, page.getContentData().length, outgoingUrls, contentType);
            state.visitedUrls.add(urlInfo);
        } 
        else {
            urlInfo = new UrlInfo(url, page.getContentData().length, outgoingUrls, "unknown");
            state.visitedUrls.add(urlInfo);
        }
        
        
      
       
	}
	
}



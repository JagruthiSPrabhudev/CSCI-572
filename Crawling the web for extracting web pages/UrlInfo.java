import java.util.ArrayList;

public class URLInfo {
	String name, type;
	int statusCode;
	int size;
	ArrayList<String> outgoingURLS;
	
	public URLInfo() {}
	public URLInfo(String url, int statusCode)
	{
		name = new String(url);
		this.statusCode = statusCode;
	}

	public URLInfo(String url, String type)
	{
		name = new String(url);
		type = new String(type);
	}
	public URLInfo(String url, int size, ArrayList<String> outGoingURLS, String type)
	{
		name = new String(url);
		this.size = size;
		this.outgoingURLS = outGoingURLS;
		this.type = type;
	}
}

class CrawlerHandler{
	ArrayList<URLInfo> attempted;
	ArrayList<URLInfo> discovered;
	ArrayList<URLInfo> visited;
	
	public CrawlerHandler() 
	{
		attempted = new ArrayList<URLInfo>();
		discovered = new ArrayList<URLInfo>();
		visited = new ArrayList<URLInfo>();
	}
}
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class indexExtractor {
	/*
	public static void main(String args[])
	{
		Document doc = null;
		try {
			doc = Jsoup.connect("https://twitter.com").get();
			System.out.println(doc.title());
			Elements links = doc.select("a[href]");
			
			for(Element link : links)
			{
				System.out.println("HI");
				System.out.println(link.attr("href"));
				System.out.println(link.text());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}*/
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		File crawled = new File("C:\\Users\\shrir\\Documents\\USC Courses\\CSCI 572\\Assign4\\Shared_Folder\\Newsday-20180325T175149Z-001\\Newsday\\HTML Files\\HTML Files");
		File Map = new File("C:\\Users\\shrir\\Documents\\USC Courses\\CSCI 572\\Assign4\\Shared_Folder\\Newsday-20180325T175149Z-001\\Newsday\\UrlToHtml_Newday.csv");
		Set<String> edges = new HashSet<String>();
		FileWriter writer = new FileWriter("C:\\Users\\shrir\\Documents\\USC Courses\\CSCI 572\\Assign4\\EdgeList.txt");
		HashMap<String,String> fileUrlMap = new HashMap<String,String>();
		HashMap<String,String> urlFileMap = new HashMap<String,String>();
		BufferedReader br = null;
		BufferedWriter bw = null;
		HashSet<String> set= new HashSet<String>();
		String line = "";
		try{
			br = new BufferedReader(new FileReader(Map));
			while((line = br.readLine()) != null){
				String[] tokens = line.split(",");
				fileUrlMap.put(tokens[0], tokens[1]);
				urlFileMap.put(tokens[1], tokens[0]);
			}
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			if(br != null){
				try{
					br.close();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
		}
		
		//make edgelist.txt
		for(File file: crawled.listFiles()){
			Document doc = Jsoup.parse(file, "UTF-8", fileUrlMap.get(file.getName()));
			Elements links = doc.select("a[href]");
			for(Element link: links){
				String url = link.attr("abs:href").trim();
				if(urlFileMap.containsKey(url)){
					edges.add(file.getName() + " " + urlFileMap.get(url));
				}
			}
		}
		//make edgelist.txt
		try{
			bw = new BufferedWriter(writer);
			for(String s: edges){
				bw.write(s);
				bw.write("\n");
			}
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			if(bw != null){
				try{
					bw.close();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
		}
	}
}

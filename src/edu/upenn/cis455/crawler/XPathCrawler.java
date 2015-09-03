package edu.upenn.cis455.crawler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.prefs.Preferences;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import com.sleepycat.persist.EntityCursor;

import edu.upenn.cis455.crawler.info.*;
import edu.upenn.cis455.storage.*;
import edu.upenn.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;

public class XPathCrawler {
	HashMap<String, RobotsTxtInfo> robots;
	HashMap<String, Long> hostCrawledTimes;
	LinkedList<String> urlQueue;
	String startUrl;
	int maxNumber;
	double maxSize;
	String DBdirec;
	int crawled;
	String currentUrl;
	DBWrapper db;
	boolean remoteControled;

	public void setup(String startUrl, String DBdirec, double maxSize,
			int maxNumber, boolean remoteControled) {
		robots = new HashMap<String, RobotsTxtInfo>();
		hostCrawledTimes = new HashMap<String, Long>();
		urlQueue = new LinkedList<String>();
		this.startUrl = startUrl;
		this.DBdirec = DBdirec;
		this.maxSize = maxSize;
		this.maxNumber = maxNumber;
		this.remoteControled = remoteControled;
	}

	public void startCrawl() throws IOException {
		Preferences root = Preferences.userRoot();
		root.putBoolean("stop", false);
		db = new DBWrapper(DBdirec);
		db.setup();
		urlQueue.offer(startUrl);
		while (!urlQueue.isEmpty() && crawled < maxNumber) {
			if (remoteControled) {
				if (root.getBoolean("stop", false)) {
					System.out.println("Crawler stopped.");
					break;
				}
			}
			currentUrl = urlQueue.poll();
			HttpClient client = new HttpClient(currentUrl);
			if (!client.isUrlValid())
				continue;
			client.sendHead();
			if (Integer.parseInt(client.getStatus()) >= 400)
				continue;
			String contentType = client.getContentType();
			int contentLength = client.getContentLength();
			long lastModified = client.getLastModified();
			if (contentLength > maxSize * 1000000)
				continue;

			String host = new URLInfo(currentUrl).getHostName();
			Long last = hostCrawledTimes.get(host);
			long hostLastCrawled;
			if (last == null)
				hostLastCrawled = 0;
			else
				hostLastCrawled = last;
			if (!robotsAllowed(currentUrl, hostLastCrawled)) {
				continue;
			}

			client.sendGet();
			String content = client.getBody();
			if (isXml(contentType)) {
				XML entry = db.getXML(currentUrl);
				if (entry != null) {
					if (entry.getLastCrawledTime() > lastModified) {
						addToChannels(entry);
						System.out.println("Not modified: " + currentUrl);
						continue;
					}
				}
				XML xml = new XML();
				xml.setUrl(currentUrl);
				xml.setContent(content);
				xml.setLastCrawledTime(System.currentTimeMillis());
				db.putXML(xml);
				addToChannels(xml);
				hostCrawledTimes.put(host, System.currentTimeMillis());
				System.out.println("Downloading: " + currentUrl);
				crawled++;
			} else if (contentType.equals("text/html")) {
				HTML html = db.getHTML(currentUrl);
				if (html != null) {
					if (html.getLastCrawledTime() > lastModified) {
						System.out.println("Not modified: " + currentUrl);
						addLocalContenttoQueue(html);
						continue;
					}
				}
				html = new HTML();
				html.setUrl(currentUrl);
				html.setContent(content);
				html.setLastCrawledTime(System.currentTimeMillis());
				db.putHTML(html);
				hostCrawledTimes.put(host, System.currentTimeMillis());
				System.out.println("Downloading: " + currentUrl);
				crawled++;

				addLinkstoQueue();
			}
		}
		db.close();
	}
	
	private void addToChannels(XML xml) {
		EntityCursor<Channel> channelCursor = db.getChannelIndex().entities();
		Iterator<Channel> i = channelCursor.iterator();
		XPathEngineImpl xEngine = (XPathEngineImpl) XPathEngineFactory
				.getXPathEngine();
		SAXParser parser = null;
		try {
			parser = SAXParserFactory.newInstance().newSAXParser();
		} catch (ParserConfigurationException | SAXException e) {
			e.printStackTrace();
		}
		
		while (i.hasNext()) {
			Channel channel = i.next();
			if (channel.getXmls().contains(xml.getUrl())) continue;
			ArrayList<String> xpaths = channel.getXpaths();
			String[] expressions = xpaths.toArray(new String[xpaths.size()]);
			xEngine.setXPaths(expressions);
			InputStream in = xml.getInputStream();
			try {
				parser.parse(in, xEngine);
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			boolean[] evaluate = xEngine.evaluate(null);
			if (hasTrue(evaluate)) {
				channel.addXml(xml.getUrl());
				db.putChannel(channel);
			}
		}
		channelCursor.close();
	}
	
	private boolean hasTrue(boolean[] evaluate) {
		for (int i = 0; i < evaluate.length; i++) {
			if (evaluate[i])
				return true;
		}
		return false;
	}
	
	private void addLocalContenttoQueue(HTML html) throws IOException {
		String content = html.getContent();
		Document doc = Jsoup.parse(content, html.getUrl());
		Elements links = doc.getElementsByTag("a");
		for (Element link : links) {
			String linkHref = link.attr("abs:href");
			urlQueue.offer(linkHref);
		}
	}

	private void addLinkstoQueue() throws IOException {
		Document doc = Jsoup.connect(currentUrl).get();
		Elements links = doc.getElementsByTag("a");
		for (Element link : links) {
			String linkHref = link.attr("abs:href");
			urlQueue.offer(linkHref);
		}
	}

	public boolean robotsAllowed(String url, long lastCrawled)
			throws IOException {
		RobotsTxtInfo robot = getRobot(url);
		if (robot == null)
			return true;
		url = trimURL(url);
		String host = new URLInfo(url).getHostName();
		long elapsed = (System.currentTimeMillis() - lastCrawled);
		if (robot.containsUserAgent("cis455crawler")) {
			Integer delay = robot.getCrawlDelay("cis455crawler");

			 if (delay != null && elapsed < 1000 * delay) {
			 urlQueue.offer(currentUrl);
			 return false;
			 }
			ArrayList<String> allowedLinks = robot
					.getAllowedLinks("cis455crawler");
			if (allowedLinks != null)
				for (String end : allowedLinks) {
					if (isSubDirec(host + end, url))
						return true;
				}
			ArrayList<String> disallowedLinks = robot
					.getDisallowedLinks("cis455crawler");
			if (disallowedLinks != null)
				for (String end : disallowedLinks) {
					if (isSubDirec(host + end, url))
						return false;
				}
		} else if (robot.containsUserAgent("*")) {
			Integer delay = robot.getCrawlDelay("*");
			if (delay != null && elapsed < 1000 * delay) {
				urlQueue.offer(currentUrl);
				return false;
			}
			ArrayList<String> allowedLinks = robot.getAllowedLinks("*");
			if (allowedLinks != null)
				for (String end : allowedLinks) {
					if (isSubDirec(host + end, url))
						return true;
				}
			ArrayList<String> disallowedLinks = robot.getDisallowedLinks("*");
			if (disallowedLinks != null)
				for (String end : disallowedLinks) {
					if (isSubDirec(host + end, url))
						return false;
				}
		}
		return true;
	}

	public boolean isSubDirec(String x, String y) {
		if (x.equals(y))
			return true;
		int a = y.indexOf(x);
		if (a != 0)
			return false;
		if (x.endsWith("/"))
			return true;
		if (y.charAt(x.length()) == '/' || y.charAt(x.length()) == '.')
			return true;
		return false;
	}

	private String trimURL(String s) {
		if (s.startsWith("http://")) {
			return s.substring(7);
		} else if (s.startsWith("https://")) {
			return s.substring(8);
		}
		return s;
	}
	
	private boolean isXml(String contentType) {
		if (contentType.equals("text/xml")
				|| contentType.equals("application/xml")
				|| contentType.endsWith("+xml"))
			return true;
		return false;
	}

	public RobotsTxtInfo getRobot(String url) throws IOException {
		URLInfo urlInfo = new URLInfo(url);
		String host = urlInfo.getHostName();
		RobotsTxtInfo robot = robots.get(host);
		if (robot != null)
			return robot;
		HttpClient client = new HttpClient(host + "/robots.txt");

		client.sendHead();
		if (!client.getStatus().equals("200"))
			return null;

		client.sendGet();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				client.getBodyInputStream()));
		String line;
		String currentAgent = "";
		robot = new RobotsTxtInfo();
		while ((line = in.readLine()) != null) {
			line = line.trim();
			if (line.startsWith("User-agent")) {
				currentAgent = getContent(line);
				robot.addUserAgent(currentAgent);
			} else if (line.startsWith("Allow")) {
				if (!getContent(line).equals(""))
					robot.addAllowedLink(currentAgent, getContent(line));
			} else if (line.startsWith("Disallow")) {
				if (!getContent(line).equals(""))
					robot.addDisallowedLink(currentAgent, getContent(line));
			} else if (line.startsWith("Crawl-delay")) {
				if (!getContent(line).equals(""))
					robot.addCrawlDelay(currentAgent,
							Integer.parseInt(getContent(line)));
			}
		}
		robots.put(host, robot);
		return robot;
	}

	private String getContent(String s) {
		return s.split(":")[1].trim();
	}

	public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			System.err.println("need 3 arguments");
			return;
		}
		String startUrl = args[0];
		String DBdirec = args[1];
		double maxSize = Double.parseDouble(args[2]);
		XPathCrawler crawler = new XPathCrawler();
		crawler.setup(startUrl, 
				DBdirec, maxSize, 1000, false);
		crawler.startCrawl();
	}
}
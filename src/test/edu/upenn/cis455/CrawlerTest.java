package test.edu.upenn.cis455;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.upenn.cis455.crawler.XPathCrawler;
import edu.upenn.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis455.storage.DBWrapper;
import junit.framework.TestCase;

public class CrawlerTest extends TestCase{
	XPathCrawler crawler;
	DBWrapper db;
	
	@Before
	public void setUp() throws Exception {
		crawler = new XPathCrawler();
		crawler.setup("https://dbappserv.cis.upenn.edu/crawltest/international/", 
				"./dbTest", 10, 1000, false);	
		db = new DBWrapper("./dbTest");
		db.setup();
	}
	
	@Test
	public void testRobots() throws IOException {
		RobotsTxtInfo robot = crawler.getRobot("https://dbappserv.cis.upenn.edu/crawltest/misc/weather.xml");
		assertTrue(robot.containsUserAgent("cis455crawler"));
		assertTrue(robot.containsUserAgent("*"));
		assertFalse(robot.containsUserAgent("randomcrawler"));
		ArrayList<String> disallow = robot.getDisallowedLinks("cis455crawler");
		assertTrue(disallow.contains("/crawltest/marie/private/"));
		assertTrue(disallow.contains("/infrastructure/"));
		assertTrue(disallow.contains("/ppod/"));
		assertFalse(disallow.contains("/random"));
		assertTrue(robot.getCrawlDelay("cis455crawler") == 5);
		
		assertFalse(crawler.robotsAllowed("https://dbappserv.cis.upenn.edu/ppod/", 0));
		assertTrue(crawler.robotsAllowed("https://dbappserv.cis.upenn.edu/ppod", 0));
		assertFalse(crawler.robotsAllowed("https://dbappserv.cis.upenn.edu/crawltest/foo/", 0));
		assertTrue(crawler.robotsAllowed("https://dbappserv.cis.upenn.edu/crawltest/foo", 0));
		//assertFalse(crawler.robotsAllowed("https://dbappserv.cis.upenn.edu/crawltest/foo", System.currentTimeMillis()));
	}
	
	@Test
	public void testCrawler() throws IOException {
		assertTrue(db.getHTML("https://dbappserv.cis.upenn.edu/crawltest/international/") == null);
		assertTrue(db.getXML("https://dbappserv.cis.upenn.edu/crawltest/international/corriere.xml") == null);
		assertTrue(db.getXML("https://dbappserv.cis.upenn.edu/crawltest/international/peoplesdaily_world.xml") == null);
		crawler.startCrawl();
		assertTrue(db.getHTML("https://dbappserv.cis.upenn.edu/crawltest/international/") != null);
		assertTrue(db.getXML("https://dbappserv.cis.upenn.edu/crawltest/international/corriere.xml") != null);
		assertTrue(db.getXML("https://dbappserv.cis.upenn.edu/crawltest/international/peoplesdaily_world.xml") != null);
		db.deleteHTML("https://dbappserv.cis.upenn.edu/crawltest/international/");
		db.deleteXML("https://dbappserv.cis.upenn.edu/crawltest/international/corriere.xml");
		db.deleteXML("https://dbappserv.cis.upenn.edu/crawltest/international/peoplesdaily_world.xml");
		assertTrue(db.getHTML("https://dbappserv.cis.upenn.edu/crawltest/international/") == null);
		assertTrue(db.getXML("https://dbappserv.cis.upenn.edu/crawltest/international/corriere.xml") == null);
		assertTrue(db.getXML("https://dbappserv.cis.upenn.edu/crawltest/international/peoplesdaily_world.xml") == null);
	}
	
	@After
	public void tearDown() throws Exception {
		db.close();
	}
	
}

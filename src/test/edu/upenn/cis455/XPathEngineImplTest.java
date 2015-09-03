package test.edu.upenn.cis455;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import edu.upenn.cis455.servlet.XPathServlet;
import edu.upenn.cis455.xpathengine.*;

public class XPathEngineImplTest extends TestCase{
	File file1;
	File file2;
	XPathEngineImpl engine;
	SAXParser parser;
	XPathServlet servlet;
	MockHttpServletRequest request;
    MockHttpServletResponse response;
	
	@Before
	public void setUp() throws Exception {
		file1 = new File("./conf/test1.xml");
		file2 = new File("./conf/test2.xml");
		engine = (XPathEngineImpl) XPathEngineFactory.getXPathEngine();
		parser = SAXParserFactory.newInstance().newSAXParser();
		servlet = new XPathServlet();
		request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
	}

	@Test
	public void testXPathEngineImpl1() {
		String[] expressions = {"/CATALOG/CD[@1color = \"123\"]/TITLE", 
				"/CATALOG/CD[@color = \"red\"]/TITLE[@url = \"w3schools\"]",
				"/CATALOG/CD[@color = \"blue\"]/TITLE[@url = \"w3schools\"]"};
		engine.setXPaths(expressions);
		
		assertFalse(engine.isValid(0));
		assertTrue(engine.isValid(1));
		assertTrue(engine.isValid(2));
		try {
			parser.parse(file1, engine);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertFalse(engine.evaluate(null)[0]);
		assertFalse(engine.evaluate(null)[1]);
		assertTrue(engine.evaluate(null)[2]);
	}
	
	@Test
	public void testXPathEngineImpl2() {
		String[] expressions = {"/CATALOG/CD/TITLE[text = \"Empire Burlesque\"]", 
				"/CATALOG/CD/TITLE[text() = \"Emp Burlesque\"]",
				"/CATALOG/CD/TITLE[text() = \"Empire Burlesque\"]"};
		engine.setXPaths(expressions);
		
		assertFalse(engine.isValid(0));
		assertTrue(engine.isValid(1));
		assertTrue(engine.isValid(2));
		try {
			parser.parse(file1, engine);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertFalse(engine.evaluate(null)[0]);
		assertFalse(engine.evaluate(null)[1]);
		assertTrue(engine.evaluate(null)[2]);
	}
	
	@Test
	public void testXPathEngineImpl3() {
		String[] expressions = {"/web-app/display-name/context-param/param-name"
				+ "[contains(text(, \"123\")]", 
				"/web-app/context-param/param-name[contains(text(), \"123\")]",
				"/web-app/context-param/param-name[contains(text(), \"BDBstore\")]"};
		engine.setXPaths(expressions);
		
		assertFalse(engine.isValid(0));
		assertTrue(engine.isValid(1));
		assertTrue(engine.isValid(2));
		try {
			parser.parse(file2, engine);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertFalse(engine.evaluate(null)[0]);
		assertFalse(engine.evaluate(null)[1]);
		assertTrue(engine.evaluate(null)[2]);
	}
	
	@Test
	public void testXPathEngineImpl14() {
		String[] expressions = {"/web-app[display-name[@color = \"blue\"]]/context-param[@type = \"123\"]"
				+ "[param-name[contains(text, \"store\")][@url = \"w3schools\"]]", 
				"/web-app[display-name[@color = \"blue\"]]/context-param[@type = \"123\"]"
						+ "[param-name[contains(text(), \"btore\")][@url = \"w3schools\"]]",
				"/web-app[display-name[@color = \"blue\"]]/context-param[@type = \"123\"]"
				+ "[param-name[contains(text(), \"store\")][@url = \"w3schools\"]]"};
		engine.setXPaths(expressions);
		
		assertFalse(engine.isValid(0));
		assertTrue(engine.isValid(1));
		assertTrue(engine.isValid(2));
		try {
			parser.parse(file2, engine);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertFalse(engine.evaluate(null)[0]);
		assertFalse(engine.evaluate(null)[1]);
		assertTrue(engine.evaluate(null)[2]);
	}
	
	@Test
	public void testServlet1() throws IOException {
		servlet.doGet(request, response);
		assertEquals("text/html", response.getContentType());
		String s = "<html> <head><title>Xpath evaluation</title></head> <body>"
				+ " <h2>Name: Lu Lu <br> Login: lulu8</h2>" + "<form method=\"post\" action=\"xpath\"> Xpath:<br> "
				+ "<input type=\"text\" size=\"100\" name=\"xPath\"> <br> "
				+ "HTML/XML URL: <br> <input type=\"text\" size=\"100\" name=\"XmlUrl\"> "
				+ "<br><input type=\"submit\" value=\"Submit\">" + "</form></body></html>";
		assertEquals(s, servlet.getDoGetString());
	}
	
	@Test
	public void testServlet2() throws IOException {
		request.addParameter("xPath", "  /CATALOG/CD/ARTIST[text() =  \"Bob Dylan\"]; "
				+ "/CATALOG/CD/ARTIST[contains(text(), \"Dylan\")]  ");
		request.addParameter("XmlUrl", "  http://www.w3schools.com/xml/cd_catalog.xml  ");
		servlet.doPost(request, response);
		assertEquals("/CATALOG/CD/ARTIST[text() =  \"Bob Dylan\"]; "
				+ "/CATALOG/CD/ARTIST[contains(text(), \"Dylan\")]", servlet.getTestXPath());
		assertEquals("http://www.w3schools.com/xml/cd_catalog.xml", servlet.getTestXmlUrl());
		assertTrue(servlet.getTestEvaluate()[0]);
		assertTrue(servlet.getTestEvaluate()[1]);
	}
}

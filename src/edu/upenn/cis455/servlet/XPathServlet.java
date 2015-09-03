package edu.upenn.cis455.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.servlet.http.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import edu.upenn.cis455.crawler.info.HttpClient;
import edu.upenn.cis455.crawler.info.URLInfo;
import edu.upenn.cis455.xpathengine.*;
/**
 * Xpath evaluation servlet class.
 * doGet method shows the HTML form to the user
 * doPost method takes in the Xpath expressions and XML url and shows the results
 */
@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {
	String doGetString = null;
	String testXPath = null;
	String testXmlUrl = null;
	boolean[] testEvaluate = null;
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.setContentType("text/html");
		String xPath = request.getParameter("xPath").trim();
		String XmlUrl = request.getParameter("XmlUrl").trim();
		testXPath = xPath;
		testXmlUrl = XmlUrl;
		PrintWriter out = response.getWriter();

		HttpClient client = new HttpClient(XmlUrl);
		if (!client.isUrlValid()) {
			out.println("<html><body><h2>Url is invalid</h2><body><html>");
			return;
		}

		client.sendHead();
		if (!client.getStatus().equals("200")) {
			out.println("<html><body><h2>Url is invalid</h2><body><html>");
			return;
		}

		client.sendGet();
		InputStream in = client.getBodyInputStream();
		XPathEngineImpl xEngine = (XPathEngineImpl) XPathEngineFactory
				.getXPathEngine();

		String[] expressions;
		if (xPath.indexOf(";") == -1) {
			expressions = new String[] { xPath };
			
		} else {
			expressions = xPath.split(";");
			for (int i = 0; i < expressions.length; i++) {
				expressions[i] = expressions[i].trim();
			}
		}
		xEngine.setXPaths(expressions);

		SAXParser parser = null;
		try {
			parser = SAXParserFactory.newInstance().newSAXParser();
		} catch (ParserConfigurationException | SAXException e) {
			e.printStackTrace();
		}

		try {
			parser.parse(in, xEngine);
		} catch (SAXException e) {
			e.printStackTrace();
		}

		boolean[] evaluate = xEngine.evaluate(null);
		testEvaluate = evaluate;

		out.println("<html><body><h3>");
		for (int i = 0; i < evaluate.length; i++) {
			String judge = evaluate[i] ? "is" : "isn't";
			out.println(expressions[i] + " " + judge
					+ " matched <br>" );
		}
		out.println("<h3><body><html>");
	}

	public boolean[] getTestEvaluate() {
		return testEvaluate;
	}

	public String getTestXPath() {
		return testXPath;
	}

	public String getTestXmlUrl() {
		return testXmlUrl;
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
		doGetString = "<html> <head><title>Xpath evaluation</title></head> <body>"
				+ " <h2>Name: Lu Lu <br> Login: lulu8</h2>" + "<form method=\"post\" action=\"xpath\"> Xpath:<br> "
				+ "<input type=\"text\" size=\"100\" name=\"xPath\"> <br> "
				+ "HTML/XML URL: <br> <input type=\"text\" size=\"100\" name=\"XmlUrl\"> "
				+ "<br><input type=\"submit\" value=\"Submit\">" + "</form></body></html>";
		out.println(doGetString);
	}
	
	public String getDoGetString() {
		return doGetString;
	}
}

package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.sleepycat.persist.EntityCursor;

import edu.upenn.cis455.storage.*;
import edu.upenn.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;

public class ChannelServlet extends HttpServlet {
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		PrintWriter out = response.getWriter();
		String channelName = request.getParameter("channelName");
		if (channelName != null) channelName = channelName.trim();
		String xpaths = request.getParameter("xpaths");
		if (xpaths != null) xpaths = xpaths.trim();
		String xsltUrl = request.getParameter("xsltUrl");
		if (xsltUrl != null) xsltUrl = xsltUrl.trim();
		String userName = request.getParameter("userName");
		if (userName != null) userName = userName.trim();
		String delete = request.getParameter("delete");
		
		ServletContext context = getServletContext();
		DBWrapper db = new DBWrapper(context.getInitParameter("BDBstore"));
		db.setup();
		Channel channel = db.getChannel(channelName);
		if (delete != null && delete.equals("on")) {
			User user = db.getUser(userName);
			user.deleteChannel(channelName);
			db.putUser(user);
			db.deleteChannel(channelName);
			out.println("<html><body><h3>Delete successfully!</h3>"
				+ "<a href=\"/HW2/home?userName=" +userName +"\"><button type=\"button\">Back</button></a>"	+ "</body></html>");
			
			db.close();
			return;
		}
		if (channel != null) {
			out.println("<html><body><h3>Channel name already exists</h3></body></html>");
			db.close();
			return;
		}
		channel = new Channel();
		channel.setName(channelName);
		channel.setUserName(userName);
		channel.setXSLurl(xsltUrl);

		ArrayList<String> xpathList = new ArrayList<String>();
		String[] expressions = xpaths.split(";");
		for (int i = 0; i < expressions.length; i++) {
			xpathList.add(expressions[i].trim());
		}
		channel.setXpaths(xpathList);
		db.putChannel(channel);

		User user = db.getUser(userName);
		user.addChannel(channelName);
		db.putUser(user);

		out.println("<html><body><h3>Channel successfully created!</h3><br>"
				+"<a href=\"/HW2/home?userName=" +userName +"\"><button type=\"button\">Back</button></a>"
				+ "</body></html>");
		db.close();
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.setContentType("text/xml");
		PrintWriter out = response.getWriter();
		String channelName = request.getParameter("channelName").trim();
		ServletContext context = getServletContext();
		DBWrapper db = new DBWrapper(context.getInitParameter("BDBstore"));
		db.setup();
		Channel channel = db.getChannel(channelName);

		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<?xml-stylesheet type=\"text/xsl\" href=\"" + channel.getXSLurl() + "\"?>");
		out.println("<documentcollection>");
		
		for (String xmlUrl: channel.getXmls()) {
			XML xml = db.getXML(xmlUrl);
			String pattern = "yyyy-MM-dd'T'HH:mm:ss";
			SimpleDateFormat format = new SimpleDateFormat(pattern);
			Date date = new Date(xml.getLastCrawledTime());
			String str = format.format(date);
			out.println("<document crawled = \"" + str
					+ "\" location = \"" + xml.getUrl() + "\">");
			out.println(xml.getRawContent());
			out.println("</document>");
		}
		
		out.println("</documentcollection>");
		db.close();
	}

	private boolean hasTrue(boolean[] evaluate) {
		for (int i = 0; i < evaluate.length; i++) {
			if (evaluate[i])
				return true;
		}
		return false;
	}
}

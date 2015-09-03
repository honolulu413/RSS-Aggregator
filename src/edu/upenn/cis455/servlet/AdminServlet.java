package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.prefs.Preferences;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.PrimaryIndex;

import edu.upenn.cis455.crawler.info.*;
import edu.upenn.cis455.storage.*;

public class AdminServlet extends HttpServlet{
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException{
		PrintWriter out = response.getWriter();
		Preferences root = Preferences.userRoot();
		root.putBoolean("stop", true);
		out.println("<html><body>Crawler stopped" +
				"<br><a href=\"/HW2/admin\"><button type=\"button\">Back to admin</button></a>"
				+ "</body></html>");
	}
	
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		HttpSession session = request.getSession(true);
		PrintWriter out = response.getWriter();
		String sessionUserName = (String) session.getAttribute("userName");
		if (sessionUserName == null || !sessionUserName.equals("admin")) {
			out.println("<html><body><h3>Need to login!</h3><br><a href=\"/HW2/login\">"
					+ "<button type=\"button\">Back to login</button></a></body></html>");
			return;
		}
		ServletContext context = getServletContext();
		DBWrapper db = new DBWrapper(context.getInitParameter("BDBstore"));
		db.setup();
		
		PrimaryIndex<String, HTML> HTMLIndex = db.getHTMLIndex();
		PrimaryIndex<String, XML> XMLIndex = db.getXMLIndex();
		PrimaryIndex<String, Channel> channelIndex = db.getChannelIndex();
		int count = 0;
		EntityCursor<HTML> HTMLCursor = HTMLIndex.entities();
		EntityCursor<XML> XMLCursor = XMLIndex.entities();
		EntityCursor<Channel> channelCursor = channelIndex.entities();
		Iterator<HTML> HTMLi = HTMLCursor.iterator();
		Iterator<XML> XMLi = XMLCursor.iterator();
		Iterator<Channel> channeli = channelCursor.iterator();
		
		HashSet<String> servers = new HashSet<String>();
		HashMap<String, ArrayList<String>> serverCount = new HashMap<String, ArrayList<String>>();
		
		while (HTMLi.hasNext()) {
			HTML html = HTMLi.next();
			count += html.getContent().length();
			String server = getServer(html.getUrl());
			if (!servers.contains(server)) {
				servers.add(server);
			}
		}
		
		while (XMLi.hasNext()) {
			XML xml = XMLi.next();
			count += xml.getContent().length();
			String server = getServer(xml.getUrl());
			if (!servers.contains(server)) {
				servers.add(server);
			}
		}

		out.println("<html><body>");
		out.println("<h2>Welcome! " + "admin" + "</h2><br>");
		
		out.println("<h3>Start a crawler</h3>");
		out.println("<form method=\"post\" action=\"home\"> Start url:<br> "
				+ "<input type=\"text\" size=\"50\" name=\"start\"> <br> "
				+ "DBstore location: <br> <input type=\"text\" size=\"50\" name=\"dbStore\"> "
				+ "<br>Max size(MB)<br> "
				 + "<input type=\"text\" size=\"50\" name=\"maxSize\"> <br><input type=\"submit\" value=\"Start\"></form>");
		
		out.println("<form method=\"post\" action=\"admin\"><input type=\"submit\" value=\"Stop\"></form>");
		out.println("Number of HTML pages scanned: " + HTMLIndex.count());
		out.println("<br>Number of XML documents retrieved: " + XMLIndex.count());
		out.println("<br>Amout of data downloaded: " + count + " Bytes");
		out.println("<br>Number of servers: " + servers.size());
		out.println("<br>Number of XML documents in each channel:<br>");
		while (channeli.hasNext()) {
			Channel channel = channeli.next();
			for (String url: channel.getXmls()) {
				String serverName = getServer(url);
				ArrayList<String> list = serverCount.get(serverName);
				if (list == null) {
					list = new ArrayList<String>();
					list.add(url);
					serverCount.put(serverName, list);
				} else {
					if (!list.contains(url)) {
						list.add(url);
					}
				}
			}
			out.println(channel.getName() + ": " + channel.getXmls().size() + "&nbsp;&nbsp;");
		}
		out.println("<br>");
		int max = 0;
		for (String server: serverCount.keySet()) {
			max = Math.max(max, serverCount.get(server).size());
		}
		out.println("The most popular servers :<br>");
		for (String server: serverCount.keySet()) {
			if (max == serverCount.get(server).size()) {
				out.println(server + "<br>");
			}
		}
		
		
		out.println("</body></html>");
		
		HTMLCursor.close();
		XMLCursor.close();
		channelCursor.close();
		db.close();
	}
	
	private String getServer(String url) {
		return new URLInfo(url).getHostName();
	}
}

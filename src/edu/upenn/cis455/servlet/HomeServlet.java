package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.crawler.XPathCrawler;
import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.User;

public class HomeServlet extends HttpServlet {
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException{
		PrintWriter out = response.getWriter();
		String start = request.getParameter("start").trim();
		String dbStore = request.getParameter("dbStore").trim();
		int maxSize = Integer.parseInt(request.getParameter("maxSize").trim());
		
		XPathCrawler crawler = new XPathCrawler();
		crawler.setup(start, dbStore, maxSize, 1000, true);
		out.println("<html><body>Starting to crawl!" +
				"<br><a href=\"/HW2/admin\"><button type=\"button\">Back to admin</button></a>"
				+ "</body></html>");
		out.flush();
		crawler.startCrawl();
	}
	
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String userName = request.getParameter("userName").trim();
		HttpSession session = request.getSession(true);
		PrintWriter out = response.getWriter();
		String sessionUserName = (String) session.getAttribute("userName");
		if (sessionUserName == null || !sessionUserName.equals(userName)) {
			out.println("<html><body><h3>Need to login!</h3><br><a href=\"/HW2/login\">"
					+ "<button type=\"button\">Back to login</button></a></body></html>");
			return;
		}

		ServletContext context = getServletContext();
		DBWrapper db = new DBWrapper(context.getInitParameter("BDBstore"));
		db.setup();
		User user = db.getUser(userName);
		out.println("<html><body>");
		out.println("<h2>Welcome! " + userName + "</h2><br>");
		out.println("<h3><a href=/HW2/logout><button type=\"button\">Logout</button></a></h3>");
		out.println("<h3>Your channels: </h3>");
		ArrayList<String> channels = user.getChannels();
		out.println("<table border=\"1\" style=\"width:100%\">");
		out.println("<tr> <th>Channal Name</th> <th>Xpaths</th> <th>XSLT url</th> </tr>");
		for (String channelName : channels) {
			out.println("<tr> <td><a href = \"/HW2/channel?channelName="
					+ channelName
					+ "\">"
					+ channelName
					+ "</a>"
					+ "<form method=\"post\" action=\"channel\"><input type = \"hidden\" name=\"userName\" value = \""
					+ userName
					+ "\"><input type = \"hidden\" name=\"channelName\" value = \""
					+ channelName
					+ "\"><input type = \"hidden\" name=\"delete\" value = \"on\"><input type=\"submit\" value=\"Delete\"></form></td>");
			Channel channel = db.getChannel(channelName);
			ArrayList<String> Xpaths = channel.getXpaths();
			out.println("<td>");
			for (String xPath : Xpaths) {
				out.println(xPath + "<br>");
			}
			out.println("</td>");
			out.println("<td>" + channel.getXSLurl() + "</td></tr>");
		}
		out.println("</table><br>");

		out.println("<h3>Create a channel</h3>");
		out.println("<form method=\"post\" action=\"channel\"> Channel name:<br> "
				+ "<input type=\"text\" size=\"50\" name=\"channelName\"> <br> "
				+ "Xpaths(separated by semicolon): <br> <input type=\"text\" size=\"50\" name=\"xpaths\"> "
				+ "<br>XSLT url:<br> "
				+ "<input type = \"hidden\" name=\"userName\" value = \""
				+ userName
				+ "\"><input type=\"text\" size=\"50\" name=\"xsltUrl\"> <br><input type=\"submit\" value=\"Create\"></form>");

		out.println("</body></html>");
		db.close();
	}
}

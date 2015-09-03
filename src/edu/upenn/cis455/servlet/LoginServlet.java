package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.xml.sax.SAXException;

import com.sleepycat.persist.EntityCursor;

import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.User;
import edu.upenn.cis455.storage.XML;

public class LoginServlet extends HttpServlet {
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		String userName = request.getParameter("userName").trim();
		String password = request.getParameter("password").trim();
		if (userName.equals("admin") || password.equals("admin")) {
			HttpSession session = request.getSession(true);
			session.setAttribute("userName", userName);
			response.sendRedirect("/HW2/admin");
			return;
		}
		
		ServletContext context = getServletContext();
		DBWrapper db = new DBWrapper(context.getInitParameter("BDBstore"));
		db.setup();
		User user = db.getUser(userName);
		if (user == null || !user.getPassword().equals(password)) {
			out.println("<html><body><h3>Incorrect username and password</h3></body></html>");
			db.close();
			return;
		}
		db.close();
		HttpSession session = request.getSession(true);
		session.setAttribute("userName", userName);
		response.sendRedirect("/HW2/home?userName=" + userName);
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		String s = "<html> <head><title>Login</title></head> <body>"
				+ " <h2>Name: Lu Lu <br> Login: lulu8</h2>"
				+ "<form method=\"post\" action=\"login\"> Username:<br> "
				+ "<input type=\"text\" size=\"30\" name=\"userName\"> <br> "
				+ "Password: <br> <input type=\"text\" size=\"30\" name=\"password\"> "
				+ "<br><input type=\"submit\" value=\"Login\"></form>"
				+ "<br><br>Haven't got an account? <form method=\"get\" action=\"register\"><input type=\"submit\" value=\"register\"></form>";
		out.println(s);
		out.println("<br><br>Current channels: <br>");
		ServletContext context = getServletContext();
		DBWrapper db = new DBWrapper(context.getInitParameter("BDBstore"));
		db.setup();
		EntityCursor<Channel> channelCursor = db.getChannelIndex().entities();
		Iterator<Channel> i = channelCursor.iterator();
		while (i.hasNext()) {
			Channel channel = i.next();
			out.println("<a href = \"/HW2/channel?channelName=" + channel.getName() + "\">" + channel.getName() + "</a>\t");
		}
		
		channelCursor.close();
		db.close();
		out.println("</body></html>");
		
	}
}

package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis455.storage.*;

public class RegisterServlet extends HttpServlet{
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		String userName = request.getParameter("userName").trim();
		String password = request.getParameter("password").trim();
		ServletContext context = getServletContext();
		DBWrapper db = new DBWrapper(context.getInitParameter("BDBstore"));
		db.setup();
		User user = db.getUser(userName);		
		if (user != null) {
			out.println("<html><body>User name already registered!"
					+ " Please try a new one.</body></html>");
			db.close();
			return;
		}
		user = new User();
		user.setUserName(userName);
		user.setPassword(password);
		db.putUser(user);
		out.println("<html><body>Registered successfully!" +
				"<br><a href=\"/HW2/login\"><button type=\"button\">Back to login</button></a>"
				+ "</body></html>");
		db.close();
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		String s = "<html> <head><title>Register</title></head> <body>"
				+ "<form method=\"post\" action=\"register\"> Username:<br> "
				+ "<input type=\"text\" size=\"30\" name=\"userName\"> <br> "
				+ "Password: <br> <input type=\"text\" size=\"30\" name=\"password\"> "
				+ "<br><input type=\"submit\" value=\"Register\">"
				+ "</form></body></html>";
		out.println(s);
	}
}

package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LogoutServlet extends HttpServlet{
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		HttpSession session = request.getSession(true);
		session.invalidate();
		PrintWriter out = response.getWriter();
		out.println("<html><body><h3>Logout successfully</h3><br><a href=\"/HW2/login\">"
				+ "<button type=\"button\">Back to login</button></a></body></html>");
	}
}

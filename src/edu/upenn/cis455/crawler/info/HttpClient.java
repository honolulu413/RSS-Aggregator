package edu.upenn.cis455.crawler.info;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.prefs.Preferences;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

import edu.upenn.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;

public class HttpClient {
	String url;
	URLInfo urlInfo;
	Socket socket;
	boolean valid;
	PrintWriter out;
	InputStream in;
	int contentLength;
	String contentType;
	String body;
	long lastModified;
	String status;
	HttpsURLConnection con;
	URL myurl;

	public HttpClient(String webUrl) {
		valid = true;
		this.url = webUrl;

		if (url.startsWith("https")) {
			try {
				myurl = new URL(url);
			} catch (MalformedURLException e) {
				valid = false;
			}
			try {
				con = (HttpsURLConnection) myurl.openConnection();
			} catch (Exception e) {
				valid = false;
			}
			return;
		}
		urlInfo = new URLInfo(webUrl);
		try {
			socket = new Socket(urlInfo.getHostName(), urlInfo.getPortNo());
			out = new PrintWriter(socket.getOutputStream(), true);
			in = socket.getInputStream();
		} catch (IOException e) {
			valid = false;
		}

	}

	public void sendHead() throws IOException  {
		if (url.startsWith("https")) {
			con.setRequestMethod("HEAD");
			con.setRequestProperty("User-Agent", "cis455crawler");
			contentType = getValue(con.getContentType());
			contentLength = con.getContentLength();
			status = "" + con.getResponseCode();
			lastModified = con.getLastModified();
			con.disconnect();
			return;
		}

		String request = "HEAD " + urlInfo.getFilePath() + " HTTP/1.0\r\n"
				+ "Host: " + urlInfo.getHostName()
				+ "\r\nUser-Agent: cis455crawler\r\n\r\n";
		out.println(request);

		String line = "";
		try {
			line = readOneLine();
			status = line.split("\\s+")[1].trim();
			while (!(line = readOneLine()).equals("")) {
				if (line.startsWith("Content-Type")) {
					contentType = getValue(line);
				} else if (line.startsWith("Content-Length")) {
					contentLength = Integer.parseInt(getValue(line));
				} else if (line.startsWith("Last-Modified")) {
					String format = "EEE, d MMM yyyy HH:mm:ss z";
					SimpleDateFormat sf = new SimpleDateFormat(format);
					String dateString = getValue(line);
					try {
						lastModified = sf.parse(dateString).getTime();
					} catch (ParseException e) {
						throw new IllegalArgumentException();
					}
				}
			}
		} catch (IOException e) {
		}
		socket.close();
	}

	public String getStatus() {
		return status;
	}

	public void sendGet() throws IOException {
		if (url.startsWith("https")) {
			con = (HttpsURLConnection) myurl.openConnection();
			con.setRequestProperty("User-Agent", "cis455crawler");
			in = con.getInputStream();
		} else {
			socket = new Socket(urlInfo.getHostName(), urlInfo.getPortNo());
			out = new PrintWriter(socket.getOutputStream(), true);
			in = socket.getInputStream();
			String request = "GET " + urlInfo.getFilePath() + " HTTP/1.0\r\n"
					+ "Host: " + urlInfo.getHostName()
					+ "\r\nUser-Agent: cis455crawler\r\n\r\n";
			out.println(request);
			String line = "";
			try {
				while (!(line = readOneLine()).equals("")) {
				}
			} catch (IOException e) {
			}
		}		

		if (contentLength != 0) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < contentLength; i++) {
				sb.append((char) in.read());
			}
			body = sb.toString();
		} else {
			StringBuilder sb = new StringBuilder();
			int a;
			while ((a = in.read()) != -1) {
				sb.append((char) a);
			}
			body = sb.toString();
		}
	}

	private String getValue(String s) {
		int start = s.indexOf(":");
		int end = s.indexOf(";");
		if (end == -1)
			return s.substring(start + 1).trim();
		return s.substring(start + 1, end).trim();
	}

	public long getLastModified() {
		return lastModified;
	}

	public String getContentType() {
		return contentType;
	}

	public int getContentLength() {
		return contentLength;
	}

	public boolean isUrlValid() {
		return valid;
	}

	public String getBody() {
		return body;
	}

	public InputStream getBodyInputStream() throws UnsupportedEncodingException {
		if (contentType.equals("text/html")) {
			Tidy tidy = new Tidy();
			tidy.setXHTML(true);
			tidy.setDocType("omit");
			ByteArrayInputStream inputStream = new ByteArrayInputStream(
					body.getBytes("UTF-8"));
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			tidy.parseDOM(inputStream, outputStream);
			return new ByteArrayInputStream(outputStream.toString("UTF-8").getBytes());
		}
		return new ByteArrayInputStream(body.getBytes());
	}

	private String readOneLine() throws IOException {
		StringBuilder sb = new StringBuilder();
		char a;
		while ((a = (char) in.read()) != '\n') {
			sb.append(a);
		}
		return sb.toString().trim();
	}

	public static void main(String args[]) throws IOException {
		Preferences root = Preferences.userRoot();
		root.putBoolean("run", true);
		System.out.println(root.getBoolean("run", false));
	}

}

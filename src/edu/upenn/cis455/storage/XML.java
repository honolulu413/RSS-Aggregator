package edu.upenn.cis455.storage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import java.util.regex.*;

@Entity
public class XML {
	@PrimaryKey
	private String url;
	private String content;
	private long lastCrawledTime;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public long getLastCrawledTime() {
		return lastCrawledTime;
	}

	public void setLastCrawledTime(long lastCrawledTime) {
		this.lastCrawledTime = lastCrawledTime;
	}

	public InputStream getInputStream() {
		return new ByteArrayInputStream(content.getBytes());
	}

	public String getRawContent() {
		int start = 0;
		StringBuilder sb = new StringBuilder();
		Pattern p = Pattern.compile("<\\?.*?\\?>");
		Matcher m = p.matcher(content);
		while (m.find()) {
			sb.append(content.subSequence(start, m.start()));
			start = m.end();
		}
		sb.append(content.substring(start, content.length()));
		return sb.toString();
	}

}

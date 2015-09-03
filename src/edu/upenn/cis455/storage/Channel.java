package edu.upenn.cis455.storage;

import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class Channel {
	@PrimaryKey
	private String name;
	private String userName;
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	private ArrayList<String> Xpaths = new ArrayList<String>();
	private ArrayList<String> xmls = new ArrayList<String>();
	private String XSLurl;
	
	public void addXml(String xml) {
		xmls.add(xml);
	}
	
	public ArrayList<String> getXmls() {
		return xmls;
	}
	
	public String getName() {
		return name;
	}
	
	public ArrayList<String> getXpaths() {
		return Xpaths;
	}
	
	public void setXpaths(ArrayList<String> list) {
		Xpaths = list;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getXSLurl() {
		return XSLurl;
	}
	
	public void setXSLurl(String xSLurl) {
		XSLurl = xSLurl;
	}
	
}

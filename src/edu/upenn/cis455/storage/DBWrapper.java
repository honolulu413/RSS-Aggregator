package edu.upenn.cis455.storage;

import java.io.File;
import java.util.Iterator;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

public class DBWrapper {
	private String direc;
	private static String envDirectory = null;
	private static Environment myEnv;
	private static EntityStore store;
	PrimaryIndex<String, User> userIndex;
	PrimaryIndex<String, Channel> channelIndex;
	PrimaryIndex<String, HTML> HTMLIndex;
	PrimaryIndex<String, XML> XMLIndex;
	
	public DBWrapper(String direc) {
		this.direc = direc;
	}


	public void setup() {
		EnvironmentConfig envConfig = new EnvironmentConfig();
		StoreConfig storeConfig = new StoreConfig();

		envConfig.setAllowCreate(true);
		storeConfig.setAllowCreate(true);

		myEnv = new Environment(new File(direc), envConfig);
		store = new EntityStore(myEnv, "EntityStore", storeConfig);
		
		userIndex = store.getPrimaryIndex(String.class, User.class);
		channelIndex = store.getPrimaryIndex(String.class, Channel.class);
		HTMLIndex = store.getPrimaryIndex(String.class, HTML.class);
		XMLIndex = store.getPrimaryIndex(String.class, XML.class);
	}
	
	public void putUser(User user) {
		userIndex.put(user);
	}
	
	public PrimaryIndex<String, XML> getXMLIndex() {
		return XMLIndex;
	}
	
	public PrimaryIndex<String, HTML> getHTMLIndex() {
		return HTMLIndex;
	}

	public PrimaryIndex<String, Channel> getChannelIndex() {
		return channelIndex;
	}
	
	public void deleteChannel(String channel) {
		channelIndex.delete(channel);
	}
	
	public void deleteHTML(String channel) {
		HTMLIndex.delete(channel);
	}
	
	public void deleteXML(String channel) {
		XMLIndex.delete(channel);
	}
	
	public void deleteUser(String user) {
		userIndex.delete(user);
	}
	
	public User getUser(String userName) {
		return userIndex.get(userName);
	}
	
	public void putHTML(HTML html) {
		HTMLIndex.put(html);
	}
	
	public HTML getHTML(String userName) {
		return HTMLIndex.get(userName);
	}
	
	public void putXML(XML xml) {
		XMLIndex.put(xml);
	}
	
	public XML getXML(String userName) {
		return XMLIndex.get(userName);
	}
	
	public void putChannel(Channel channel) {
		channelIndex.put(channel);
	}
	
	public Channel getChannel(String userName) {
		return channelIndex.get(userName);
	}
	
	public void close() {
		if (store != null) store.close();
		if (myEnv != null) myEnv.close();
	}
	
	public static void main(String[] args) {
		DBWrapper db = new DBWrapper("./dbStore");
		db.setup();
		//db.putUser(user);
		System.out.println(db.getXML("https://dbappserv.cis.upenn.edu/crawltest/misc/weather.xml").getContent());

		db.close();
	}

}

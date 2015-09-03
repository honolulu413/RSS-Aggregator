package edu.upenn.cis455.storage;

import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class User {
	
	@PrimaryKey
	private String userName;
	private String password;
	private ArrayList<String> channels = new ArrayList<String>();
	
	public String getUserName() {
		return userName;
	}
	
	public void addChannel(String channel) {
		this.channels.add(channel);
	}
	
	public void deleteChannel(String channel) {
		this.channels.remove(channel);
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public ArrayList<String> getChannels() {
		return channels;
	}
	

}

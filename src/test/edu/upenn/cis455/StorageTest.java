package test.edu.upenn.cis455;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.upenn.cis455.storage.*;
import junit.framework.TestCase;

public class StorageTest extends TestCase{
	DBWrapper db;
	
	@Before
	public void setUp() throws Exception {
		db = new DBWrapper("./dbTest");
		db.setup();
	}
	
	@Test
	public void testUser() {
		User user = db.getUser("lu");
		assertTrue(user == null);
		user = new User();
		user.setUserName("lu");
		user.setPassword("123");
		user.addChannel("/xpath");
		db.putUser(user);
		User user1 = db.getUser("lu");
		assertEquals("lu", user1.getUserName());
		assertEquals("123", user1.getPassword());
		assertEquals("/xpath", user1.getChannels().get(0));
		assertTrue(user1.getChannels().size() == 1);
		db.deleteUser("lu");
		assertTrue(db.getUser("lu") == null);
	}
	
	@Test
	public void testChannel() {
		Channel channel = db.getChannel("lu");
		assertTrue(channel == null);
		channel = new Channel();
		channel.setName("anime");
		channel.setUserName("lu");
		channel.setXSLurl("www.123.com");
		ArrayList<String> list = new ArrayList<String>();
		list.add("/xpath");
		channel.setXpaths(list);
		db.putChannel(channel);
		Channel channel1 = db.getChannel("anime");
		assertEquals("anime", channel1.getName());
		assertEquals("lu", channel1.getUserName());
		assertEquals("www.123.com", channel1.getXSLurl());
		assertEquals("/xpath", channel1.getXpaths().get(0));
		assertTrue(channel1.getXpaths().size() == 1);
		db.deleteChannel("anime");
		assertTrue(db.getChannel("anime") == null);
	}
	
	@After
	public void tearDown() throws Exception {
		db.close();
	}
	
}

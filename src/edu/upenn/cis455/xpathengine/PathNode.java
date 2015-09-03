package edu.upenn.cis455.xpathengine;

import java.util.ArrayList;
/**
 * PathNode class
 * A PathNode object has the information about id, name, position, relativePos
 * level, attributes, nextPathNodeSet.
 */
public class PathNode {
	String id;
	String name;
	int position;
	int relativePos;
	int level;
	ArrayList<Filter> filters;
	ArrayList<PathNode> nextPathNodeSet;
	Boolean attributesOK;
	
	PathNode(String id, String name, int position, int relativePos, int level) {
		this.name = name;
		attributesOK = false;
		this.id = id;
		this.position = position;
		this.relativePos = relativePos;
		this.level = level;
		filters = new ArrayList<Filter>();
		nextPathNodeSet = new ArrayList<PathNode>();
	}
	
	public String toString() {
		String str = "";
		for (PathNode x: nextPathNodeSet) {
			str = str +  x.id + "-" + x.position;
		}
		return id + "-" + position + " " + relativePos + " " + 
				level + "\n" + filters + " " + str;
	}
	
	public boolean attributesOK() {
		return attributesOK;
	}
	
	public void setAttributesOK() {
		attributesOK = true;
	}
	
	public void resetAttributesOK() {
		attributesOK = false;
	}
	
	public int idNumber() {
		return Integer.parseInt(id.substring(1));
	}
	
	public boolean hasTextFilter() {
		for (Filter f: filters) {
			if (!f.type.startsWith("@")) {
				return true;
			}
		}
		return false;
	}
}

class Filter{
	String type;
	String content;
	Filter(String type, String content) {
		this.type = type;
		this.content = content;
	}
	
	public String toString() {
		return type + " " + content;
	}
}

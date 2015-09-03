package edu.upenn.cis455.xpathengine;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.regex.*;

/**
 * XpathTokenizer judges whether an xpath is valid and extracts tokens from the 
 * expression. 
 */
public class XpathTokenizer {
    private enum State {INITIAL, IN_FILTER}
    String Xpath;
    int position;
    
    public XpathTokenizer(String Xpath) { 
    	position = 0;
    	this.Xpath = Xpath;
    }
    
    public boolean isValid() {
    	return isValid(Xpath, new XpathTokenizer(Xpath));
    }
    
    private boolean isValid(String Xpath, XpathTokenizer tokenizer) {
    	String str = "";
    	if (!Xpath.startsWith("/")) return false;
    	while ((str = tokenizer.nextString()) != null) {
    		if (str.startsWith("/")) {
    			String nodeName = str.substring(1);
    			if (!isValidNodeName(nodeName)) return false;
    		} else if (str.startsWith("[")) {
    			if (count(str, '[') != count(str, ']')) return false;
    			String pureStr = str.substring(1, str.length() - 1).trim();
    			String textPattern = "text\\(\\)\\s*=\\s*\".*\"";
    			Pattern p1 = Pattern.compile(textPattern);
    			Matcher m1 = p1.matcher(pureStr);
    			if (m1.matches()) continue;
    			String attriPattern = "contains\\(\\s*text\\(\\)\\s*,\\s*\".*\"\\)";
    			Pattern p2 = Pattern.compile(attriPattern);
    			Matcher m2 = p2.matcher(pureStr);
    			if (m2.matches()) continue;
    			String containsPattern = "@.+\\s*=\\s*\".*\"";
    			Pattern p3 = Pattern.compile(containsPattern);
    			Matcher m3 = p3.matcher(pureStr);	
    			if (m3.matches()) {
    				String attriName = pureStr.substring(1, pureStr.indexOf("=")).trim();
    				if (isValidNodeName(attriName)) continue;
    				return false;
    			}
    			pureStr = "/" + pureStr;
    			if (!isValid(pureStr, new XpathTokenizer(pureStr))) return false;	
    		} else return false;
    	}
    	return true;
    }
    
    private int count(String str, char a) {
    	int n = 0;
    	for (int i = 0; i < str.length(); i++) {
    		if (str.charAt(i) == a) n++;
    	}
    	return n;
    }
    
    public boolean isValidNodeName(String str) {
    	if (str.equals("")) return false;
    	if (str.indexOf("\\s+") != -1) return false;
    	char start = str.charAt(0);
    	if (!(Character.isLetter(start) || start == '_')) return false;
    	if (str.toLowerCase().startsWith("xml")) return false;
    	for (int i = 0; i < str.length(); i++) {
    		char a = str.charAt(i);
    		if (!(Character.isLetter(a) || Character.isDigit(a) 
    				|| a == '_' || a == '-')) return false;
    	}
    	return true;
    }
    


    public String nextString() throws IllegalStateException {
    	if (position == Xpath.length()) return null;
        char a = Xpath.charAt(position);
        int start = position;
        if (a == '/') {
        	position++;
        	while (position < Xpath.length() && Xpath.charAt(position) != '[' 
        			&& Xpath.charAt(position) != '/') {
        		position++;
        	}
        } else if (a == '[') {
        	int left = 1;
        	position++;
        	while (left != 0 && position < Xpath.length()) {
        		char c = Xpath.charAt(position);
        		if (c == ']') {
        			left--;
        		} else if (c == '['){
        			left++;
        		}
        		position++;
        	} 	
        }
        return Xpath.substring(start, position);
    }
    
    public static void main(String[] args) {
   	XpathTokenizer x = new XpathTokenizer("/asdf");
    	//XpathTokenizer x = new XpathTokenizer("/a[  @topic= \"123\"][ text() =  \"456\"]/b[ contains( text(), \"567\")]/c");
    	System.out.println(x.isValid());
    }
}

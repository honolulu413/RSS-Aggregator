package edu.upenn.cis455.xpathengine;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class XPathEngineImpl extends DefaultHandler implements XPathEngine {
	String expressions[];
	boolean evaluate[];
	int[] count;
	HashMap<String, ArrayList<ArrayList<PathNode>>> queryIndex;
	PathNode current = null;
	int level;
	String currentQName;
  
	public XPathEngineImpl() {
    // Do NOT add arguments to the constructor!!
    }
	
  public void setXPaths(String[] s) {
	  expressions = s;
	  int length = expressions.length;
		evaluate = new boolean[length];
		count = new int[length];
		for (int i = 0; i < length; i++) {
			evaluate[i] = false;
			count[i] = 1;
		}
		
		level = 0;
		queryIndex = new XpathQueryIndex(expressions).queryIndex;
  }

  public boolean isValid(int i) {
	  XpathTokenizer tokenizer = new XpathTokenizer(expressions[i]);
	  return tokenizer.isValid();
  }
	
  public boolean[] evaluate(Document d) { 
	  return evaluate;
  }
  
  public void startElement(String uri, String localName, String qName,
			Attributes attributes) {
		currentQName = qName;
		level++;
		ArrayList<ArrayList<PathNode>> lists = queryIndex.get(qName);
		if (lists != null) {
			for (PathNode node : lists.get(0)) {
				boolean attributePass = checkAttributes(attributes, node);
				if (node.level == level && attributePass) {
					if (!node.hasTextFilter()) {
						pushNext(node);
					} else {
						node.setAttributesOK();
					}
				}
			}
		}
	}

	private boolean checkAttributes(Attributes attributes, PathNode node) {
		for (Filter f : node.filters) {
			if (f.type.startsWith("@")) {
				String attribute = f.type.substring(1);
				if (attributes.getValue(attribute) == null
						|| !attributes.getValue(attribute).equals(f.content)) {
					return false;
				}
			}
		}
		return true;
		
	}

	public void pushToCL(PathNode node, int level) {
		String elementName = node.name;
		ArrayList<ArrayList<PathNode>> lists = queryIndex.get(elementName);
		lists.get(0).add(node);
		node.level = level + node.relativePos;
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		String str = new String(ch, start, length);
		if (currentQName == null) return;
		ArrayList<ArrayList<PathNode>> lists = queryIndex.get(currentQName);
		if (lists == null) return;

		for (PathNode node : lists.get(0)) {
			if (node.attributesOK()) {
				node.resetAttributesOK();
				if (checkText(node, str)) {
					pushNext(node);
				}
			}
		}	
	}
	
	private void pushNext(PathNode node) {
		if (node.nextPathNodeSet.size() == 0) {
			int idNum = node.idNumber();
			count[idNum - 1]--;
			if (count[idNum - 1] == 0) {
				evaluate[idNum - 1] = true;
			}
		} else {
			if (node.nextPathNodeSet.size() > 1) count[node.idNumber() - 1]++;
			for (PathNode nextNode : node.nextPathNodeSet) {
				pushToCL(nextNode, node.level);
			}
		}
	}
	
	public boolean checkText(PathNode node, String str) {
		for (Filter f: node.filters) {
			if (f.type.equals("text")) {
				if (!f.content.equals(str)) return false;
			} else if (f.type.equals("contains")) {
				if (str.indexOf(f.content) == -1) return false;
			}
		}
		return true;
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		currentQName = null;
		ArrayList<ArrayList<PathNode>> lists = queryIndex.get(qName);
		if (lists != null) {
			for (PathNode node : lists.get(0)) {
				if (node.level == level) {
					if (node.nextPathNodeSet.size() > 1) {
						count[node.idNumber() - 1]--;
					}
					for (PathNode nextNode : node.nextPathNodeSet) {
						removeFromCL(nextNode);
					}
				}
			}
		}
		level--;
	}

	public void removeFromCL(PathNode node) {
		String elementName = node.name;
		ArrayList<ArrayList<PathNode>> lists = queryIndex.get(elementName);
		lists.get(0).remove(node);
		node.level = 0;
	}
        
}

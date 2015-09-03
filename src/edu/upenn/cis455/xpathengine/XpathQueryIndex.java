package edu.upenn.cis455.xpathengine;

import java.util.ArrayList;
import java.util.HashMap;
/**
 * Create a query index from a set of xpath expressions.
 */
public class XpathQueryIndex {
	String[] expressions;
	PathNode[] nodes;
	int[] positions;
	HashMap<String, ArrayList<ArrayList<PathNode>>> queryIndex;

	XpathQueryIndex(String[] expressions) {
		nodes = new PathNode[expressions.length];
		positions = new int[expressions.length];
		queryIndex = new HashMap<String, ArrayList<ArrayList<PathNode>>>();
		this.expressions = expressions;
		for (int i = 0; i < expressions.length; i++) {
			String xPath = expressions[i];
			XpathTokenizer tokenizer = new XpathTokenizer(xPath);
			if (!tokenizer.isValid()){
				continue;
			}
			nodes[i] = parse(i, null, tokenizer);	
		}
	}

	private PathNode parse(int num, PathNode lastNode,
			XpathTokenizer tokenizer) {
		String str = tokenizer.nextString();
		if (str == null)
			return null;
		if (str.startsWith("/")) {
			positions[num]++;
			PathNode current;
			String id = "Q" + (num + 1);
			str = str.substring(1);
			if (positions[num] == 1) {
				current = new PathNode(id, str, 1, 0, 1);
			} else {
				current = new PathNode(id, str, positions[num], 1, 0);
			}
			
			ArrayList<ArrayList<PathNode>> lists = queryIndex.get(str);
			if (lists == null) {
				lists = new ArrayList<ArrayList<PathNode>>();
				ArrayList<PathNode> cl = new ArrayList<PathNode>();
				ArrayList<PathNode> wl = new ArrayList<PathNode>();
				if (positions[num] == 1) {
					cl.add(current);
				} else {
					wl.add(current);
				}
				lists.add(cl);
				lists.add(wl);
				queryIndex.put(str, lists);
			} else {
				if (positions[num] == 1) {
					lists.get(0).add(current);
				} else {
					lists.get(1).add(current);
				}
			}
			PathNode next = parse(num, current, tokenizer);
			if (next != null) {
				current.nextPathNodeSet.add(next);
			}
			return current;
		} else if (str.startsWith("[")) {
			str = str.substring(1, str.length() - 1).trim();
			if (str.startsWith("text()")) {
				String content = str.split("=")[1].trim();
				content = content.substring(1, content.length() - 1);
				lastNode.filters.add(new Filter("text", content));
			} else if (str.startsWith("@")) {
				String type = str.split("=")[0].trim();
				String content = str.split("=")[1].trim();
				content = content.substring(1, content.length() - 1);
				lastNode.filters.add(new Filter(type, content));
			} else if (str.startsWith("contains")) {
				int start = str.indexOf("\"");
				int end = str.lastIndexOf("\"");
				String content = str.substring(start + 1, end);
				lastNode.filters.add(new Filter("contains", content));
			} else {
				String nestString = "/" + str;
				PathNode branchNode = parse(num, lastNode, new XpathTokenizer(nestString));
				lastNode.nextPathNodeSet.add(branchNode);
			}
			PathNode next = parse(num, lastNode, tokenizer);
			if (next != null) {
				lastNode.nextPathNodeSet.add(next);
			}
			return null;
		}
		return null;
	}
	
	public static void main(String[] args) {
		String[] strings = new String[2];
		strings[0] = "/a[d/e]/b/c";
		strings[1] = "/b/a";
	
		XpathQueryIndex qi = new XpathQueryIndex(strings);
//		for (int i = 0; i < strings.length; i++) {
//			PathNode node = qi.nodes[i];
//			while (node.nextPathNodeSet.size() != 0) {
//				System.out.println(node);
//				node = node.nextPathNodeSet.get(0);
//			}
//			System.out.println(node);
//		}
		for (String str: qi.queryIndex.keySet()) {
			System.out.println(str + ": "+ qi.queryIndex.get(str).get(0) + "\n" + 
					qi.queryIndex.get(str).get(1));
		}
	}

}

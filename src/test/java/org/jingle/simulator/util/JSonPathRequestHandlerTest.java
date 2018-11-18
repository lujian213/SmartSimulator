package org.jingle.simulator.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathConstants;

import org.jingle.simulator.util.RequestHandler.JSonPathRequestHandler;
import org.junit.Test;

import net.minidev.json.JSONArray;

public class JSonPathRequestHandlerTest extends JSonPathRequestHandler {

	@Test
	public void test() {
		JSonPathRequestHandler handler = new JSonPathRequestHandler();
		String requestBody = "{\r\n" + 
				"    \"store\": {\r\n" + 
				"        \"book\": [\r\n" + 
				"            {\r\n" + 
				"                \"category\": \"reference\",\r\n" + 
				"                \"author\": \"Nigel Rees\",\r\n" + 
				"                \"title\": \"Sayings of the Century\",\r\n" + 
				"                \"price\": 8.95\r\n" + 
				"            },\r\n" + 
				"            {\r\n" + 
				"                \"category\": \"fiction\",\r\n" + 
				"                \"author\": \"Evelyn Waugh\",\r\n" + 
				"                \"title\": \"Sword of Honour\",\r\n" + 
				"                \"price\": 12.99\r\n" + 
				"            },\r\n" + 
				"            {\r\n" + 
				"                \"category\": \"fiction\",\r\n" + 
				"                \"author\": \"Herman Melville\",\r\n" + 
				"                \"title\": \"Moby Dick\",\r\n" + 
				"                \"isbn\": \"0-553-21311-3\",\r\n" + 
				"                \"price\": 8.99\r\n" + 
				"            },\r\n" + 
				"            {\r\n" + 
				"                \"category\": \"fiction\",\r\n" + 
				"                \"author\": \"J. R. R. Tolkien\",\r\n" + 
				"                \"title\": \"The Lord of the Rings\",\r\n" + 
				"                \"isbn\": \"0-395-19395-8\",\r\n" + 
				"                \"price\": 22.99\r\n" + 
				"            }\r\n" + 
				"        ],\r\n" + 
				"        \"bicycle\": {\r\n" + 
				"            \"color\": \"red\",\r\n" + 
				"            \"price\": 19.95\r\n" + 
				"        }\r\n" + 
				"    },\r\n" + 
				"    \"expensive\": 10\r\n" + 
				"}";
		Map<String, XPathExp> pathMap = new HashMap<>();
		pathMap.put("author", new XPathExp("$.store.book[?(@.price>20)].author", XPathConstants.NODESET));
		Map<String, Object> ret;
		try {
			ret = handler.retrievePathValue(requestBody, pathMap);
			assertEquals(1, ret.size());
			assertEquals("J. R. R. Tolkien", ((Object[])ret.get("author"))[0]);
		} catch (IOException e) {
			fail("unexpected exception:" + e);
		}

		pathMap = new HashMap<>();
		pathMap.put("author", new XPathExp("$.store.book[?(@.price>20)].author", XPathConstants.STRING));
		try {
			ret = handler.retrievePathValue(requestBody, pathMap);
			assertEquals(1, ret.size());
			assertEquals("J. R. R. Tolkien", ret.get("author"));
		} catch (IOException e) {
			fail("unexpected exception:" + e);
		}

		pathMap = new HashMap<>();
		pathMap.put("price", new XPathExp("$.store.book[?(@.price>20)].price", XPathConstants.STRING));
		try {
			ret = handler.retrievePathValue(requestBody, pathMap);
			assertEquals(1, ret.size());
			assertEquals(22.99, ret.get("price"));
		} catch (IOException e) {
			fail("unexpected exception:" + e);
		}

		pathMap = new HashMap<>();
		pathMap.put("price", new XPathExp("$.store.book[?(@.author=='J. R. R. Tolkien')].price", XPathConstants.NODESET));
		try {
			ret = handler.retrievePathValue(requestBody, pathMap);
			assertEquals(1, ret.size());
			assertEquals(1, ((Object[])ret.get("price")).length);
		} catch (IOException e) {
			fail("unexpected exception:" + e);
		}
	}

}

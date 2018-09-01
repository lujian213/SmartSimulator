package org.jingle.simulator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.jingle.simulator.webbit.WebbitSimRequest;
import org.junit.Test;

public class SimRequestTemplateTest {

	@Test
	public void test() {
    	String temp = "POST /test123 HTTP/1.1\r\n" + 
    			"Host: {$hostName}\r\n" + 
    			"Content-Length: 373\r\n" + 
    			"Expect: 100-continue\r\n" + 
    			"Accept-Encoding: gzip, deflate\r\n" + 
    			"Authentication: {$user},{$passwd}\r\n" + 
    			"\r\n" + 
    			"body {$status}\r\n";
    			
    	try {
    		SimRequestTemplate srt = new SimRequestTemplate(temp);
    		SimTemplate template = srt.getTopLineTemplate();
    		assertEquals(1, template.getAllTokens().size());
    		assertEquals(1, template.getAllTokens().get(0).size());
    		
    		Map<String, SimTemplate> headerTemplates = srt.getHeaderTemplate();
    		assertEquals(4, headerTemplates.size());
    		assertEquals("hostName", headerTemplates.get("Host").getAllTokens().get(0).get(1).getName());
    		assertEquals("user", srt.getAuthenticationsTemplate().getAllTokens().get(0).get(1).getName());
    		assertEquals("passwd", srt.getAuthenticationsTemplate().getAllTokens().get(0).get(3).getName());
    		assertEquals("status", srt.getBodyTemplate().getAllTokens().get(0).get(1).getName());
    	} catch (Exception e) {
    		fail ("unexpected exception:" + e);
    	}
	}

	@Test
	public void test2() {
    	String temp = "POST /test/{$id} HTTP/1.1\r\n"; 
    			
    	try {
    		SimRequestTemplate srt = new SimRequestTemplate(temp);
    		SimTemplate template = srt.getTopLineTemplate();
    		assertEquals(1, template.getAllTokens().size());
    		assertEquals("id", template.getAllTokens().get(0).get(1).getName());
    		
    		Map<String, SimTemplate> headerTemplates = srt.getHeaderTemplate();
    		assertEquals(0, headerTemplates.size());
    		assertNull(srt.getAuthenticationsTemplate());
    		assertNull(srt.getBodyTemplate());
    	} catch (Exception e) {
    		fail ("unexpected exception:" + e);
    	}
	}

	@Test
	public void test3() {
    	String temp = "POST /test/{$id} HTTP/1.1\r\n" + 
    			"Host: {$hostName}\r\n" + 
    			"Content-Length: 373\r\n" + 
    			"Expect: 100-continue\r\n" + 
    			"Accept-Encoding: gzip, deflate\r\n" + 
    			"Authentication: {$user},{$passwd}\r\n" + 
    			"\r\n" + 
    			"body {$status}\r\n";

    	String topLine = "POST /test/123 HTTP/1.1"; 
    	Map<String, String> headers = new HashMap<>();
    	headers.put("Host",  "Host: www.baidu.com"); 
    	headers.put("Content-Length", "Content-Length: 373");
    	headers.put("Expect", "Expect: 100-continue"); 
    	headers.put("Accept-Encoding", "Accept-Encoding: gzip, deflate"); 
    	String authenticationLine = "Authentication: dummy,pwd"; 
    	String bodyLine = "body started\r\n";
    			
    	
    	try {
    		SimRequestTemplate srt = new SimRequestTemplate(temp);
    		SimRequest request = new WebbitSimRequest() {
    			public String getTopLine() {
    				return topLine;
    			}
    			
    			public String getHeaderLine(String header) {
    				return headers.get(header);
    			}
    			
    			public String getAutnenticationLine() {
    				return authenticationLine;
    			}
    			
    			public String getBody() {
    				return bodyLine;
    			}

    		};
    		Map<String, Object> result = srt.match(request);
    		assertEquals(5, result.size());
    		assertEquals("123", result.get("id"));
    		assertEquals("www.baidu.com", result.get("hostName"));
    		assertEquals("dummy", result.get("user"));
    		assertEquals("pwd", result.get("passwd"));
    		assertEquals("started", result.get("status"));
    	} catch (Exception e) {
    		fail ("unexpected exception:" + e);
    	}
	}

}

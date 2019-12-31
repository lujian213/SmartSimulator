package io.github.lujian213.simulator;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import io.github.lujian213.simulator.SimRequest;
import io.github.lujian213.simulator.SimRequestTemplate;
import io.github.lujian213.simulator.SimRequestTemplate.HeaderItem;
import io.github.lujian213.simulator.SimTemplate;
import io.github.lujian213.simulator.webbit.WebbitSimRequest;

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

    		Map<String, HeaderItem> headerTemplates = srt.getHeaderTemplate();
    		assertEquals(4, headerTemplates.size());
    		assertEquals("hostName", headerTemplates.get("Host").getTemplate().getAllTokens().get(0).get(1).getName());
    		assertEquals("user", srt.getAuthenticationsTemplate().getTemplate().getAllTokens().get(0).get(1).getName());
    		assertEquals("passwd", srt.getAuthenticationsTemplate().getTemplate().getAllTokens().get(0).get(3).getName());
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

    		Map<String, HeaderItem> headerTemplates = srt.getHeaderTemplate();
    		assertEquals(0, headerTemplates.size());
    		assertNull(srt.getAuthenticationsTemplate());
    		assertNull(srt.getBody());
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

    			public String getAuthenticationLine() {
    				return authenticationLine;
    			}

    			public String getBody() {
    				return bodyLine;
    			}

    		};
    		Map<String, Object> result = srt.match(new HashMap<>(), request);
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

	@Test
	public void test4() {
    	String temp = "GET /test/query?queryStr={$queryStr} HTTP/1.1\r\n" +
    			"\r\n";

    	String topLine = "GET /test/query?queryStr=Select table where fileA=\"A\" and filedB=\"B\" HTTP/1.1";
    	Map<String, String> headers = new HashMap<>();
    	headers.put("Host",  "Host: www.baidu.com");
    	headers.put("Content-Length", "Content-Length: 373");
    	headers.put("Expect", "Expect: 100-continue");
    	headers.put("Accept-Encoding", "Accept-Encoding: gzip, deflate");
    	String authenticationLine = "Authentication: dummy,pwd";
    	String bodyLine = null;


    	try {
    		SimRequestTemplate srt = new SimRequestTemplate(temp);
    		SimRequest request = new WebbitSimRequest() {
    			public String getTopLine() {
    				return topLine;
    			}

    			public String getHeaderLine(String header) {
    				return headers.get(header);
    			}

    			public String getAuthenticationLine() {
    				return authenticationLine;
    			}

    			public String getBody() {
    				return bodyLine;
    			}

    		};
    		Map<String, Object> result = srt.match(new HashMap<>(), request);
    		assertNotNull(result);
    		assertEquals(1, result.size());
    		assertEquals("Select table where fileA=\"A\" and filedB=\"B\"", result.get("queryStr"));
    	} catch (Exception e) {
    		e.printStackTrace();
    		fail ("unexpected exception:" + e);
    	}
	}

	@Test
	public void test5() {
    	String temp = "GET /test/query?queryStr={$queryStr} HTTP/1.1\r\n" +
    			"\r\n";

    	String topLine = "GET /test/query?queryStr=Select table where fileA=\"A\" and\r\n"
    			+ " filedB=\"B\" HTTP/1.1";
    	Map<String, String> headers = new HashMap<>();
    	headers.put("Host",  "Host: www.baidu.com");
    	headers.put("Content-Length", "Content-Length: 373");
    	headers.put("Expect", "Expect: 100-continue");
    	headers.put("Accept-Encoding", "Accept-Encoding: gzip, deflate");
    	String authenticationLine = "Authentication: dummy,pwd";
    	String bodyLine = null;


    	try {
    		SimRequestTemplate srt = new SimRequestTemplate(temp);
    		SimRequest request = new WebbitSimRequest() {
    			public String getTopLine() {
    				return topLine;
    			}

    			public String getHeaderLine(String header) {
    				return headers.get(header);
    			}

    			public String getAuthenticationLine() {
    				return authenticationLine;
    			}

    			public String getBody() {
    				return bodyLine;
    			}

    		};
    		Map<String, Object> result = srt.match(new HashMap<>(), request);
    		assertNotNull(result);
    		assertEquals(1, result.size());
    		assertEquals("Select table where fileA=\"A\" and  filedB=\"B\"", result.get("queryStr"));
    	} catch (Exception e) {
    		e.printStackTrace();
    		fail ("unexpected exception:" + e);
    	}
	}

	@Test
	public void test6() throws IOException {
    	String temp = "/GET .*\\/mirror\\/(?<type>[^\\/]+)\\/(?<id>[^\\/]+)/\r\n";

		SimRequestTemplate srt = new SimRequestTemplate(temp);
		SimTemplate template = srt.getTopLineTemplate();
		assertEquals(SimRegexTemplate.class, template.getClass());
	}

	@Test
	public void test7() throws IOException {
    	String temp = "/GET .*\\/profile\\/?<id>([^\\/]+)(\\/.*)? HTTP/1.1/\r\n";

		SimRequestTemplate srt = new SimRequestTemplate(temp);
		SimTemplate template = srt.getTopLineTemplate();
		assertEquals(SimRegexTemplate.class, template.getClass());
	}

	@Test
	public void test8() {
    	String temp = "POST /test/{$id} HTTP/1.1\r\n" +
    			"*Host: {$hostName}\r\n" +
    			"Content-Length: 373\r\n" +
    			"Expect: 100-continue\r\n" +
    			"Accept-Encoding: gzip, deflate\r\n" +
    			"*Authentication: {$user},{$passwd}\r\n" +
    			"\r\n" +
    			"body {$status}\r\n";

    	String topLine = "POST /test/123 HTTP/1.1";
    	Map<String, String> headers = new HashMap<>();
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

    			public String getAuthenticationLine() {
    				return authenticationLine;
    			}

    			public String getBody() {
    				return bodyLine;
    			}

    		};
    		Map<String, Object> result = srt.match(new HashMap<>(), request);
    		assertEquals(4, result.size());
    		assertEquals("123", result.get("id"));
    		assertNull(result.get("hostName"));
    		assertEquals("dummy", result.get("user"));
    		assertEquals("pwd", result.get("passwd"));
    		assertEquals("started", result.get("status"));
    	} catch (Exception e) {
    		fail ("unexpected exception:" + e);
    	}
	}
}

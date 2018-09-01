package org.jingle.simulator;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.jingle.simulator.SimScript;
import org.junit.Test;

public class SimScriptTest {

	@Test
	public void test() {
		String temp = 
				"POST http://localhost:9011/CoreDiscoveryService HTTP/1.1\r\n" + 
				"Host: localhost:9011\r\n" + 
				"Content-Type: text/xml; charset=UTF-8\r\n" + 
				"Accept: */*\r\n" + 
				"Accept-Language: en-US,en;q=0.9\r\n" + 
				"\r\n" + 
				"<?xml version=\"1.0\" encoding=\"utf-8\"?><soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><InternalDiscoveryActivityMessage xmlns=\"http://tempuri.org/\"><eventData>&lt;Msg Type=\"WND\" WSID=\"1\" UTC=\"2018-08-13T06:56:33.348Z\" RAM=\"1347584\"&gt;&lt;App Name=\"Chrome\" xmlns=\"http://tempuri.org/CXMYApp.xsd\" Hwnd=\"67598\" PID=\"7504\" Cls=\"Chrome_WidgetWin_1\"&gt;&lt;Img xmlns=\"http://tempuri.org/CXMYImage.xsd\" Lbl=\"URL\"&gt;http://staffdb/search?q=Wang+Yirong&lt;/Img&gt;&lt;Img xmlns=\"http://tempuri.org/CXMYImage.xsd\" Lbl=\"MAINURL\"&gt;http://staffdb/search?q=Wang+Yirong&lt;/Img&gt;&lt;Img xmlns=\"http://tempuri.org/CXMYImage.xsd\" Lbl=\"Ver\"&gt;67.0.3396.87&lt;/Img&gt;&lt;Img xmlns=\"http://tempuri.org/CXMYImage.xsd\" Lbl=\"DESC\"&gt;Google Chrome&lt;/Img&gt;&lt;Img xmlns=\"http://tempuri.org/CXMYImage.xsd\" Lbl=\"TITLE\"&gt;StaffDB Search&lt;/Img&gt;&lt;/App&gt;&lt;/Msg&gt;</eventData></InternalDiscoveryActivityMessage></soap:Body></soap:Envelope>\r\n" + 
				"HTTP/1.1 200 OK\r\n" + 
				"Content-Length: 324\r\n" + 
				"Server: Microsoft-HTTPAPI/2.0\r\n" + 
				"Date: Mon, 13 Aug 2018 06:56:32 GMT\r\n" + 
				"\r\n" + 
				"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body><InternalDiscoveryActivityMessageResponse xmlns=\"http://tempuri.org/\"><InternalDiscoveryActivityMessageResult>f405be39-9ebb-4d59-a0ed-091ecdf4b667</InternalDiscoveryActivityMessageResult></InternalDiscoveryActivityMessageResponse></s:Body></s:Envelope>\r\n" + 
				"\r\n";
		SimScript script = new SimScript();
		
		try (BufferedReader reader = new BufferedReader(new StringReader(temp))) {
			List<List<String>> blocks = script.loadReqResp(reader);
			assertEquals(2, blocks.size());
			assertEquals(7, blocks.get(0).size());
			assertEquals(6, blocks.get(1).size());
		} catch (IOException e) {
			fail("unexpected exception: " + e);
		}
		
	}

	@Test
	public void test2() {
		String temp = 
				"HTTP/1.1 200 OK\r\n" + 
				"Content-Length: 324\r\n" + 
				"Server: Microsoft-HTTPAPI/2.0\r\n" + 
				"Date: Mon, 13 Aug 2018 06:56:32 GMT\r\n" + 
				"\r\n" + 
				"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body><InternalDiscoveryActivityMessageResponse xmlns=\"http://tempuri.org/\"><InternalDiscoveryActivityMessageResult>f405be39-9ebb-4d59-a0ed-091ecdf4b667</InternalDiscoveryActivityMessageResult></InternalDiscoveryActivityMessageResponse></s:Body></s:Envelope>\r\n" + 
				"\r\n";
		SimScript script = new SimScript();
		
		try (BufferedReader reader = new BufferedReader(new StringReader(temp))) {
			script.loadReqResp(reader);
			fail("IOException expected");
		} catch (IOException e) {
		}
		
	}
	
	@Test
	public void test3() {
		String temp = 
				"POST http://localhost:9011/CoreDiscoveryService HTTP/1.1\r\n" + 
				"Host: localhost:9011\r\n" + 
				"Content-Type: text/xml; charset=UTF-8\r\n" + 
				"Accept: */*\r\n" + 
				"Accept-Language: en-US,en;q=0.9\r\n" + 
				"\r\n" + 
				"<?xml version=\"1.0\" encoding=\"utf-8\"?><soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><InternalDiscoveryActivityMessage xmlns=\"http://tempuri.org/\"><eventData>&lt;Msg Type=\"WND\" WSID=\"1\" UTC=\"2018-08-13T06:56:33.348Z\" RAM=\"1347584\"&gt;&lt;App Name=\"Chrome\" xmlns=\"http://tempuri.org/CXMYApp.xsd\" Hwnd=\"67598\" PID=\"7504\" Cls=\"Chrome_WidgetWin_1\"&gt;&lt;Img xmlns=\"http://tempuri.org/CXMYImage.xsd\" Lbl=\"URL\"&gt;http://staffdb/search?q=Wang+Yirong&lt;/Img&gt;&lt;Img xmlns=\"http://tempuri.org/CXMYImage.xsd\" Lbl=\"MAINURL\"&gt;http://staffdb/search?q=Wang+Yirong&lt;/Img&gt;&lt;Img xmlns=\"http://tempuri.org/CXMYImage.xsd\" Lbl=\"Ver\"&gt;67.0.3396.87&lt;/Img&gt;&lt;Img xmlns=\"http://tempuri.org/CXMYImage.xsd\" Lbl=\"DESC\"&gt;Google Chrome&lt;/Img&gt;&lt;Img xmlns=\"http://tempuri.org/CXMYImage.xsd\" Lbl=\"TITLE\"&gt;StaffDB Search&lt;/Img&gt;&lt;/App&gt;&lt;/Msg&gt;</eventData></InternalDiscoveryActivityMessage></soap:Body></soap:Envelope>\r\n"; 
		SimScript script = new SimScript();
		
		try (BufferedReader reader = new BufferedReader(new StringReader(temp))) {
			script.loadReqResp(reader);
			fail("IOException expected");
		} catch (IOException e) {
		}
		
	}
	
	@Test
	public void test4() {
		String temp = "POST http://localhost:9011/CoreDiscoveryService HTTP/1.1\r\n" + 
				"Content-Type: text/xml; charset=UTF-8\r\n" + 
				"Accept-Language: en-US,en;q=0.9\r\n" + 
				"\r\n" + 
				"<?xml version=\"1.0\" encoding=\"utf-8\"?><soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><InternalDiscoveryActivityMessage xmlns=\"http://tempuri.org/\"><eventData>&lt;Msg Type=\"WND\" WSID=\"1\" UTC=\"2018-08-13T06:56:33.348Z\" RAM=\"1347584\"&gt;&lt;App Name=\"Chrome\" xmlns=\"http://tempuri.org/CXMYApp.xsd\" Hwnd=\"67598\" PID=\"7504\" Cls=\"Chrome_WidgetWin_1\"&gt;&lt;Img xmlns=\"http://tempuri.org/CXMYImage.xsd\" Lbl=\"URL\"&gt;http://staffdb/search?q=Wang+Yirong&lt;/Img&gt;&lt;Img xmlns=\"http://tempuri.org/CXMYImage.xsd\" Lbl=\"MAINURL\"&gt;http://staffdb/search?q=Wang+Yirong&lt;/Img&gt;&lt;Img xmlns=\"http://tempuri.org/CXMYImage.xsd\" Lbl=\"Ver\"&gt;67.0.3396.87&lt;/Img&gt;&lt;Img xmlns=\"http://tempuri.org/CXMYImage.xsd\" Lbl=\"DESC\"&gt;Google Chrome&lt;/Img&gt;&lt;Img xmlns=\"http://tempuri.org/CXMYImage.xsd\" Lbl=\"TITLE\"&gt;StaffDB Search&lt;/Img&gt;&lt;/App&gt;&lt;/Msg&gt;</eventData></InternalDiscoveryActivityMessage></soap:Body></soap:Envelope>\r\n" + 
				"HTTP/1.1 200 OK\r\n" + 
				"Content-Length: 324\r\n" + 
				"Date: Mon, 13 Aug 2018 06:56:32 GMT\r\n" + 
				"\r\n" + 
				"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body><InternalDiscoveryActivityMessageResponse xmlns=\"http://tempuri.org/\"><InternalDiscoveryActivityMessageResult>f405be39-9ebb-4d59-a0ed-091ecdf4b667</InternalDiscoveryActivityMessageResult></InternalDiscoveryActivityMessageResponse></s:Body></s:Envelope>\r\n" + 
				"\r\n" + 
				"------------------------------------------------------------------\r\n"; 
		SimScript script = new SimScript();
		
		try (BufferedReader reader = new BufferedReader(new StringReader(temp))) {
			List<List<String>> blocks = script.loadReqResp(reader);
			assertEquals(2, blocks.size());
			assertEquals(5, blocks.get(0).size());
			assertEquals(5, blocks.get(1).size());
		} catch (IOException e) {
			fail("unexpected exception: " + e);
		}
		
	}

	@Test
	public void test5() {
		String temp = "POST http://localhost:9011/CoreDiscoveryService HTTP/1.1\r\n" + 
				"Content-Type: text/xml; charset=UTF-8\r\n" + 
				"Accept-Language: en-US,en;q=0.9\r\n" + 
				"\r\n" + 
				"<?xml version=\"1.0\" encoding=\"utf-8\"?><soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><InternalDiscoveryActivityMessage xmlns=\"http://tempuri.org/\"><eventData>&lt;Msg Type=\"WND\" WSID=\"1\" UTC=\"2018-08-13T06:56:33.348Z\" RAM=\"1347584\"&gt;&lt;App Name=\"Chrome\" xmlns=\"http://tempuri.org/CXMYApp.xsd\" Hwnd=\"67598\" PID=\"7504\" Cls=\"Chrome_WidgetWin_1\"&gt;&lt;Img xmlns=\"http://tempuri.org/CXMYImage.xsd\" Lbl=\"URL\"&gt;http://staffdb/search?q=Wang+Yirong&lt;/Img&gt;&lt;Img xmlns=\"http://tempuri.org/CXMYImage.xsd\" Lbl=\"MAINURL\"&gt;http://staffdb/search?q=Wang+Yirong&lt;/Img&gt;&lt;Img xmlns=\"http://tempuri.org/CXMYImage.xsd\" Lbl=\"Ver\"&gt;67.0.3396.87&lt;/Img&gt;&lt;Img xmlns=\"http://tempuri.org/CXMYImage.xsd\" Lbl=\"DESC\"&gt;Google Chrome&lt;/Img&gt;&lt;Img xmlns=\"http://tempuri.org/CXMYImage.xsd\" Lbl=\"TITLE\"&gt;StaffDB Search&lt;/Img&gt;&lt;/App&gt;&lt;/Msg&gt;</eventData></InternalDiscoveryActivityMessage></soap:Body></soap:Envelope>\r\n" + 
				"HTTP/1.1 200 OK\r\n" + 
				"Content-Length: 324\r\n" + 
				"Date: Mon, 13 Aug 2018 06:56:32 GMT\r\n" + 
				"\r\n" + 
				"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body><InternalDiscoveryActivityMessageResponse xmlns=\"http://tempuri.org/\"><InternalDiscoveryActivityMessageResult>f405be39-9ebb-4d59-a0ed-091ecdf4b667</InternalDiscoveryActivityMessageResult></InternalDiscoveryActivityMessageResponse></s:Body></s:Envelope>\r\n" + 
				"\r\n" + 
				"------------------------------------------------------------------\r\n" +
				"\r\n" + 
				"POST http://localhost:9011/CoreDiscoveryService HTTP/1.1\r\n" + 
				"Accept-Language: en-US,en;q=0.9\r\n" + 
				"\r\n" + 
				"<?xml version=\"1.0\" encoding=\"utf-8\"?><soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><InternalDiscoveryActivityMessage xmlns=\"http://tempuri.org/\"><eventData>&lt;Msg Type=\"NAV\" WSID=\"1\" UTC=\"2018-08-13T06:56:34.922Z\" RAM=\"1345536\"&gt;&lt;App Name=\"Chrome\" xmlns=\"http://tempuri.org/CXMYApp.xsd\" Hwnd=\"67598\" PID=\"7504\" Cls=\"Chrome_WidgetWin_1\"&gt;&lt;Img xmlns=\"http://tempuri.org/CXMYImage.xsd\" Lbl=\"URL\"&gt;http://staffdb/search?q=Wang+Yirong&lt;/Img&gt;&lt;Img xmlns=\"http://tempuri.org/CXMYImage.xsd\" Lbl=\"MAINURL\"&gt;http://staffdb/search?q=Wang+Yirong&lt;/Img&gt;&lt;Img xmlns=\"http://tempuri.org/CXMYImage.xsd\" Lbl=\"Ver\"&gt;67.0.3396.87&lt;/Img&gt;&lt;Img xmlns=\"http://tempuri.org/CXMYImage.xsd\" Lbl=\"DESC\"&gt;Google Chrome&lt;/Img&gt;&lt;Img xmlns=\"http://tempuri.org/CXMYImage.xsd\" Lbl=\"Cap\"&gt;StaffDB Search&lt;/Img&gt;&lt;/App&gt;&lt;/Msg&gt;</eventData></InternalDiscoveryActivityMessage></soap:Body></soap:Envelope>\r\n" + 
				"HTTP/1.1 200 OK\r\n" + 
				"Content-Length: 324\r\n" + 
				"Date: Mon, 13 Aug 2018 06:56:34 GMT\r\n" + 
				"\r\n" + 
				"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body><InternalDiscoveryActivityMessageResponse xmlns=\"http://tempuri.org/\"><InternalDiscoveryActivityMessageResult>f405be39-9ebb-4d59-a0ed-091ecdf4b667</InternalDiscoveryActivityMessageResult></InternalDiscoveryActivityMessageResponse></s:Body></s:Envelope>\r\n" + 
				"\r\n" + 
				"------------------------------------------------------------------\r\n" + 
				"\r\n";
				 
		SimScript script = new SimScript();
		
		try (BufferedReader reader = new BufferedReader(new StringReader(temp))) {
			List<List<String>> blocks = script.loadReqResp(reader);
			assertEquals(2, blocks.size());
			assertEquals(5, blocks.get(0).size());
			assertEquals(5, blocks.get(1).size());
			blocks = script.loadReqResp(reader);
			assertEquals(2, blocks.size());
			assertEquals(4, blocks.get(0).size());
			assertEquals(5, blocks.get(1).size());
			blocks = script.loadReqResp(reader);
			assertNull(blocks);
		} catch (IOException e) {
			fail("unexpected exception: " + e);
		}
		
	}

	@Test
	public void test7() {
		try {
			SimScript script = new SimScript(new File("scripts/websocket"));
			assertEquals(1, script.templatePairs.size());
			assertEquals(2, script.getSubScripts().size());
			assertEquals(4, script.getSubScripts().get("hellowebsocket.1").templatePairs.size());
		} catch (IOException e) {
			fail("unexpected exception" + e);
		}
		
	}
}

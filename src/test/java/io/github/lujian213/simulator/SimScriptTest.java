package io.github.lujian213.simulator;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.List;
import java.util.zip.ZipFile;

import org.junit.Test;

import io.github.lujian213.simulator.SimScript;
import io.github.lujian213.simulator.manager.SimulatorFolder;

import static io.github.lujian213.simulator.SimSimulatorConstants.*;


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
			assertEquals(3, blocks.size());
			assertEquals(5, blocks.get(0).size());
			assertEquals(5, blocks.get(1).size());
			assertEquals(5, blocks.get(2).size());
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
			SimScript script = new SimScript(new SimScript(new File("scripts")), new File("scripts/websocket"));
			assertEquals(2, script.getEffectiveTemplatePairs().size());
			assertEquals(2, script.getSubScripts().size());
			assertEquals(6, script.getSubScripts().get("hellowebsocket.1").getEffectiveTemplatePairs().size());
		} catch (IOException e) {
			fail("unexpected exception" + e);
		}
		
	}

	@Test
	public void test8() {
		try {
			SimScript script = new SimScript(new SimScript(new File("scripts")), new ZipFile("scripts/websocket2.zip"), new File("scripts/websocket2.zip"));
			assertEquals(2, script.getEffectiveTemplatePairs().size());
			assertEquals(2, script.getSubScripts().size());
			assertEquals(6, script.getSubScripts().get("hellowebsocket.1").getEffectiveTemplatePairs().size());
		} catch (IOException e) {
			fail("unexpected exception" + e);
		}
	}

	@Test
	public void test9() {
		try {
			SimScript script = new SimScript(new SimScript(new File("scripts")), new File("scripts/websocket"));
			assertNotNull(script.getProperty(PROP_NAME_SIMULATOR_URL));
			String s= script.getProperty(PROP_NAME_SIMULATOR_URL).toString() + "init.properties";
			String content = readFromURL(new URL(s));
			assertFalse(content.isEmpty());
			SimScript subScript = script.getSubScripts().get("hellowebsocket.1");
			assertNotNull(subScript.getProperty(PROP_NAME_SIMULATOR_URL));
			assertNotEquals(script.getProperty(PROP_NAME_SIMULATOR_URL), subScript.getProperty(PROP_NAME_SIMULATOR_URL));
			assertFalse(readFromURL(new URL(subScript.getProperty(PROP_NAME_SIMULATOR_URL) + "open.sim")).isEmpty());
		} catch (IOException e) {
			e.printStackTrace();
			fail("unexpected exception" + e);
		}
	}

	@Test
	public void test10() {
		try {
			SimScript script = new SimScript(new SimScript(new File("scripts")), new ZipFile("scripts/websocket2.zip"), new File("scripts/websocket2.zip"));
			assertNotNull(script.getProperty(PROP_NAME_SIMULATOR_URL));
			String s= script.getProperty(PROP_NAME_SIMULATOR_URL).toString() + "init.properties";
			String content = readFromURL(new URL(s));
			assertFalse(content.isEmpty());
			SimScript subScript = script.getSubScripts().get("hellowebsocket.1");
			assertNotNull(subScript.getProperty(PROP_NAME_SIMULATOR_URL));
			assertNotEquals(script.getProperty(PROP_NAME_SIMULATOR_URL), subScript.getProperty(PROP_NAME_SIMULATOR_URL));
			assertFalse(readFromURL(new URL(subScript.getProperty(PROP_NAME_SIMULATOR_URL) + "open.sim")).isEmpty());
		} catch (IOException e) {
			e.printStackTrace();
			fail("unexpected exception" + e);
		}
	}
	
	protected String readFromURL(URL url) throws IOException {
		StringBuffer sb = new StringBuffer();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		}
		return sb.toString();
	}

	@Test
	public void test11() {
		try {
			SimScript script = new SimScript(new SimScript(new File("scripts")), new File("scripts/dummy"));
			assertTrue(script.isIgnored());
		} catch (IOException e) {
			e.printStackTrace();
			fail("unexpected exception" + e);
		}
	}
	
	@Test
	public void test12() {
		try {
			SimScript script = new SimScript(new SimScript(new File("scripts")), new SimulatorFolder(new File("scripts/websocket")));
			assertEquals(4, script.getLocalConfigAsRawProperties().size());
			assertEquals(10, script.getConfigAsProperties().size());
			assertEquals(2, script.getEffectiveTemplatePairs().size());
			assertEquals(2, script.getSubScripts().size());
			assertEquals(6, script.getSubScripts().get("hellowebsocket.1").getEffectiveTemplatePairs().size());
		} catch (IOException e) {
			fail("unexpected exception" + e);
		}
	}

	@Test
	public void test13() {
		try {
			SimScript script = new SimScript(new SimScript(new File("scripts")), new SimulatorFolder(new File("scripts/dummy")));
			assertTrue(script.isIgnored());
		} catch (IOException e) {
			e.printStackTrace();
			fail("unexpected exception" + e);
		}
	}

	@Test
	public void test14() {
		try {
			SimScript script = new SimScript(new SimScript(new File("scripts")), new SimulatorFolder(new File("scripts/kafka")));
			assertEquals(10, script.getSubScripts().get("broker").getConfigAsProperties().size());
		} catch (IOException e) {
			fail("unexpected exception" + e);
		}
	}
}

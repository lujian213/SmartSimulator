package io.github.lujian213.simulator.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.junit.Test;

import io.github.lujian213.simulator.util.SimUtils;
import io.netty.buffer.ByteBuf;

public class SimUtilsTest {

	@Test
	public void test() {
		try {
			assertEquals("/abc", SimUtils.decodeURL("/abc"));
			assertEquals("/abc?a=1", SimUtils.decodeURL("/abc?a=1"));
			assertEquals("/abc?a=1&b=2", SimUtils.decodeURL("/abc?a=1&b=2"));
			assertEquals("/abc?a=1&b=2&c=TPS 8.4", SimUtils.decodeURL("/abc?a=1&b=2&c=TPS%208.4"));
		} catch (UnsupportedEncodingException e) {
			fail("unexpected exception:" + e);
		}
	}
	
	@Test
	public void testParseURL() {
		try {
			assertTrue(SimUtils.parseURL("/abc").isEmpty());
			assertEquals(1, SimUtils.parseURL("/abc?a=1").size());
			assertEquals("1", SimUtils.parseURL("/abc?a=1").get("a"));
			assertEquals(2, SimUtils.parseURL("/abc?a=1&b=2").size());
			assertEquals("2", SimUtils.parseURL("/abc?a=1&b=2").get("b"));
			assertEquals(2, SimUtils.parseURL("/abc?a=1&b=2&c=").size());
		} catch (UnsupportedEncodingException e) {
			fail("unexpected exception:" + e);
		}
	}

	@Test
	public void testEncodeURL() {
		String source = "/jira/rest/api/2/search?jql=project = 21406  and labels IN (\"QTP_AUTOMATION\",\"MANUAL_TESTING\",\"Hammer_AUTOMATION\"  ,\"API_TESTING\" ,\"QA\") AND \"TPS Stream\" = \"Cash\"&fields=labels&maxResults=0";
		String expected = "/jira/rest/api/2/search?jql=project+%3D+21406++and+labels+IN+%28%22QTP_AUTOMATION%22%2C%22MANUAL_TESTING%22%2C%22Hammer_AUTOMATION%22++%2C%22API_TESTING%22+%2C%22QA%22%29+AND+%22TPS+Stream%22+%3D+%22Cash%22&fields=labels&maxResults=0";
		try {
			assertEquals(expected, SimUtils.encodeURL(source));
		} catch (UnsupportedEncodingException e) {
			fail("unexpected exception:" + e);
		}
	}
	
	@Test
	public void testParseDelimiters() {
		String s= "0x0D0x0A, 0x0A";
		ByteBuf[] ret = SimUtils.parseDelimiters(s);
		assertEquals(2, ret.length);
		assertEquals(2, ret[0].array().length);
		assertEquals(13, ret[0].array()[0]);
		assertEquals(10, ret[0].array()[1]);
		assertEquals(1, ret[1].array().length);
		assertEquals(10, ret[1].array()[0]);
	}

	@Test
	public void testTransformTime() {
		String s= "20180823";
		assertEquals("2018-08-23T00:00:00", SimUtils.transformTime("yyyyMMdd", s, "yyyy-MM-dd'T'HH:mm:ss"));
	}

	@Test
	public void testMergeResult() {
		VelocityContext context = new VelocityContext();
		context.put("simulator.url", "file:/c:/temp/abc/");
		context.put("simulator_url", "file:/c:/temp/abc/");
		String templateStr = "${simulator_url}index.html";
		try {
			String result = SimUtils.mergeResult(context, "tag", templateStr);
			assertEquals("file:/c:/temp/abc/index.html", result);
		} catch (IOException e) {
			fail("unexpected exception: " + e);
		}
	}
	
	@Test
	public void testContext2Properties() {
		VelocityContext context = new VelocityContext();
		context.put("key1", "value1");
		context.put("key2", 100);
		Properties props = SimUtils.context2Properties(context);
		assertEquals("value1", props.getProperty("key1"));
		assertEquals(100, props.get("key2"));
	}
}

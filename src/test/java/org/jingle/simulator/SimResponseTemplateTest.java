package org.jingle.simulator;

import static org.junit.Assert.*;
import org.apache.commons.lang.StringUtils;
import org.jingle.simulator.SimResponseTemplate;
import org.junit.Test;

public class SimResponseTemplateTest {

	@Test
	public void test() {
		try {
			StringUtils.capitalize("a");
			String temp = "HTTP/1.1 200 OK\r\n" + 
					"User-Defind-Head: Dummy Data\r\n" + 
					"\r\n" + 
					"It is first line\r\n" + 
					"ID is $id.";
			SimResponseTemplate resp = new SimResponseTemplate(temp);
			assertEquals(200, resp.code);
			assertEquals(1, resp.headers.size());
			assertEquals("Dummy Data", resp.headers.get("User-Defind-Head"));
		} catch (Exception e) {
			fail("unexpected exception:" + e);
		}
	}

}

package io.github.lujian213.simulator;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

public class SimRegexTemplateTest {

	@Test
	public void test() throws Exception {
		SimRegexTemplate inst = new SimRegexTemplate("/GET .*\\/mirror\\/(?<type>[^\\/]+)\\/(?<id>[^\\/]+)/");
		Map<String, Object> ret = inst.parse("GET localhost:8080/mirror/profile/123");
		
		assertEquals(2, ret.size());
		assertEquals("profile", ret.get("type"));
		assertEquals("123", ret.get("id"));
	}

}

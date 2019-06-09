package io.github.lujian213.simulator.util;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Test;

import io.github.lujian213.simulator.util.RequestHandler.GroovyRequestHandler;

public class GroovyRequestHandlerTest extends GroovyRequestHandler {

	@Test
	public void test() {
		GroovyRequestHandler handler = new GroovyRequestHandler();
		String templateBody = "def map = new HashMap()\r\n" + 
				"map.put(\"key1\", binding.getVariable(\"key1\") + \"1\")\r\n" + 
				"map.put(\"key2\", binding.getVariable(\"key2\") + \"1\")\r\n" + 
				"map.put(\"key3\", binding.getVariable(\"body\"))\r\n" + 
				"map";
		HashMap<String, Object> allContext = new HashMap<>();
		allContext.put("key1", "value1");
		allContext.put("key2", "value2");
		allContext.put("key3", "line1");
		Object ret = handler.executeGrovvy(allContext, templateBody, "line1");
		assertEquals("value11", ((HashMap<?, ?>)ret).get("key1"));
		assertEquals("value21", ((HashMap<?, ?>)ret).get("key2"));
		assertEquals("line1", ((HashMap<?, ?>)ret).get("key3"));
		assertEquals(3, ((HashMap<?, ?>)ret).size());
	}

}

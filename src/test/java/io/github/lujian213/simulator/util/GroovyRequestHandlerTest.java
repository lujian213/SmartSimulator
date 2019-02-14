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
				"map.put(\"key1\", \"value1\")\r\n" + 
				"map.put(\"key2\", \"value2\")\r\n" + 
				"map";
		Object ret = handler.executeGrovvy(templateBody, null);
		assertEquals(2, ((HashMap)ret).size());
	}

}

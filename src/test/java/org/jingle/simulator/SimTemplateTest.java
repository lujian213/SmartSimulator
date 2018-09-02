package org.jingle.simulator;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jingle.simulator.SimTemplate;
import org.jingle.simulator.SimTemplate.Token;
import org.junit.Test;

public class SimTemplateTest {

	@Test
	public void testParseTemplateLinet() {
		List<Token> tokenList = SimTemplate.parseTemplateLine("Name of this good is {$good}. It's price is {$value}.");
		assertEquals(5, tokenList.size());
		assertEquals(new Token("Name of this good is ", 0, 20), tokenList.get(0));
		assertEquals(new Token("{$good}", "good", 21, 27) , tokenList.get(1));
		assertEquals(new Token(". It's price is ", 28, 43) , tokenList.get(2));
		assertEquals(new Token("{$value}", "value", 44, 51) , tokenList.get(3));
		assertEquals(new Token(".", 52, 52) , tokenList.get(4));

		tokenList = SimTemplate.parseTemplateLine("Name of this good is {$good}");
		assertEquals(2, tokenList.size());
		assertEquals(new Token("Name of this good is ", 0, 20), tokenList.get(0));
		assertEquals(new Token("{$good}", "good", 21, 27) , tokenList.get(1));

		tokenList = SimTemplate.parseTemplateLine("{$good} is valuable");
		assertEquals(2, tokenList.size());
		assertEquals(new Token("{$good}", "good", 0, 6) , tokenList.get(0));
		assertEquals(new Token(" is valuable", 7, 18) , tokenList.get(1));

		tokenList = SimTemplate.parseTemplateLine("{$good}");
		assertEquals(1, tokenList.size());
		assertEquals(new Token("{$good}", "good", 0, 6) , tokenList.get(0));

		tokenList = SimTemplate.parseTemplateLine("");
		assertEquals(0, tokenList.size());

		tokenList = SimTemplate.parseTemplateLine("abc");
		assertEquals(1, tokenList.size());
		assertEquals(new Token("abc", 0, 2) , tokenList.get(0));
	}
	
	@Test
	public void testTryParse() {
		try {
			SimTemplate template = new SimTemplate("");
			List<Token> tokenList = new ArrayList<>();
			tokenList.add(new Token("Name of this good is ", 0, 20));
			tokenList.add(new Token("{$good}", "good", 21, 27));
			tokenList.add(new Token(". It's price is ", 28, 43));
			tokenList.add(new Token("{$value}", "value", 44, 51));
			tokenList.add(new Token(".", 52, 52));
			
			Map<String, Object> map = template.parseLine("Name of this good is apple. It's price is 100.", tokenList, 0);
			assertEquals(2, map.size());
			assertEquals("apple", map.get("good"));
			assertEquals("100", map.get("value"));
			
			
			map = template.parseLine("Name of this good is apple It's. It's price is 100.", tokenList, 0);
			assertEquals(2, map.size());
			assertEquals("apple It's", map.get("good"));
			assertEquals("100", map.get("value"));
			
			map = template.parseLine("Name of this good is apple. It's price is 100. ", tokenList, 0);
			assertNull(map);
		} catch (IOException e) {
			fail("unexpected exception:" + e);
		}
	}
	
	@Test
	public void testParse() {
		try {
			SimTemplate template = new SimTemplate("Line 1: This is an {$name}. And it's price is {$value}.\n"
					+ "Line2: There is nothing here.\n"
					+ "{$name2} is cheaper");
			
			Map<String, Object> map = template.parse("Line 1: This is an apple. And it's price is 100.\n" 
					+ "Line2: There is nothing here.\n" 
					+ "banana is cheaper");
			assertEquals(3, map.size());
			assertEquals("apple", map.get("name"));
			assertEquals("100", map.get("value"));
			assertEquals("banana", map.get("name2"));
			
			map = template.parse("Line 1: This is an apple. And it's price is 100\n" 
						+ "Line2: There is nothing here.\n" 
						+ "banana is cheaper");
			assertNull(map);

			map = template.parse("Line 1: This is an apple. And it's price is 100.\n" 
						+ "Line2: There is nothing here.\n" 
						+ "banana is cheaper\n"
						+ "line 4");
				assertNull(map);
			map = template.parse("Line 1: This is an apple. And it's price is 100\n" 
						+ "Line2: There is nothing here."); 
			assertNull(map);
		} catch (IOException e) {
			fail("unexpected exception:" + e);
		}
	}
	
	@Test
	public void testParse3() {
		try {
			SimTemplate template = new SimTemplate("GET {$url}/granite/cash/{$type}/{$value}/figure?t={$qt}&v=0&td={$td}&sd={$sd}&valuationdate=20180823&n={$n} HTTP/1.1");
			
			Map<String, Object> map = template.parse("GET http://APACCNSHLZN0099:443/granite/cash/SCSP/08051R9T0/figure?t=price&v=0&td=20180823&sd=20180827&valuationdate=20180823&n=100000 HTTP/1.1");
			assertNotNull(map);
		} catch (IOException e) {
			fail("unexpected exception:" + e);
		}
	}

}

package org.jingle.simulator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	public void testParseTemplateLinet2() {
		List<Token> tokenList = SimTemplate.parseTemplateLine("This is a complex token {$_abc_1.1-2}");
		assertEquals(2, tokenList.size());
		assertEquals(new Token("This is a complex token ", 0, 23), tokenList.get(0));
		assertEquals(new Token("{$_abc_1.1-2}", "_abc_1.1-2", 24, 36) , tokenList.get(1));

		tokenList = SimTemplate.parseTemplateLine("This is a complex token {$_abc_1.1-2:}");
		assertEquals(2, tokenList.size());
		assertEquals(new Token("This is a complex token ", 0, 23), tokenList.get(0));
		assertEquals(new Token("{$_abc_1.1-2:}", "_abc_1.1-2", 24, 37) , tokenList.get(1));

		tokenList = SimTemplate.parseTemplateLine("This is a complex token {$_abc_1.1-2:,}");
		assertEquals(2, tokenList.size());
		assertEquals(new Token("This is a complex token ", 0, 23), tokenList.get(0));
		assertEquals(new Token("{$_abc_1.1-2:,}", "_abc_1.1-2", 24, 38) , tokenList.get(1));

		tokenList = SimTemplate.parseTemplateLine("This is a complex token {$_abc_1.1-2:1,}");
		assertEquals(2, tokenList.size());
		assertEquals(new Token("This is a complex token ", 0, 23), tokenList.get(0));
		assertEquals(new Token("{$_abc_1.1-2:1,}", "_abc_1.1-2", 24, 39, 1, -1) , tokenList.get(1));
		
		tokenList = SimTemplate.parseTemplateLine("This is a complex token {$_abc_1.1-2:1,3}");
		assertEquals(2, tokenList.size());
		assertEquals(new Token("This is a complex token ", 0, 23), tokenList.get(0));
		assertEquals(new Token("{$_abc_1.1-2:1,3}", "_abc_1.1-2", 24, 40, 1, 3) , tokenList.get(1));

		tokenList = SimTemplate.parseTemplateLine("This is a complex token {$_abc_1.1-2:,3}");
		assertEquals(2, tokenList.size());
		assertEquals(new Token("This is a complex token ", 0, 23), tokenList.get(0));
		assertEquals(new Token("{$_abc_1.1-2:,3}", "_abc_1.1-2", 24, 39, -1, 3) , tokenList.get(1));

		tokenList = SimTemplate.parseTemplateLine("This is a complex token {$_abc_1.1-2:3}");
		assertEquals(2, tokenList.size());
		assertEquals(new Token("This is a complex token ", 0, 23), tokenList.get(0));
		assertEquals(new Token("{$_abc_1.1-2:3}", "_abc_1.1-2", 24, 38, 3, 3) , tokenList.get(1));
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

	@Test
	public void testParse4() {
		try {
			SimTemplate template = new SimTemplate("{$len:4}{$mon:3,4}+800");
			
			Map<String, Object> map = template.parse("0123Jul+800");
			assertEquals(2, map.size());
			assertEquals("0123", map.get("len"));
			assertEquals("Jul", map.get("mon"));

		
			template = new SimTemplate("{$msg:10,}100{$msg2}");
			
			map = template.parse("abc100defaas100jjj");
			assertEquals(2, map.size());
			assertEquals("abc100defaas", map.get("msg"));
			assertEquals("jjj", map.get("msg2"));
		} catch (IOException e) {
			fail("unexpected exception:" + e);
		}
	}
	
	@Test
	public void testParse5() {
		try {
			SimTemplate template = new SimTemplate("Our team has {$name}, {$name} and {$name}.");
			
			Map<String, Object> map = template.parse("Our team has Alex, Bruce and Chris.");
			assertEquals(1, map.size());
			assertEquals("Chris", map.get("name"));
		} catch (IOException e) {
			fail("unexpected exception:" + e);
		}
	}

	@Test
	public void testParse6() {
		try {
			SimTemplate template = new SimTemplate("Our team has {$name[]}, {$name[]} and {$name[]}.");
			
			Map<String, Object> map = template.parse("Our team has Alex, Bruce and Chris.");
			assertEquals(1, map.size());
			assertEquals("Alex", ((Object[])map.get("name"))[0]);
			assertEquals("Bruce", ((Object[])map.get("name"))[1]);
			assertEquals("Chris", ((Object[])map.get("name"))[2]);
		} catch (IOException e) {
			fail("unexpected exception:" + e);
		}
	}

	@Test
	public void testParse7() {
		try {
			SimTemplate template = new SimTemplate("Our team has {$name[]} and {$name[]}.\nTheir team has {$name[]} and {$name[]}.");
			
			Map<String, Object> map = template.parse("Our team has Alex and Bruce.\nTheir team has Chris and David.");
			assertEquals(1, map.size());
			assertEquals("Alex", ((Object[])map.get("name"))[0]);
			assertEquals("Bruce", ((Object[])map.get("name"))[1]);
			assertEquals("Chris", ((Object[])map.get("name"))[2]);
			assertEquals("David", ((Object[])map.get("name"))[3]);
		} catch (IOException e) {
			fail("unexpected exception:" + e);
		}
	}

	@Test
	public void testParse8() {
		try {
			SimTemplate template = new SimTemplate("2000TRADE{$p10:301}{$p311:2}{$p313:8}{$p321:12}{$p333:10}{$p343:8}{$p351:814}{$p1165:3}{$p1168:61}{$p1229:3}{$p1232:115}{$p1347:6}{$p1353:18}{$p1371:28}{$p1399:80}{$p1479:2}{$p1481:520}");
			
			Map<String, Object> map = template.parse("2000TRADECDTSTES ZMQX    TES.TPSCORP.S.OUTPUT20190101084901CDTS1T  CDTS1MQTTEQB0006TQ1     0101      N139          2019010220190101 20:48          010250NAAF2019X S459200HT1                     CAICB           N139    010419USD                 LMCOP           11000000          $77.8               E-CDTSMQ    00        8558000.0002          84608.33                                                                                                                                                                                                                                                                       20                M             X                                                                                                                                                                                                  7DTC 0901                      A/C 003414                     .                                                          02/12 CPN 1.95                02/06 1 will be corr                                                                                                                                                         142                                                             840                                                                                                                   70.164                  8642608.33 010250NAAF7362811RESP-140876166,1,TPS,CDTS                                                       TA                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ");
			assertEquals(17, map.size());
			assertEquals("00", map.get("p311"));
			assertEquals("8558000.0002", map.get("p321"));
		} catch (IOException e) {
			fail("unexpected exception:" + e);
		}
	}
}

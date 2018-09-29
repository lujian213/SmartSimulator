package org.jingle.simulator.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.jingle.simulator.util.RequestHandler.XPathRequestHandler;
import org.jingle.simulator.util.RequestHandler.XPathRequestHandler.NamespaceResolver;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XPathRequestHandlerTest extends XPathRequestHandler {

	@Test
	public void test() {
		XPathRequestHandler handler = new XPathRequestHandler();
		String requestBody="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
				"\r\n" + 
				"<bookstore>\r\n" + 
				"\r\n" + 
				"<book category=\"cooking\">\r\n" + 
				"  <title lang=\"en\">Everyday Italian</title>\r\n" + 
				"  <author>Giada De Laurentiis</author>\r\n" + 
				"  <year>2005</year>\r\n" + 
				"  <price>30.00</price>\r\n" + 
				"</book>\r\n" + 
				"\r\n" + 
				"<book category=\"children\">\r\n" + 
				"  <title lang=\"en\">Harry Potter</title>\r\n" + 
				"  <author>J K. Rowling</author>\r\n" + 
				"  <year>2005</year>\r\n" + 
				"  <price>29.99</price>\r\n" + 
				"</book>\r\n" + 
				"\r\n" + 
				"<book category=\"web\">\r\n" + 
				"  <title lang=\"en\">XQuery Kick Start</title>\r\n" + 
				"  <author>James McGovern</author>\r\n" + 
				"  <author>Per Bothner</author>\r\n" + 
				"  <author>Kurt Cagle</author>\r\n" + 
				"  <author>James Linn</author>\r\n" + 
				"  <author>Vaidyanathan Nagarajan</author>\r\n" + 
				"  <year>2003</year>\r\n" + 
				"  <price>49.99</price>\r\n" + 
				"</book>\r\n" + 
				"\r\n" + 
				"<book category=\"web\">\r\n" + 
				"  <title lang=\"en\">Learning XML</title>\r\n" + 
				"  <author>Erik T. Ray</author>\r\n" + 
				"  <year>2003</year>\r\n" + 
				"  <price>39.95</price>\r\n" + 
				"</book>\r\n" + 
				"\r\n" + 
				"</bookstore>";
		
		
		Map<String, XPathExp> xPathMap = new HashMap<>();
		xPathMap.put("title", new XPathExp("//book[price>49]/title[text()]", XPathConstants.STRING));
		xPathMap.put("year", new XPathExp("//book[@category='children']/year/text()", XPathConstants.STRING));
		xPathMap.put("prices", new XPathExp("//book[@category='web']/year/text()", XPathConstants.NODESET));
		try {
 			Map<String, Object> ret = handler.retrieveXPathValue(requestBody, xPathMap);
			assertEquals(3, ret.size());
			assertEquals("XQuery Kick Start", ret.get("title"));
			assertEquals("2005", ret.get("year"));
			assertEquals(2, ((NodeList)ret.get("prices")).getLength());
		} catch (IOException e) {
			fail("unexpected exception:" + e);
		}
	}

	@Test
	public void test2() {
		XPathRequestHandler handler = new XPathRequestHandler();
		String requestBody="<ns2:bookStore xmlns:ns2=\"http://bookstore.com/schemes\">\r\n" + 
				"    <ns2:book id=\"1\">\r\n" + 
				"        <ns2:name>Data Structure</ns2:name>\r\n" + 
				"    </ns2:book>\r\n" + 
				"    <ns2:book id=\"2\">\r\n" + 
				"        <ns2:name>Java Core</ns2:name>\r\n" + 
				"    </ns2:book>\r\n" + 
				"</ns2:bookStore>";
	
		Map<String, XPathExp> xPathMap = new HashMap<>();
		xPathMap.put("name", new XPathExp("//ns2:bookStore/ns2:book[1]/ns2:name/text()", XPathConstants.STRING));
		xPathMap.put("id", new XPathExp("//ns2:bookStore/ns2:book[1]/@id", XPathConstants.STRING));
		try {
			Map<String, Object> ret = handler.retrieveXPathValue(requestBody, xPathMap);
			assertEquals(2, ret.size());
			assertEquals("Data Structure", ret.get("name"));
			assertEquals("1", ret.get("id"));
		} catch (IOException e) {
			fail("unexpected exception:" + e);
		}
	}

	@Test
	public void test3() {
		XPathRequestHandler handler = new XPathRequestHandler();
		String requestBody="<ns9:citimlResponseException xmlns=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:ns14=\"http://tradecapturesys.cmb.citigroup.net/citiml-eqd-extended-2-0\" xmlns:ns9=\"http://tradecapturesys.cmb.citigroup.net/citiml-2-0\" xmlns:ns5=\"http://www.dtcc.com/ext\" xmlns:ns12=\"http://tradecapturesys.cmb.citigroup.net/citiml-bond-option-extended-2-0\" xmlns:ns6=\"http://tradecapturesys.cmb.citigroup.net/citiml-common-2-0\" xmlns:ns13=\"http://tradecapturesys.cmb.citigroup.net/citiml-cds-extended-2-0\" xmlns:ns7=\"http://tradecapturesys.cmb.citigroup.net/citiml-eq-shared-extended-2-0\" xmlns:ns10=\"http://tradecapturesys.cmb.citigroup.net/citiml-payload-2-0\" xmlns:ns8=\"http://tradecapturesys.cmb.citigroup.net/citiml-return-swaps-extended-2-0\" xmlns:ns11=\"http://www.citi.com/citisdrre-fpml/recordkeeping\" xmlns:ns2=\"http://tradecapturesys.cmb.citigroup.net/citiml-com-extended-2-0\" xmlns:ns4=\"http://tradecapturesys.cmb.citigroup.net/citiml-ird-extended-2-0\" xmlns:ns3=\"http://www.fpml.org/FpML-5/recordkeeping\">\r\n" + 
				"<ns9:citimlResponseMessageHeader>\r\n" + 
				"<ns9:citimlMessageId messageIdScheme=\"Oasys\">Msg21551463280409107194118747912017</ns9:citimlMessageId>\r\n" + 
				"<ns9:citimlInReplyTo messageIdScheme=\"TPS\">QA-Msg5664948213286586007707799515560287</ns9:citimlInReplyTo>\r\n" + 
				"<ns9:citimlSentBy messageAddressScheme=\"Oasys\">OasysTradeCapture</ns9:citimlSentBy>\r\n" + 
				"<ns9:citimlSendTo>RIO</ns9:citimlSendTo>\r\n" + 
				"<ns9:citimlCreationTimestamp>2018-09-18T06:20:12-04:00</ns9:citimlCreationTimestamp>\r\n" + 
				"<ns9:citimlMessageFilter citimlMessageFilterScheme=\"http://www.citigroup.com/coding-scheme/front-office-filter\">TPS</ns9:citimlMessageFilter>\r\n" + 
				"</ns9:citimlResponseMessageHeader>\r\n" + 
				"<ns9:citimlReason>\r\n" + 
				"<ns6:citimlDescription>\r\n" + 
				"90774109|1: Transaction is of ProductClass:34. It should have minimum 2 Corpuses, but it has 1 corpus.\r\n" + 
				"</ns6:citimlDescription>\r\n" + 
				"</ns9:citimlReason>\r\n" + 
				"</ns9:citimlResponseException>";
	
		Map<String, XPathExp> xPathMap = new HashMap<>();
		xPathMap.put("sendTo", new XPathExp("//ns9:citimlSendTo/text()", XPathConstants.STRING));
		try {
			Map<String, Object> ret = handler.retrieveXPathValue(requestBody, xPathMap);
			assertEquals(1, ret.size());
			assertEquals("RIO", ret.get("sendTo"));
		} catch (IOException e) {
			fail("unexpected exception:" + e);
		}
	}
}

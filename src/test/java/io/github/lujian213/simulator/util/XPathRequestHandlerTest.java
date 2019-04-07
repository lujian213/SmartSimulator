package io.github.lujian213.simulator.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathConstants;

import org.junit.Test;

import io.github.lujian213.simulator.util.RequestHandler.XPathRequestHandler;

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
			assertEquals(2, ((String[])ret.get("prices")).length);
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
		String requestBody="<ns9:citymlResponseException xmlns=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:ns14=\"http://tradecapturesys.cmb.citygroup.net/cityml-eqd-extended-2-0\" xmlns:ns9=\"http://tradecapturesys.cmb.citygroup.net/cityml-2-0\" xmlns:ns5=\"http://www.dtcc.com/ext\" xmlns:ns12=\"http://tradecapturesys.cmb.citygroup.net/cityml-bond-option-extended-2-0\" xmlns:ns6=\"http://tradecapturesys.cmb.citygroup.net/cityml-common-2-0\" xmlns:ns13=\"http://tradecapturesys.cmb.citygroup.net/cityml-cds-extended-2-0\" xmlns:ns7=\"http://tradecapturesys.cmb.citygroup.net/cityml-eq-shared-extended-2-0\" xmlns:ns10=\"http://tradecapturesys.cmb.citygroup.net/cityml-payload-2-0\" xmlns:ns8=\"http://tradecapturesys.cmb.citygroup.net/cityml-return-swaps-extended-2-0\" xmlns:ns11=\"http://www.city.com/citysdrre-fpml/recordkeeping\" xmlns:ns2=\"http://tradecapturesys.cmb.citygroup.net/cityml-com-extended-2-0\" xmlns:ns4=\"http://tradecapturesys.cmb.citygroup.net/cityml-ird-extended-2-0\" xmlns:ns3=\"http://www.fpml.org/FpML-5/recordkeeping\">\r\n" + 
				"<ns9:citymlResponseMessageHeader>\r\n" + 
				"<ns9:citymlMessageId messageIdScheme=\"OBsys\">XXXXXXXXXX</ns9:citymlMessageId>\r\n" + 
				"<ns9:citymlInReplyTo messageIdScheme=\"TQS\">XXXXXXX</ns9:citymlInReplyTo>\r\n" + 
				"<ns9:citymlSentBy messageAddressScheme=\"Oasys\">OBysTradeCapture</ns9:citymlSentBy>\r\n" + 
				"<ns9:citymlSendTo>RIO</ns9:citymlSendTo>\r\n" + 
				"<ns9:citymlCreationTimestamp>2018-09-18T06:20:12-04:00</ns9:citymlCreationTimestamp>\r\n" + 
				"<ns9:citymlMessageFilter citymlMessageFilterScheme=\"http://www.citygroup.com/coding-scheme/front-office-filter\">TQS</ns9:citymlMessageFilter>\r\n" + 
				"</ns9:citymlResponseMessageHeader>\r\n" + 
				"<ns9:citymlReason>\r\n" + 
				"<ns6:citymlDescription>\r\n" + 
				"TTTTTTT.\r\n" + 
				"</ns6:citymlDescription>\r\n" + 
				"</ns9:citymlReason>\r\n" + 
				"</ns9:citymlResponseException>";
	
		Map<String, XPathExp> xPathMap = new HashMap<>();
		xPathMap.put("sendTo", new XPathExp("//ns9:citymlSendTo/text()", XPathConstants.STRING));
		try {
			Map<String, Object> ret = handler.retrieveXPathValue(requestBody, xPathMap);
			assertEquals(1, ret.size());
			assertEquals("RIO", ret.get("sendTo"));
		} catch (IOException e) {
			fail("unexpected exception:" + e);
		}

		xPathMap = new HashMap<>();
		xPathMap.put("sendTo", new XPathExp("//ns9:citymlSendTo/text()", XPathConstants.NODESET));
		try {
			Map<String, Object> ret = handler.retrieveXPathValue(requestBody, xPathMap);
			assertEquals(1, ret.size());
			assertEquals("RIO", ((String[])ret.get("sendTo"))[0]);
		} catch (IOException e) {
			fail("unexpected exception:" + e);
		}
	}
	
	@Test
	public void test4() {
		XPathRequestHandler handler = new XPathRequestHandler();
		String requestBody="<cityml>\n" + 
				"	<transaction>\n" + 
				"		<transactionType>New</transactionType>\n" + 
				"	</transaction>\n" + 
				"	<productDetails>\n" + 
				"		<fpml>\n" + 
				"			<FpML xmlns=\"http://www.fpml.org/2010/FpML-4-9\" xmlns:fpml=\"http://www.fpml.org/2010/FpML-4-9\"\n" + 
				"				xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"4-9\"\n" + 
				"				xsi:type=\"DataDocument\"\n" + 
				"				xsi:schemaLocation=\"http://www.fpml.org/2010/FpML-4-9 ../fpml-main-4-9.xsd http://www.w3.org/2000/09/xmldsig# ../xmldsig-core-schema.xsd\">\n" + 
				"				<trade>\n" + 
				"						<feeLeg>\n" + 
				"							<singlePayment>\n" + 
				"								<adjustablePaymentDate>2019-03-12</adjustablePaymentDate>\n" + 
				"								<fee id='abc'>19.0</fee>\n" + 
				"								<fee id='xyz'>18.0</fee>\n" + 
				"							</singlePayment>\n" + 
				"						</feeLeg>\n" + 
				"				</trade>\n" + 
				"			</FpML>\n" + 
				"		</fpml>\n" + 
				"	</productDetails>\n" + 
				"</cityml>";
	
		Map<String, XPathExp> xPathMap = new HashMap<>();
		xPathMap.put("date", new XPathExp("//*[local-name()='singlePayment']/*[local-name()='adjustablePaymentDate']/text()", XPathConstants.STRING));
		xPathMap.put("feeXYZ", new XPathExp("//*[local-name()='singlePayment']/*[local-name()='fee'][@id='xyz']/text()", XPathConstants.STRING));
		try {
			Map<String, Object> ret = handler.retrieveXPathValue(requestBody, xPathMap);
			assertEquals(2, ret.size());
			assertEquals("2019-03-12", ret.get("date"));
			assertEquals("18.0", ret.get("feeXYZ"));
		} catch (IOException e) {
			fail("unexpected exception:" + e);
		}
	}
}

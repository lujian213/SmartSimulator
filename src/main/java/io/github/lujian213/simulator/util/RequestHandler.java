package io.github.lujian213.simulator.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import io.github.lujian213.simulator.SimRequest;
import io.github.lujian213.simulator.SimTemplate;
import net.minidev.json.JSONArray;
import static io.github.lujian213.simulator.SimSimulatorConstants.*;

public interface RequestHandler {
	static RequestHandlerChain inst = new RequestHandlerChain (
			new XPathRequestHandler(),
			new JSonPathRequestHandler(),
			new JSonObjectRequestHandler(),
			new GroovyRequestHandler(),
			new DefaultRequestHandler()
	);
			
	public Map<String, Object> handle(Map<String, String> headers, String templateBody, SimRequest request) throws IOException;
	
	public static RequestHandler getHandlerChain() {
		return inst;
	}
	
	static class RequestHandlerChain implements RequestHandler {
		List<RequestHandler> handlerList = new ArrayList<>();
		
		public RequestHandlerChain(RequestHandler ... handlers) {
			for (RequestHandler handler: handlers) {
				handlerList.add(handler);
			}
		}
		
		public void addHandler(RequestHandler handler) {
			if (handlerList.size() > 0) {
				handlerList.add(handlerList.size() - 1, handler);
			} else {
				handlerList.add(handler);
			}
		}
		
		@Override
		public Map<String, Object> handle(Map<String, String> headers, String templateBody, SimRequest request) throws IOException {
			for (RequestHandler handler: handlerList) {
				Map<String, Object> ret = handler.handle(headers, templateBody, request);
				if (ret != null) {
					return ret;
				}
			}
			return null;
		}
	}
	
	static class DefaultRequestHandler implements RequestHandler {
		@Override
		public Map<String, Object> handle(Map<String, String> headers, String templateBody, SimRequest request) throws IOException {
			if (templateBody != null) {
				Map<String, Object> ret = new SimTemplate(templateBody).parse(request.getBody());
				if (ret == null) {
					SimUtils.printMismatchInfo("body template", templateBody, request.getBody());
				}
				return ret;
			}
			return new HashMap<>();
		}
	}

	static class XPathRequestHandler implements RequestHandler {
		public static final String BODY_TYPE_XPATH = "XPath";

		static class NamespaceResolver implements NamespaceContext {
		    //Store the source document to search the namespaces
		    private Document sourceDocument;
		 
		    public NamespaceResolver(Document document) {
		        sourceDocument = document;
		    }
		 
		    //The lookup for the namespace uris is delegated to the stored document.
		    public String getNamespaceURI(String prefix) {
		        if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
		            return sourceDocument.lookupNamespaceURI(null);
		        } else {
		        	return sourceDocument.lookupNamespaceURI(prefix);
		        }
		    }
		 
		    public String getPrefix(String namespaceURI) {
		        return null;
		    }
		 
		    @SuppressWarnings("rawtypes")
		    public Iterator getPrefixes(String namespaceURI) {
		        return null;
		    }
		}
		
		static class XPathExp {
			private String xpath;
			private QName type;
			
			public XPathExp(String xpath, QName type) {
				this.xpath = xpath;
				this.type = type;
			}
			public String getXpath() {
				return xpath;
			}
			public QName getType() {
				return type;
			}
		}
		
		@Override
		public Map<String, Object> handle(Map<String, String> headers, String templateBody, SimRequest request) throws IOException {
			String bodyType = (String) headers.get(HEADER_NAME_BODY_TYPE);
			if (BODY_TYPE_XPATH.equals(bodyType)) {
				return retrieveXPathValue(request.getBody(), parse(templateBody));
			}
			return null;
		}
		
		protected Map<String, XPathExp> parse(String templateBody) throws IOException {
			Map<String, XPathExp> xPathMap = new HashMap<>();
			try (BufferedReader reader = new BufferedReader(new StringReader(templateBody))) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					String[] prop = SimUtils.parseProperty(line);
					if (prop != null) {
						if (prop[0].endsWith("[]")) {
							xPathMap.put(prop[0].substring(0, prop[0].length() - 2), new XPathExp(prop[1], XPathConstants.NODESET));
						} else {
							xPathMap.put(prop[0], new XPathExp(prop[1], XPathConstants.STRING));
						}
					}
				}
			}
			return xPathMap;
		}
		
		protected Map<String, Object> retrieveXPathValue(String requestBody, Map<String, XPathExp> xPathMap) throws IOException {
			try (Reader reader = new StringReader(requestBody)) {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setNamespaceAware(true);
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document document = builder.parse(new InputSource(reader)); 
				XPath xpath = XPathFactory.newInstance().newXPath();
				xpath.setNamespaceContext(new NamespaceResolver(document));
				
				Map<String, Object> ret = new HashMap<>();
				for (Map.Entry<String, XPathExp> entry: xPathMap.entrySet()) {
			        XPathExpression expr = xpath.compile(entry.getValue().getXpath());
					Object value = expr.evaluate(document, entry.getValue().getType());
					if (value == null || (value instanceof String && ((String)value).isEmpty()) || (value instanceof NodeList && ((NodeList)value).getLength() == 0)) {
						SimLogger.getLogger().error("can not find value for xpath [" + entry.getValue().getXpath() + "]");
						return null;
					}
					if (entry.getValue().getType().equals(XPathConstants.NODESET)) {
						String[] valueArray = new String[((NodeList)value).getLength()];
						for (int i = 1; i <= valueArray.length; i++) {
							valueArray[i - 1] = ((NodeList)value).item(i - 1).getTextContent();
						}
						ret.put(entry.getKey(), valueArray);
					} else {
						ret.put(entry.getKey(), value);
					}
				}
				return ret;
			} catch (ParserConfigurationException | SAXException | XPathExpressionException e) {
				SimLogger.getLogger().error("error to evaluate xpath", e);
			}
			return null;
		}
	}
	
	static class JSonPathRequestHandler extends XPathRequestHandler {
		public static final String BODY_TYPE_JSONPATH = "JSonPath";

		@Override
		public Map<String, Object> handle(Map<String, String> headers, String templateBody, SimRequest request) throws IOException {
			String bodyType = (String) headers.get(HEADER_NAME_BODY_TYPE);
			if (BODY_TYPE_JSONPATH.equals(bodyType)) {
				return retrievePathValue(request.getBody(), parse(templateBody));
			}
			return null;
		}
		
		protected Map<String, Object> retrievePathValue(String requestBody, Map<String, XPathExp> pathMap) throws IOException {
			Object document = Configuration.defaultConfiguration().jsonProvider().parse(requestBody);
			Map<String, Object> ret = new HashMap<>();
			for (Map.Entry<String, XPathExp> entry: pathMap.entrySet()) {
				Object value = JsonPath.read(document, entry.getValue().getXpath());
				if (value == null || (value instanceof String && ((String)value).isEmpty()) || (value instanceof JSONArray && ((JSONArray)value).isEmpty())) {
					SimLogger.getLogger().error("can not find value for path [" + entry.getValue().getXpath() + "]");
					return null;
				}
				if (entry.getValue().getType().equals(XPathConstants.NODESET)) {
					if (value instanceof JSONArray) {
						Object[] valueArray = new Object[((JSONArray)value).size()];
						for (int i = 1; i <= valueArray.length; i++) {
							valueArray[i - 1] = ((JSONArray)value).get(i - 1);
						}
						ret.put(entry.getKey(), valueArray);
					} else {
						ret.put(entry.getKey(), new Object[] {value});
					}
				} else {
					if (value instanceof JSONArray) {
						ret.put(entry.getKey(), ((JSONArray)value).get(0));
					} else {
						ret.put(entry.getKey(), value);
					}
				}
			}
			return ret;
		}
	}

	static class JSonObjectRequestHandler implements RequestHandler {
		public static final String BODY_TYPE_JSONOBJECT = "JSonObject";

		@Override
		public Map<String, Object> handle(Map<String, String> headers, String templateBody, SimRequest request) throws IOException {
			String bodyType = (String) headers.get(HEADER_NAME_BODY_TYPE);
			if (BODY_TYPE_JSONOBJECT.equals(bodyType)) {
				return retrieveJSonObject(request.getBody(), headers);
			}
			return null;
		}
		
		protected Map<String, Object> retrieveJSonObject(String requestBody, Map<String, String> headers) throws IOException {
			Map<String, Object> ret = new HashMap<>();
			ObjectMapper mapper = new ObjectMapper();
			Object obj;
			try {
				obj = mapper.readValue(requestBody, Class.forName(headers.get(HEADER_NAME_JSON_CLASS_NAME), true, Thread.currentThread().getContextClassLoader()));
			} catch (ClassNotFoundException e) {
				throw new IOException(e);
			}
			ret.put(headers.get(HEADER_NAME_JSON_VAR_NAME), obj);
			return ret;
		}
	}

	static class GroovyRequestHandler implements RequestHandler {
		public static final String BODY_TYPE_GROOVY = "Groovy";

		@Override
		public Map<String, Object> handle(Map<String, String> headers, String templateBody, SimRequest request) throws IOException {
			String bodyType = (String) headers.get(HEADER_NAME_BODY_TYPE);
			if (BODY_TYPE_GROOVY.equals(bodyType)) {
				Object value = executeGrovvy(templateBody, request);
				if (value instanceof Map) {
					return (Map<String, Object>) value;
				}
			}
			return null;
		}
		
		protected Object executeGrovvy(String templateBody, SimRequest request) {
			Binding binding = new Binding();
			binding.setVariable("request", request);
			GroovyShell shell = new GroovyShell(binding);
			return shell.evaluate(templateBody);
		}
	}
}


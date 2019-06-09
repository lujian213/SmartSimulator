package io.github.lujian213.simulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import io.github.lujian213.simulator.util.RequestHandler;
import io.github.lujian213.simulator.util.SimLogger;
import io.github.lujian213.simulator.util.SimUtils;

public class SimRequestTemplate {
	public static class HeaderItem {
		private String name;
		private String value;
		private boolean optional = false;
		private SimTemplate template;
		
		public HeaderItem(String name, String value) throws IOException {
			this(name, value, false);
		}

		public HeaderItem(String name, String value, boolean optional) throws IOException {
			this.name = name;
			this.value = value;
			this.optional = optional;
			this.template = new SimTemplate(name + ": " + value);
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}

		public boolean isOptional() {
			return optional;
		}
		
		public SimTemplate getTemplate() {
			return this.template;
		}

		public Map<String, Object> parse(String line) throws IOException {
			return this.template.parse(line);
		}

		@Override
		public String toString() {
			return (optional ? "*" : "") + name + ": " + value;
		}
	}
	
	public static final String HEADER_AUTHENTICATION = "Authentication";
	private SimTemplate topLineTemplate;
	private Map<String, HeaderItem> headerTemplates = new HashMap<>();
	private HeaderItem authenticationsTemplate = null;
	private String bodyContent;
	private Map<String, HeaderItem> extraHeader = new HashMap<>();

	public SimRequestTemplate(String content) throws IOException {
		init(content);
	}
	
	protected void init(String content) throws IOException {
		try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
			int lineNum = 1;
			String line = null;
			StringBuffer body = null;
			while ((line = reader.readLine()) != null) {
				if (lineNum == 1) {
					line = line.trim();
					if (!line.isEmpty() && '/' == line.charAt(0) && '/' == line.charAt(line.length() - 1)) {
						topLineTemplate = new SimRegexTemplate(line);
					} else {
						topLineTemplate = new SimTemplate(line);
					}
				} else if (body == null) {
					String[] prop = SimUtils.parseProperty(line);
					if (prop != null) {
						boolean optionalHeader = false;
						if (prop[0].startsWith("*")) {
							optionalHeader = true;
							prop[0] = prop[0].substring(1);
						}
						if (HEADER_AUTHENTICATION.equals(prop[0])) {
							this.authenticationsTemplate = new HeaderItem(prop[0], prop[1], optionalHeader);
						} else if (prop[0].startsWith("_")) {
							extraHeader.put(prop[0], new HeaderItem(prop[0], prop[1], optionalHeader));
						} else {
							this.headerTemplates.put(prop[0], new HeaderItem(prop[0], prop[1], optionalHeader));
						}
					}
					if (line.isEmpty()) {
						body = new StringBuffer();
					}
				} else {
					if (body.length() != 0) {
						body.append("\n");
					} 
					body.append(line);				
				}
				lineNum++;
			}
			if (body != null && !body.toString().isEmpty()) {
				this.bodyContent = body.toString(); 
			}
		}
	}
	
	public SimTemplate getTopLineTemplate() {
		return this.topLineTemplate;
	}
	
	public Map<String, HeaderItem> getHeaderTemplate() {
		return this.headerTemplates;
	}
	
	public HeaderItem getAuthenticationsTemplate() {
		return this.authenticationsTemplate;
	}
	
	public String getBody() {
		return this.bodyContent;
	}
	
	public Map<String, Object> match(Map<String, Object> allContext, SimRequest request) throws IOException {
		Map<String, Object> ret = new HashMap<>();
		String topLine = request.getTopLine() == null ? null : request.getTopLine().replaceAll("\\r\\n|\\r|\\n", " ");
		Map<String, Object> res = topLineTemplate.parse(topLine);
		if (res == null)
			return null;
		ret.putAll(res);
		SimLogger.getLogger().info("topline template [" + topLineTemplate + "] match with [" + request.getTopLine() + "]");
		for (Map.Entry<String, HeaderItem> entry: headerTemplates.entrySet()) {
			HeaderItem headerItem = entry.getValue();
			res = headerItem.parse(request.getHeaderLine(entry.getKey()));
			if (res == null) {
				if (!headerItem.isOptional()) {
					SimUtils.printMismatchInfo("header does not match", entry.getValue().toString(), request.getHeaderLine(entry.getKey()));
					return null; 
				}
			} else {
				ret.putAll(res);
			}
		}
		if (authenticationsTemplate != null) {
			res = authenticationsTemplate.parse(request.getAutnenticationLine());
			if (res == null) {
				if (!authenticationsTemplate.isOptional()) {
					SimUtils.printMismatchInfo("authentication does not match", authenticationsTemplate.toString(), request.getAutnenticationLine());
					return null;
				}
			} else {
				ret.putAll(res);
			}
		}
		res = RequestHandler.getHandlerChain().handle(allContext, extraHeader, bodyContent, request);
		if (res == null) {
			SimUtils.printMismatchInfo("body does not match", bodyContent, request.getBody());
			return null; 
		} else {
			ret.putAll(res);
		}
		return ret;
	}

	public Map<String, HeaderItem> getExtraHeader() {
		return extraHeader;
	}
}

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
	public static final String HEADER_AUTHENTICATION = "Authentication";
	private SimTemplate topLineTemplate;
	private Map<String, SimTemplate> headerTemplates = new HashMap<>();
	private SimTemplate authenticationsTemplate = null;
	private String bodyContent;
	private Map<String, String> extraHeader = new HashMap<>();

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
					if (!line.isEmpty() && '/' == line.charAt(0) && '/' == line.charAt(line.charAt(line.length() - 1))) {
						topLineTemplate = new SimRegexTemplate(line);
					} else {
						topLineTemplate = new SimTemplate(line);
					}
				} else if (body == null) {
					String[] prop = SimUtils.parseProperty(line);
					if (prop != null) {
						if (HEADER_AUTHENTICATION.equals(prop[0])) {
							this.authenticationsTemplate = new SimTemplate(line);
						} else if (prop[0].startsWith("_")) {
							extraHeader.put(prop[0], prop[1]);
						} else {
							this.headerTemplates.put(prop[0], new SimTemplate(line));
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
	
	public Map<String, SimTemplate> getHeaderTemplate() {
		return this.headerTemplates;
	}
	
	public SimTemplate getAuthenticationsTemplate() {
		return this.authenticationsTemplate;
	}
	
	public String getBody() {
		return this.bodyContent;
	}
	
	public Map<String, Object> match(SimRequest request) throws IOException {
		Map<String, Object> ret = new HashMap<>();
		String topLine = request.getTopLine() == null ? null : request.getTopLine().replaceAll("\\r\\n|\\r|\\n", " ");
		Map<String, Object> res = topLineTemplate.parse(topLine);
		if (res == null)
			return null;
		ret.putAll(res);
		SimLogger.getLogger().info("topline template [" + topLineTemplate + "] match with [" + request.getTopLine() + "]");
		for (Map.Entry<String, SimTemplate> entry: headerTemplates.entrySet()) {
			res = entry.getValue().parse(request.getHeaderLine(entry.getKey()));
			if (res == null) {
				SimUtils.printMismatchInfo("header does not match", entry.getValue().toString(), request.getHeaderLine(entry.getKey()));
				return null; 
			} else {
				ret.putAll(res);
			}
		}
		if (authenticationsTemplate != null) {
			res = authenticationsTemplate.parse(request.getAutnenticationLine());
			if (res == null) {
				SimUtils.printMismatchInfo("authentication does not match", authenticationsTemplate.toString(), request.getAutnenticationLine());
				return null; 
			} else {
				ret.putAll(res);
			}
		}
		res = RequestHandler.getHandlerChain().handle(extraHeader, bodyContent, request);
		if (res == null) {
			SimUtils.printMismatchInfo("body does not match", bodyContent, request.getBody());
			return null; 
		} else {
			ret.putAll(res);
		}
		return ret;
	}

	public Map<String, String> getExtraHeader() {
		return extraHeader;
	}
}

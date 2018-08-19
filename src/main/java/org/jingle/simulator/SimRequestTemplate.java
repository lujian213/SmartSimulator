package org.jingle.simulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class SimRequestTemplate {
	private static final Logger logger = Logger.getLogger(SimRequestTemplate.class);
	private SimTeplate topLineTemplate;
	private Map<String, SimTemplate> headerTemplates = new HashMap<>();
	private SimTemplate authenticationsTemplate = null;
	private SimTemplate bodyTemplate;
	
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
					topLineTemplate = new SimTemplate(line);
				} else {
					int index = line.indexOf(':');
					if (index != -1) {
						String headerName = line.substring(0, index);
						if ("Authentication".equals(headerName)) {
							this.authenticationsTemplate = new SimTemplate(line);
						} else {
							this.headerTemplates.put(headerName, new SimTemplate(line));
						}
					} else {
						if (line.isEmpty()) {
							if (body == null)
								body = new StringBuffer();
							else
								body.append(line).append("\n");
						} else {
							if (body != null) {
								body.append(line).append("\n");
							}
						}
					}
				}
				lineNum++;
			}
			if (body != null && !body.toString().isEmpty()) {
				this.bodyTemplate = new SimTemplate(body.toString());
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
	
	public SimTemplate getBodyTemplate() {
		return this.bodyTemplate;
	}
	
	public Map<String, Object> match(SimRequest request) throws IOException {
		Map<String, Object> ret = new HashMap<>();
		Map<String, Object> res = topLineTemplate.parse(request.getTopLine());
		if (res == null)
			return null;
		ret.putAll(res);
		logger.info("topline template [" + topLineTemplate + "] match with [" + request.getTopLine());
		for (Map.Entry<String, SimTemplate> entry: headerTemplates.entrySet()) {
			res = entry.getValue().parse(request.getHeaderLine(entry.getKey()));
			if (res == null) {
				logger.info("header template [" + entry.getValue() + "] does not match [" + request.getHeaderLine(entry.getKey()));
				return null; 
			} else {
				ret.putAll(res);
			}
		}
		if (authenticationsTemplate != null) {
			res = authenticationsTemplate.parse(request.getAutnenticationLine());
			if (res == null) {
				logger.info("authentication template [" + authenticationsTemplate + "] does not match [" + request.getAutnenticationLine());
				return null; 
			} else {
				ret.putAll(res);
			}
		}
		if (bodyTemplate != null) {
			res = bodyTemplate.parse(request.getBody());
			if (res == null) {
				logger.info("body template [" + bodyTemplate + "] does not match [" + request.getBody());
				return null; 
			} else {
				ret.putAll(res);
			}
		}
		return ret;
	}

}

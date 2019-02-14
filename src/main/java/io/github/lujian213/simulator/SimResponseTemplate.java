package io.github.lujian213.simulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class SimResponseTemplate {
	int code;
	Map<String, String> headers = new HashMap<>();
	String body = "";
	
	public SimResponseTemplate(String content) throws IOException {
		init(content);
	}
	
	public int getCode() {
		return code;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public String getBody() {
		return body;
	}

	protected void init(String content) throws IOException {
		try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
			int lineNum = 1;
			String line = null;
			StringBuffer sb = null;
			while ((line = reader.readLine()) != null) {
				if (lineNum == 1) {
					genCode(line);
				} else if (sb == null) {
					int index = line.indexOf(':');
					if (index != -1) {
						String headerName = line.substring(0, index);
						String headerValue = line.substring(index + 1).trim();
						this.headers.put(headerName,  headerValue);
					}
					if (line.isEmpty()) {
						sb = new StringBuffer();
					}
				} else {
					sb.append(line).append("\n");
				}
				lineNum++;
			}
			if (sb != null && !sb.toString().isEmpty()) {
				this.body = sb.toString();
			}
		}
	}
	
	protected void genCode(String line) {
		String[] parts = line.split(" ");
		if (parts != null && parts.length >= 2) {
			code = Integer.parseInt(parts[1]);
		} else {
			throw new RuntimeException("can not get code on [" + line + "]");
		}
	}
}

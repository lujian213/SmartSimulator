package io.github.lujian213.simulator.manager;

import java.util.Map;

import io.github.lujian213.simulator.SimResponseTemplate;

public class ResponseTemplateInfo extends TemplateInfo {
	private int code;
	
	public ResponseTemplateInfo(SimResponseTemplate temp) {
		super();
		for (Map.Entry<String, String> entry: temp.getHeaders().entrySet()) {
			this.headers.add(entry.getKey() +": " + entry.getValue());
		}
		this.body = temp.getBody();
		this.code = temp.getCode();
	}

	public int getCode() {
		return code;
	}
}

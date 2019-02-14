package io.github.lujian213.simulator.manager;

import java.util.Map;

import io.github.lujian213.simulator.SimRequestTemplate;
import io.github.lujian213.simulator.SimTemplate;

public class RequestTemplateInfo extends TemplateInfo {
	private String topLine;
	
	public RequestTemplateInfo(SimRequestTemplate temp) {
		super();
		for (SimTemplate line: temp.getHeaderTemplate().values()) {
			this.headers.add(line.toString());
		}
		for (Map.Entry<String, String> entry: temp.getExtraHeader().entrySet()) {
			this.headers.add(entry.getKey() + ": " + entry.getValue());
		}
		this.body = temp.getBody() == null ? null : temp.getBody().toString();
		this.topLine = temp.getTopLineTemplate().toString();
	}

	public String getTopLine() {
		return topLine;
	}
}

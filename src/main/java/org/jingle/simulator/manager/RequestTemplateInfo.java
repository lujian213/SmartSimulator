package org.jingle.simulator.manager;

import org.jingle.simulator.SimRequestTemplate;
import org.jingle.simulator.SimTemplate;

public class RequestTemplateInfo extends TemplateInfo {
	private String topLine;
	
	public RequestTemplateInfo(SimRequestTemplate temp) {
		super();
		for (SimTemplate line: temp.getHeaderTemplate().values()) {
			this.headers.add(line.toString());
		}
		this.body = temp.getBody() == null ? null : temp.getBody().toString();
		this.topLine = temp.getTopLineTemplate().toString();
	}

	public String getTopLine() {
		return topLine;
	}
}

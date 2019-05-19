package io.github.lujian213.simulator.manager;

import io.github.lujian213.simulator.SimRequestTemplate;
import io.github.lujian213.simulator.SimRequestTemplate.HeaderItem;

public class RequestTemplateInfo extends TemplateInfo {
	private String topLine;
	
	public RequestTemplateInfo(SimRequestTemplate temp) {
		super();
		for (HeaderItem line: temp.getHeaderTemplate().values()) {
			this.headers.add(line.toString());
		}
		for (HeaderItem header: temp.getExtraHeader().values()) {
			this.headers.add(header.toString());
		}
		this.body = temp.getBody() == null ? null : temp.getBody().toString();
		this.topLine = temp.getTopLineTemplate().toString();
	}

	public String getTopLine() {
		return topLine;
	}
}

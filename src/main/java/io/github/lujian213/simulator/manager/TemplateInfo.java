package io.github.lujian213.simulator.manager;

import java.util.ArrayList;
import java.util.List;

public class TemplateInfo {
	protected String body = null;
	protected List<String> headers = new ArrayList<>();
	
	public TemplateInfo() {
		
	}

	public List<String> getHeaders() {
		return headers;
	}

	public String getBody() {
		return body;
	}
	
	
}

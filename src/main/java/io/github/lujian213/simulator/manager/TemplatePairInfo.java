package io.github.lujian213.simulator.manager;

import io.github.lujian213.simulator.SimScript.TemplatePair;

public class TemplatePairInfo {
	private RequestTemplateInfo requestTemplate;
	private ResponseTemplateInfo[] responseTemplates;
	
	public TemplatePairInfo(TemplatePair pair) {
		this.requestTemplate = new RequestTemplateInfo(pair.getReq());
		this.responseTemplates = new ResponseTemplateInfo[pair.getResps().length];
		for (int i = 1; i <= pair.getResps().length; i++) {
			this.responseTemplates[i - 1] = new ResponseTemplateInfo(pair.getResps()[i - 1]);
		}
	}

	public RequestTemplateInfo getRequestTemplate() {
		return requestTemplate;
	}

	public ResponseTemplateInfo[] getResponseTemplates() {
		return responseTemplates;
	}
}

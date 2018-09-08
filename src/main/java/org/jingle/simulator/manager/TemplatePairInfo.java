package org.jingle.simulator.manager;

import org.jingle.simulator.SimScript.TemplatePair;

public class TemplatePairInfo {
	private RequestTemplateInfo requestTemplate;
	private ResponseTemplateInfo responseTemplate;
	
	public TemplatePairInfo(TemplatePair pair) {
		this.requestTemplate = new RequestTemplateInfo(pair.getReq());
		this.responseTemplate = new ResponseTemplateInfo(pair.getResp());
	}

	public RequestTemplateInfo getRequestTemplate() {
		return requestTemplate;
	}

	public ResponseTemplateInfo getResponseTemplate() {
		return responseTemplate;
	}
}

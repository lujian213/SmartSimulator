package io.github.lujian213.simulator.mem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.lujian213.simulator.AbstractSimRequest;
import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.util.ReqRespConvertor;

public class MemSimRequest extends AbstractSimRequest {
	private ReqRespConvertor convertor;
	private String body;
	private Object inputObj;
	
	public MemSimRequest(Object inputObj, ReqRespConvertor convertor) throws IOException {
		this.inputObj = inputObj;
		this.convertor = convertor;
		genBody();
	}
	
	protected MemSimRequest() {
		
	}
	
	@Override
	public ReqRespConvertor getReqRespConvertor() {
		return this.convertor;
	}
	
	protected void genBody() throws IOException {
		this.body = convertor.rawRequestToBody(inputObj);
	}
	
	public String getTopLine() {
		return inputObj.getClass().getName();
	}
	
	public String getHeaderLine(String header) {
		return null;
	}
	
	public String getAutnenticationLine() {
		return null;
	}
	
	public String getBody() {
		return this.body;
	}
	
	@Override
	protected void doFillResponse(SimResponse response) throws IOException {
		convertor.fillRawResponse(null, response);
	}
	
	@Override
	public List<String> getAllHeaderNames() {
		return new ArrayList<>();
	}

	@Override
	public String getRemoteAddress() {
		return null;
	}
}

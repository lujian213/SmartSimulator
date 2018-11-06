package org.jingle.simulator;

import java.io.IOException;
import java.util.List;

import org.jingle.simulator.util.ReqRespConvertor;

public interface SimRequest {
	public List<String> getAllHeaderNames();

	public String getHeaderLine(String header);

	public String getAutnenticationLine();

	public String getBody();

	public String getTopLine();

	public void fillResponse(SimResponse response) throws IOException; 
	
	public ReqRespConvertor getReqRespConvertor();
}

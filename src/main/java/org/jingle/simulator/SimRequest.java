package org.jingle.simulator;

import java.io.IOException;
import java.util.Map;

public interface SimRequest {
	public String getHeaderLine(String header);
	public String getAutnenticationLine();
	public String getBody();
	public String getTopLine();
	public void fillResponse(Map<String, Object> context, SimResponseTemplate response) throws IOException;
}

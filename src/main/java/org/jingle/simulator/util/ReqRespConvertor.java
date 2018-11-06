package org.jingle.simulator.util;

import java.io.IOException;
import java.util.Map;

import org.jingle.simulator.SimResponse;

public interface ReqRespConvertor {
	public String rawRequestToBody(Object rawRequest) throws IOException;
	public Map<String, Object> getRespContext() throws IOException;
	public void fillRawResponse(Object rawResponse, SimResponse simResponse) throws IOException;
}

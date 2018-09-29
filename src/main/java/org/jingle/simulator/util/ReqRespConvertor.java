package org.jingle.simulator.util;

import java.io.IOException;

import org.jingle.simulator.SimResponse;

public interface ReqRespConvertor {
	public String rawRequestToBody(Object rawRequest) throws IOException;
	public void fillRawResponse(Object rawResponse, SimResponse simResponse) throws IOException;
}

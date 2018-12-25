package org.jingle.simulator.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jingle.simulator.SimResponse;

public interface ReqRespConvertor extends SimContextAwareness {
	public String rawRequestToBody(Object rawRequest) throws IOException;
	default public Map<String, Object> getRespContext() throws IOException {
		return new HashMap<>();
	}
	public void fillRawResponse(Object rawResponse, SimResponse simResponse) throws IOException;
	default public void init(Properties props) {
	}
}

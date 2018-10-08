package org.jingle.simulator.webbit;

import java.io.IOException;

import org.jingle.simulator.SimResponse;
import org.jingle.simulator.util.ReqRespConvertor;
import org.webbitserver.WebSocketConnection;

public class DefaultWebbitWSReqRespConvertor implements ReqRespConvertor {

	@Override
	public String rawRequestToBody(Object rawRequest) throws IOException {
		if (rawRequest == null)
			return null;
		return new String((byte[])rawRequest);
	}

	@Override
	public void fillRawResponse(Object rawResponse, SimResponse simResponse) throws IOException {
		((WebSocketConnection)rawResponse).send(simResponse.getBodyAsString());
	}

}

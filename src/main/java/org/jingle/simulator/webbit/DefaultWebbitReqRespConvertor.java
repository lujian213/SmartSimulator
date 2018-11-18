package org.jingle.simulator.webbit;

import java.io.IOException;

import org.jingle.simulator.SimResponse;
import org.jingle.simulator.util.ReqRespConvertor;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

public class DefaultWebbitReqRespConvertor implements ReqRespConvertor {

	@Override
	public String rawRequestToBody(Object rawRequest) throws IOException {
		return ((HttpRequest)rawRequest).body();
	}

	@Override
	public void fillRawResponse(Object rawResponse, SimResponse simResponse) throws IOException {
		((HttpResponse)rawResponse).content(simResponse.getBody());
	}
}

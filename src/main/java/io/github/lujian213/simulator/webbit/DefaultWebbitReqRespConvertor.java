package io.github.lujian213.simulator.webbit;

import java.io.IOException;

import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.util.ReqRespConvertor;

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

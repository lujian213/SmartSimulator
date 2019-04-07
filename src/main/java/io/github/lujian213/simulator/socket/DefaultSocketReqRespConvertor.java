package io.github.lujian213.simulator.socket;

import java.io.IOException;
import java.nio.charset.Charset;

import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.util.ReqRespConvertor;
import io.netty.buffer.ByteBuf;

public class DefaultSocketReqRespConvertor implements ReqRespConvertor {

	@Override
	public String rawRequestToBody(Object rawRequest) throws IOException {
		if (rawRequest == null) {
			return null;
		} else {
			return ((ByteBuf) rawRequest).toString(Charset.forName("utf-8"));
		}
	}

	@Override
	public void fillRawResponse(Object rawResponse, SimResponse simResponse) throws IOException {
		((ByteBuf)rawResponse).writeBytes(simResponse.getBody());
	}
}

package io.github.lujian213.simulator.jms;

import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.util.ReqRespConvertor;

public class DefaultJMSReqRespConvertor implements ReqRespConvertor {

	@Override
	public String rawRequestToBody(Object rawRequest) throws IOException {
		try {
			return ((TextMessage)rawRequest).getText();
		} catch (JMSException e) {
			throw new IOException("error when doing request convert", e);
		}
	}

	@Override
	public void fillRawResponse(Object rawResponse, SimResponse simResponse) throws IOException {
		try {
			((TextMessage)rawResponse).setText(simResponse.getBodyAsString());
		} catch (JMSException e) {
			throw new IOException("error when doing response convert", e);
		}
	}
}

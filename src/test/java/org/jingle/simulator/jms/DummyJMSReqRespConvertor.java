package org.jingle.simulator.jms;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.BytesMessage;

import org.jingle.simulator.SimResponse;
import org.jingle.simulator.util.ReqRespConvertor;

public class DummyJMSReqRespConvertor implements ReqRespConvertor {

	@Override
	public String rawRequestToBody(Object rawRequest) throws IOException {
		try {
			StringBuffer sb = new StringBuffer();
			for (int i = 1; i <= 3; i++) {
				sb.append(((BytesMessage)rawRequest).readByte());
			}
			return sb.toString();
		} catch (JMSException e) {
			throw new IOException("error when doing request convert", e);
		}
	}

	@Override
	public void fillRawResponse(Object rawResponse, SimResponse simResponse) throws IOException {
		try {
			byte[] bytes = simResponse.getBodyAsString().trim().getBytes();
			for (int i = 1; i <= bytes.length; i++) {
				((BytesMessage)rawResponse).writeByte((byte) (bytes[i - 1] - '0'));
			}
		} catch (JMSException e) {
			throw new IOException("error when doing response convert", e);
		}
	}

	@Override
	public Map<String, Object> getRespContext() throws IOException {
		return new HashMap<>();
	}
}

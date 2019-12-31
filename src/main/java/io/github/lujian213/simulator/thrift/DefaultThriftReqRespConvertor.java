package io.github.lujian213.simulator.thrift;

import java.io.IOException;

import org.apache.thrift.TException;

import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.thrift.ThriftSimulator.CallTrace;
import io.github.lujian213.simulator.util.ReqRespConvertor;

public class DefaultThriftReqRespConvertor implements ReqRespConvertor {

	@Override
	public String rawRequestToBody(Object rawRequest) throws IOException {
		if (rawRequest == null) {
			return null;
		} else {
			return rawRequest.toString();
		}
	}

	@Override
	public void fillRawResponse(Object rawResponse, SimResponse simResponse) throws IOException {
		CallTrace trace = (CallTrace) rawResponse;
		try {
			trace.getFunction().process(trace.getSeqid(), trace.getProtocolIn(), trace.getProtocolOut(), trace.getSimInst());
		} catch (TException e) {
			throw new IOException(e);
		}
	}
}

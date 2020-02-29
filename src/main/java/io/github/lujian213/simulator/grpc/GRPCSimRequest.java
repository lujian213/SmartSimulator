package io.github.lujian213.simulator.grpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.lujian213.simulator.AbstractSimRequest;
import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.grpc.GRPCSimulator.CallTrace;
import io.github.lujian213.simulator.util.ReqRespConvertor;

public class GRPCSimRequest extends AbstractSimRequest {
	private ReqRespConvertor convertor;
	private CallTrace<?> trace;

	public GRPCSimRequest(CallTrace<?> trace, ReqRespConvertor convertor) throws IOException {
		this.convertor = convertor;
		this.trace = trace;
	}

	protected GRPCSimRequest() {
	}

	@Override
	public ReqRespConvertor getReqRespConvertor() {
		return this.convertor;
	}

	public String getTopLine() {
		return trace.getMethod().getName();
	}

	public String getHeaderLine(String header) {
		return null;
	}

	public String getAuthenticationLine() {
		return null;
	}

	public String getBody() {
		return null;
	}

	@Override
	protected void doFillResponse(SimResponse response) throws IOException {
		convertor.fillRawResponse(trace, response);
	}

	@Override
	public List<String> getAllHeaderNames() {
		return new ArrayList<>();
	}

	@Override
	public String getRemoteAddress() {
		return trace.getRemoteAddress();
	}

}

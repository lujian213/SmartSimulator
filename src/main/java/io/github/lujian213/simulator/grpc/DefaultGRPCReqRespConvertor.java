package io.github.lujian213.simulator.grpc;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.grpc.GRPCSimulator.CallTrace;
import io.github.lujian213.simulator.util.ReqRespConvertor;
import io.grpc.BindableService;

public class DefaultGRPCReqRespConvertor implements ReqRespConvertor {

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
		CallTrace<BindableService> ct = (CallTrace<BindableService>) rawResponse;
		try {
			Method method = ct.getMethod();
			Object result = method.invoke(ct.getSimInst(), ct.getArgs());
			ct.setResult(result);
		} catch (InvocationTargetException e) {
			ct.setThrowable(e.getCause());
		} catch (Exception e) {
			ct.setThrowable(e);
		}
	}
}

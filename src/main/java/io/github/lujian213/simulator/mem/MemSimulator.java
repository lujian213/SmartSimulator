package io.github.lujian213.simulator.mem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import io.github.lujian213.simulator.SimRequest;
import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.SimScript;
import io.github.lujian213.simulator.SimSimulator;
import io.github.lujian213.simulator.util.ReqRespConvertor;
import io.github.lujian213.simulator.util.SimLogger;
import io.github.lujian213.simulator.util.SimUtils;

public class MemSimulator extends SimSimulator {
	private ReqRespConvertor convertor;
	
	public MemSimulator(SimScript script) throws IOException {
		super(script);
		convertor = SimUtils.createMessageConvertor(script, new DefaultMemReqRespConvertor());
	}
	
	protected MemSimulator() {
	}
	
	@Override
	protected void init() throws IOException {
		super.init();
	}

	@Override
	protected void doStart() throws IOException {
		this.runningURL = "mem://127.0.0.1";
	}

	@Override
	protected void doStop() {
	}

	@Override
	public String getType() {
		return "Mem";
	}
	
	public String handleRequest(Object inputObj) throws IOException {
		SimRequest request = null;
		try {
			request = new MemSimRequest(inputObj, convertor);
			SimUtils.logIncomingMessage("local", this.getName(), request);
		} catch (Exception e) {
			SimLogger.getLogger().error("error when create SimRequest", e);
		}
		if (request != null) {
			List<SimResponse> respList = new ArrayList<>();
			try {
				respList = script.genResponse(request);
			} catch (Exception e) {
				SimLogger.getLogger().error("match and fill error", e);
			}
			castToSimulatorListener().onHandleMessage(getName(), request, respList, !respList.isEmpty());
			return respList.get(0).getBodyAsString();
		}
		return null;
	}
}

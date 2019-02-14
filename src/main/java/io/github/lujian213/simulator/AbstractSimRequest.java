package io.github.lujian213.simulator;

import java.io.IOException;
import java.util.Map;

public abstract class AbstractSimRequest implements SimRequest {
	public void fillResponse(SimResponse response) throws IOException {			
		Map<String, Object> headers = response.getHeaders();
		String simulator = (String) headers.remove(SimResponse.PROP_NAME_RESPONSE_TARGETSIMULATOR);
		if (simulator == null) {
			doFillResponse(response);
			simulator = (String) headers.remove(SimResponse.PROP_NAME_RESPONSE_TARGETSIMULATOR);
		}
		if (simulator != null) {
			SimSimulator sim = SimulatorRepository.getInstance().getSimulator(simulator);
			if (!sim.isRunning()) {
				throw new IOException("Simulator [" + simulator + "] is not running");
			}
			if (!(sim instanceof SimSesseionLessSimulator)) {
				throw new IOException("Simulator [" + simulator + "] is not an instance of SimSesseionLessSimulator");
			}
			SimSesseionLessSimulator.class.cast(sim).fillResponse(response);
		}
	}

	protected abstract void doFillResponse(SimResponse response) throws IOException;			

}

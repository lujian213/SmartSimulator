package io.github.lujian213.simulator.monitor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import io.github.lujian213.simulator.SimRequest;
import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.SimulatorListener;

public class MessageTracer implements SimulatorListener {
	private static final String PROP_NAME_TRACE_FILE = "trace.file";
	private static final Logger logger = Logger.getLogger(MessageTracer.class);
	private URL url = null;
	private PrintWriter pw = null;
	
	@Override
	public void onStart(String simulatorName) {
		try {
			pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(url.getFile())));
		} catch(IOException e) {
			logger.error("error when open stream", e);
		}
	}

	@Override
	public void onStop(String simulatorName) {
		pw.close();
	}

	@Override
	public synchronized void onHandleMessage(String simulatorName, SimRequest request, List<SimResponse> responseList,
			boolean status) {
		request.print(pw);
		for (SimResponse response: responseList) {
			response.print(pw);
		}
		pw.println("---------------------------------------");
		pw.flush();
	}

	@Override
	public void init(Properties props) {
		try {
			url = new URL(props.getProperty(PROP_NAME_TRACE_FILE));
			logger.info("trace file url = " + url);
		} catch (IOException e) {
			logger.error("error when init message tracer", e);
			throw new RuntimeException(e);
		}
	}

}

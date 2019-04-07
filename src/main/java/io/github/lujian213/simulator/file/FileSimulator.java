package io.github.lujian213.simulator.file;

import static io.github.lujian213.simulator.file.FileSimulatorConstants.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.SimScript;
import io.github.lujian213.simulator.SimSimulator;
import io.github.lujian213.simulator.util.ReqRespConvertor;
import io.github.lujian213.simulator.util.SimLogger;
import io.github.lujian213.simulator.util.SimUtils;

public class FileSimulator extends SimSimulator {
	private ReqRespConvertor convertor;
	private File inputDir;
	private long pollInterval;
	private FileAlterationObserver observer = null;
	private FileAlterationMonitor monitor = null;
	
	public FileSimulator(SimScript script) throws IOException {
		super(script);
		convertor = SimUtils.createMessageConvertor(script, new DefaultFileReqRespConvertor());
	}
	
	protected FileSimulator() {
	}
	
	@Override
	protected void init() throws IOException {
		super.init();
		inputDir = script.getMandatoryFileProperty(PROP_NAME_INPUT_DIR, "no input defined");
		pollInterval = script.getLongProperty(PROP_NAME_POLL_INTERVAL, 3000);
		convertor = SimUtils.createMessageConvertor(script, new DefaultFileReqRespConvertor());
	}

	@Override
	protected void doStart() throws IOException {
		if (!inputDir.isDirectory())
			throw new IOException(inputDir + " is not a valid dir");
		observer = new FileAlterationObserver(inputDir.getAbsolutePath());
		monitor = new FileAlterationMonitor(pollInterval);
		FileAlterationListener listener = new FileAlterationListenerAdaptor() {
		    @Override
		    public void onFileCreate(File file) {
				SimUtils.setThreadContext(script);
				FileSimRequest request = null;
				try {
					request = new FileSimRequest(file, convertor);
					SimUtils.logIncomingMessage(file.getAbsolutePath(), FileSimulator.this.getName(), request);
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
				}
		    }
		};
		observer.addListener(listener);
		monitor.addObserver(observer);
		try {
			monitor.start();
		} catch (Exception e) {
			throw new IOException(e);
		}
		this.runningURL = inputDir.toURI().toURL().toString();
	}

	@Override
	protected void doStop() {
		try {
			monitor.stop();
		} catch (Exception e) {
		}
	}

	@Override
	public String getType() {
		return "File";
	}
}

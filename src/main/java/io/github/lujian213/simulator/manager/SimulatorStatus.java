package io.github.lujian213.simulator.manager;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.github.lujian213.simulator.SimSimulator;
import io.github.lujian213.simulator.monitor.MessageCounter;
import io.github.lujian213.simulator.monitor.MessageCounter.Counter;

public class SimulatorStatus {
	private String name;
	private String type;
	private String status;
	private String runningURL;
	private int successfulMessages = 0;
	private int failedMessages = 0;
	private String startTime = null;
	private long startTimeInMillSec = 0;
	private long duration = 0;
	
	public SimulatorStatus(SimSimulator simulator) {
		this.name = simulator.getName();
		this.type = simulator.getType();
		this.status = simulator.isRunning() ? "running" : "stopped";
		this.runningURL = simulator.getRunningURL();
		
		Counter counter = MessageCounter.getInstance().getCounter(simulator.getName());
		if (counter != null && simulator.isRunning()) {
			this.startTimeInMillSec = counter.getStartTime();
			this.startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").format(new Date(counter.getStartTime()));
			this.duration = counter.getDuration();
			this.failedMessages = counter.getFailedMessages();
			this.successfulMessages = counter.getSuccessfulMessages();
		}
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getStatus() {
		return status;
	}

	public String getRunningURL() {
		return runningURL;
	}

	public int getTotalMessages() {
		return successfulMessages + failedMessages;
	}

	public int getSuccessfulMessages() {
		return successfulMessages;
	}

	public int getFailedMessages() {
		return failedMessages;
	}

	public String getStartTime() {
		return startTime;
	}

	public long getDuration() {
		return duration;
	}
	
	public long getStartTimeInMillSec() {
		return startTimeInMillSec;
	}
	
}

package org.jingle.simulator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

/*
 * a simple static http server
*/
public abstract class SmartSimulator {
	protected static final Logger logger = Logger.getLogger(SmartSimulator.class);
	protected SimScript script;
	protected String name;
	protected int port;
	
	public SmartSimulator(int port, String name, File folder) throws IOException {
		this.port = port;
		this.name = name;
		script = new SimScript(folder);
	}
	
	protected SmartSimulator() {
	}
	
	public String getName() {
		return name;
	}
	
	public abstract void setSSL(InputStream keystore, String passwd) throws IOException;
	
	public abstract void start() throws IOException;
	
}
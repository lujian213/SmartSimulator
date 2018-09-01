package org.jingle.simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

public class SimScript {
	public static class TemplatePair {
		private SimRequestTemplate req;
		private SimResponseTemplate resp;
		
		public TemplatePair(SimRequestTemplate req, SimResponseTemplate resp) {
			this.req = req;
			this.resp = resp;
		}

		public SimRequestTemplate getReq() {
			return req;
		}

		public SimResponseTemplate getResp() {
			return resp;
		}
	}
	
	private static final Logger logger = Logger.getLogger(SimScript.class);
	private static final Logger msgLogger = Logger.getLogger("messageLogger");
	
	private static final String SEP_LINE = "------------------------------------------------------------------"; 
	private static final String RESP_START = "HTTP/"; 
	private static final String SCRIPT_EXT = ".sim"; 
	private static final String INIT_FILE = "init.properties"; 
	public static final String PROP_NAME_SIMULATOR_CLASS = "simulator.class"; 
	public static final String PROP_NAME_SIMULATOR_NAME = "simulator.name"; 
	
	List<TemplatePair> templatePairs = new ArrayList<>();
	Map<String, SimScript> subScripts = new HashMap<>();
	Properties props = new Properties();
	
	public SimScript(File parent, File file) throws IOException {
		if (parent != null) {
			loadFolder(parent, false);
		}
		loadFolder(file, true); 
	}
	
	protected SimScript() {
		
	}
	
	protected void loadFolder(File folder, boolean includeSubFolder) throws IOException {
		if (!folder.exists() || !folder.isDirectory()) {
			throw new IOException("no folder [" + folder + "] exists");
		}
		File[] files = folder.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				if (file.isFile() && (file.getName().endsWith(SCRIPT_EXT) || file.getName().equals(INIT_FILE)) || (includeSubFolder && file.isDirectory())) {
					return true;
				}
				return false;
			}
		});
		int total = 0;
		for (File file: files) {
			if (file.isFile()) {
				if (INIT_FILE.equals(file.getName())) {
					logger.info("loadint init file [" + file.getName() + "] in [" + folder + "]");
					try (InputStream is = new FileInputStream(file)) {
						Properties p = new Properties();
						p.load(is);
						props.putAll(p);
					}
				} else {
					logger.info("loading script file [" + file + "] in [" + folder + "]");
					try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
						total += load(reader);
					}
				}
			} else {
				subScripts.put(file.getName(), new SimScript(null, file));
			}
		}
		logger.info("Total " + total + " req/resp pairs loaded in [" + folder + "]");
	}
	
	public Properties getProps() {
		return props;
	}
	
	@SuppressWarnings("unchecked")
    public Class<? extends SimSimulator> getSimulatorClass() throws ClassNotFoundException {
		return (Class<? extends SimSimulator>) Class.forName(props.getProperty(PROP_NAME_SIMULATOR_CLASS));
	}

	public String getSimulatorName() {
		return props.getProperty(PROP_NAME_SIMULATOR_NAME);
	}

	protected int load(BufferedReader reader) throws IOException {
		List<List<String>> block = null;
		int count = 0;
		while ((block = loadReqResp(reader)) != null) {
			templatePairs.add(new TemplatePair(new SimRequestTemplate(concatContent(block.get(0))), new SimResponseTemplate(concatContent(block.get(1)))));
			count++;
		}
		logger.info(count + " req/resp pairs loaded");
		return count;
	}
	
	
	protected List<List<String>> loadReqResp(BufferedReader reader) throws IOException {
		List<List<String>> ret = new ArrayList<>();
		List<String> lines = new ArrayList<>();
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith(RESP_START)) {
				if (lines.size() != 0) {
					if (lines.get(0).isEmpty()) {
						lines.remove(0);
					}
				}
				if (!lines.isEmpty()) {
					ret.add(lines);//req
				}
				lines = new ArrayList<>();
				lines.add(line);
			} else if (SEP_LINE.equals(line)) {
				break;
			} else {
				lines.add(line);
			}
		}
		
		if (lines.size() != 0) {
			if (lines.get(lines.size() - 1).isEmpty()) {
				lines.remove(lines.size() - 1);
			}
		}
		if (!lines.isEmpty()) {
			ret.add(lines);//resp
		}
		
		if (ret.size() == 1) {
			throw new IOException("Last request has no response");
		} else if (ret.size() == 0) {
			return null;
		} else if (ret.size() == 2) {
			return ret;
		} else {
			throw new IOException("wrong format.");
		}
	}
	
	protected String concatContent(List<String> lines) {
		if (lines.size() > 0) {
			StringBuffer sb = new StringBuffer();
			for (int i = 1; i <= lines.size(); i++) {
				sb.append(lines.get(i - 1));
				if (i != lines.size()) {
					sb.append("\n");
				}
			}
			return sb.toString();
		}
		return null;

	}

	public Map<String, SimScript> getSubScripts() {
		return subScripts;
	}
	
	public void genResponse(SimRequest request) throws IOException {
		for (TemplatePair pair: templatePairs) {
			Map<String, Object> context = pair.getReq().match(request);
			if (context != null) {
				logger.info("match with template: [" + pair.getReq().getTopLineTemplate() + "]");
				request.fillResponse(new SimResponse(context, pair.getResp()));
				return;
			}
		}
		msgLogger.info(request);
		msgLogger.info(SEP_LINE);
		throw new RuntimeException("Can not match request: [" + request.getTopLine() + "]");
	}
}
	
	
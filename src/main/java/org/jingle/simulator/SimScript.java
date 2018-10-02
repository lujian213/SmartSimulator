package org.jingle.simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.jingle.simulator.util.SimLogger;
import org.jingle.simulator.util.SimUtils;
import org.jingle.simulator.util.ZipFileVisitor;
import org.jingle.simulator.util.ZipFileVisitor.EntryWrapper;
import org.jingle.simulator.util.ZipFileVisitor.ZipFileVisitorHandler;

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
	
	private static final Logger msgLogger = Logger.getLogger("messageLogger");
	
	private static final String SEP_LINE = "------------------------------------------------------------------"; 
	private static final String RESP_START = "HTTP/"; 
	public static final String SCRIPT_EXT = ".sim"; 
	public static final String SCRIPT_ZIP = ".zip"; 
	public static final String INIT_FILE = "init.properties"; 
	public static final String PROP_NAME_SIMULATOR_CLASS = "simulator.class"; 
	public static final String PROP_NAME_SIMULATOR_NAME = "simulator.name"; 
	public static final String PROP_NAME_SIMULATOR_AUTOSTART = "simulator.autostart"; 
	
	private Logger scriptLogger = null;

	private List<TemplatePair> templatePairs = new ArrayList<>();
	private Map<String, SimScript> subScripts = new HashMap<>();
	private Properties props = new Properties();
	
	public SimScript(SimScript parent, File file) throws IOException {
		props.putAll(parent.getProps());
		templatePairs.addAll(parent.getTemplatePairs());
		loadFolder(file, true); 
	}
	
	public SimScript(SimScript parent, final ZipFile zf) throws IOException {
		props.putAll(parent.getProps());
		templatePairs.addAll(parent.getTemplatePairs());
		ZipFileVisitorHandler<SimScript> handler = new ZipFileVisitorHandler<SimScript>() {

			private EntryWrapper<SimScript> parent;
			@Override
			public void setParent(EntryWrapper<SimScript> parent) {
				this.parent = parent;
			}

			@Override
			public EntryWrapper<SimScript> handleDir(ZipEntry entry) {
				SimScript sim = new SimScript(parent.getTarget());
				File file = new File(entry.getName());
				subScripts.put(file.getName(), sim);
				return new EntryWrapper<SimScript>(entry, sim);
			}

			@Override
			public void handleFile(ZipEntry entry) throws IOException {
				String name = new File(entry.getName()).getName();
				if (name.equals(INIT_FILE)) {
					SimLogger.getLogger().info("loadint init file [" + name + "] in [" + parent.getEntry() + "]");
					try (InputStream is = zf.getInputStream(entry)) {
						Properties p = new Properties();
						p.load(is);
						parent.getTarget().getProps().putAll(p);
					}

				} else if (name.endsWith(SCRIPT_EXT)) {
					SimLogger.getLogger().info("loading script file [" + name + "] in [" + parent.getEntry() + "]");
					try (BufferedReader reader = new BufferedReader(new InputStreamReader(zf.getInputStream(entry), "utf-8"))) {
						List<TemplatePair> pairList = load(reader); 
						parent.getTarget().templatePairs.addAll(0, pairList);
					}
				}
			}
		};
		ZipFileVisitor<SimScript> visitor = new ZipFileVisitor<SimScript>(zf, this, handler);
		visitor.visit();
	}

	public SimScript(File file) throws IOException {
		loadFolder(file, false); 
	}
	
	SimScript(SimScript parent) {
		props.putAll(parent.getProps());
		templatePairs.addAll(parent.getTemplatePairs());
	}
	
	protected SimScript() {
		
	}

	public boolean isValid() {
		if (getSimulatorName() == null) {
			return false;
		}
		return true;
	}
	public void prepareLogger() {
		String simulatorName = this.getSimulatorName();

		this.scriptLogger = Logger.getLogger(simulatorName);
		String appenderName = simulatorName + "_file";
		if (scriptLogger.getAppender(appenderName) == null) {
			RollingFileAppender appender = new org.apache.log4j.RollingFileAppender();
			appender.setName(simulatorName + "_file");
			PatternLayout layout = new org.apache.log4j.PatternLayout();
			layout.setConversionPattern("%d{yyyy-MM-dd HH:mm:ss,SSS} [%24F:%-4L:%-5p][%x] -%m%n");
			appender.setLayout(layout);
			try {
				appender.setFile("logs/" + simulatorName + ".log", true, true, 500);
			} catch (IOException e) {
			}
			appender.activateOptions();	
			scriptLogger.addAppender(appender);
		}
		for (SimScript subScript: subScripts.values()) {
			subScript.scriptLogger = scriptLogger;
		}
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
		
		List<File> fileList = Arrays.asList(files);
		Collections.sort(fileList, new Comparator<File>() {
			@Override
			public int compare(File file1, File file2) {
				int n1 = file1.isFile() ? 0 : 1;
				int n2 = file2.isFile() ? 0 : 1;
				return n1 - n2;
			}
		});
		
		int total = 0;
		for (File file: fileList) {
			if (file.isFile()) {
				if (INIT_FILE.equals(file.getName())) {
					SimLogger.getLogger().info("loadint init file [" + file.getName() + "] in [" + folder + "]");
					try (InputStream is = new FileInputStream(file)) {
						Properties p = new Properties();
						p.load(is);
						props.putAll(p);
					}
				} else {
					SimLogger.getLogger().info("loading script file [" + file + "] in [" + folder + "]");
					try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
						List<TemplatePair> pairList = load(reader); 
						templatePairs.addAll(0, pairList);
						total += pairList.size();
					}
				}
			} else {
				subScripts.put(file.getName(), new SimScript(this, file));
			}
		}
		SimLogger.getLogger().info("Total " + total + " req/resp pairs loaded in [" + folder + "]");
	}
	
	public Properties getProps() {
		return props;
	}
	
	public String getProperty(String propName) {
		return props.getProperty(propName);
	}
	
	public String getMandatoryProperty(String propName, String errMsg) {
		String ret = props.getProperty(propName);
		if (ret == null) {
			throw new RuntimeException(errMsg);
		}
		return ret;
	}

	public int getMandatoryIntProperty(String propName, String errMsg) {
		String ret = props.getProperty(propName);
		if (ret == null) {
			throw new RuntimeException(errMsg);
		}
		return Integer.parseInt(ret);
	}

	public boolean getProperty(String propName, boolean defaultValue) {
		String ret = props.getProperty(propName);
		if (ret == null) {
			return defaultValue;
		}
		return Boolean.parseBoolean(ret);
	}

	public String getProperty(String propName, String defaultValue) {
		return props.getProperty(propName, defaultValue);
	}

	public int getProperty(String propName, int defaultValue) {
		String ret = props.getProperty(propName);
		if (ret == null) {
			return defaultValue;
		}
		return Integer.parseInt(ret);
	}

	@SuppressWarnings("unchecked")
    public Class<? extends SimSimulator> getSimulatorClass() throws ClassNotFoundException {
		return (Class<? extends SimSimulator>) Class.forName(props.getProperty(PROP_NAME_SIMULATOR_CLASS));
	}

	public String getSimulatorName() {
		return props.getProperty(PROP_NAME_SIMULATOR_NAME);
	}

	protected List<TemplatePair> load(BufferedReader reader) throws IOException {
		List<TemplatePair> pairList = new ArrayList<>();
		List<List<String>> block = null;
		int count = 0;
		while ((block = loadReqResp(reader)) != null) {
			pairList.add(new TemplatePair(new SimRequestTemplate(SimUtils.concatContent(block.get(0))), 
					new SimResponseTemplate(SimUtils.concatContent(block.get(1)))));
			count++;
		}
		SimLogger.getLogger().info(count + " req/resp pairs loaded");
		return pairList;
	}
	
	public List<TemplatePair> getTemplatePairs() {
		return this.templatePairs;
	}
	
	public Logger getLogger() {
		return this.scriptLogger;
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
	
	public Map<String, SimScript> getSubScripts() {
		return subScripts;
	}
	
	public void genResponse(SimRequest request) throws IOException {
		for (TemplatePair pair: templatePairs) {
			Map<String, Object> context = pair.getReq().match(request);
			if (context != null) {
				SimLogger.getLogger().info("match with template: [" + pair.getReq().getTopLineTemplate() + "]");
				Map<String, Object> allContext = new HashMap<>();
				for (Map.Entry<Object, Object> entry: props.entrySet()) {
					allContext.put((String) entry.getKey(), entry.getValue());
				}
				allContext.putAll(context);
				request.fillResponse(new SimResponse(allContext, pair.getResp()));
				return;
			}
		}
		msgLogger.info(request);
		msgLogger.info(SEP_LINE);
		throw new IOException("Can not match request: [" + request.getTopLine() + "]");
	}
}
	
	
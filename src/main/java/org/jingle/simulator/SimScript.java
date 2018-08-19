package org.jingle.simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	private static final String SEP_LINE = "------------------------------------------------------------------"; 
	private static final String RESP_START = "HTTP/"; 
	private static final String SCRIPT_EXT = ".sim"; 
	List<TemplatePair> templatePairs = new ArrayList<>();
	Map<String, SimScript> subScripts = new HashMap<>();
	
	public SimScript(File file) throws IOException {
		loadFolder(file);
	}
	
	protected SimScript() {
		
	}
	
	protected void loadFolder(File folder) throws IOException {
		if (!folder.exists() || !folder.isDirectory()) {
			throw new IOException("no folder [" + folder + "] exists");
		}
		File[] files = folder.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				if (file.isFile() && file.getName().endsWith(SCRIPT_EXT) || file.isDirectory()) {
					return true;
				}
				return false;
			}
		});
		int total = 0;
		for (File file: files) {
			if (file.isFile()) {
				logger.info("loading script file [" + file + "]");
				try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
					total += load(reader);
				}
			} else {
				subScripts.put(file.getName(), new SimScript(file));
			}
		}
		logger.info("Total " + total + " req/resp pairs loaded");
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
			for (String eachLine: lines) {
				sb.append(eachLine).append("\n");
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
				request.fillRsponse(context, pair.getResp());
				return;
			}
		}
		throw new RuntimeException("Can not match request: [" + request.getTopLine() + "]");
	}
}
	
	
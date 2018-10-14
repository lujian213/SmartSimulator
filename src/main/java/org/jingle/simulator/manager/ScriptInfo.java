package org.jingle.simulator.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jingle.simulator.SimScript;
import org.jingle.simulator.SimScript.TemplatePair;

public class ScriptInfo {
	List<TemplatePairInfo> templatePairs = new ArrayList<>();
	List<ScriptInfo> subScripts = new ArrayList<>();
	Properties props = new Properties();

	public ScriptInfo(SimScript script) {
		this.props = script.getConfigAsProperties();
		for (SimScript s: script.getSubScripts().values()) {
			subScripts.add(new ScriptInfo(s));
		}
		for (TemplatePair pair: script.getTemplatePairs()) {
			this.templatePairs.add(new TemplatePairInfo(pair));
		}
	}

	public List<TemplatePairInfo> getTemplatePairs() {
		return templatePairs;
	}

	public List<ScriptInfo> getSubScripts() {
		return subScripts;
	}

	public Properties getProps() {
		return props;
	}
	
	
}

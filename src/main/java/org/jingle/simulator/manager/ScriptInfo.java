package org.jingle.simulator.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jingle.simulator.SimScript;

public class ScriptInfo {
	String name;
	List<TemplatePairInfo> templatePairs = new ArrayList<>();
	List<ScriptInfo> subScripts = new ArrayList<>();
	Properties props = new Properties();

	public ScriptInfo(String name, SimScript script) {
		this.name = name;
		this.props = script.getConfigAsProperties();
		script.getSubScripts().forEach((key, value)->subScripts.add(new ScriptInfo(key, value)));
		script.getTemplatePairs().forEach((pair) ->this.templatePairs.add(new TemplatePairInfo(pair)));
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

	public String getName() {
		return name;
	}
}

package io.github.lujian213.simulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.lujian213.simulator.util.SimLogger;

public class SimRegexTemplate extends SimTemplate {
	private Pattern pattern;
	
	public SimRegexTemplate(String templateContent) throws IOException {
		super(templateContent);
	}
	
	@Override
	protected void init() throws IOException {
		String regexStr = templateContent.substring(1, templateContent.length() - 1);
		this.pattern = Pattern.compile(regexStr);
	}

	public Map<String, Object> parse(String content) {
		if (content == null)
			return null;
		Matcher matcher = pattern.matcher(content);
		if (matcher.matches()) {
			return getAllGroups(matcher);
		} else {
			return null;
		}
	}
	
	protected Map<String, Object> getAllGroups(Matcher matcher) {
		try {
			Map<String, Object> ret = new HashMap<>();
			Set<String> names = getGroupNames(pattern);
			for (String name: names) {
				ret.put(name, matcher.group(name));
			}
			return ret;
		} catch (Exception e) {
			SimLogger.getLogger().error("error when get all regex groups", e);
			return null;
		}
	}
	
	@Override
	public Map<String, Object> parse(BufferedReader reader) throws IOException {
		throw new IOException("Not supported function");
	}

	@SuppressWarnings("unchecked")
	private Set<String> getGroupNames(Pattern regex) throws Exception {
		Method namedGroupsMethod = Pattern.class.getDeclaredMethod("namedGroups");
		namedGroupsMethod.setAccessible(true);

		Map<String, Integer> namedGroups = null;
		namedGroups = (Map<String, Integer>) namedGroupsMethod.invoke(regex);
		return new HashSet<>(namedGroups.keySet());
	}
}

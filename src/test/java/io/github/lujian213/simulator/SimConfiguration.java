package io.github.lujian213.simulator;

import org.apache.commons.configuration2.PropertiesConfiguration;

public class SimConfiguration {
	public static void main(String[] args) {
		PropertiesConfiguration config = new PropertiesConfiguration();
		config.setProperty("abc", "123a");
		System.out.println(config.getString("abcd"));
	}
}

package io.github.lujian213.simulator.util;

import java.util.Properties;

public interface SimContextAwareness {
	public void init(String simulatorName, Properties props);
}

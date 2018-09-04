package org.jingle.simulator.webbit;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jingle.simulator.util.SimLogger;

public class WebbitWSHandlerBundle {
	private Map<String, WebbitWSHandler> bundles = new HashMap<>();
    
    public void addHandler(WebbitWSHandler handler) {
    	bundles.put(handler.getName(), handler);
    }
    public void sendMessage(String channel, String message) {
    	WebbitWSHandler handler = bundles.get(channel);
    	
    	if (handler != null) {
    		handler.sendMessage(message);
    	} else {
    		SimLogger.getLogger().error("can not find proper channel [" + channel + "] to send message");
    	}
    }
}
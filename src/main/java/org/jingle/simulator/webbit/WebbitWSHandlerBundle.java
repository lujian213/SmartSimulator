package org.jingle.simulator.webbit;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class WebbitWSHandlerBundle {
	private static final Logger logger = Logger.getLogger(WebbitWSHandlerBundle.class);

	private Map<String, WebbitWSHandler> bundles = new HashMap<>();
    
    public void addHandler(WebbitWSHandler handler) {
    	bundles.put(handler.getName(), handler);
    }
    public void sendMessage(String channel, String message) {
    	WebbitWSHandler handler = bundles.get(channel);
    	
    	if (handler != null) {
    		handler.sendMessage(message);
    	} else {
    		logger.error("can not find proper channel [" + channel + "] to send message");
    	}
    }
}
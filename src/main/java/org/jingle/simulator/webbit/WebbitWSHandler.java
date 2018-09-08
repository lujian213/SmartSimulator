package org.jingle.simulator.webbit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jingle.simulator.SimScript;
import org.jingle.simulator.util.SimLogger;
import org.jingle.simulator.util.SimUtils;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebSocketConnection;

public class WebbitWSHandler extends BaseWebSocketHandler {
	private final static String TYPE_OPEN = "OPEN";
	private final static String TYPE_CLOSE = "CLOSE";
	private final static String TYPE_MESSAGE = "MESSAGE";
	private static Map<String, WebSocketConnection> bundles = new HashMap<>();
    
    private String channel;
    private SimScript script;
    
    public WebbitWSHandler(String channel, SimScript script) {
    	this.channel = channel;
    	this.script = script;
    }

    public String getChannel() {
		return channel;
	}
    
	public void onOpen(WebSocketConnection connection) {
		SimLogger.setLogger(script.getLogger());
		WebbitWSSimRequest request = new WebbitWSSimRequest(connection, channel, TYPE_OPEN, null);
    	try {
	    	script.genResponse(request);
    	} catch (Exception e) {
    		SimLogger.getLogger().error("error when open WS [" + channel + "]", e);
    	} finally {
    		handleIDChange(request);
    	}
    }

    public void onClose(WebSocketConnection connection) {
		SimLogger.setLogger(script.getLogger());
    	try {
	    	WebbitWSSimRequest request = new WebbitWSSimRequest(connection, channel, TYPE_CLOSE, null);
	    	script.genResponse(request);
    	} catch (Exception e) {
    		SimLogger.getLogger().error("error when close WS [" + channel + "]", e);
    	} finally {
    		removeConnection(connection);
    	}
    }

    public void onMessage(WebSocketConnection connection, String message) {
		SimLogger.setLogger(script.getLogger());
		WebbitWSSimRequest request = new WebbitWSSimRequest(connection, channel, TYPE_MESSAGE, message);
    	try {
	    	script.genResponse(request);
    	} catch (Exception e) {
    		SimLogger.getLogger().error("error when handle message in WS [" + channel + "]", e);
    	} finally {
    		handleIDChange(request);
    	}
    }


    protected static void addConnection(String name, WebSocketConnection connection) {
    	SimLogger.getLogger().info("add connection [" + name + "]");
    	synchronized (bundles) {
    		bundles.put(name, connection);
    	}
    }
    
    protected static void removeConnection(WebSocketConnection connection) {
    	synchronized (bundles) {
    		Iterator<Map.Entry<String, WebSocketConnection>> it = bundles.entrySet().iterator();
    		while (it.hasNext()) {
    			Map.Entry<String, WebSocketConnection> entry = it.next();
    			if (entry.getValue() == connection) {
    				it.remove();
    			}
    		}
    	}
    }
    
    protected void handleIDChange(WebbitWSSimRequest request) {
    	String headerLine = request.getHeaderLine(WebbitWSSimRequest.HEADER_NAME_CHANNEL_ID);
    	if (headerLine != null) {
    		Map.Entry<String, String> entry = SimUtils.parseHeaderLine(headerLine);
    		SimLogger.getLogger().info("ID change to " + entry.getValue());
        	addConnection(entry.getValue(), request.getConnection());
    	}
    }

    public static void sendMessage(String name, String message) {
    	WebSocketConnection conn = null;
    	synchronized (bundles) {
    		conn = bundles.get(name);
    	}
    	
    	if (conn != null) {
    		conn.send(message);
    	} else {
    		SimLogger.getLogger().error("can not find proper connection [" + name + "] to send message");
    	}
    }

}
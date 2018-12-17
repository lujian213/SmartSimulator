package org.jingle.simulator.webbit;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jingle.simulator.SimResponse;
import org.jingle.simulator.SimScript;
import org.jingle.simulator.SimSimulator;
import org.jingle.simulator.SimulatorListener;
import org.jingle.simulator.http.HTTPSimulator;
import org.jingle.simulator.util.ReqRespConvertor;
import org.jingle.simulator.util.SimLogger;
import org.jingle.simulator.util.SimUtils;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebSocketConnection;

public class WebbitWSHandler extends BaseWebSocketHandler {
	static class ConnectionWithDelegator {
		private String id;
		private WebSocketConnection connection;
		private WebbitWSClient delegator;
		
		public ConnectionWithDelegator(WebSocketConnection connection) {
			this.connection = connection;
		}

		public ConnectionWithDelegator(WebSocketConnection connection, WebbitWSClient delegator) {
			this.connection = connection;
			this.delegator = delegator;
		}
		
		public WebSocketConnection getConnection() {
			return connection;
		}

		public WebbitWSClient getDelegator() {
			return delegator;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public void setDelegator(WebbitWSClient delegator) {
			this.delegator = delegator;
		}
	}
	
	private final static String TYPE_OPEN = "OPEN";
	private final static String TYPE_CLOSE = "CLOSE";
	private final static String TYPE_MESSAGE = "MESSAGE";
	private static List<ConnectionWithDelegator> bundles = new ArrayList<>();
    
	private WebbitSimulator simulator;
	private SimulatorListener simulatorListener;
	private String channel;
    private SimScript script;
	protected boolean proxy;
	protected String proxyURL;
	private ReqRespConvertor convertor;
    
    public WebbitWSHandler(WebbitSimulator simulator, SimulatorListener simulatorListener, String channel, SimScript script) {
    	this.simulator = simulator;
    	this.simulatorListener = simulatorListener;
    	this.channel = channel;
    	this.script = script;
    	this.convertor = SimUtils.createMessageConvertor(script, new DefaultWebbitWSReqRespConvertor());
		this.proxy = script.getConfig().getBoolean(SimSimulator.PROP_NAME_PROXY, false);
		if (proxy) {
			proxyURL = script.getMandatoryProperty(HTTPSimulator.PROP_NAME_PROXY_URL, "no proxy url defined");
		}
    }

    public String getChannel() {
		return channel;
	}
    
    @Override
	public void onOpen(WebSocketConnection connection) {
    	SimUtils.setThreadContext(script);
		SimLogger.getLogger().info("on open ...");
		WebbitWSSimRequest request = new WebbitWSSimRequest(connection, channel, TYPE_OPEN, null, convertor);
		List<SimResponse> respList = new ArrayList<>();
    	try {
    		respList = script.genResponse(request);
	    	addConnection(new ConnectionWithDelegator(connection));
    	} catch (Exception e) {
			if (proxy) {
				try {
			    	URL pURL = new URL(proxyURL);
			    	URI uri = new URI("http".equalsIgnoreCase(pURL.getProtocol()) ? "ws" : "wss", null, pURL.getHost(), pURL.getPort(), connection.httpRequest().uri(), null, null);
			    	SimLogger.getLogger().info("proxy url: " + uri);
					WebbitWSClient wsClient = new WebbitWSClient(uri, connection, script);
					wsClient.start();
			    	addConnection(new ConnectionWithDelegator(connection, wsClient));
					respList.add(new SimResponse("Unknown due to proxy mechanism"));
				} catch (IOException | URISyntaxException e1) {
					SimLogger.getLogger().error("proxy error", e1);
				}
			} else {
				SimLogger.getLogger().error("error when open WS [" + channel + "]", e);
			}
    	} finally {
    		handleIDChange(request);
    		simulatorListener.onHandleMessage(simulator.getName(), request, respList, !respList.isEmpty());
    	}
    }

	@Override
    public void onClose(WebSocketConnection connection) {
		SimUtils.setThreadContext(script);
		WebbitWSSimRequest request = new WebbitWSSimRequest(connection, channel, TYPE_CLOSE, null, convertor);
		List<SimResponse> respList = new ArrayList<>();
    	try {
	    	respList = script.genResponse(request);
    	} catch (IOException e) {
			if (proxy) {
				WebbitWSClient wsClient = findDelegator(connection);
				wsClient.stop();
				respList.add(new SimResponse("Unknown due to proxy mechanism"));
			} else {
				SimLogger.getLogger().error("error when close WS [" + channel + "]", e);
			}
    	} finally {
    		removeConnection(connection);
    		connection.close();
    		simulatorListener.onHandleMessage(simulator.getName(), request, respList, !respList.isEmpty());
    	}
    }

    @Override
    public void onMessage(WebSocketConnection connection, String message) {
    	onMessage(connection, message.getBytes());
    }

	@Override
    public void onMessage(WebSocketConnection connection, byte[] message) {
		SimUtils.setThreadContext(script);

		WebbitWSSimRequest request = new WebbitWSSimRequest(connection, channel, TYPE_MESSAGE, message, convertor);
		List<SimResponse> respList = new ArrayList<>();
    	try {
	    	respList = script.genResponse(request);
    	} catch (Exception e) {
			if (proxy) {
				WebbitWSClient wsClient = findDelegator(connection);
				wsClient.send(message);
				respList.add(new SimResponse("Unknown due to proxy mechanism"));
			} else {
	    		SimLogger.getLogger().error("error when handle message in WS [" + channel + "]", e);
			}
    	} finally {
    		handleIDChange(request);
    		simulatorListener.onHandleMessage(simulator.getName(), request, respList, !respList.isEmpty());
    	}
    }


    protected static void addConnection(ConnectionWithDelegator cwd) {
    	synchronized (bundles) {
    		bundles.add(cwd);
    	}
    }
    
    public static void closeAllConnections() {
    	synchronized (bundles) {
    		for (ConnectionWithDelegator cwb: bundles) {
    			cwb.getConnection().close();
    			if (cwb.getDelegator() != null) {
    				cwb.getDelegator().stop();;
    			}
    		}
    	}
    }

    protected static void updateConnection(String id, WebSocketConnection connection) {
    	SimLogger.getLogger().info("update connection [" + id + "]");
    	synchronized (bundles) {
    		for (ConnectionWithDelegator cwb: bundles) {
    			if (connection == cwb.getConnection()) {
    				cwb.setId(id);
    			}
    		}
    	}
    }

    protected static WebbitWSClient findDelegator(WebSocketConnection connection) {
    	synchronized (bundles) {
    		for (ConnectionWithDelegator cwb: bundles) {
    			if (connection == cwb.getConnection()) {
    				return cwb.getDelegator();
    			}
    		}
    	}
    	SimLogger.getLogger().warn("can not find related delegator for connection [" + connection + "]");
    	return null;
    }

    public static WebSocketConnection findConnection(String id) {
    	synchronized (bundles) {
    		for (ConnectionWithDelegator cwb: bundles) {
    			if (id.equals(cwb.getId())) {
    				return cwb.getConnection();
    			}
    		}
    	}
    	SimLogger.getLogger().warn("can not find connection with id [" + id + "]");
    	return null;
    }

    protected static void removeConnection(WebSocketConnection connection) {
    	synchronized (bundles) {
    		Iterator<ConnectionWithDelegator> it = bundles.iterator();
    		while (it.hasNext()) {
    			ConnectionWithDelegator cwd = it.next();
    			if (cwd.getConnection() == connection) {
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
        	updateConnection(entry.getValue(), request.getConnection());
    	}
    }
}
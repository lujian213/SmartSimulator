package io.github.lujian213.simulator.webbit;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebSocketConnection;

import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.SimScript;
import io.github.lujian213.simulator.SimulatorListener;
import io.github.lujian213.simulator.util.ReqRespConvertor;
import io.github.lujian213.simulator.util.SimLogger;
import io.github.lujian213.simulator.util.SimUtils;
import io.github.lujian213.simulator.webbit.WebbitWSHandler.ConnectionBundle.ConnectionWithDelegator;
import static io.github.lujian213.simulator.webbit.WebbitSimulatorConstants.*;

public class WebbitWSHandler extends BaseWebSocketHandler {
	static class ConnectionBundle {
		class ConnectionWithDelegator {
			private Object id;
			private WebSocketConnection connection;
			private WebbitWSClient delegator;
			
			public ConnectionWithDelegator(WebSocketConnection connection) {
				this.connection = connection;
				this.id = UNKNOWN;
			}

			public ConnectionWithDelegator(WebSocketConnection connection, WebbitWSClient delegator) {
				this.connection = connection;
				this.delegator = delegator;
				this.id = UNKNOWN;
			}
			
			public WebSocketConnection getConnection() {
				return connection;
			}

			public WebbitWSClient getDelegator() {
				return delegator;
			}

			public Object getId() {
				return id;
			}

			public void setId(String id) {
				if (this.id == UNKNOWN) {
					this.id = id;
					synchronized(connectionMap) {
						unknownConnectionList.remove(this);
						addConnection(this);
					}
				}
			}

			public void setDelegator(WebbitWSClient delegator) {
				this.delegator = delegator;
			}
			
			public void close() {
				this.connection.close();
				if (delegator != null) {
					delegator.stop();
				}
			}
		}
		
		private final static Object UNKNOWN = new Object();
		private final static String ALL = "_ALL_";
		private Map<Object, List<ConnectionWithDelegator>> connectionMap = new HashMap<>();
		private List<ConnectionWithDelegator> unknownConnectionList = new ArrayList<>();
		
		public ConnectionBundle() {
			connectionMap.put(UNKNOWN, unknownConnectionList);
		}
		
		public void addConnection(ConnectionWithDelegator conn) {
			synchronized (connectionMap) {
				List<ConnectionWithDelegator> connList = connectionMap.get(conn.getId());
				if (connList == null) {
					connList = new ArrayList<>();
					connectionMap.put(conn.getId(), connList);
				}
				connList.add(conn);
			}
		}

		public void removeConnection(ConnectionWithDelegator conn) {
			synchronized (connectionMap) {
				List<ConnectionWithDelegator> connList = connectionMap.get(conn.getId());
				if (connList != null) {
					connList.remove(conn);
					if (connList.isEmpty()) {
						connectionMap.remove(conn.getId());
					}
				}
			}
		}

		public ConnectionWithDelegator findConnection(WebSocketConnection connection) {
			ConnectionWithDelegator ret = null;
			synchronized (connectionMap) {
				for (List<ConnectionWithDelegator> connList: connectionMap.values()) {
					for (ConnectionWithDelegator conn: connList) {
						if (conn.getConnection() == connection) {
							ret = conn;
							break;
						}
					}
				}
			}
			return ret;
		}
		
		public List<ConnectionWithDelegator> findConnections(String id) {
			if (id.startsWith(ALL)) {
				List<ConnectionWithDelegator> list = new ArrayList<>();
				synchronized (connectionMap) {
					for (List<ConnectionWithDelegator> item : connectionMap.values()) {
						list.addAll(item);
					}
				}
				return list;
			} else {
				synchronized (connectionMap) {
					List<ConnectionWithDelegator> list = connectionMap.get(id);
					if (list == null)
						return null;
					return new ArrayList<>(list);
				}
			}
		}

		public void close() {
			synchronized (connectionMap) {
				for (List<ConnectionWithDelegator> connList: connectionMap.values()) {
					for (ConnectionWithDelegator conn: connList) {
						conn.close();
					}
				}
			}
		}
	}
	
	private final static String TYPE_OPEN = "OPEN";
	private final static String TYPE_CLOSE = "CLOSE";
	private final static String TYPE_MESSAGE = "MESSAGE";
	private ConnectionBundle bundle = new ConnectionBundle();
    
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
		this.proxy = script.getBooleanProperty(PROP_NAME_PROXY, false);
		if (proxy) {
			proxyURL = script.getMandatoryProperty(PROP_NAME_PROXY_URL, "no proxy url defined");
		}
    }

    public String getChannel() {
		return channel;
	}
    
    @Override
	public void onOpen(WebSocketConnection connection) {
    	SimUtils.setThreadContext(script);
		SimLogger.getLogger().info("on open ...");
		WebbitWSSimRequest request = new WebbitWSSimRequest(this, connection, channel, TYPE_OPEN, null, convertor);
		List<SimResponse> respList = new ArrayList<>();
    	try {
    		respList = script.genResponse(request);
    		SimUtils.logIncomingMessage(request.getRemoteAddress(), simulator.getName(), request);
	    	bundle.addConnection(bundle.new ConnectionWithDelegator(connection));
    	} catch (Exception e) {
			if (proxy) {
				try {
			    	URL pURL = new URL(proxyURL);
			    	URI uri = new URI("http".equalsIgnoreCase(pURL.getProtocol()) ? "ws" : "wss", null, pURL.getHost(), pURL.getPort(), connection.httpRequest().uri(), null, null);
			    	SimLogger.getLogger().info("proxy url: " + uri);
					WebbitWSClient wsClient = new WebbitWSClient(uri, connection, script);
					wsClient.start();
					bundle.addConnection(bundle.new ConnectionWithDelegator(connection, wsClient));
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
		WebbitWSSimRequest request = new WebbitWSSimRequest(this, connection, channel, TYPE_CLOSE, null, convertor);
		List<SimResponse> respList = new ArrayList<>();
		ConnectionWithDelegator conn = bundle.findConnection(connection);
    	try {
    		SimUtils.logIncomingMessage(request.getRemoteAddress(), simulator.getName(), request);
	    	respList = script.genResponse(request);
    	} catch (IOException e) {
			if (proxy) {
				WebbitWSClient wsClient = conn.getDelegator();
				wsClient.stop();
				respList.add(new SimResponse("Unknown due to proxy mechanism"));
			} else {
				SimLogger.getLogger().error("error when close WS [" + channel + "]", e);
			}
    	} finally {
    		bundle.removeConnection(conn);
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

		WebbitWSSimRequest request = new WebbitWSSimRequest(this, connection, channel, TYPE_MESSAGE, message, convertor);
		List<SimResponse> respList = new ArrayList<>();
		ConnectionWithDelegator conn = bundle.findConnection(connection);
    	try {
    		SimUtils.logIncomingMessage(request.getRemoteAddress(), simulator.getName(), request);
	    	respList = script.genResponse(request);
    	} catch (Exception e) {
			if (proxy) {
				WebbitWSClient wsClient = conn.getDelegator();
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

    protected void handleIDChange(WebbitWSSimRequest request) {
    	String headerLine = request.getHeaderLine(HEADER_NAME_CHANNEL_ID);
    	if (headerLine != null) {
    		Map.Entry<String, String> entry = SimUtils.parseHeaderLine(headerLine);
    		SimLogger.getLogger().info("ID change to " + entry.getValue());
    		ConnectionWithDelegator conn = bundle.findConnection(request.getConnection());
    		conn.setId(entry.getValue() + "@" + channel);
    	}
    }
    
    public void close() {
    	bundle.close();
    }
    
    public void sendResponse(String channel, SimResponse resp) throws IOException {
    	List<ConnectionWithDelegator> connList = bundle.findConnections(channel);
    	if (connList == null || connList.isEmpty()) {
    		throw new IOException("there is no such channel named [" + channel + "]");
    	}
    	for (ConnectionWithDelegator conn : connList) {
    		convertor.fillRawResponse(conn.getConnection(), resp);
    	}
    }
}
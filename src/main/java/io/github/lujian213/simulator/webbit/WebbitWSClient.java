package io.github.lujian213.simulator.webbit;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.netty.WebSocketClient;

import io.github.lujian213.simulator.SimScript;
import io.github.lujian213.simulator.util.SimLogger;
import io.github.lujian213.simulator.util.SimUtils;
import static io.github.lujian213.simulator.http.HTTPSimulatorConstants.*;

public class WebbitWSClient extends BaseWebSocketHandler {
	private Object lock = new Object();
	
	private URI uri;
	private WebSocketConnection delegation;
	private WebSocketConnection connection;
	private boolean useSSL = false;
	private String keystore;
	private String passwd;
	private WebSocketClient wsClient;
	private SimScript script;
	
	public WebbitWSClient(URI uri, WebSocketConnection delegation, SimScript script)  {
		this.uri = uri;
		this.delegation = delegation;
		this.script = script;
		useSSL = script.getConfig().getBoolean(PROP_NAME_USE_SSL, false);
		if (useSSL) {
			keystore = script.getMandatoryProperty(PROP_NAME_KEYSTORE, "no keystore defined");
			passwd = script.getMandatoryProperty(PROP_NAME_KS_PASSWD, "no keystore passwd defined");
		}
	}
	
	protected WebSocketConnection getConnection() {
		synchronized (lock) {
			while(true) {
				if (connection != null)
					return connection;
				else {
					try {
						lock.wait();
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}

	public void send(byte[] msg) {
		getConnection().send(msg);
	}
	
	public void start() throws IOException {
		wsClient = new WebSocketClient(uri, this);
		if (useSSL) {
			try (InputStream is = new FileInputStream(keystore)){
				wsClient.setupSsl(is, passwd);
			} 
		}
		try {
			wsClient.start().get();
		} catch (ExecutionException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void stop() {
		getConnection().close();
		try {
			wsClient.stop().get();
		} catch (InterruptedException | ExecutionException e) {
			SimLogger.getLogger().error("error when stop ws client", e);
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}
		((ExecutorService)wsClient.getExecutor()).shutdown();

	}
	
	@Override
	public void onOpen(WebSocketConnection connection) throws Exception {
		SimUtils.setThreadContext(script);
		SimLogger.getLogger().info("proxy connection established");
		synchronized (lock) {
			this.connection = connection;
			lock.notifyAll();
		}
	}

	@Override
	public void onClose(WebSocketConnection connection) throws Exception {
		SimUtils.setThreadContext(script);
		SimLogger.getLogger().info("proxy close");
		if (connection != null) {
			connection.close();
		}
	}

	@Override
	public void onMessage(WebSocketConnection connection, String msg) throws Throwable {
		SimUtils.setThreadContext(script);
		SimLogger.getLogger().info("proxy message:[" + msg + "]");
		delegation.send(msg);
	}
}

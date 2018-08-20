package org.jingle.simulator.webbit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.BasicConfigurator;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;

import org.jingle.simulator.SimRequest;
import org.jingle.simulator.SimScript;
import org.jingle.simulator.SmartSimulator;

/*
 * a simple static http server
*/
public class WebbitSimulator extends SmartSimulator implements HttpHandler {
	private WebServer webServer;
	private boolean useSSL = false;
	private InputStream keystore;
	private String keystorePasswd;
	
	public WebbitSimulator(int port, String name, File folder) throws IOException {
		super(port, name, folder);
	}
	
	protected WebbitSimulator() {
		super();
	}
	
	protected void gen500Response(HttpResponse resp, String errorMsg) {
		try {
			byte[] response = errorMsg.getBytes();
			resp.content(response).status(500).end();
		} catch (Exception e) {
			logger.error("error when write 500 error response", e);
		}
	}
	
	@Override
	public void handleHttpRequest(HttpRequest req, HttpResponse resp, HttpControl ctrl) throws Exception {
		try {
			SimRequest request = new WebbitSimRequest(req, resp);
			logger.info("incoming request: [" + request.getTopLine() + "]");
			script.genResponse(request);
		} catch (Exception e) {
			logger.error("", e);
			gen500Response(resp, e.getMessage());
			
		}
		
	}
  
	@Override
	public void start() throws IOException {
		webServer = WebServers.createWebServer(port);
		WebbitWSHandlerBundle bundle = new WebbitWSHandlerBundle();
		for (Map.Entry<String, SimScript> entry: this.script.getSubScripts().entrySet()) {
			String channelName = "/" + reformatChannelName(entry.getKey());
			webServer.add(channelName, new WebbitWSHandler(channelName, entry.getValue(), bundle));
		}
		webServer.add(this);
		
		if (useSSL) {
			try {
				webServer.setupSsl(keystore, keystorePasswd);
			} finally {
				try {
					if (keystore != null) {
						keystore.close();
					}
				} catch (Exception e) {
				}
			}
		}
        webServer.start();
        logger.info("Simulator [" + name + "] running at " + webServer.getUri());
	}
	
	protected String reformatChannelName(String channelName) {
		return channelName.replaceAll("\\.", "/");
	}

	@Override
	public void setSSL(InputStream keystore, String passwd) throws IOException {
		useSSL = true;
		this.keystore = keystore;
		this.keystorePasswd = passwd;
		
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        try {
	        SSLContext sc = SSLContext.getInstance("SSL");
	        sc.init(null, trustAllCerts, new java.security.SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
        	throw new RuntimeException(e);
        }
	}

	public static void main(String[] args) throws Exception {
		BasicConfigurator.configure();
		args = new String[] {"demo", "scripts/websocket", "10080"};
		File folder = new File(args[1]);
		int port = Integer.parseInt(args[2]);
		WebbitSimulator inst = new WebbitSimulator(port, args[0], folder);
//		inst.setSSL(new FileInputStream("c:/temp/keystore.jks"), "password");

		inst.start();
	}
}
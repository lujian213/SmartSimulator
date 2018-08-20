package org.jingle.simulator.simple;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.BasicConfigurator;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import org.jingle.simulator.SimRequest;
import org.jingle.simulator.SmartSimulator;

/*
 * a simple static http server
*/
@SuppressWarnings("restriction")
public class SimpleSimulator extends SmartSimulator implements HttpHandler {
	private HttpServer server;
	private boolean useSSL = false;
	private SSLContext sslContext = null;
	
	public SimpleSimulator(int port, String name, File folder) throws IOException {
		super(port, name, folder);
	}
  
	@Override
	public void handle(HttpExchange exchange) {
		try {
			SimRequest request = new SimpleSimRequest(exchange);
			logger.info("incoming request: [" + request.getTopLine() + "]");
			script.genResponse(request);
		} catch (Exception e) {
			logger.error("", e);
			gen500Response(exchange, e.getMessage());

		}

	}

	protected void gen500Response(HttpExchange exchange, String errorMsg) {
		try {
			byte[] response = errorMsg.getBytes();
			exchange.sendResponseHeaders(500, response.length);
			try (OutputStream os = exchange.getResponseBody()) {
				os.write(response);
			}
		} catch (Exception e) {
			logger.error("error when write 500 error response", e);
		}
	}

	@Override
	public void start() throws IOException {
		if (useSSL) {
			server = HttpsServer.create(new InetSocketAddress(port), 0);
		} else {
			server = HttpServer.create(new InetSocketAddress(port), 0);
		}
		server.createContext("/", this);
		server.setExecutor(null); // creates a default executor
		if (useSSL) {
			((HttpsServer) server).setHttpsConfigurator(new HttpsConfigurator(sslContext) {
				@Override
				public void configure(HttpsParameters params) {
	
					// get the remote address if needed
					InetSocketAddress remote = params.getClientAddress();
					SSLContext c = getSSLContext();
	
					// get the default parameters
					SSLParameters sslparams = c.getDefaultSSLParameters();
	
					params.setSSLParameters(sslparams);
				}
			});
		}
		server.start();
		String address = (useSSL ? "https://" : "http://") + InetAddress.getLocalHost().getHostName() + ":" + port;
		logger.info("Simulator [" + name + "] running at " + address);
	}

    protected SSLContext initSSL(InputStream keystore, String passwd) throws IOException {
    	SSLContext sslContext = null;
        logger.info("Start initializing SSL");

        try {
            logger.info("Initializing SSL from built-in default certificate");
           
            char[] passphrase = passwd.toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(keystore, passphrase);
           
            logger.info("SSL certificate loaded");
           
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, passphrase);
            logger.info("Key manager factory is initialized");

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);
            logger.info("Trust manager factory is initialized");

            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            logger.info("SSL context is initialized");
        } catch (Exception ioe) {
            logger.error("error when init SSLContext", ioe);
            throw new IOException(ioe);
        } finally {
        	if (keystore != null) {
        		try {
        			keystore.close();
        		} catch (IOException e) {
        		}
        	}
        }
        return sslContext;
    }
   
	public void setSSL(InputStream keystore, String passwd) throws IOException {
		useSSL = true;
		sslContext = initSSL(keystore, passwd);
	}

	
	public static void main(String[] args) throws Exception {
		BasicConfigurator.configure();
		args = new String[] {"Echo", "scripts/SimpleEcho", "8000"};
		File folder = new File(args[1]);
		int port = Integer.parseInt(args[2]);
		SimpleSimulator inst = new SimpleSimulator(port, args[0], folder);
//		inst.setSSL(new FileInputStream("c:/temp/keystore.jks"), "password");
		inst.start();
	}
	

}
package io.github.lujian213.simulator.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.core.MediaType;

import org.apache.velocity.VelocityContext;
import org.codehaus.groovy.control.CompilationFailedException;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import io.github.lujian213.simulator.SimResponseTemplate;

public interface ResponseHandler {
	public static final String HEADER_NAME_CONTENT_TYPE = "Content-Type";
	static ResponseHandlerChain inst = new ResponseHandlerChain(
			new BridgeResponseHandler(),
			new FunctionResponseHandler(),
			new DefaultResponseHandler()
			);
			
	public byte[] handle(Map<String, Object> headers, VelocityContext vc, SimResponseTemplate resp) throws IOException;
	
	public static ResponseHandler getHandlerChain() {
		return inst;
	}
	
	static class ResponseHandlerChain implements ResponseHandler {
		List<ResponseHandler> handlerList = new ArrayList<>();
		
		public ResponseHandlerChain(ResponseHandler ... handlers) {
			for (ResponseHandler handler: handlers) {
				handlerList.add(handler);
			}
		}
		
		public void addHandler(ResponseHandler handler) {
			if (handlerList.size() > 0) {
				handlerList.add(handlerList.size() - 1, handler);
			} else {
				handlerList.add(handler);
			}
		}
		
		@Override
		public byte[] handle(Map<String, Object> headers, VelocityContext vc, SimResponseTemplate resp) throws IOException {
			for (ResponseHandler handler: handlerList) {
				byte[] ret = handler.handle(headers, vc, resp);
				if (ret != null) {
					return ret;
				}
			}
			return null;
		}
	}
	
	static class DefaultResponseHandler implements ResponseHandler {
		public static final String CONTEXT_NAME_SIMUTILS = "SimUtils";

		@Override
		public byte[] handle(Map<String, Object> headers, VelocityContext vc, SimResponseTemplate resp) throws IOException {
			vc.put(CONTEXT_NAME_SIMUTILS, SimUtils.class);
			return SimUtils.mergeResult(vc, "body", resp.getBody()).getBytes();
		}
	}

	static class BridgeResponseHandler implements ResponseHandler {
		public static final String HEADER_NAME_BRIDGE = "_Bridge";
		public static final String HEADER_NAME_BRIDGE_TYPE = "_Bridge.Type";
		public static final String BRIDGE_TYPE_VM = "VM";
		public static final String BRIDGE_TYPE_GROOVY = "Groovy";
		public static final String CONTEXT_NAME_SIMUTILS = "SimUtils";
		private MimetypesFileTypeMap map;

		public BridgeResponseHandler() {
			map = (MimetypesFileTypeMap) MimetypesFileTypeMap.getDefaultFileTypeMap();
			map.addMimeTypes("text/css css");
			map.addMimeTypes("application/javascript js");
		}
		
		@Override
		public byte[] handle(Map<String, Object> headers, VelocityContext vc, SimResponseTemplate resp) throws IOException {
			String bridge = (String) headers.remove(HEADER_NAME_BRIDGE);
			String bridgeType = (String) headers.remove(HEADER_NAME_BRIDGE_TYPE);
			if (bridge != null) {
				String contentType = (String) headers.get(HEADER_NAME_CONTENT_TYPE);
				byte[] bodyBytes = handleBridgeRequest(bridge);
				if (contentType == null) {
					headers.put(HEADER_NAME_CONTENT_TYPE, map.getContentType(bridge));
				}
				if (BRIDGE_TYPE_VM.equals(bridgeType)) {
					vc.put(CONTEXT_NAME_SIMUTILS, SimUtils.class);
					return SimUtils.mergeResult(vc, "body", bodyBytes).getBytes();
				} else if (BRIDGE_TYPE_GROOVY.equals(bridgeType)) {
					Binding binding = new Binding();
					vc.put(CONTEXT_NAME_SIMUTILS, SimUtils.class);
					binding.setVariable("context", vc);
					GroovyShell shell = new GroovyShell(binding);
					try {
						Object ret = shell.evaluate(new URI(bridge));
						bodyBytes = (ret == null) ? null : ret.toString().getBytes();
					} catch (CompilationFailedException | URISyntaxException e) {
						throw new IOException(e);
					}
				}
				return bodyBytes;
			}
			return null;
		}
		
		protected byte[] handleBridgeRequest(String urlStr) throws IOException {
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				URL url = new URL(urlStr);
				URLConnection conn = url.openConnection();
				conn.connect();
				try (BufferedInputStream bis = new BufferedInputStream(conn.getInputStream())) {
					byte[] buffer = new byte[8 * 1024];
					int count = -1;
					while ((count = bis.read(buffer)) != -1) {
						baos.write(buffer, 0, count);
					}
				}
				return baos.toByteArray();
			}
		}
	}

	static class FunctionResponseHandler implements ResponseHandler {
		public static final String HEADER_NAME_CLASS = "_Class.Name";
		public static final String HEADER_NAME_METHOD = "_Method.Name";
		
		@Override
		public byte[] handle(Map<String, Object> headers, VelocityContext vc, SimResponseTemplate resp) throws IOException {
			String className = (String) headers.remove(HEADER_NAME_CLASS);
			String methodName = (String) headers.remove(HEADER_NAME_METHOD);
			String contentType = (String) headers.get(HEADER_NAME_CONTENT_TYPE);
			if (className != null) {
				for (Map.Entry<String, Object> entry: headers.entrySet()) {
					vc.put(entry.getKey(), entry.getValue());
				}
				Object result = BeanRepository.getInstance().invoke(className, methodName, vc);
				if (result != null) {
					if (contentType != null && contentType.contains(MediaType.APPLICATION_JSON)) {
						FunctionBean bean = BeanRepository.getInstance().addBean(className, vc);
						return bean.getContext().getObjectMapper().writeValueAsBytes(result);
					} else if (MediaTypeHelper.isText(contentType) && result instanceof String) {
						return ((String)result).getBytes();
					} else {
						throw new RuntimeException("unsupport Content-Type [" + contentType + "] with result [" + result + "]");
					}
				} else {
					return new byte[0];
				}
			}
			return null;
		}
	}

}


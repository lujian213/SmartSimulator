package org.jingle.simulator.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class SimUtils {
    static VelocityEngine ve = new VelocityEngine();
    static {
    	ve.init();
    }

	
	public static String decodeURL(String url) throws UnsupportedEncodingException {
		String[] parts = url.split("\\?");
		StringBuffer sb = new StringBuffer(parts[0]);
		String queryStr = parts.length > 1 ? parts[1] : null;
		if (queryStr != null) {
			sb.append("?");
			final String[] pairs = queryStr.split("&");
			int count = 1;
			for (String pair : pairs) {
				final int idx = pair.indexOf("=");
				final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
				if (count > 1) {
					sb.append("&");
				}
				sb.append(key).append("=");
				final String value = idx > 0 && pair.length() > idx + 1
						? URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
						: null;
				if (value != null) {
					sb.append(value);
				}
				count++;
			}
		}
		return sb.toString();
	}

	public static String mergeResult(VelocityContext context, String tagName, String templateStr) throws IOException {
		try (StringWriter writer = new StringWriter()) {
			ve.evaluate(context, writer, tagName, templateStr);
			return writer.toString();
		}
	}

	public static String formatString(String format, String ... args) {
		StringWriter sbw = new StringWriter();
		try (PrintWriter pw = new PrintWriter(sbw)) {
			pw.printf(format, args);
		}
		return sbw.toString();
	}
}

package io.github.lujian213.simulator;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import io.github.lujian213.simulator.util.ReqRespConvertor;

public interface SimRequest {
	public static final String HTTP1_1 = "HTTP/1.1";
	
	public List<String> getAllHeaderNames();

	public String getHeaderLine(String header);

	public String getAutnenticationLine();

	public String getBody();
	
	default public byte[] getRawBodyAsBytes() {
		String body = getBody();
		if (body != null) {
			return body.getBytes();
		} else {
			return null;
		}
	}

	public String getTopLine();

	public void fillResponse(SimResponse response) throws IOException;
	
	public ReqRespConvertor getReqRespConvertor();
	
	default public void print(PrintWriter pw) {
		pw.println(getTopLine());
		for (String header: getAllHeaderNames()) {
			pw.println(getHeaderLine(header));
		}
		pw.println(getBody());
		pw.flush();
	}
	
	public default String getRemoteAddress() {
		return "UNKNOWN";
	}
}

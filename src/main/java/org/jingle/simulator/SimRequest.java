package org.jingle.simulator;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import org.jingle.simulator.util.ReqRespConvertor;

public interface SimRequest {
	public List<String> getAllHeaderNames();

	public String getHeaderLine(String header);

	public String getAutnenticationLine();

	public String getBody();

	public String getTopLine();

	public void fillResponse(SimResponse response) throws IOException; 
	
	public ReqRespConvertor getReqRespConvertor();
	
	default void print() {
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out));
		
		pw.println(getTopLine());
		for (String header: getAllHeaderNames()) {
			pw.println(getHeaderLine(header));
		}
		pw.println(getBody());
		pw.flush();
	}
}

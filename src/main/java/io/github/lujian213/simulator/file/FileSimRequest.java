package io.github.lujian213.simulator.file;

import static io.github.lujian213.simulator.file.FileSimulatorConstants.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.lujian213.simulator.AbstractSimRequest;
import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.util.ReqRespConvertor;
import io.github.lujian213.simulator.util.SimUtils;

public class FileSimRequest extends AbstractSimRequest {
	private File file;
	private ReqRespConvertor convertor;
	private String body;

	public FileSimRequest(File file, ReqRespConvertor convertor) throws IOException {
		this.file = file;
		this.convertor = convertor;
		while(!file.renameTo(file)) {
	        // Cannot read from file, windows still working on it.
	        try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
	    }
		genBody();
	}

	protected FileSimRequest() {

	}

	@Override
	public ReqRespConvertor getReqRespConvertor() {
		return this.convertor;
	}

	protected void genBody() throws IOException {
		this.body = convertor.rawRequestToBody(file);
	}

	public String getTopLine() {
		return file.getAbsolutePath().replaceAll("\\\\","/");
	}

	public String getHeaderLine(String header) {
		return null;
	}

	public String getAuthenticationLine() {
		return null;
	}

	public String getBody() {
		return this.body;
	}

	@Override
	protected void doFillResponse(SimResponse response) throws IOException {
		Map<String, Object> respHeaders = response.getHeaders();
		String file = (String) respHeaders.get(HEADER_NAME_FILE_NAME);
		File outputFile = SimUtils.str2File(file);
		if (outputFile == null) {
			throw new IOException("output file name [" + file + "] is not valid");
		}
		convertor.fillRawResponse(outputFile, response);
	}

	@Override
	public List<String> getAllHeaderNames() {
		return new ArrayList<>();
	}

	@Override
	public String getRemoteAddress() {
		return null;
	}
}

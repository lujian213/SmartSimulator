package io.github.lujian213.simulator.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.util.ReqRespConvertor;

public class DefaultFileReqRespConvertor implements ReqRespConvertor {

	@Override
	public String rawRequestToBody(Object rawRequest) throws IOException {
		return FileUtils.readFileToString((File) rawRequest, "utf-8");
	}

	@Override
	public void fillRawResponse(Object rawResponse, SimResponse simResponse) throws IOException {
		File file = (File) rawResponse;
		try (FileWriter writer = new FileWriter(file)) {
			writer.write(simResponse.getBodyAsString());
		}
	}
}

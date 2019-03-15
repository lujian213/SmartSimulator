package io.github.lujian213.simulator.kafka;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;

import io.github.lujian213.simulator.AbstractSimRequest;
import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.SimScript;
import io.github.lujian213.simulator.util.ReqRespConvertor;
import io.github.lujian213.simulator.util.SimUtils;
import static io.github.lujian213.simulator.kafka.KafkaSimulatorConstants.*;

public class KafkaSimRequest extends AbstractSimRequest {
	private static final String HEADER_LINE_FORMAT = "%s: %s";
	private ConsumerRecord<?, ?> record;
	private String unifiedEndpointName;
	private String topLine;
	private Map<String, Object> headers = new HashMap<>();
	private String body;
	private ReqRespConvertor convertor;
	private SimScript script;
	
	public KafkaSimRequest(SimScript script, ConsumerRecord<?, ?> record, String unifiedEndpointName, ReqRespConvertor convertor) throws IOException {
		this.script = script;
		this.record = record;
		this.unifiedEndpointName = unifiedEndpointName;
		this.convertor = convertor;
		this.topLine = record.topic();
		genHeaders();
		genBody();
	}
	
	protected KafkaSimRequest() {
		
	}
	
	@Override
	public ReqRespConvertor getReqRespConvertor() {
		return this.convertor;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.topLine).append("\n");
		for (Map.Entry<String, Object> entry: headers.entrySet()) {
			sb.append(this.getHeaderLine(entry.getKey())).append("\n");
		}
		sb.append("\n");
		if (body != null) {
			sb.append(body);
		}
		sb.append("\n");
		return sb.toString();
	}

	protected void genHeaders() throws IOException {
		for (Header header: record.headers()) {
			String name = header.key();
			String value = new String(header.value());
			headers.put(name, value);
		}
		headers.put(HEADER_NAME_MESSGAE_KEY, record.key());
	}
	
	protected void genBody() throws IOException {
		this.body = convertor.rawRequestToBody(record);
	}
	
	public String getTopLine() {
		return this.topLine;
	}
	
	public String getHeaderLine(String header) {
		Object value = headers.get(header);
		return SimUtils.formatString(HEADER_LINE_FORMAT, header, value == null ? "" : value.toString());
	}
	
	public String getAutnenticationLine() {
		return null;
	}
	
	public String getBody() {
		return this.body;
	}
	
	@Override
	protected void doFillResponse(SimResponse response) throws IOException {
		Map<String, Object> respHeaders = response.getHeaders();
		String channel = (String) respHeaders.get(HEADER_NAME_CHANNEL);
		if (channel == null) {
			respHeaders.put(HEADER_NAME_CHANNEL, unifiedEndpointName);
		}
		String simulatorName = (String) respHeaders.get(PROP_NAME_RESPONSE_TARGETSIMULATOR);
		if (simulatorName == null) {
			respHeaders.put(PROP_NAME_RESPONSE_TARGETSIMULATOR, script.getSimulatorName());
		}
	}
	
	@Override
	public List<String> getAllHeaderNames() {
		return new ArrayList<>(headers.keySet());
	}

	@Override
	public String getRemoteAddress() {
		if (this.record != null) {
			return this.record.topic();
		} else {
			return super.getRemoteAddress();
		}
	}
}

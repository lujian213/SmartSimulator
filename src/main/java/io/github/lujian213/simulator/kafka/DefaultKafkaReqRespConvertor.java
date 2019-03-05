package io.github.lujian213.simulator.kafka;

import java.io.IOException;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;

import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.util.ReqRespConvertor;

public class DefaultKafkaReqRespConvertor implements ReqRespConvertor {

	@Override
	public String rawRequestToBody(Object rawRequest) throws IOException {
		return ((ConsumerRecord<?, ?>)rawRequest).value().toString();
	}

	@Override
	public void fillRawResponse(Object rawResponse, SimResponse simResponse) throws IOException {
		String topic = (String) simResponse.getHeaders().get(KafkaSimulator.HEADER_NAME_MESSGAE_TOPIC);
		String key = (String) simResponse.getHeaders().get(KafkaSimulator.HEADER_NAME_MESSGAE_KEY);
		ProducerRecord<?, ?> record = new ProducerRecord<>(topic, key, simResponse.getBodyAsString());
		((Object[])rawResponse)[0] = record;
	}
}

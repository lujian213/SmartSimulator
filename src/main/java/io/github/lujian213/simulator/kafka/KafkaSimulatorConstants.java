package io.github.lujian213.simulator.kafka;

import io.github.lujian213.simulator.SimSimulatorConstants;

public class KafkaSimulatorConstants extends SimSimulatorConstants {
	public static final String PROP_NAME_BOOTSTRAP_SERVERS = "simulator.kafka.bootstrap.servers";
	public static final String PROP_NAME_BROKER_NAME = "simulator.kafka.broker.name";
	public static final String PROP_NAME_ENDPOINT_BROKER = "simulator.kafka.endpoint.broker";
	public static final String PROP_NAME_ENDPOINT_NAME = "simulator.kafka.endpoint.name";
	public static final String PROP_NAME_ENDPOINT_TYPE = "simulator.kafka.endpoint.type";
	public static final String PROP_NAME_ENDPOINT_PROXY = "simulator.kafka.endpoint.proxy";
	public static final String PROP_NAME_ENDPOINT_PROXY_CHANNEL = "simulator.kafka.endpoint.proxy.channel";
	public static final String PROP_NAME_PUBLISHER_TOPIC = "simulator.kafka.publisher.topic";
	public static final String PROP_NAME_SUBSCRIBER_TOPICS = "simulator.kafka.subscriber.topics";
	public static final String PROP_NAME_SUBSCRIBER_POLLINTERVAL = "simulator.kafka.subscriber.pollinterval";

	public static final String HEADER_NAME_MESSGAE_KEY = "_Message.key";
	public static final String HEADER_NAME_MESSGAE_TOPIC = "_Message.topic";
}

package io.github.lujian213.simulator.jms;

import io.github.lujian213.simulator.SimSimulatorConstants;

public class JMSSimulatorConstants extends SimSimulatorConstants {
	public static final String PROP_NAME_DESTINATION_TYPE = "simulator.jms.destination.type";
	public static final String PROP_NAME_DESTINATION_BROKER = "simulator.jms.destination.broker";
	public static final String PROP_NAME_DESTINATION_NAME = "simulator.jms.destination.name";
	public static final String PROP_NAME_CLIENT_TYPE = "simulator.jms.client.type";
	public static final String PROP_NAME_DESTINATION_PROXY = "simulator.jms.destination.proxy";
	public static final String PROP_NAME_DESTINATION_PROXY_CHANNEL = "simulator.jms.destination.proxy.channel";
	public static final String PROP_NAME_TOPIC_FACTORY_NAME = "simulator.jms.topicconnectionfactory";
	public static final String PROP_NAME_QUEUE_FACTORY_NAME = "simulator.jms.queueconnectionfactory";
	public static final String PROP_NAME_FACTORY_USERNAME = "simulator.jms.factory.username";
	public static final String PROP_NAME_FACTORY_PASSWORD = "simulator.jms.factory.password";
	public static final String PROP_NAME_BROKER_NAME = "simulator.jms.broker.name";

	public static final String HEADER_NAME_MESSAGE_TYPE = "_Message.Type";
}

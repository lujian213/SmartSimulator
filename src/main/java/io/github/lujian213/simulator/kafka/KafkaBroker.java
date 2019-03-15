package io.github.lujian213.simulator.kafka;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;

import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.SimScript;
import io.github.lujian213.simulator.util.ReqRespConvertor;
import io.github.lujian213.simulator.util.SimLogger;
import io.github.lujian213.simulator.util.SimUtils;
import static io.github.lujian213.simulator.kafka.KafkaSimulatorConstants.*;

public class KafkaBroker {
	public static final String PROP_NAME_BOOTSTRAP_SERVERS = "simulator.kafka.bootstrap.servers";
	public static final String PROP_NAME_BROKER_NAME = "simulator.kafka.broker.name";

	public class KafkaPublisher {
		public static final String PROP_NAME_PUBLISHER_TOPIC = "simulator.kafka.publisher.topic";
		private String name;
		private SimScript script;
		private String topic;
		private KafkaProducer<?, ?> producer = null;
		
		public KafkaPublisher(SimScript script) {
			this.script = script;
			String localName = script.getMandatoryProperty(PROP_NAME_ENDPOINT_NAME, "no publisher name defined");
			this.name = SimUtils.createUnifiedName(localName, KafkaBroker.this.name);
			this.topic = script.getMandatoryProperty(PROP_NAME_PUBLISHER_TOPIC, "no publisher topic defined");
		}

		public String getName() {
			return name;
		}

		public SimScript getScript() {
			return script;
		}

		public synchronized void start() throws IOException {
			if (producer == null) {
				Properties props = script.getConfigAsProperties();
				props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaBroker.this.bootstrapServers);
				producer = new KafkaProducer<>(props);
				SimLogger.getLogger().warn("producer [" + name + "] is started");
			} else {
				SimLogger.getLogger().warn("producer [" + name + "] already started");
			}
		}
		
		public synchronized void stop() {
			if (producer != null) {
				producer.close();
				SimLogger.getLogger().warn("producer [" + name + "] is stopped");
				producer = null;
			} else {
				SimLogger.getLogger().warn("producer [" + name + "] already stopped");
			}
		}
		
		public void sendMessage(SimResponse response) throws IOException {
			Object respMsg = new Object[1];
			response.getHeaders().put(HEADER_NAME_MESSGAE_TOPIC, topic);
			convertor.fillRawResponse(respMsg, response);
			ProducerRecord record = (ProducerRecord<?, ?>) ((Object[])respMsg)[0];
			for (Map.Entry<String, Object> entry : response.getAllInternalHeaders().entrySet()) {
				record.headers().add(entry.getKey(), entry.getValue().toString().getBytes());
			}
			producer.send(record);
		}

		public void sendMessage(ConsumerRecord<?, ?> record) throws IOException {
			ProducerRecord pRecord = new ProducerRecord<>(topic, record.key(), record.value());
			for (Header header: record.headers()) {
				pRecord.headers().add(header);
			}
			producer.send(pRecord);
		}
	}
	
	public interface KafkaMessageListener {
		public void onMessage(ConsumerRecord<?, ?> record);
	}
	
	public class KafkaSubscriber {
		public static final String PROP_NAME_SUBSCRIBER_TOPICS = "simulator.kafka.subscriber.topics";
		public static final String PROP_NAME_SUBSCRIBER_POLLINTERVAL = "simulator.kafka.subscriber.pollinterval";
		private String name;
		private SimScript script;
		private List<String> topics = new ArrayList<>();
		private KafkaConsumer<?, ?> consumer = null;
		private transient boolean running = false;
		private long poolInterval;
		private boolean autoCommit = false;
		private Thread listenThread = null;
		private KafkaMessageListener listener;
		
		public KafkaSubscriber(SimScript script) {
			this.script = script;
			String localName = script.getMandatoryProperty(PROP_NAME_ENDPOINT_NAME, "no subscriber name defined");
			this.name = SimUtils.createUnifiedName(localName, KafkaBroker.this.name);
			String topicsStr = script.getMandatoryProperty(PROP_NAME_SUBSCRIBER_TOPICS, "no subscriber topics defined");
			Arrays.stream(topicsStr.split(",")).forEach((str)-> {
				if (!str.trim().isEmpty()) {
					topics.add(str);
				}
			});
			this.poolInterval = script.getConfig().getLong(PROP_NAME_SUBSCRIBER_POLLINTERVAL, 100);
			this.autoCommit = script.getConfig().getBoolean(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
		}

		public String getName() {
			return name;
		}

		public SimScript getScript() {
			return script;
		}
		
		public void setListener(KafkaMessageListener listener) {
			this.listener = listener;
		}

		public synchronized void start() throws IOException {
			if (consumer == null) {
				Properties props = script.getConfigAsProperties();
				props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaBroker.this.bootstrapServers);
				consumer = new KafkaConsumer<>(props);
				consumer.subscribe(topics);
				SimLogger.getLogger().warn("consumer [" + name + "] is started");
				listenThread = new Thread(new Runnable() {
					@Override
					public void run() {
						while (running) {
					         ConsumerRecords<?, ?> records = consumer.poll(Duration.ofMillis(poolInterval));
					         if (!autoCommit) {
					        	 consumer.commitSync();
					         }
					         for (ConsumerRecord<?, ?> record : records) {
					        	 listener.onMessage(record);
					         }
					     }
					}
				});
				running = true;
				listenThread.start();
			} else {
				SimLogger.getLogger().warn("consumer [" + name + "] already started");
			}

		}
		
		public synchronized void stop() {
			running = false;
			if (listenThread != null) {
				while (listenThread.isAlive()) {
					try {
						wait(100);
					} catch (InterruptedException e) {
					}
				}
			}
			
			if (consumer != null) {
				consumer.close();
				consumer = null;
				SimLogger.getLogger().warn("consumer [" + name + "] is stopped");
			} else {
				SimLogger.getLogger().warn("consumer [" + name + "] already stopped");
			}

		}
	}

	private ReqRespConvertor convertor;
	private Map<String, KafkaPublisher> publisherMap = new HashMap<>();
	private Map<String, KafkaSubscriber> subscriberMap = new HashMap<>();
	private String bootstrapServers = null;
	private String name = null;
	
	public KafkaBroker(SimScript script, ReqRespConvertor convertor) {
		this.convertor = convertor;
		this.bootstrapServers = script.getMandatoryProperty(PROP_NAME_BOOTSTRAP_SERVERS, "no " + PROP_NAME_BOOTSTRAP_SERVERS + " defined");
		this.name = script.getMandatoryProperty(PROP_NAME_BROKER_NAME, "no broker name defined");
	}
	
	public String getName() {
		return this.name;
	}
	
	public void addPublisher(KafkaPublisher publisher) {
		this.publisherMap.put(publisher.getName(), publisher);
	}
	
	public void addSubscriber(KafkaSubscriber subscriber) {
		this.subscriberMap.put(subscriber.getName(), subscriber);
	}
	
	public void start() throws IOException {
		try {
			for (KafkaPublisher pub: publisherMap.values()) {
				pub.start();
			}
			for (KafkaSubscriber sub: subscriberMap.values()) {
				sub.start();
			}
		} catch (Exception e) {
			stop();
			if (e instanceof IOException) {
				throw IOException.class.cast(e);
			} else {
				throw new IOException(e);
			}
		}
	}
	
	public void stop() {
		for (KafkaPublisher pub: publisherMap.values()) {
			pub.stop();
		}
		for (KafkaSubscriber sub: subscriberMap.values()) {
			sub.stop();
		}
	}
	
	public void sendMessage(String pubName, SimResponse response) throws IOException {
		KafkaPublisher pub = publisherMap.get(pubName);
		if (pub == null) {
			throw new IOException("no such publisher [" + pubName + "] exists");
		}
		pub.sendMessage(response);
	}

	public void sendMessage(String pubName, ConsumerRecord<?, ?> record) throws IOException {
		KafkaPublisher pub = publisherMap.get(pubName);
		if (pub == null) {
			throw new IOException("no such publisher [" + pubName + "] exists");
		}
		pub.sendMessage(record);
	}
}

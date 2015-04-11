package com.github.pfichtner.revoltusbautomationjava.mqtt;

import static java.lang.Boolean.parseBoolean;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.github.pfichtner.revoltusbautomationjava.message.Function;
import com.github.pfichtner.revoltusbautomationjava.message.Message.MessageBuilder;
import com.github.pfichtner.revoltusbautomationjava.message.Outlet;
import com.github.pfichtner.revoltusbautomationjava.message.State;
import com.github.pfichtner.revoltusbautomationjava.message.Trimmer;
import com.github.pfichtner.revoltusbautomationjava.usb.ClasspathDependentUsb;
import com.github.pfichtner.revoltusbautomationjava.usb.Usb;

public class MqttClient {

	@Option(name = "-brokerTopic", usage = "Topic to register. To switch outlets a message of the form $brokerTopic/$enumeratedName$NUM/value/set must be sent")
	private String brokerTopic = "home/devices/px1675/";

	@Option(name = "-enumeratedName", usage = "Name for the outlets. To switch outlets a message of the form $brokerTopic/$enumeratedName$NUM/value/set must be sent")
	private String enumeratedName = "outlet";

	@Option(name = "-brokerHost", usage = "Hostname of the broker to connect to")
	private String brokerHost = "localhost";

	@Option(name = "-brokerPort", usage = "Port of the broker to connect to")
	private int brokerPort = 1883;

	@Option(name = "-clientId", usage = "This client's name")
	private String clientId = "px1675";

	@Option(name = "-publishClientInfo", usage = "When set, publish messages on connect/disconnect under this topic")
	private String publishClientInfoTopic;

	// ---------------------------------------------------------------------------

	@Option(name = "-vendorId", usage = "VendorId of the revolt usb stick")
	private short vendorId = (short) 0xffff;

	@Option(name = "-productId", usage = "ProductId of the revolt usb stick")
	private short productId = 0x1122;

	private Integer rawFrames = 10;

	@Option(name = "-rawFrames", usage = "How many times the frame should be sent (3-255)")
	public void setRawFrames(int rawFrames) {
		if (rawFrames < 3 || rawFrames > 255) {
			throw new IllegalStateException("rawFrames must be 3-255");
		}
		this.rawFrames = rawFrames;
	}

	@Option(name = "-rawId", hidden = true, usage = "Fine tuning: RawId")
	private Integer rawId;

	@Option(name = "-msgFin", hidden = true, usage = "Fine tuning: MsgFin")
	private String msgFin;

	// ------------------------------------------------------------------------

	@Option(name = "--usbInterface", hidden = true, usage = "Fine tuning: InterfaceNum")
	private Integer interfaceNum;

	@Option(name = "--usbEndpoint", hidden = true, usage = "Fine tuning: OutEndpoint")
	private Byte outEndpoint;

	@Option(name = "--usbTimeout", hidden = true, usage = "usb send timeout in milliseconds")
	private Long timeout;

	private Pattern topicPattern;

	private org.eclipse.paho.client.mqttv3.MqttClient client;

	private class Callback implements MqttCallback {

		private final Usb usb;
		private final MessageBuilder messageBuilder;

		public Callback(Usb usb, MessageBuilder messageGenerator) {
			this.usb = usb;
			this.messageBuilder = messageGenerator;
		}

		public void connectionLost(Throwable cause) {
			do {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e1) {
					Thread.currentThread().interrupt();
				}
				try {
					connect();
					subscribe();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} while (!MqttClient.this.client.isConnected());
		}

		public void messageArrived(String topic, MqttMessage message)
				throws IOException {
			Matcher matcher = MqttClient.this.topicPattern.matcher(topic);
			if (matcher.matches()) {
				Integer outlet = tryParse(Trimmer.on('0')
						.trim(matcher.group(1)));
				boolean isAll = "ALL".equalsIgnoreCase(matcher.group(1));
				if (outlet != null || isAll) {
					Outlet[] outlets = isAll ? Outlet.all()
							: new Outlet[] { Outlet.of(outlet.intValue()) };
					State state = parseBoolean(new String(message.getPayload())) ? State.ON
							: State.OFF;
					this.usb.write(this.messageBuilder.build(
							Function.of(outlets, state)).asBytes());
				}
			}
		}

		public void deliveryComplete(IMqttDeliveryToken token) {
			// nothing to do
		}

	}

	public void setBrokerTopic(String brokerTopic) {
		this.brokerTopic = brokerTopic.endsWith("/") ? brokerTopic
				: brokerTopic + '/';
	}

	public void setPublishClientInfoTopic(String publishClientInfoTopic) {
		this.publishClientInfoTopic = publishClientInfoTopic;
	}

	public static void main(String[] args) throws MqttException,
			InterruptedException, IOException {
		new MqttClient().doMain(args);
	}

	protected Usb newUsb() throws IOException {
		Usb usb = ClasspathDependentUsb.newInstance(this.vendorId,
				this.productId);
		usb.connect();
		if (this.interfaceNum != null) {
			usb.setInterfaceNum(this.interfaceNum.intValue());
		}
		if (this.outEndpoint != null)
			usb.setOutEndpoint(this.outEndpoint.byteValue());
		if (this.timeout != null) {
			usb.setTimeout(TimeUnit.MILLISECONDS, this.timeout.longValue());
		}
		return usb;
	}

	private MessageBuilder newMessageGenerator() {
		MessageBuilder generator = new MessageBuilder();
		generator = this.rawId == null ? generator : generator
				.rawId(this.rawId);
		generator = this.rawFrames == null ? generator : generator
				.rawFrames(this.rawFrames);
		generator = this.msgFin == null ? generator : generator
				.msgFin(this.msgFin);
		return generator;
	}

	public void doMain(String... args) throws MqttException,
			InterruptedException, IOException {
		CmdLineParser cmdLineParser = new CmdLineParser(this);
		try {
			cmdLineParser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			cmdLineParser.printUsage(System.err);
			return;
		}

		// ensure brokerTopic is normalized
		setBrokerTopic(this.brokerTopic);
		this.topicPattern = Pattern.compile(this.brokerTopic
				+ this.enumeratedName + "(\\w+)/value/set");
		Usb usb = newUsb();
		MessageBuilder messageGenerator = newMessageGenerator();
		try {
			this.client = newClient(this.brokerHost, this.brokerPort,
					this.clientId);
			connect();
			try {
				subscribe();
				this.client.setCallback(new Callback(usb, messageGenerator));
				wait4ever();
			} finally {
				this.client.disconnect();
				this.client.close();
			}
		} finally {
			usb.close();
		}

	}

	private void connect() throws MqttSecurityException, MqttException {
		this.client.connect(mqttConnectOptions());
		publishClientStatus(Boolean.TRUE);
	}

	private void publishClientStatus(Boolean state) throws MqttException,
			MqttPersistenceException {
		if (publishClientStatus()) {
			this.client.publish(this.publishClientInfoTopic, state.toString()
					.getBytes(), 0, false);
		}
	}

	public void disconnect() throws MqttException {
		// "kill" the callback since it retries to reconnect
		this.client.setCallback(null);
		publishClientStatus(Boolean.FALSE);
		this.client.disconnect();
	}

	private MqttConnectOptions mqttConnectOptions() {
		MqttConnectOptions options = new MqttConnectOptions();
		String topic = this.publishClientInfoTopic;
		if (publishClientStatus()) {
			options.setWill(topic, Boolean.FALSE.toString().getBytes(), 0,
					false);
		}
		return options;
	}

	private boolean publishClientStatus() {
		return this.publishClientInfoTopic != null
				&& !this.publishClientInfoTopic.isEmpty();
	}

	private void subscribe() throws MqttException {
		this.client.subscribe(this.brokerTopic + '#');
	}

	private static Integer tryParse(String string) {
		try {
			return Integer.valueOf(string);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static void wait4ever() throws InterruptedException {
		Object blocker = new Object();
		synchronized (blocker) {
			blocker.wait();
		}
	}

	private org.eclipse.paho.client.mqttv3.MqttClient newClient(String host,
			int port, String clientId) throws MqttException,
			MqttSecurityException {
		return new org.eclipse.paho.client.mqttv3.MqttClient("tcp://" + host
				+ ":" + port, clientId);
	}

	public boolean isConnected() {
		return this.client != null && this.client.isConnected();
	}

}

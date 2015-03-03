package com.github.pfichtner.revoltusbautomationjava.mqtt;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.github.pfichtner.revoltusbautomationjava.message.Function;
import com.github.pfichtner.revoltusbautomationjava.message.Message.MessageBuilder;
import com.github.pfichtner.revoltusbautomationjava.message.Outlet;
import com.github.pfichtner.revoltusbautomationjava.message.State;
import com.github.pfichtner.revoltusbautomationjava.message.Trimmer;
import com.github.pfichtner.revoltusbautomationjava.usb.Usb;
import com.github.pfichtner.revoltusbautomationjava.usb.UsbUsb4Java;

public class MqttClient {

	@Option(name = "-brokerTopic")
	private String brokerTopic = "/home/automation/px1675";

	@Option(name = "-brokerHost")
	private String brokerHost = "localhost";

	@Option(name = "-brokerPort")
	private int brokerPort = 1883;

	@Option(name = "-clientId")
	private String clientId = "px1675";

	@Option(name = "-vendorId")
	private short vendorId = (short) 0xffff;

	@Option(name = "-productId")
	private short productId = 0x1122;

	private Integer rawFrames = 10;

	@Option(name = "-rawFrames", metaVar = "How many times the frame should be sent (3-255)")
	public void setRawFrames(int rawFrames) {
		if (rawFrames < 3 || rawFrames > 255) {
			throw new IllegalStateException("rawFrames must be 3-255");
		}
		this.rawFrames = rawFrames;
	}

	@Option(name = "-rawId")
	private Integer rawId;

	@Option(name = "-msgFin")
	private String msgFin;

	// ------------------------------------------------------------------------

	@Option(name = "--usbInterface", hidden = true)
	private Integer interfaceNum;

	@Option(name = "--usbEndpoint", hidden = true)
	private Byte outEndpoint;

	@Option(name = "--usbTimeout", hidden = true, metaVar = "usb send timeout in milliseconds")
	private Long timeout;

	private org.eclipse.paho.client.mqttv3.MqttClient client;

	public class Callback implements MqttCallback {

		private final Usb usb;
		private final MessageBuilder messageBuilder;

		public Callback(Usb usb, MessageBuilder messageGenerator) {
			this.usb = usb;
			this.messageBuilder = messageGenerator;
		}

		public void connectionLost(Throwable cause) {
			org.eclipse.paho.client.mqttv3.MqttClient client = MqttClient.this.client;
			do {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e1) {
					Thread.currentThread().interrupt();
				}
				try {
					client.connect();
					client.subscribe(MqttClient.this.brokerTopic);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} while (!client.isConnected());
		}

		public void messageArrived(String topic, MqttMessage message) throws IOException {
			String payload = new String(message.getPayload());
			String[] split = payload.split("\\=");
			if (split.length == 2) {
				Integer outlet = tryParse(Trimmer.on('0').trim(split[0]));
				boolean isAll = "ALL".equalsIgnoreCase(split[0]);
				if (outlet != null || isAll) {
					Outlet[] outlets = isAll ? Outlet.all()
							: new Outlet[] { Outlet.of(outlet.intValue()) };
					this.usb.write(this.messageBuilder.build(
							Function.of(outlets, State.forString(split[1])))
							.asBytes());
				}
			}
		}

		public void deliveryComplete(IMqttDeliveryToken token) {
			// nothing to do
		}

	}

	public void setBrokerTopic(String brokerTopic) {
		this.brokerTopic = brokerTopic;
	}

	public static void main(String[] args) throws MqttException,
			InterruptedException, IOException {
		new MqttClient().doMain(args);
	}

	protected Usb newUsb() {
		UsbUsb4Java usb = UsbUsb4Java.newInstance(this.vendorId, this.productId).connect();
		usb = this.interfaceNum == null ? usb : usb
				.interfaceNum(this.interfaceNum.intValue());
		usb = this.outEndpoint == null ? usb : usb.outEndpoint(this.outEndpoint
				.byteValue());
		usb = this.timeout == null ? usb : usb.timeout(TimeUnit.MILLISECONDS,
				this.timeout.longValue());
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

		Usb usb = newUsb();
		MessageBuilder messageGenerator = newMessageGenerator();
		try {
			this.client = connect(this.brokerHost, this.brokerPort,
					this.clientId);
			try {
				this.client.subscribe(this.brokerTopic);
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

	private org.eclipse.paho.client.mqttv3.MqttClient connect(String host,
			int port, String clientId) throws MqttException,
			MqttSecurityException {
		org.eclipse.paho.client.mqttv3.MqttClient client = new org.eclipse.paho.client.mqttv3.MqttClient(
				"tcp://" + host + ":" + port, clientId);
		client.connect();
		return client;
	}

	public boolean isConnected() {
		return this.client != null && this.client.isConnected();
	}

}

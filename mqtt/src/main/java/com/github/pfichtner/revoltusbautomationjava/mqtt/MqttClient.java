package com.github.pfichtner.revoltusbautomationjava.mqtt;

import static com.github.pfichtner.revoltusbautomationjava.message.Padder.leftPadder;

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
import com.github.pfichtner.revoltusbautomationjava.message.MessageGenerator;
import com.github.pfichtner.revoltusbautomationjava.message.Outlet;
import com.github.pfichtner.revoltusbautomationjava.message.State;
import com.github.pfichtner.revoltusbautomationjava.message.Trimmer;
import com.github.pfichtner.revoltusbautomationjava.usb.Usb;

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

	private String msgFin;

	@Option(name = "-msgFin")
	public void setMsgFin(String msgFin) {
		int length = msgFin.length();
		this.msgFin = leftPadder('0', length).pad(msgFin).substring(0, length);
	}

	// ------------------------------------------------------------------------

	@Option(name = "--usbInterface", hidden = true)
	private Integer interfaceNum;

	@Option(name = "--usbEndpoint", hidden = true)
	private Byte outEndpoint;

	@Option(name = "--usbTimeout", hidden = true, metaVar = "usb send timeout in milliseconds")
	private Long timeout;

	public static class Callback implements MqttCallback {

		private final Usb usb;
		private final MessageGenerator messageGenerator;

		public Callback(Usb usb, MessageGenerator messageGenerator) {
			this.usb = usb;
			this.messageGenerator = messageGenerator;
		}

		public void connectionLost(Throwable cause) {
			// nothing to do
		}

		public void messageArrived(String topic, MqttMessage message) {
			String payload = new String(message.getPayload());
			String[] split = payload.split("\\=");
			if (split.length == 2) {
				Integer outlet = tryParse(Trimmer.on('0').trim(split[0]));
				boolean isAll = "ALL".equalsIgnoreCase(split[0]);
				if (outlet != null || isAll) {
					Outlet[] outlets = isAll ? Outlet.all()
							: new Outlet[] { Outlet.of(outlet.intValue()) };
					this.usb.write(this.messageGenerator.bytesMessage(Function
							.of(outlets, State.forString(split[1]))));
				}
			}
		}

		private Integer tryParse(String string) {
			try {
				return Integer.valueOf(string);
			} catch (NumberFormatException e) {
				return null;
			}
		}

		public void deliveryComplete(IMqttDeliveryToken token) {
			// nothing to do
		}

	}

	public static void main(String[] args) throws MqttException,
			InterruptedException {
		new MqttClient().doMain(args);
	}

	private Usb newUsb() {
		Usb usb = Usb.newInstance(this.vendorId, this.productId).connect();
		usb = this.interfaceNum == null ? usb : usb
				.interfaceNum(this.interfaceNum.intValue());
		usb = this.outEndpoint == null ? usb : usb.outEndpoint(this.outEndpoint
				.byteValue());
		usb = this.timeout == null ? usb : usb.timeout(TimeUnit.MILLISECONDS,
				this.timeout.longValue());
		return usb;
	}

	private MessageGenerator newMessageGenerator() {
		MessageGenerator generator = new MessageGenerator();
		generator = this.rawId == null ? generator : generator
				.rawId(this.rawId);
		generator = this.rawFrames == null ? generator : generator
				.rawFrames(this.rawFrames);
		generator = this.msgFin == null ? generator : generator
				.msgFin(this.msgFin);
		return generator;
	}

	public void doMain(String[] args) throws MqttException,
			InterruptedException {
		CmdLineParser cmdLineParser = new CmdLineParser(this);
		try {
			cmdLineParser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			cmdLineParser.printUsage(System.err);
			return;
		}

		Usb usb = newUsb();
		MessageGenerator messageGenerator = newMessageGenerator();
		try {
			org.eclipse.paho.client.mqttv3.MqttClient client = connect(
					this.brokerHost, this.brokerPort, this.clientId);
			try {
				client.subscribe(this.brokerTopic);
				client.setCallback(new Callback(usb, messageGenerator));
				wait4ever();
			} finally {
				client.disconnect();
				client.close();
			}
		} finally {
			usb.close();
		}

	}

	private void wait4ever() throws InterruptedException {
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

}

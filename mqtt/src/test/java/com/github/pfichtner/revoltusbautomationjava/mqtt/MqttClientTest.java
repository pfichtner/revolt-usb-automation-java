package com.github.pfichtner.revoltusbautomationjava.mqtt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.dna.mqtt.moquette.server.Server;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.junit.Test;

import com.github.pfichtner.revoltusbautomationjava.message.Function;
import com.github.pfichtner.revoltusbautomationjava.message.Message.MessageBuilder;
import com.github.pfichtner.revoltusbautomationjava.message.Outlet;
import com.github.pfichtner.revoltusbautomationjava.message.State;
import com.github.pfichtner.revoltusbautomationjava.usb.Usb;

public class MqttClientTest {

	public static class ReceivedMessage {

		private final String topic;
		private final byte[] message;

		public ReceivedMessage(String topic, MqttMessage message) {
			this.topic = topic;
			this.message = message.getPayload();
		}

		public String getTopic() {
			return this.topic;
		}

		public byte[] getMessage() {
			return this.message;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(this.message);
			result = prime * result
					+ ((this.topic == null) ? 0 : this.topic.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ReceivedMessage other = (ReceivedMessage) obj;
			if (!Arrays.equals(this.message, other.message))
				return false;
			if (this.topic == null) {
				if (other.topic != null)
					return false;
			} else if (!this.topic.equals(other.topic))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ReceivedMessage [topic=" + this.topic + ", message="
					+ new String(this.message) + "]";
		}

	}

	@Test
	public void testSimple() throws IOException, MqttException,
			InterruptedException {
		Server broker = startBroker();
		final List<Exception> exceptions = new ArrayList<Exception>();
		final Usb mock = mock(Usb.class);

		Outlet outlet = Outlet.ONE;
		State state = State.ON;
		try {
			final MqttClient client = new MqttClient() {
				@Override
				protected Usb newUsb() {
					return mock;
				}
			};

			String topic = "/foo/bar";
			org.eclipse.paho.client.mqttv3.MqttClient mqttClient = mqttClient();

			try {
				startClientInBackground(exceptions, client, topic);
				switchOutlet(mqttClient, topic, outlet, state);
				TimeUnit.SECONDS.sleep(3);
			} finally {
				mqttClient.disconnect();
			}
		} finally {
			broker.stopServer();
		}

		assertTrue(exceptions.isEmpty());
		verify(mock).write(
				new MessageBuilder().build(Function.of(outlet, state))
						.asBytes());
		verifyNoMoreInteractions(mock);
	}

	@Test
	public void doesSendConnectDisconnectInfos() throws IOException,
			MqttException, InterruptedException {
		Server broker = startBroker();
		final List<Exception> exceptions = new ArrayList<Exception>();
		final Usb mock = mock(Usb.class);

		try {
			final MqttClient client = new MqttClient() {
				@Override
				protected Usb newUsb() {
					return mock;
				}
			};
			client.setPublishClientInfoTopic("clients/testclient");

			String topic = "/foo/bar";
			org.eclipse.paho.client.mqttv3.MqttClient mqttClient = mqttClient();
			mqttClient.subscribe("clients/testclient");

			List<ReceivedMessage> receivedMessages = new ArrayList<ReceivedMessage>();
			mqttClient.setCallback(collectMessages(receivedMessages));

			try {
				startClientInBackground(exceptions, client, topic);
				assertEquals(Arrays.asList(new ReceivedMessage(
						"clients/testclient",
						new MqttMessage("true".getBytes()))), receivedMessages);
				// ---------------------------------------------------------------------------------
				receivedMessages.clear();
				// ---------------------------------------------------------------------------------
				client.disconnect();
				assertEquals(Arrays.asList(new ReceivedMessage(
						"clients/testclient", new MqttMessage("false"
								.getBytes()))), receivedMessages);
			} finally {
				mqttClient.disconnect();
			}
		} finally {
			broker.stopServer();
		}

		assertTrue(exceptions.isEmpty());
	}

	private MqttCallback collectMessages(
			final List<ReceivedMessage> receivedMessages) {
		return new MqttCallback() {

			public void messageArrived(String topic, MqttMessage message)
					throws Exception {
				receivedMessages.add(new ReceivedMessage(topic, message));
			}

			public void deliveryComplete(IMqttDeliveryToken arg0) {
				// TODO Auto-generated method stub

			}

			public void connectionLost(Throwable arg0) {
				// TODO Auto-generated method stub

			}
		};
	}

	private void switchOutlet(
			org.eclipse.paho.client.mqttv3.MqttClient mqttClient, String topic,
			Outlet outlet, State state) throws MqttException,
			MqttPersistenceException {
		if (!topic.endsWith("/")) {
			topic += '/';
		}
		mqttClient.publish(topic + "outlet" + outlet.getIndex() + "/value/set",
				new MqttMessage(Boolean.valueOf(state == State.ON).toString()
						.getBytes()));
	}

	@Test
	public void testReconnect() throws InterruptedException, IOException,
			MqttSecurityException, MqttException {
		Server broker = startBroker();
		final List<Exception> exceptions = new ArrayList<Exception>();
		final Usb mock = mock(Usb.class);

		try {
			final MqttClient client = new MqttClient() {
				@Override
				protected Usb newUsb() {
					return mock;
				}
			};

			String topic = "/foo/bar";

			Outlet outlet = Outlet.ONE;
			State state = State.ON;

			startClientInBackground(exceptions, client, topic);

			broker.stopServer();
			broker = startBroker();

			org.eclipse.paho.client.mqttv3.MqttClient mqttClient = mqttClient();
			try {
				startClientInBackground(exceptions, client, topic);
				switchOutlet(mqttClient, topic, outlet, state);
				TimeUnit.SECONDS.sleep(3);
			} finally {
				mqttClient.disconnect();
			}

			verify(mock).write(
					new MessageBuilder().build(Function.of(outlet, state))
							.asBytes());
			verifyNoMoreInteractions(mock);

		} finally {
			broker.stopServer();
		}

		assertTrue(exceptions.isEmpty());
	}

	private Server startBroker() throws IOException {
		Server broker = new Server();
		broker.startServer();
		return broker;
	}

	private org.eclipse.paho.client.mqttv3.MqttClient mqttClient()
			throws MqttException, MqttSecurityException {
		org.eclipse.paho.client.mqttv3.MqttClient mqttClient = new org.eclipse.paho.client.mqttv3.MqttClient(
				"tcp://localhost:1883", "clientId");
		mqttClient.connect();
		return mqttClient;
	}

	private void startClientInBackground(final List<Exception> exceptions,
			final MqttClient client, final String brokerTopic)
			throws InterruptedException {
		new Thread() {
			{
				setDaemon(true);
				start();
			}

			@Override
			public void run() {
				try {
					client.setBrokerTopic(brokerTopic);
					client.doMain();
				} catch (MqttException e) {
					exceptions.add(e);
				} catch (InterruptedException e) {
					exceptions.add(e);
				} catch (IOException e) {
					exceptions.add(e);
				}
			}
		};
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		while (!client.isConnected()) {
			TimeUnit.MILLISECONDS.sleep(250);
			if (stopWatch.getTime() > TimeUnit.SECONDS.toMillis(5)) {
				throw new IllegalStateException(
						"Could not connect within 5 seconds");
			}
		}
	}

}

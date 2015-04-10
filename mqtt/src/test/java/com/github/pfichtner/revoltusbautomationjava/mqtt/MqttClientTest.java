package com.github.pfichtner.revoltusbautomationjava.mqtt;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.dna.mqtt.moquette.server.Server;
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

	private void switchOutlet(
			org.eclipse.paho.client.mqttv3.MqttClient mqttClient, String topic,
			Outlet outlet, State state) throws MqttException,
			MqttPersistenceException {
		if (!topic.endsWith("/")) {
			topic += '/';
		}
		String m = topic + "outlet" + outlet.getIndex() + "/value/set";
		String p = Boolean.valueOf(state == State.ON).toString();
		System.out.println(m);
		System.out.println(p);
		mqttClient.publish(m, new MqttMessage(p.getBytes()));
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

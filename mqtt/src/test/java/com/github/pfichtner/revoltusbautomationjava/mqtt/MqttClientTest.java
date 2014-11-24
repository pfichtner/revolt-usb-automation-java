package com.github.pfichtner.revoltusbautomationjava.mqtt;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.dna.mqtt.moquette.server.Server;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.junit.Test;

import com.github.pfichtner.revoltusbautomationjava.message.Function;
import com.github.pfichtner.revoltusbautomationjava.message.Message.MessageBuilder;
import com.github.pfichtner.revoltusbautomationjava.message.Outlet;
import com.github.pfichtner.revoltusbautomationjava.message.State;
import com.github.pfichtner.revoltusbautomationjava.usb.Usb;

public class MqttClientTest {

	@Test
	public void test() throws IOException, MqttException, InterruptedException {

		Server broker = new Server();
		broker.startServer();
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
				mqttClient.publish(topic, new MqttMessage((outlet.getIndex()
						+ "=" + state.getIdentifier()).getBytes()));
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
				}
			}
		};
		TimeUnit.SECONDS.sleep(3);
	}

}

package com.github.pfichtner.revoltusbautomationjava.message;

import static com.github.pfichtner.revoltusbautomationjava.message.Primitives.hexToBytes;
import static org.junit.Assert.assertArrayEquals;

import org.junit.Assert;
import org.junit.Test;

import com.github.pfichtner.revoltusbautomationjava.message.Message.MessageBuilder;

public class MessageTest {

	private MessageBuilder defaults = new MessageBuilder();
	private MessageBuilder msfFin0018 = new MessageBuilder().msgFin("0018");

	@Test
	public void switchOutletOneWithDefaults() {
		assertEquals(Function.of(Outlet.ONE, State.ON), defaults,
				"1a85f070200a0000", new byte[] { 26, -123, -16, 112, 32, 10, 0,
						0 });
		assertEquals(Function.of(Outlet.ONE, State.OFF), defaults,
				"1a85e080200a0000", new byte[] { 26, -123, -32, -128, 32, 10,
						0, 0 });
	}

	@Test
	public void switchOutletOneWithMsgFin0018() {
		assertEquals(Function.of(Outlet.ONE, State.ON), msfFin0018,
				"1a85f070200a0018", new byte[] { 26, -123, -16, 112, 32, 10, 0,
						24 });
		assertEquals(Function.of(Outlet.ONE, State.OFF), msfFin0018,
				"1a85e080200a0018", new byte[] { 26, -123, -32, -128, 32, 10,
						0, 24 });
	}

	private void assertEquals(Function function, MessageBuilder builder,
			String expectedHex, byte[] expectedBytes) {
		Message message = builder.build(function);
		Assert.assertEquals(expectedHex, message.asString());
		assertArrayEquals(expectedBytes, message.asBytes());
		assertArrayEquals(hexToBytes(expectedHex), message.asBytes());
	}

}

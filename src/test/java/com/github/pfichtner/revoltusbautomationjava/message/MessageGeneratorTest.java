package com.github.pfichtner.revoltusbautomationjava.message;

import static com.github.pfichtner.revoltusbautomationjava.message.Primitives.hexToBytes;
import static org.junit.Assert.assertArrayEquals;

import org.junit.Assert;
import org.junit.Test;

public class MessageGeneratorTest {

	private MessageGenerator defaults = new MessageGenerator();
	private MessageGenerator msfFin0018 = new MessageGenerator().msgFin("0018");

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

	private void assertEquals(Function function, MessageGenerator mg,
			String expectedHex, byte[] expectedBytes) {
		String msg = mg.hexMessage(function);
		Assert.assertEquals(expectedHex, msg);
		byte[] bytes = mg.bytesMessage(function);
		assertArrayEquals(expectedBytes, bytes);
		assertArrayEquals(hexToBytes(expectedHex), bytes);
	}

}

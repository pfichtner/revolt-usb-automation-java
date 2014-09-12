package com.github.pfichtner.revoltusbautomationjava.message;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MessageGeneratorTest {

	@Test
	public void switchOutletOneWithDefaults() {
		Function function = Function.of(Outlet.ONE, State.ON);
		MessageGenerator mg = new MessageGenerator();
		assertEquals("6789f01f200a0000", mg.hexMessage(function));
		assertArrayEquals(new byte[] { 103, -119, -16, 31, 32, 10, 0, 0 },
				mg.bytesMessage(function));
	}

}

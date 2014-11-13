package com.github.pfichtner.revoltusbautomationjava.message;

import static com.github.pfichtner.revoltusbautomationjava.message.Primitives.hexToBytes;

public class Message {

	private String content;

	public Message(String content) {
		this.content = content;
	}

	public String asString() {
		return content;
	}

	public byte[] asBytes() {
		return hexToBytes(this.content);
	}

	@Override
	public String toString() {
		return asString();
	}

}

package com.github.pfichtner.revoltusbautomationjava.message;

import static com.github.pfichtner.revoltusbautomationjava.message.Primitives.hex2Int;
import static com.github.pfichtner.revoltusbautomationjava.message.Primitives.hexToBytes;
import static com.github.pfichtner.revoltusbautomationjava.message.Primitives.intToHex;
import static com.github.pfichtner.revoltusbautomationjava.message.Strings.padLeft;
import static com.github.pfichtner.revoltusbautomationjava.message.Strings.padRight;
import static java.lang.Math.ceil;

public class MessageGenerator {

	private final Function function;
	private final int rawFrames;
	private final String msgFin;
	private final String msgId;

	public MessageGenerator(Function function, int rawFrames, int rawId,
			String msgFin) {
		this.function = function;
		this.rawFrames = rawFrames;
		this.msgId = padLeft(intToHex(rawId), '0', 4);
		this.msgFin = msgFin;
	}

	public String hexMessage() {
		String msgPaddingBytes = "20"; // not relevant padding
		String msgAction = padRight(intToHex(function.asInt()), '0', 2);
		String msgFrame = padLeft(intToHex(rawFrames), '0', 2);
		return msgId + msgAction + getChecksum() + msgPaddingBytes + msgFrame
				+ msgFin;
	}

	public byte[] bytesMessage() {
		return hexToBytes(hexMessage());
	}

	public String getChecksum() {
		return padLeft(intToHex(getRawChecksum()), '0', 2);
	}

	public int getRawChecksum() {
		int sum = getSum();
		return (int) (ceil(sum / 256.0) * 256 - sum - 1);
	}

	public int getSum() {
		return hex2Int(msgId.substring(0, 2)) + hex2Int(msgId.substring(2, 4))
				+ function.asInt() * 16;
	}

}
package com.github.pfichtner.revoltusbautomationjava.message;

import static com.github.pfichtner.revoltusbautomationjava.message.Primitives.hex2Int;
import static com.github.pfichtner.revoltusbautomationjava.message.Primitives.hexToBytes;
import static com.github.pfichtner.revoltusbautomationjava.message.Primitives.intToHex;
import static com.github.pfichtner.revoltusbautomationjava.message.Strings.padLeft;
import static com.github.pfichtner.revoltusbautomationjava.message.Strings.padRight;
import static java.lang.Math.ceil;

public class MessageGenerator {

	private int rawFrames = 10;
	private String rawId = "6789";
	private String msgFin = "0000";

	public MessageGenerator rawFrames(int rawFrames) {
		this.rawFrames = rawFrames;
		return this;
	}

	public MessageGenerator rawId(int rawId) {
		this.rawId = padLeft(intToHex(rawId), '0', 4);
		return this;
	}

	public MessageGenerator msgFin(String msgFin) {
		this.msgFin = msgFin;
		return this;
	}

	public String hexMessage(Function function) {
		String msgPaddingBytes = "20"; // not relevant padding
		String msgAction = padRight(intToHex(function.asInt()), '0', 2);
		String msgFrame = padLeft(intToHex(this.rawFrames), '0', 2);
		return this.rawId + msgAction + getChecksum(function) + msgPaddingBytes
				+ msgFrame + this.msgFin;
	}

	public byte[] bytesMessage(Function function) {
		return hexToBytes(hexMessage(function));
	}

	public String getChecksum(Function function) {
		return padLeft(intToHex(getRawChecksum(function)), '0', 2);
	}

	public int getRawChecksum(Function function) {
		int sum = getSum(function);
		return (int) (ceil(sum / 256.0) * 256 - sum - 1);
	}

	public int getSum(Function function) {
		return hex2Int(this.rawId.substring(0, 2))
				+ hex2Int(this.rawId.substring(2, 4)) + function.asInt() * 16;
	}

}
package com.github.pfichtner.revoltusbautomationjava.message;

import static com.github.pfichtner.revoltusbautomationjava.message.Padder.leftPadder;
import static com.github.pfichtner.revoltusbautomationjava.message.Padder.rightPadder;
import static com.github.pfichtner.revoltusbautomationjava.message.Primitives.hex2Int;
import static com.github.pfichtner.revoltusbautomationjava.message.Primitives.hexToBytes;
import static com.github.pfichtner.revoltusbautomationjava.message.Primitives.intToHex;
import static java.lang.Math.ceil;

public class MessageGenerator {

	private int rawFrames = 10;
	private String rawId = rawIdToString(6789);
	private String msgFin = "0000";

	public MessageGenerator rawFrames(int rawFrames) {
		this.rawFrames = rawFrames;
		return this;
	}

	public int getRawFrames() {
		return this.rawFrames;
	}

	public MessageGenerator rawId(int rawId) {
		this.rawId = rawIdToString(rawId);
		return this;
	}

	public int getRawId() {
		return hex2Int(this.rawId);
	}

	private static String rawIdToString(int rawId) {
		return leftPadder('0', 4).pad(intToHex(rawId));
	}

	public MessageGenerator msgFin(String msgFin) {
		this.msgFin = msgFin;
		return this;
	}

	public Message message(Function function) {
		String msgPaddingBytes = "20"; // not relevant padding
		String msgAction = rightPadder('0', 2).pad(intToHex(function.asInt()));
		String msgFrame = leftPadder('0', 2).pad(intToHex(this.rawFrames));
		return new Message(this.rawId + msgAction + getChecksum(function)
				+ msgPaddingBytes + msgFrame + this.msgFin);
	}

	public String getChecksum(Function function) {
		return leftPadder('0', 2).pad(intToHex(getRawChecksum(function)));
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
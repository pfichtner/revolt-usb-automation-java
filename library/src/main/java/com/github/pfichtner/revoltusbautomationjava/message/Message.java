package com.github.pfichtner.revoltusbautomationjava.message;

import static com.github.pfichtner.revoltusbautomationjava.message.Padder.leftPadder;
import static com.github.pfichtner.revoltusbautomationjava.message.Padder.rightPadder;
import static com.github.pfichtner.revoltusbautomationjava.message.Primitives.hex2Int;
import static com.github.pfichtner.revoltusbautomationjava.message.Primitives.hexToBytes;
import static com.github.pfichtner.revoltusbautomationjava.message.Primitives.intToHex;
import static java.lang.Math.ceil;

public class Message {

	public static class MessageBuilder {

		private int rawFrames = 10;
		private String rawId = "6789";
		private String msgFin = "0000";

		public MessageBuilder rawFrames(int rawFrames) {
			this.rawFrames = rawFrames;
			return this;
		}

		public int getRawFrames() {
			return this.rawFrames;
		}

		public MessageBuilder rawId(int rawId) {
			this.rawId = rawIdToString(rawId);
			return this;
		}

		public int getRawId() {
			return hex2Int(this.rawId);
		}

		private String rawIdToString(int rawId) {
			return leftPadder('0', this.rawId.length()).pad(intToHex(rawId));
		}

		public MessageBuilder msgFin(String msgFin) {
			this.msgFin = leftPadder('0', this.msgFin.length()).pad(msgFin);
			return this;
		}

		public Message build(Function function) {
			return new Message(this, function);
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
					+ hex2Int(this.rawId.substring(2, 4)) + function.asInt()
					* 16;
		}

	}

	private final String content;

	public Message(MessageBuilder builder, Function function) {
		String msgPaddingBytes = "20"; // not relevant padding
		String msgAction = rightPadder('0', 2).pad(intToHex(function.asInt()));
		String msgFrame = leftPadder('0', 2).pad(intToHex(builder.rawFrames));
		this.content = builder.rawId + msgAction
				+ builder.getChecksum(function) + msgPaddingBytes + msgFrame
				+ builder.msgFin;
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

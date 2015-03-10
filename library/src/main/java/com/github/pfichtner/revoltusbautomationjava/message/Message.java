package com.github.pfichtner.revoltusbautomationjava.message;

import static com.github.pfichtner.revoltusbautomationjava.message.Padder.leftPadder;
import static com.github.pfichtner.revoltusbautomationjava.message.Padder.rightPadder;
import static com.github.pfichtner.revoltusbautomationjava.message.Primitives.hexToInt;
import static com.github.pfichtner.revoltusbautomationjava.message.Primitives.hexToBytes;
import static com.github.pfichtner.revoltusbautomationjava.message.Primitives.intToHex;
import static java.lang.Math.ceil;

public class Message {

	public static class MessageBuilder {

		private int rawFrames = 10;
		private String rawId = leftPadder('0', 4).pad(intToHex(6789));
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
			return hexToInt(this.rawId);
		}

		private String rawIdToString(int rawId) {
			return leftPadder('0', this.rawId.length()).pad(intToHex(rawId));
		}

		public MessageBuilder msgFin(String msgFin) {
			this.msgFin = leftPadder('0', this.msgFin.length()).pad(msgFin);
			return this;
		}

		public Message build(PayloadSupplier payloadSupplier) {
			return new Message(this, payloadSupplier);
		}

		public String getChecksum(PayloadSupplier payloadSupplier) {
			return leftPadder('0', 2).pad(
					intToHex(getRawChecksum(payloadSupplier)));
		}

		public int getRawChecksum(PayloadSupplier payloadSupplier) {
			int sum = getSum(payloadSupplier);
			return (int) (ceil(sum / 256.0) * 256 - sum - 1);
		}

		public int getSum(PayloadSupplier payloadSupplier) {
			return hexToInt(this.rawId.substring(0, 2))
					+ hexToInt(this.rawId.substring(2, 4))
					+ payloadSupplier.asInt() * 16;
		}

	}

	private final String content;

	public Message(MessageBuilder builder, PayloadSupplier payloadSupplier) {
		String msgPaddingBytes = "20"; // not relevant padding
		String msgAction = rightPadder('0', 2).pad(
				intToHex(payloadSupplier.asInt()));
		String msgFrame = leftPadder('0', 2).pad(intToHex(builder.rawFrames));
		this.content = builder.rawId + msgAction
				+ builder.getChecksum(payloadSupplier) + msgPaddingBytes
				+ msgFrame + builder.msgFin;
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

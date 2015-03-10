package com.github.pfichtner.revoltusbautomationjava.message;

import java.math.BigInteger;

public final class Primitives {

	private Primitives() {
		super();
	}

	public static String intToBin(int intValue) {
		return Integer.toBinaryString(intValue);
	}

	public static int binToInt(String binary) {
		return Integer.parseInt(binary, 2);
	}

	public static String intToHex(int id) {
		return Integer.toHexString(id);
	}

	public static int hexToInt(String hex) {
		return Integer.parseInt(hex, 16);
	}

	public static byte[] hexToBytes(String hex) {
		return new BigInteger(hex, 16).toByteArray();
	}

	public static int shiftLeft(int value, int positions) {
		return value << positions;
	}

}

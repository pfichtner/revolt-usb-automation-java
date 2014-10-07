package com.github.pfichtner.revoltusbautomationjava.message;

import java.util.Arrays;

public abstract class Padder {

	// replacement for guava's Strings#padLeft
	public static Padder leftPadder(final char ch, final int len) {
		return new Padder() {
			@Override
			public String pad(String in) {
				return in.length() >= len ? in.substring(0, len)
						: filledString(in, ch, len, len - in.length());
			}
		};
	}

	public abstract String pad(String in);

	// replacement for guava's Strings#padRight
	public static Padder rightPadder(final char ch, final int len) {
		return new Padder() {
			@Override
			public String pad(String in) {
				return in.length() >= len ? in.substring(0, len)
						: filledString(in, ch, len, 0);
			}
		};
	}

	private static String filledString(String in, char ch, int len, int startPos) {
		char[] chars = newFilledArray(ch, len);
		in.getChars(0, in.length(), chars, startPos);
		return new String(chars);
	}

	private static char[] newFilledArray(char ch, int len) {
		char[] chars = new char[len];
		Arrays.fill(chars, ch);
		return chars;
	}

}
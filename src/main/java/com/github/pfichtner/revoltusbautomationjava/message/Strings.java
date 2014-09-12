package com.github.pfichtner.revoltusbautomationjava.message;

import java.util.Arrays;

public final class Strings {

	private Strings() {
		super();
	}

	public static String padLeft(String in, char ch, int len) {
		return in.length() >= len ? in.substring(0, len) : filledString(in, ch,
				len, len - in.length());
	}

	public static String padRight(String in, char ch, int len) {
		return in.length() >= len ? in.substring(0, len) : filledString(in, ch,
				len, 0);
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

	public static String trim(String string, char c) {
		for (int i = 0; i < string.length(); i++) {
			char charAt = string.charAt(i);
			if (charAt != c) {
				return string.substring(i);
			}
		}
		return "";
	}

}

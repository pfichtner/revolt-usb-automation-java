package com.github.pfichtner.revoltusbautomationjava.message;

public final class Trimmer {

	private char character;

	private Trimmer(char character) {
		this.character = character;
	}

	public static Trimmer on(char character) {
		return new Trimmer(character);
	}

	// replacement for guava's CharMatcher#trim
	public String trim(String string) {
		for (int i = 0; i < string.length(); i++) {
			char charAt = string.charAt(i);
			if (charAt != character) {
				return string.substring(i);
			}
		}
		return "";
	}

}

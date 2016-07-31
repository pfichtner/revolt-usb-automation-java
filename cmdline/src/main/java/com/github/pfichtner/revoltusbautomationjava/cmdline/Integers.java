package com.github.pfichtner.revoltusbautomationjava.cmdline;

public final class Integers {

	private Integers() {
		super();
	}

	public static Integer tryParse(String string) {
		try {
			return Integer.valueOf(string);
		} catch (NumberFormatException e) {
			return null;
		}
	}

}

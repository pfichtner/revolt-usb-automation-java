package com.github.pfichtner.revoltusbautomationjava;

public final class Preconditions {

	private Preconditions() {
		super();
	}

	public static <T> T checkNotNull(T t, String message, Object... args) {
		if (t == null) {
			throw new NullPointerException(String.format(message, args));
		}
		return t;
	}

}

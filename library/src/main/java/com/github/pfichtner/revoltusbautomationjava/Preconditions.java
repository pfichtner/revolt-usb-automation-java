package com.github.pfichtner.revoltusbautomationjava;

public final class Preconditions {

	private Preconditions() {
		super();
	}

	public static <T> T checkNotNull(T t, String message) {
		if (t == null) {
			throw new NullPointerException(message);
		}
		return t;
	}

}

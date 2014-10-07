package com.github.pfichtner.revoltusbautomationjava.message;

import java.util.Arrays;

public enum Outlet {

	ONE(1), TWO(2), THREE(3), FOUR(4);

	private int index;

	private Outlet(int index) {
		this.index = index;
	}

	public static Outlet[] all() {
		return values();
	}

	public int getIndex() {
		return index;
	}

	public static Outlet of(int index) {
		for (Outlet outlet : values()) {
			if (outlet.index == index) {
				return outlet;
			}
		}
		throw new IllegalStateException(index + " is not a valid outlet index");
	}

	public static boolean isAll(Outlet[] outlets) {
		return Arrays.equals(outlets, values());
	}

}

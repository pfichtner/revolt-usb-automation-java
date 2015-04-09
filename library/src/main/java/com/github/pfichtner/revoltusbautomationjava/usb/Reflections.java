package com.github.pfichtner.revoltusbautomationjava.usb;

public final class Reflections {

	private Reflections() {
		super();
	}

	public static boolean existsClass(String clazz) {
		return existsClass(clazz, Reflections.class.getClassLoader());
	}

	public static boolean existsClass(String clazz, ClassLoader classLoader) {
		try {
			classLoader.loadClass(clazz);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

}

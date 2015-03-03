package com.github.pfichtner.revoltusbautomationjava.usb;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ClasspathDependentUsb implements Usb {

	private final Usb delegate;

	public static ClasspathDependentUsb newInstance(short vendorId,
			short productId) {
		return new ClasspathDependentUsb(vendorId, productId);
	}

	public ClasspathDependentUsb(short vendorId, short productId) {
		this.delegate = isCodeMinersPresent() ? UsbCodeminers.newInstance(
				vendorId, productId) : UsbUsb4Java.newInstance(vendorId,
				productId);
	}

	private boolean isCodeMinersPresent() {
		return existsClass("com.codeminders.hidapi.ClassPathLibraryLoader");
	}

	private boolean existsClass(String clazz) {
		try {
			getClass().getClassLoader().loadClass(clazz);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	public void setInterfaceNum(int intValue) {
		delegate.setInterfaceNum(intValue);
	}

	public void setOutEndpoint(byte byteValue) {
		delegate.setOutEndpoint(byteValue);
	}

	public void setTimeout(TimeUnit timeUnit, long longValue) {
		delegate.setTimeout(timeUnit, longValue);
	}

	public void connect() throws IOException {
		delegate.connect();
	}

	public void close() throws IOException {
		delegate.close();
	}

	public void write(byte[] data) throws IOException {
		delegate.write(data);
	}

	public boolean hasHotplug() {
		return delegate.hasHotplug();
	}

	public void registerCallback(UsbHotPlugEventListener hotPlugEventListener) {
		delegate.registerCallback(hotPlugEventListener);
	}

}

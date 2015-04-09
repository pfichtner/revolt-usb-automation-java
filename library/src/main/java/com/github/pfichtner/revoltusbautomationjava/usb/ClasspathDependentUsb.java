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
		this.delegate = UsbUsb4Java.dependenciesOnClasspath() ? UsbUsb4Java
				.newInstance(vendorId, productId) : UsbCodeminers.newInstance(
				vendorId, productId);
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

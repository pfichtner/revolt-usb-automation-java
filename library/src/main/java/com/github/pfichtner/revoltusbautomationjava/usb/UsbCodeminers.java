package com.github.pfichtner.revoltusbautomationjava.usb;

import java.io.Closeable;
import java.io.IOException;

import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDManager;

public class UsbCodeminers implements Usb, Closeable {

	private short vendorId;

	private short productId;

	public boolean connected;

	private HIDManager hidManager;

	private HIDDevice device;

	private UsbCodeminers(short vendorId, short productId) throws IOException {
		this.hidManager = HIDManager.getInstance();
		this.vendorId = vendorId;
		this.productId = productId;
	}

	private String noSerialNumber() {
		return null;
	}

	public static UsbCodeminers newInstance(short vendorId, short productId)
			throws IOException {
		return new UsbCodeminers(vendorId, productId);
	}

	public UsbCodeminers connect() throws IOException {
		device = hidManager.openById(vendorId, productId, noSerialNumber());
		connected = true;
		return this;
	}

	public void close() throws IOException {
		this.device.close();
		this.hidManager.release();
	}

	public void write(byte[] data) throws IOException {
		this.device.write(data);
	}

	public boolean hasHotplug() {
		// https://github.com/baalhiverne/judraw/blob/master/judrawlib/src/org/judraw/judrawlib/RawDevice.java
		return false;
	}

	public void registerCallback(UsbHotPlugEventListener hotPlugEventListener) {
		throw new UnsupportedOperationException("hotplug not supported");
	}

}

package com.github.pfichtner.revoltusbautomationjava.usb;

import java.io.IOException;

public interface Usb {

	public interface UsbHotPlugEventListener {

		void deviceConnected(short idVendor, short idProduct);

		void deviceDisconnected(short idVendor, short idProduct);

		void errorConnecting(short idVendor, short idProduct, Exception e);

	}

	Usb connect() throws IOException;

	void close() throws IOException;

	void write(byte[] data) throws IOException;

	boolean hasHotplug();

	void registerCallback(UsbHotPlugEventListener hotPlugEventListener);

}
package com.github.pfichtner.revoltusbautomationjava.usb;

import static org.usb4java.LibUsb.SUCCESS;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.TimeUnit;

import org.usb4java.BufferUtils;
import org.usb4java.Context;
import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceHandle;
import org.usb4java.DeviceList;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

public class Usb implements Closeable {

	private int interfaceNum = 0;

	private byte outEndpoint = 0x02;

	private long timeout = TimeUnit.SECONDS.toMillis(5);

	private final DeviceHandle deviceHandle;

	private final Context context = new Context();

	public Usb(short vendorId, short productId) {
		checkRc(LibUsb.init(this.context), "Unable to initialize libusb");
		this.deviceHandle = getDeviceHandle(findDevice(vendorId, productId));
		claimInterface(this.deviceHandle, this.interfaceNum);
	}

	public Usb interfaceNum(int interfaceNum) {
		this.interfaceNum = interfaceNum;
		return this;
	}

	public Usb outEndpoint(byte outEndpoint) {
		this.outEndpoint = outEndpoint;
		return this;
	}

	public Usb timeout(TimeUnit timeUnit, long value) {
		this.timeout = timeUnit.toMillis(value);
		return this;
	}

	private DeviceHandle getDeviceHandle(Device device) {
		DeviceHandle handle = new DeviceHandle();
		checkRc(LibUsb.open(device, handle), "Unable to open USB device");
		return handle;
	}

	private static void claimInterface(DeviceHandle deviceHandle,
			int interfaceNum) {
		int result = LibUsb.claimInterface(deviceHandle, interfaceNum);
		if (result != SUCCESS) {
			throw new LibUsbException("Unable to claim interface", result);
		}
	}

	public void close() {
		release(this.deviceHandle, interfaceNum);
		LibUsb.close(this.deviceHandle);
		LibUsb.exit(this.context);
	}

	private Device findDevice(short vendorId, short productId) {
		DeviceList deviceList = new DeviceList();
		try {
			int result = LibUsb.getDeviceList(null, deviceList);
			if (result < 0) {
				throw new LibUsbException("Unable to get device list", result);
			}
			for (Device device : deviceList) {
				DeviceDescriptor descriptor = new DeviceDescriptor();
				checkRc(LibUsb.getDeviceDescriptor(device, descriptor),
						"Cannot get descriptor");
				if (descriptor.idVendor() == vendorId
						&& descriptor.idProduct() == productId) {
					return device;
				}
			}
		} finally {
			LibUsb.freeDeviceList(deviceList, true);
		}

		throw new LibUsbException("Device vendorId " + vendorId + " productId "
				+ productId + " not found", -1);
	}

	private static void checkRc(int result, String message) {
		if (result != SUCCESS) {
			throw new LibUsbException(message, result);
		}
	}

	public void write(byte[] data) {
		ByteBuffer buffer = newByteBuffer(data);
		IntBuffer transferred = BufferUtils.allocateIntBuffer();
		checkRc(LibUsb.bulkTransfer(this.deviceHandle, outEndpoint, buffer,
				transferred, timeout), "Unable to send data");
	}

	private static void release(DeviceHandle deviceHandle, int interfaceNum) {
		checkRc(LibUsb.releaseInterface(deviceHandle, interfaceNum),
				"Unable to release interface");
	}

	private static ByteBuffer newByteBuffer(byte[] data) {
		ByteBuffer buffer = BufferUtils.allocateByteBuffer(data.length);
		buffer.put(data);
		return buffer;
	}

}

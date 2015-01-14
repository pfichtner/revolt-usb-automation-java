package com.github.pfichtner.revoltusbautomationjava.usb;

import static com.github.pfichtner.revoltusbautomationjava.Preconditions.checkNotNull;
import static org.usb4java.LibUsb.CAP_HAS_HOTPLUG;
import static org.usb4java.LibUsb.HOTPLUG_ENUMERATE;
import static org.usb4java.LibUsb.HOTPLUG_EVENT_DEVICE_ARRIVED;
import static org.usb4java.LibUsb.HOTPLUG_EVENT_DEVICE_LEFT;
import static org.usb4java.LibUsb.HOTPLUG_MATCH_ANY;
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
import org.usb4java.HotplugCallback;
import org.usb4java.HotplugCallbackHandle;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

public class Usb implements Closeable {

	public interface UsbHotPlugEventListener {

		void deviceConnected(short idVendor, short idProduct);

		void deviceDisconnected(short idVendor, short idProduct);

		void errorConnecting(short idVendor, short idProduct, Exception e);

	}

	private int interfaceNum = 0;

	private byte outEndpoint = 0x02;

	private long timeout = TimeUnit.SECONDS.toMillis(5);

	private DeviceHandle deviceHandle;

	private final Context context = new Context();

	private short vendorId;

	private short productId;

	public boolean connected;

	private Usb(short vendorId, short productId) {
		checkRc(LibUsb.init(this.context), "Unable to initialize libusb");
		this.vendorId = vendorId;
		this.productId = productId;
	}

	public static Usb newInstance(short vendorId, short productId) {
		return new Usb(vendorId, productId);
	}

	public Usb connect() {
		this.deviceHandle = getDeviceHandle(findDevice(vendorId, productId));
		claimInterface(this.deviceHandle, this.interfaceNum);
		connected = true;
		return this;
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
		DeviceHandle dh = this.deviceHandle;
		if (dh != null) {
			release(dh, interfaceNum);
			LibUsb.close(dh);
		}
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

	public static void checkRc(int result, String message) {
		if (result != SUCCESS) {
			throw new LibUsbException(message, result);
		}
	}

	public void write(byte[] data) {
		ByteBuffer buffer = newByteBuffer(data);
		IntBuffer transferred = BufferUtils.allocateIntBuffer();
		checkRc(LibUsb.bulkTransfer(
				checkNotNull(this.deviceHandle, "not connected"), outEndpoint,
				buffer, transferred, timeout), "Unable to send data");
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

	public boolean hasHotplug() {
		return LibUsb.hasCapability(CAP_HAS_HOTPLUG);
	}

	private static class EventHandlingThread extends Thread {

		{
			setDaemon(true);
		}

		@Override
		public void run() {
			while (true) {
				checkRc(LibUsb.handleEventsTimeout(null,
						TimeUnit.SECONDS.toMicros(1)),
						"Unable to handle events");
			}
		}
	}

	private class Callback implements HotplugCallback {

		private UsbHotPlugEventListener hotPlugEventListener;

		public Callback(UsbHotPlugEventListener hotPlugEventListener) {
			this.hotPlugEventListener = hotPlugEventListener;
		}

		public int processEvent(Context context, Device device, int event,
				Object userData) {
			DeviceDescriptor descriptor = new DeviceDescriptor();
			checkRc(LibUsb.getDeviceDescriptor(device, descriptor),
					"Unable to read device descriptor");
			if (vendorId == descriptor.idVendor()
					&& productId == descriptor.idProduct()) {
				if (event == HOTPLUG_EVENT_DEVICE_ARRIVED) {
					try {
						connect();
						hotPlugEventListener.deviceConnected(
								descriptor.idVendor(), descriptor.idProduct());
					} catch (Exception e) {
						hotPlugEventListener.errorConnecting(
								descriptor.idVendor(), descriptor.idProduct(),
								e);
					}
				} else {
					hotPlugEventListener.deviceDisconnected(
							descriptor.idVendor(), descriptor.idProduct());
					connected = false;
				}
			}
			return 0;
		}

	}

	public void registerCallback(UsbHotPlugEventListener hotPlugEventListener) {
		new EventHandlingThread().start();
		checkRc(LibUsb.hotplugRegisterCallback(null,
				HOTPLUG_EVENT_DEVICE_ARRIVED | HOTPLUG_EVENT_DEVICE_LEFT,
				HOTPLUG_ENUMERATE, HOTPLUG_MATCH_ANY, HOTPLUG_MATCH_ANY,
				HOTPLUG_MATCH_ANY, new Callback(hotPlugEventListener), null,
				new HotplugCallbackHandle()),
				"Unable to register hotplug callback");

	}

}

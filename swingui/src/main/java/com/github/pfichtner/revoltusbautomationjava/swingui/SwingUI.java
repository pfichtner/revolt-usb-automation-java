package com.github.pfichtner.revoltusbautomationjava.swingui;

import static org.usb4java.LibUsb.CAP_HAS_HOTPLUG;
import static org.usb4java.LibUsb.HOTPLUG_ENUMERATE;
import static org.usb4java.LibUsb.HOTPLUG_EVENT_DEVICE_ARRIVED;
import static org.usb4java.LibUsb.HOTPLUG_EVENT_DEVICE_LEFT;
import static org.usb4java.LibUsb.HOTPLUG_MATCH_ANY;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import org.usb4java.Context;
import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.HotplugCallback;
import org.usb4java.HotplugCallbackHandle;
import org.usb4java.LibUsb;

import com.github.pfichtner.revoltusbautomationjava.message.Function;
import com.github.pfichtner.revoltusbautomationjava.message.MessageGenerator;
import com.github.pfichtner.revoltusbautomationjava.message.Outlet;
import com.github.pfichtner.revoltusbautomationjava.message.State;
import com.github.pfichtner.revoltusbautomationjava.usb.Usb;

// TODO Do not fail on usb errors
// TODO Add menu bar analog to EXE
public class SwingUI extends JFrame {

	static class EventHandlingThread extends Thread {

		{
			setDaemon(true);
		}

		@Override
		public void run() {
			while (true) {
				Usb.checkRc(LibUsb.handleEventsTimeout(null, 1 * 1000 * 1000),
						"Unable to handle events");
			}
		}
	}

	private static final short vendorId = (short) 0xffff;

	private static final short productId = (short) 0x1122;

	private class Callback implements HotplugCallback {

		public int processEvent(Context context, Device device, int event,
				Object userData) {
			DeviceDescriptor descriptor = new DeviceDescriptor();
			Usb.checkRc(LibUsb.getDeviceDescriptor(device, descriptor),
					"Unable to read device descriptor");
			if (vendorId == descriptor.idVendor()
					&& productId == descriptor.idProduct()) {
				if (event == HOTPLUG_EVENT_DEVICE_ARRIVED) {
					connect();
				} else {
					connected = false;
					status.setText("Disconnetced");
				}
			}
			return 0;
		}

	}

	private static final long serialVersionUID = -7029240022142504077L;

	private Usb usb = Usb.newInstance();
	private MessageGenerator msgGenerator = new MessageGenerator();

	private JLabel status;

	private boolean connected;

	public SwingUI() {
		setTitle("SwingUI");
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		JComponent c = new JPanel();
		c.setLayout(new GridLayout(5, 3));
		getContentPane().add(c, BorderLayout.CENTER);
		for (int i = 0; i < 4; i++) {
			addRow(c, String.valueOf(i + 1));
		}
		addRow(c, "All");

		status = new JLabel("Ready");
		status.setBorder(new BevelBorder(BevelBorder.LOWERED));
		getContentPane().add(status, BorderLayout.SOUTH);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (connected) {
					usb.close();
				}
			}

		});
		pack();

		if (!LibUsb.hasCapability(CAP_HAS_HOTPLUG)) {
			this.status.setText("System doesn't support hotplug");
		} else {
			new EventHandlingThread().start();
			registerCallback();
		}
	}

	private void connect() {
		usb.connect(vendorId, productId);
		status.setText("Connected");
		connected = true;
	}

	private void registerCallback() {
		Usb.checkRc(LibUsb.hotplugRegisterCallback(null,
				HOTPLUG_EVENT_DEVICE_ARRIVED | HOTPLUG_EVENT_DEVICE_LEFT,
				HOTPLUG_ENUMERATE, HOTPLUG_MATCH_ANY, HOTPLUG_MATCH_ANY,
				HOTPLUG_MATCH_ANY, new Callback(), null,
				new HotplugCallbackHandle()),
				"Unable to register hotplug callback");
	}

	private void addRow(Container contentPane, String name) {
		JButton onButton = new JButton("On");
		onButton.addActionListener(newaddActionListener(name, State.ON));
		contentPane.add(onButton);
		JLabel label = new JLabel(name);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(label);
		JButton offButton = new JButton("Off");
		offButton.addActionListener(newaddActionListener(name, State.OFF));
		contentPane.add(offButton);
	}

	private ActionListener newaddActionListener(final String name,
			final State state) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				usb.write(msgGenerator.bytesMessage(Function.of(
						Outlet.forString(name), state)));
			}
		};
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				SwingUI swingUI = new SwingUI();
				swingUI.setVisible(true);
			}
		});
	}

}

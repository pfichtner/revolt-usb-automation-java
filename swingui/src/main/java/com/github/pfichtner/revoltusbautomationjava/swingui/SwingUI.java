package com.github.pfichtner.revoltusbautomationjava.swingui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import com.github.pfichtner.revoltusbautomationjava.message.Function;
import com.github.pfichtner.revoltusbautomationjava.message.MessageGenerator;
import com.github.pfichtner.revoltusbautomationjava.message.Outlet;
import com.github.pfichtner.revoltusbautomationjava.message.State;
import com.github.pfichtner.revoltusbautomationjava.usb.Usb;
import com.github.pfichtner.revoltusbautomationjava.usb.Usb.UsbHotPlugEventListener;

// TODO Add menu bar analog to EXE
public class SwingUI extends JFrame {

	private static final short vendorId = (short) 0xffff;

	private static final short productId = (short) 0x1122;

	private static final long serialVersionUID = -7029240022142504077L;

	private final Usb usb = Usb.newInstance(vendorId, productId);
	private final MessageGenerator msgGenerator = new MessageGenerator();

	private JLabel status;

	private boolean connected;

	private JComponent buttonPanel;

	public SwingUI() {
		setTitle("SwingUI");
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setJMenuBar(createMenuBar());
		setLayout(new BorderLayout());

		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(5, 3));
		getContentPane().add(buttonPanel, BorderLayout.CENTER);
		for (int i = 0; i < 4; i++) {
			addRow(buttonPanel, String.valueOf(i + 1));
		}
		addRow(buttonPanel, "All");

		status = new JLabel();
		disconneted();
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

		installUncaughtExceptionHandler();

		if (usb.hasHotplug()) {
			usb.registerCallback(new UsbHotPlugEventListener() {

				public void deviceConnected(short idVendor, short idProduct) {
					connected();
				}

				public void deviceDisconnected(short idVendor, short idProduct) {
					disconneted();
				}

				public void errorConnecting(short idVendor, short idProduct,
						Exception e) {
					status.setText(e.getMessage());
				}

			});
		} else {
			this.status.setText("System doesn't support hotplug");
		}
	}

	private void installUncaughtExceptionHandler() {
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			public void uncaughtException(Thread t, Throwable e) {
				try {
					e.printStackTrace();
					status.setText(e.getMessage());
				} catch (Exception e2) {
					e2.printStackTrace();
				}

			}
		});
	}

	private JMenuBar createMenuBar() {
		JMenuBar menu = new JMenuBar();
		JMenu file = new JMenu("System");
		JMenuItem exitMi = new JMenuItem("Exit");
		exitMi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		file.add(exitMi);
		menu.add(file);
		return menu;
	}

	private void connected() {
		status.setText("Device is connected");
		setState(true);
	}

	private void disconneted() {
		status.setText("Device is not connected");
		setState(false);
	}

	private void setState(boolean state) {
		for (Component component : buttonPanel.getComponents()) {
			if (component instanceof JButton) {
				component.setEnabled(state);
			}
		}
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

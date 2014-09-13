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
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.text.NumberFormatter;

import com.github.pfichtner.revoltusbautomationjava.message.Function;
import com.github.pfichtner.revoltusbautomationjava.message.MessageGenerator;
import com.github.pfichtner.revoltusbautomationjava.message.Outlet;
import com.github.pfichtner.revoltusbautomationjava.message.State;
import com.github.pfichtner.revoltusbautomationjava.usb.Usb;
import com.github.pfichtner.revoltusbautomationjava.usb.Usb.UsbHotPlugEventListener;

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
		status.setBorder(new BevelBorder(BevelBorder.LOWERED));
		getContentPane().add(status, BorderLayout.SOUTH);

		disconneted();

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (connected) {
					usb.close();
				}
			}

		});
		pack();

		Thread.setDefaultUncaughtExceptionHandler(new JLabelExceptionHandler(
				status));

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

	private JMenuBar createMenuBar() {
		JMenuBar menu = new JMenuBar();
		menu.add(system());
		menu.add(settings());
		return menu;
	}

	private JMenu system() {
		JMenu system = new JMenu("System");
		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		system.add(exit);
		return system;
	}

	private JMenu settings() {
		JMenu settings = new JMenu("Settings");
		JMenuItem ds = new JMenuItem("Device settings");
		ds.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openDeviceSettings();
			}

			private void openDeviceSettings() {

				JDialog jDialog = new JDialog(SwingUI.this, true);
				jDialog.setResizable(false);
				jDialog.setLocationRelativeTo(jDialog.getOwner());
				jDialog.setLayout(new GridLayout(3, 2));
				Container c = jDialog.getContentPane();
				c.add(new JLabel("Frame 3-255"));
				c.add(createNumericField(10, 3, 255));
				c.add(new JLabel("ID 0-65535"));
				c.add(createNumericField(6789, 0, 65535));
				c.add(new JButton("Ok"));
				c.add(new JButton("Cancel"));
				jDialog.pack();
				jDialog.setVisible(true);
			}

			private JFormattedTextField createNumericField(int current,
					int min, int max) {
				NumberFormat nf = NumberFormat.getInstance();
				nf.setGroupingUsed(false);
				NumberFormatter formatter = new NumberFormatter(nf);
				formatter.setValueClass(Integer.class);
				formatter.setMinimum(min);
				formatter.setMaximum(max);
				formatter.setAllowsInvalid(false);
				JFormattedTextField textField = new JFormattedTextField(
						formatter);
				textField.setValue(current);
				return textField;
			}

		});
		settings.add(ds);
		return settings;
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

	private void addRow(Container c, String name) {
		JButton onButton = new JButton("On");
		onButton.addActionListener(newaddActionListener(name, State.ON));
		c.add(onButton);
		JLabel label = new JLabel(name);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		c.add(label);
		JButton offButton = new JButton("Off");
		offButton.addActionListener(newaddActionListener(name, State.OFF));
		c.add(offButton);
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

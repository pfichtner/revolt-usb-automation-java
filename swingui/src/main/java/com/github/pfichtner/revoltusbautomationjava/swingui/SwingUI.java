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
import javax.swing.border.EmptyBorder;

import com.github.pfichtner.revoltusbautomationjava.message.Function;
import com.github.pfichtner.revoltusbautomationjava.message.Message.MessageBuilder;
import com.github.pfichtner.revoltusbautomationjava.message.Outlet;
import com.github.pfichtner.revoltusbautomationjava.message.State;
import com.github.pfichtner.revoltusbautomationjava.usb.Usb;
import com.github.pfichtner.revoltusbautomationjava.usb.Usb.UsbHotPlugEventListener;

/**
 * SwingUI of project com.github.pfichtner.revoltusbautomationjava.swingui.
 * 
 * @author Peter Fichtner
 */
public class SwingUI extends JFrame {

	private static final short vendorId = (short) 0xffff;

	private static final short productId = (short) 0x1122;

	private static final long serialVersionUID = -7029240022142504077L;

	private final Usb usb = Usb.newInstance(vendorId, productId);

	private MessageBuilder msgBuilder = new MessageBuilder();

	private JLabel status;

	private boolean connected;

	private JComponent buttonPanel;

	public SwingUI() {
		setTitle("SwingUI");
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setJMenuBar(createMenuBar());
		setLayout(new BorderLayout());

		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(6, 3, 15, 5));
		buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(buttonPanel, BorderLayout.CENTER);

		addHeader(buttonPanel);

		for (int i = 1; i <= 4; i++) {
			addRow(buttonPanel, String.valueOf(i), Outlet.of(i));
		}
		addRow(buttonPanel, "All", Outlet.all());

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
		setLocationRelativeTo(null);

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
			try {
				usb.connect();
				connected();
			} catch (Exception e) {
				this.status.setText(e.getMessage());
			}
		}
	}

	private static void addHeader(JComponent buttonPanel) {
		JLabel on = new JLabel("On");
		on.setHorizontalAlignment(SwingConstants.CENTER);
		buttonPanel.add(on);

		JLabel dummy = new JLabel();
		buttonPanel.add(dummy);

		JLabel off = new JLabel("Off");
		off.setHorizontalAlignment(SwingConstants.CENTER);
		buttonPanel.add(off);
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
				SettingsDialog settings = new SettingsDialog(SwingUI.this);
				settings.setId(msgBuilder.getRawId());
				settings.setFrame(msgBuilder.getRawFrames());
				if (settings.showDialog()) {
					msgBuilder = msgBuilder.rawId(settings.getId());
					msgBuilder = msgBuilder.rawFrames(settings.getFrame());
				}
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

	private void addRow(Container c, String text, Outlet... outlets) {
		JButton onButton = new SquareButton();
		onButton.addActionListener(newaddActionListener(outlets, State.ON));
		c.add(onButton);
		JLabel label = new JLabel(text);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		c.add(label);
		JButton offButton = new SquareButton();
		offButton.addActionListener(newaddActionListener(outlets, State.OFF));
		c.add(offButton);
	}

	private ActionListener newaddActionListener(final Outlet[] outlets,
			final State state) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				usb.write(msgBuilder.build(Function.of(outlets, state))
						.asBytes());
			}
		};
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				new SwingUI().setVisible(true);
			}
		});
	}

}

package com.github.pfichtner.revoltusbautomationjava.swingui;

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

import com.github.pfichtner.revoltusbautomationjava.message.Function;
import com.github.pfichtner.revoltusbautomationjava.message.MessageGenerator;
import com.github.pfichtner.revoltusbautomationjava.message.Outlet;
import com.github.pfichtner.revoltusbautomationjava.message.State;
import com.github.pfichtner.revoltusbautomationjava.usb.Usb;

// TODO Do not fail on usb errors
// TODO Listen for added USB devices
// TODO Add menu bar analog to EXE
public class SwingUI extends JFrame {

	private static final long serialVersionUID = -7029240022142504077L;

	private Usb usb = new Usb((short) 0xffff, (short) 0x1122);
	private MessageGenerator msgGenerator = new MessageGenerator();

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

		JLabel status = new JLabel(" ");
		getContentPane().add(status, BorderLayout.SOUTH);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				usb.close();
			}

		});
		pack();
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

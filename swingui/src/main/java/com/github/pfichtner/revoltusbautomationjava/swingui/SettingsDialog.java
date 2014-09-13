package com.github.pfichtner.revoltusbautomationjava.swingui;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.NumberFormatter;

public class SettingsDialog extends JDialog {

	private static final long serialVersionUID = 6344039963255252742L;

	private JFormattedTextField frameField = createNumericField(3, 255);
	private JFormattedTextField idField = createNumericField(0, 65535);

	private boolean okWasPressed;

	public SettingsDialog(Frame owner) {
		super(owner, true);
		setResizable(false);
		setLocationRelativeTo(owner);
		setLayout(new GridLayout(3, 2, 15, 5));
		Container c = getContentPane();
		((JComponent) c).setBorder(new EmptyBorder(10, 10, 10, 10));
		c.add(new JLabel("Frame 3-255"));
		c.add(frameField);
		c.add(new JLabel("ID 0-65535"));
		c.add(idField);
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okWasPressed = true;
				setVisible(false);
			}
		});
		c.add(okButton);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		c.add(cancelButton);
		pack();
	}

	public void setId(int id) {
		this.idField.setValue(id);
	}

	public int getId() {
		return (Integer) this.idField.getValue();
	}

	public void setFrame(int frame) {
		this.frameField.setValue(frame);
	}

	public int getFrame() {
		return (Integer) this.frameField.getValue();
	}

	private static JFormattedTextField createNumericField(int min, int max) {
		return new JFormattedTextField(createNumberFormatter(min, max,
				createNumberFormat()));
	}

	private static NumberFormatter createNumberFormatter(int min, int max,
			NumberFormat numberFormat) {
		NumberFormatter formatter = new NumberFormatter(numberFormat);
		formatter.setValueClass(Integer.class);
		formatter.setMinimum(min);
		formatter.setMaximum(max);
		formatter.setAllowsInvalid(false);
		return formatter;
	}

	private static NumberFormat createNumberFormat() {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setGroupingUsed(false);
		return nf;
	}

	public boolean showDialog() {
		setVisible(true);
		return okWasPressed;
	}

}

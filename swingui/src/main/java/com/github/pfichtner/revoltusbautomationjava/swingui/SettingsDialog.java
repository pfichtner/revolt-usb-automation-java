package com.github.pfichtner.revoltusbautomationjava.swingui;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GridLayout;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.text.NumberFormatter;

public class SettingsDialog extends JDialog {

	private static final long serialVersionUID = 6344039963255252742L;

	private JFormattedTextField frameField = createNumericField(3, 255);
	private JFormattedTextField idField = createNumericField(0, 65535);

	public SettingsDialog(Frame owner) {
		super(owner, true);
		setResizable(false);
		setLocationRelativeTo(owner);
		setLayout(new GridLayout(3, 2));
		Container c = this.getContentPane();
		c.add(new JLabel("Frame 3-255"));

		c.add(frameField);
		c.add(new JLabel("ID 0-65535"));
		c.add(idField);
		c.add(new JButton("Ok"));
		c.add(new JButton("Cancel"));
		pack();
	}

	public void setId(int id) {
		this.idField.setValue(id);
	}

	public int getId() {
		return (Integer) this.frameField.getValue();
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

}

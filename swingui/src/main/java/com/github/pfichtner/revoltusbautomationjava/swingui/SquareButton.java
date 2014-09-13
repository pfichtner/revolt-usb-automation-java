package com.github.pfichtner.revoltusbautomationjava.swingui;

import java.awt.Dimension;

import javax.swing.JButton;

public class SquareButton extends JButton {

	private static final long serialVersionUID = 1339527454325437908L;

	public SquareButton() {
		super();
	}

	public SquareButton(String text) {
		super(text);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		int max = (int) Math.max(d.getHeight(), d.getWidth());
		return new Dimension(max, max);
	}

}

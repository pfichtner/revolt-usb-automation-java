package com.github.pfichtner.revoltusbautomationjava.swingui;

import java.awt.Container;
import java.awt.EventQueue;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class SwingUI extends JFrame {

	private static final long serialVersionUID = -7029240022142504077L;

	public SwingUI() {
		setTitle("SwingUI");
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new GridLayout(5, 3));
		Container contentPane = getContentPane();
		for (int i = 0; i < 4; i++) {
			contentPane.add(new JButton("On"));
			JLabel label = new JLabel(String.valueOf(i));
			label.setHorizontalAlignment(SwingConstants.CENTER);
			contentPane.add(label);
			contentPane.add(new JButton("Off"));
		}
		pack();
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

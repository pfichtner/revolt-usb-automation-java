package com.github.pfichtner.revoltusbautomationjava.swingui;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class JLabelExceptionHandler implements UncaughtExceptionHandler {

	private final JLabel status;

	public JLabelExceptionHandler(JLabel status) {
		this.status = status;
	}

	public void uncaughtException(Thread t, final Throwable e) {
		if (SwingUtilities.isEventDispatchThread()) {
			status.setText(e.getMessage());
		} else {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						status.setText(e.getMessage());
					}
				});
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			} catch (InvocationTargetException ite) {
				// not much more we can do here except log the exception
				ite.getCause().printStackTrace();
			}
		}
	}

}

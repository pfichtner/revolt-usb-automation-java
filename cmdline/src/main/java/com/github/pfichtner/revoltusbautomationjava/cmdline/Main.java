package com.github.pfichtner.revoltusbautomationjava.cmdline;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.github.pfichtner.revoltusbautomationjava.message.Function;
import com.github.pfichtner.revoltusbautomationjava.message.Message.MessageBuilder;
import com.github.pfichtner.revoltusbautomationjava.message.Outlet;
import com.github.pfichtner.revoltusbautomationjava.message.State;
import com.github.pfichtner.revoltusbautomationjava.message.Trimmer;
import com.github.pfichtner.revoltusbautomationjava.usb.Usb;
import com.github.pfichtner.revoltusbautomationjava.usb.UsbUsb4Java;

public class Main {

	@Option(name = "-vendorId")
	private short vendorId = (short) 0xffff;

	@Option(name = "-productId")
	private short productId = 0x1122;

	private Outlet[] outlets;

	@Option(name = "-outlet", metaVar = "1-4 or ALL", required = true)
	public void setOutlet(String outlet) {
		this.outlets = "ALL".equalsIgnoreCase(outlet) ? Outlet.all()
				: new Outlet[] { Outlet.of(Integer.parseInt(Trimmer.on('0')
						.trim(outlet))) };
	}

	@Option(name = "-state", required = true)
	private State state;

	private Integer rawFrames = 10;

	@Option(name = "-rawFrames", metaVar = "How many times the frame should be sent (3-255)")
	public void setRawFrames(int rawFrames) {
		if (rawFrames < 3 || rawFrames > 255) {
			throw new IllegalStateException("rawFrames must be 3-255");
		}
		this.rawFrames = rawFrames;
	}

	@Option(name = "-rawId")
	private Integer rawId;

	@Option(name = "-msgFin")
	private String msgFin;

	// ------------------------------------------------------------------------

	@Option(name = "--usbInterface", hidden = true)
	private Integer interfaceNum;

	@Option(name = "--usbEndpoint", hidden = true)
	private Byte outEndpoint;

	@Option(name = "--usbTimeout", hidden = true, metaVar = "usb send timeout in milliseconds")
	private Long timeout;

	// ------------------------------------------------------------------------

	public static void main(String[] args) throws IOException {
		new Main().doMain(args);
	}

	private void doMain(String[] args) throws IOException {
		CmdLineParser cmdLineParser = new CmdLineParser(this);
		try {
			cmdLineParser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			cmdLineParser.printUsage(System.err);
			return;
		}
		Usb usb = newUsb();
		try {
			usb.write(newMessageGenerator().build(
					Function.of(this.outlets, this.state)).asBytes());
		} finally {
			usb.close();
		}
	}

	private Usb newUsb() {
		UsbUsb4Java usb = UsbUsb4Java.newInstance(this.vendorId, this.productId).connect();
		usb = this.interfaceNum == null ? usb : usb
				.interfaceNum(this.interfaceNum.intValue());
		usb = this.outEndpoint == null ? usb : usb.setOutEndpoint(this.outEndpoint
				.byteValue());
		usb = this.timeout == null ? usb : usb.timeout(TimeUnit.MILLISECONDS,
				this.timeout.longValue());
		return usb;
	}

	private MessageBuilder newMessageGenerator() {
		MessageBuilder generator = new MessageBuilder();
		generator = this.rawId == null ? generator : generator
				.rawId(this.rawId);
		generator = this.rawFrames == null ? generator : generator
				.rawFrames(this.rawFrames);
		generator = this.msgFin == null ? generator : generator
				.msgFin(this.msgFin);
		return generator;
	}
}

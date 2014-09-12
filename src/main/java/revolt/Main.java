package revolt;

import static revolt.message.Strings.padLeft;
import static revolt.message.Strings.trim;

import java.util.concurrent.TimeUnit;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import revolt.message.Function;
import revolt.message.MessageGenerator;
import revolt.message.Outlet;
import revolt.message.State;
import revolt.usb.Usb;

public class Main {

	@Option(name = "-vendorId")
	private short vendorId = (short) 0xffff;

	@Option(name = "-productId")
	private short productId = 0x1122;

	private Outlet[] outlet;

	@Option(name = "-outlet", metaVar = "1-4 or ALL", required = true)
	public void setOutlet(String outlet) {
		this.outlet = Outlet.forString(trim(outlet, '0'));
	}

	@Option(name = "-state", required = true)
	private State state;

	private int rawFrames = 10;

	@Option(name = "-rawFrames", metaVar = "How many times the frame should be sent (3-255)")
	public void setRawFrames(int rawFrames) {
		if (rawFrames < 3 || rawFrames > 255) {
			throw new IllegalStateException("rawFrames must be 3-255");
		}
		this.rawFrames = rawFrames;
	}

	@Option(name = "-rawId")
	private int rawId = 6789;

	private String msgFin = "0000";

	@Option(name = "-msgFin")
	public void setMsgFin(String msgFin) {
		int length = msgFin.length();
		this.msgFin = padLeft(msgFin, '0', length).substring(0, length);
	}

	// ------------------------------------------------------------------------

	@Option(name = "--usbInterface", hidden = true)
	private Integer interfaceNum;

	@Option(name = "--usbEndpoint", hidden = true)
	private Byte outEndpoint;

	@Option(name = "--usbTimeout", hidden = true, metaVar = "usb send timeout in milliseconds")
	private Long timeout;

	// ------------------------------------------------------------------------

	public static void main(String[] args) {
		new Main().doMain(args);
	}

	private void doMain(String[] args) {
		CmdLineParser cmdLineParser = new CmdLineParser(this);
		try {
			cmdLineParser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			cmdLineParser.printUsage(System.err);
			return;
		}
		Usb usb = configure(new Usb(this.vendorId, this.productId));
		try {
			usb.write(createMessageGenerator().byteMessage());
		} finally {
			usb.close();
		}
	}

	private Usb configure(Usb usb) {
		if (this.interfaceNum != null) {
			usb.setInterfaceNum(this.interfaceNum.intValue());
		}
		if (this.outEndpoint != null) {
			usb.setOutEndpoint(this.outEndpoint.byteValue());
		}
		if (this.timeout != null) {
			usb.setTimeout(TimeUnit.MILLISECONDS, this.timeout.longValue());
		}
		return usb;
	}

	private MessageGenerator createMessageGenerator() {
		return new MessageGenerator(Function.of(this.outlet, this.state),
				this.rawFrames, this.rawId, this.msgFin);
	}
}

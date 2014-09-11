package revolt.message;

import static revolt.message.Primitives.intToHex;
import static revolt.message.Strings.padLeft;

import java.util.Arrays;

public class Function {

	private boolean isAll;
	private Outlet outlet;
	private State state;

	public Function(Outlet[] outlets, State state) {
		isAll = Outlet.isAll(outlets);
		if (outlets.length != 1 && !isAll) {
			throw new RuntimeException(
					"Can only handle one outlet or all (got "
							+ Arrays.toString(outlets) + ")");
		}
		if (!isAll) {
			this.outlet = outlets[0];
		}
		this.state = state;
	}

	public static Function of(Outlet[] outlets, State state) {
		return new Function(outlets, state);
	}

	public int asInt() {
		return outletNoAsInt() + stateAsInt();
	}

	private int outletNoAsInt() {
		return isAll ? 1 : 8 + (4 - outlet.getIndex()) * 2;
	}

	private int stateAsInt() {
		return state == State.ON ? 1 : 0;
	}

	public String asByte() {
		return padLeft(Integer.toBinaryString(asInt()), '0', 4);
	}

	public String asHex() {
		return intToHex(asInt());
	}

}

package com.github.pfichtner.revoltusbautomationjava.message;

import static com.github.pfichtner.revoltusbautomationjava.message.Padder.leftPadder;
import static com.github.pfichtner.revoltusbautomationjava.message.Primitives.intToHex;

import java.util.Arrays;

public abstract class Function {

	private static final class SingleOutletFunction extends Function {

		private final Outlet outlet;

		public SingleOutletFunction(Outlet outlet, State state) {
			super(state);
			this.outlet = outlet;
		}

		@Override
		int outletNoAsInt() {
			return 8 + (4 - this.outlet.getIndex()) * 2;
		}

	}

	private static final class AllOutletsFunction extends Function {

		public AllOutletsFunction(State state) {
			super(state);
		}

		@Override
		int outletNoAsInt() {
			return 1;
		}

	}

	private final State state;

	private Function(State state) {
		this.state = state;
	}

	public static Function of(Outlet outlet, State state) {
		return of(new Outlet[] { outlet }, state);
	}

	public static Function of(Outlet[] outlets, State state) {
		boolean isAll = Outlet.isAll(outlets);
		if (outlets.length != 1 && !isAll) {
			throw new RuntimeException(
					"Can only handle one outlet or all (got "
							+ Arrays.toString(outlets) + ")");
		}
		return isAll ? new AllOutletsFunction(state)
				: new SingleOutletFunction(outlets[0], state);
	}

	public int asInt() {
		return outletNoAsInt() + stateAsInt();
	}

	abstract int outletNoAsInt();

	private int stateAsInt() {
		return this.state == State.ON ? 1 : 0;
	}

	public String asByte() {
		return leftPadder('0', 4).pad(Integer.toBinaryString(asInt()));
	}

	public String asHex() {
		return intToHex(asInt());
	}

}

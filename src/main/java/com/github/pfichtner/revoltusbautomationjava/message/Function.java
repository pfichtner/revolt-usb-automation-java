package com.github.pfichtner.revoltusbautomationjava.message;

import static com.github.pfichtner.revoltusbautomationjava.message.Primitives.intToHex;
import static com.github.pfichtner.revoltusbautomationjava.message.Strings.padLeft;

import java.util.Arrays;

public class Function {

	private boolean isAll;
	private Outlet outlet;
	private final State state;

	private Function(Outlet outlet, State state) {
		this.outlet = outlet;
		this.state = state;
	}

	private Function(State state) {
		this.isAll = true;
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
		return isAll ? new Function(state) : new Function(outlets[0], state);

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

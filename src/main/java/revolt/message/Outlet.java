package revolt.message;

import java.util.Arrays;

public enum Outlet {

	ONE(1), TWO(2), THREE(3), FOUR(4);

	private int index;

	private Outlet(int index) {
		this.index = index;
	}

	public static Outlet forInt(int index) {
		Outlet[] values = values();
		for (Outlet outlet : values) {
			if (outlet.index == index) {
				return outlet;
			}
		}
		throw new IllegalStateException(index + " is not a valid outlet index");
	}

	public int getIndex() {
		return index;
	}

	public static Outlet[] forString(String index) {
		return "ALL".equalsIgnoreCase(index) ? values()
				: new Outlet[] { forInt(Integer.parseInt(index)) };
	}

	public static boolean isAll(Outlet[] o) {
		return Arrays.equals(o, values());
	}

}

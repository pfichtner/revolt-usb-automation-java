package revolt.message;

public enum State {

	ON, OFF;

	public static State forString(String state) {
		return "ON".equalsIgnoreCase(state) ? ON : OFF;
	}

}

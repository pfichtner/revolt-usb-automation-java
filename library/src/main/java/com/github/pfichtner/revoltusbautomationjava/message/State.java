package com.github.pfichtner.revoltusbautomationjava.message;

public enum State {

	ON("ON"), OFF("OFF");

	private final String identifier;

	public static State forString(String stateIdentifier) {
		for (State state : values()) {
			if (state.getIdentifier().equalsIgnoreCase(stateIdentifier)) {
				return state;
			}
		}
		throw new IllegalStateException("Unknown state " + stateIdentifier);
	}

	private State(String identifier) {
		this.identifier = identifier;
	}

	public String getIdentifier() {
		return this.identifier;
	}

}

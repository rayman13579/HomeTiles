package at.rayman.hometiles.tile.model;

import java.util.Arrays;

public enum State {

	OPEN("open"),
	CLOSED("closed"),
	OPENING("opening"),
	CLOSING("closing"),
	STOPPED("stopped"),
	ERROR("error");

	private final String state;

	State(String state) {
		this.state = state;
	}

	public String getState() {
		return state;
	}

	public static State forValue(String name) {
		return Arrays.stream(State.values())
			.filter(state -> state.getState().equals(name))
			.findFirst()
			.orElse(State.ERROR);
	}

}

package at.rayman.hometiles.tile.model;

public class Blind {

	private String shelly;

	private State state;

	private int position;

	public Blind(String shelly) {
		this.shelly = shelly;
	}

	public String getShelly() {
		return shelly;
	}

	public State getState() {
		return state;
	}

	public int getPosition() {
		return position;
	}

}

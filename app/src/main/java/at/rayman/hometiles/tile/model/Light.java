package at.rayman.hometiles.tile.model;

public class Light {

	private String shelly;

	private boolean on;

	public Light(String shelly) {
		this.shelly = shelly;
	}

	public String getShelly() {
		return shelly;
	}

	public boolean isOn() {
		return on;
	}

}

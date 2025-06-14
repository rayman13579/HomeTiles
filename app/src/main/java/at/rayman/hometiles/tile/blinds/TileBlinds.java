package at.rayman.hometiles.tile.blinds;

import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;

import org.json.JSONObject;

import java.util.Map;

import at.rayman.hometiles.tile.model.Blind;
import at.rayman.hometiles.tile.model.State;
import io.socket.client.Socket;

public abstract class TileBlinds extends at.rayman.hometiles.tile.Tile {

	protected State state = State.ERROR;

	protected int position = 0;

	protected void getState() {
		socket.on(Socket.EVENT_CONNECT, args -> {
			socket.emit("blind", new JSONObject(Map.of("shelly", getName())));
		});

		socket.on("blind", args -> {
			Blind blind = gson.fromJson(args[0].toString(), Blind.class);

			if (!blind.getShelly().equalsIgnoreCase(getName())) return;

			Tile tile = getQsTile();
			tile.setIcon(Icon.createWithResource(getApplicationContext(), getIcon(blind.getState())));
			tile.setLabel(getLabel(blind.getPosition()));
			tile.setState(getTileState(blind.getState()));
			tile.updateTile();

			this.state = blind.getState();
			this.position = blind.getPosition();
		});
	}

	protected void setState(String action) {
		socket.emit("blindStatus", new JSONObject(Map.of("shelly", getName(), "action", action)));
	}

	protected void setPosition(int pos) {
		socket.emit("blindPosition", new JSONObject(Map.of("shelly", getName(), "position", pos)));
	}

	protected abstract int getTileState(State state);

	protected abstract int getIcon(State state);

	protected abstract String getLabel(int position);

}

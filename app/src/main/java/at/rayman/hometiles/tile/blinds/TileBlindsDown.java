package at.rayman.hometiles.tile.blinds;

import android.service.quicksettings.Tile;

import at.rayman.hometiles.R;
import at.rayman.hometiles.tile.model.State;

public abstract class TileBlindsDown extends TileBlinds {

	@Override
	public void onClick() {
		switch (state) {
			case OPEN:
			case OPENING:
			case CLOSED:
			case STOPPED:
				setState("close");
				break;
			case CLOSING:
				setState("stop");
				break;
			case ERROR:
			default:
				break;
		}
	}

	@Override
	protected int getTileState(State state) {
		switch (state) {
			case OPEN:
			case OPENING:
			case CLOSED:
			case STOPPED:
				return Tile.STATE_INACTIVE;
			case CLOSING:
				return Tile.STATE_ACTIVE;
			case ERROR:
			default:
				return Tile.STATE_UNAVAILABLE;
		}
	}

	@Override
	protected int getIcon(State state) {
		switch (state) {
			case OPEN:
			case OPENING:
			case CLOSED:
			case STOPPED:
				return R.drawable.arrow_down;
			case CLOSING:
				return R.drawable.pause;
			case ERROR:
			default:
				return R.drawable.arrow_down_error;
		}
	}

	@Override
	protected String getLabel(int position) {
		return getName();
	}

	@Override
	protected int getErrorImage() {
		return R.drawable.arrow_down_error;
	}
}

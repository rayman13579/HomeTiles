package at.rayman.hometiles.tile.blinds;

import android.service.quicksettings.Tile;

import at.rayman.hometiles.R;
import at.rayman.hometiles.tile.model.State;

public abstract class TileBlindsUp extends TileBlinds {

	@Override
	public void onClick() {
		switch (state) {
			case OPEN:
			case CLOSED:
			case CLOSING:
			case STOPPED:
				setState("open");
				break;
			case OPENING:
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
			case CLOSED:
			case CLOSING:
			case STOPPED:
				return Tile.STATE_INACTIVE;
			case OPENING:
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
			case CLOSED:
			case CLOSING:
			case STOPPED:
				return R.drawable.arrow_up;
			case OPENING:
				return R.drawable.pause;
			case ERROR:
			default:
				return R.drawable.arrow_up_error;
		}
	}

	@Override
	protected String getLabel(int position) {
		return position + "%";
	}

	@Override
	protected int getErrorImage() {
		return R.drawable.arrow_up_error;
	}
}

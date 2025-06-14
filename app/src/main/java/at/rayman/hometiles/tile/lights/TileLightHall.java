package at.rayman.hometiles.tile.lights;

import at.rayman.hometiles.R;

public class TileLightHall extends TileLights {

    protected String getName() {
        return getResources().getString(R.string.light_hall);
    }

    protected String getIp() {
        return getResources().getString(R.string.light_hall_url);
    }

}

package at.rayman.hometiles.tile.lights;

import at.rayman.hometiles.R;

public class TileLightRoom extends TileLights {

    protected String getName() {
        return getResources().getString(R.string.light_room);
    }

    protected String getIp() {
        return getResources().getString(R.string.light_room_url);
    }

}

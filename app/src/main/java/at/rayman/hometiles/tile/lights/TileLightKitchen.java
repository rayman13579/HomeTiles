package at.rayman.hometiles.tile.lights;

import at.rayman.hometiles.R;

public class TileLightKitchen extends TileLights {

    protected String getName() {
        return getResources().getString(R.string.light_kitchen);
    }

    protected String getIp() {
        return getResources().getString(R.string.light_kitchen_url);
    }

}

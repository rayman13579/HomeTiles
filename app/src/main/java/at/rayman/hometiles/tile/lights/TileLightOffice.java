package at.rayman.hometiles.tile.lights;

import at.rayman.hometiles.R;

public class TileLightOffice extends TileLights {

    protected String getName() {
        return getResources().getString(R.string.light_office);
    }

    protected String getIp() {
        return getResources().getString(R.string.light_office_url);
    }

}

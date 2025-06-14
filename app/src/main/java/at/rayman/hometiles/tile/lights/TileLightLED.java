package at.rayman.hometiles.tile.lights;

import at.rayman.hometiles.R;

public class TileLightLED extends TileLights {

    protected String getName() {
        return getResources().getString(R.string.light_led);
    }

    protected String getIp() {
        return getResources().getString(R.string.light_led_url);
    }

}

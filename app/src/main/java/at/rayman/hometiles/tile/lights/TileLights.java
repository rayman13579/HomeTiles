package at.rayman.hometiles.tile.lights;

import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import at.rayman.hometiles.R;
import at.rayman.hometiles.tile.model.Light;
import at.rayman.hometiles.network.RestClient;
import io.socket.client.IO;
import io.socket.client.Socket;

public abstract class TileLights extends at.rayman.hometiles.tile.Tile {

    @Override
    public void onStartListening() {
        if (isHomeWifi()) {
            getStateAtHome();
            return;
        }
        try {
            IO.Options options = IO.Options.builder()
                .setExtraHeaders(Map.of("app", List.of("Tiles " + getName())))
                .build();
            socket = IO.socket(getString(R.string.url), options);
            socket.connect();
            socket.on(Socket.EVENT_CONNECT_ERROR, args -> error());

            getState();
        } catch (URISyntaxException e) {
            error();
        }
    }

    protected void getState() {
        socket.on(Socket.EVENT_CONNECT, args -> {
            socket.emit("light", new JSONObject(Map.of("shelly", getName())));
        });

        socket.on("light", args -> {
            Light light = gson.fromJson(args[0].toString(), Light.class);

            if (!light.getShelly().equals(getName())) return;

            if (light.isOn()) {
                activate();
            } else {
                deactivate();
            }
        });
    }

    @Override
    public void onClick() {
        if (isHomeWifi()) {
            onClickAtHome();
            return;
        }
        socket.emit("toggleLight", new JSONObject(Map.of("shelly", getName(), "on", isTileActive())));
    }

    protected int getErrorImage() {
        return R.drawable.tile_light_error;
    }

    private void activate() {
        Tile tile = getQsTile();
        tile.setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.tile_light_on));
        tile.setState(Tile.STATE_ACTIVE);
        tile.updateTile();
    }

    private void deactivate() {
        Tile tile = getQsTile();
        tile.setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.tile_light_off));
        tile.setState(Tile.STATE_INACTIVE);
        tile.updateTile();
    }

    protected boolean isTileActive() {
        return getQsTile().getState() == Tile.STATE_ACTIVE;
    }

    protected void getStateAtHome() {
        getState(new StatusCallback() {
            @Override
            public void on() {
                activate();
            }

            @Override
            public void off() {
                deactivate();
            }
        });
    }

    public void onClickAtHome() {
        getState(new StatusCallback() {
            @Override
            public void on() {
                setState(false);
                deactivate();
            }

            @Override
            public void off() {
                setState(true);
                activate();
            }
        });
    }

    private void getState(StatusCallback callback) {
        String url = "http://" + getIp() + "/rpc/Switch.GetStatus?id=0";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            try {
                if (new JSONObject(response).getBoolean("output")) {
                    callback.on();
                } else {
                    callback.off();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, this::error);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(500, 2, 2));

        RestClient.getInstance(this).addRequest(stringRequest);
    }

    private void setState(boolean status) {
        String url = "http://" + getIp() + "/rpc/Switch.Set?id=0&on=" + status;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, null, this::error);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(500, 2, 2));

        RestClient.getInstance(this).addRequest(stringRequest);
    }

    private interface StatusCallback {
        void on();

        void off();
    }

    protected abstract String getIp();

}

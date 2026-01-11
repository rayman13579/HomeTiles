package at.rayman.hometiles.tile;

import android.graphics.drawable.Icon;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.service.quicksettings.TileService;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import at.rayman.hometiles.R;
import at.rayman.hometiles.tile.model.State;
import at.rayman.hometiles.tile.model.StateDeserializer;
import io.socket.client.IO;
import io.socket.client.Socket;

public abstract class Tile extends TileService {

    protected Socket socket;

    protected boolean keepSocketAlive = false;

    protected Gson gson = new GsonBuilder()
            .registerTypeAdapter(State.class, new StateDeserializer())
            .create();

    @Override
    public void onStartListening() {
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

    @Override
    public void onStopListening() {
        if (!keepSocketAlive) {
            disconnectSocket();
        }
    }

    protected boolean isHomeWifi() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String name = wifiInfo.getSSID().replace("\"", "");
        String ip = formatIp(wifiInfo.getIpAddress());
        return getResources().getString(R.string.home_wifi_name).equals(name)
                && getResources().getString(R.string.home_ip_address).equals(ip);
    }

    private String formatIp(int ipAddress) {
        return String.format(Locale.getDefault(), "%d.%d.%d.%d",
                (ipAddress & 0xFF),
                (ipAddress >> 8 & 0xFF),
                (ipAddress >> 16 & 0xFF),
                (ipAddress >> 24 & 0xFF));
    }

    protected void disconnectSocket() {
        if (socket != null) {
            socket.disconnect();
            socket.off();
        }
    }

    protected void error() {
        android.service.quicksettings.Tile tile = getQsTile();
        tile.setIcon(Icon.createWithResource(getApplicationContext(), getErrorImage()));
        tile.setState(android.service.quicksettings.Tile.STATE_INACTIVE);
        tile.updateTile();
    }

    protected void error(Exception e) {
        e.printStackTrace();
        error();
    }

    protected abstract void getState();

    protected abstract String getName();

    protected abstract int getErrorImage();

}

package at.rayman.hometiles.controls;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.os.Looper;
import android.service.controls.Control;
import android.service.controls.ControlsProviderService;
import android.service.controls.DeviceTypes;
import android.service.controls.actions.BooleanAction;
import android.service.controls.actions.ControlAction;
import android.service.controls.actions.FloatAction;
import android.service.controls.templates.ControlButton;
import android.service.controls.templates.RangeTemplate;
import android.service.controls.templates.ToggleTemplate;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;
import org.reactivestreams.FlowAdapters;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import at.rayman.hometiles.R;
import at.rayman.hometiles.tile.model.Blind;
import at.rayman.hometiles.tile.model.Light;
import at.rayman.hometiles.tile.model.State;
import at.rayman.hometiles.tile.model.StateDeserializer;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.ReplayProcessor;
import io.socket.client.IO;
import io.socket.client.Socket;

public class DeviceControlsProvider extends ControlsProviderService {

    private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(State.class, new StateDeserializer())
        .create();

    private final ReplayProcessor<Control> processor = ReplayProcessor.create();

    private Socket socket;

    private List<String> lights;

    private List<String> blinds;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            socket = IO.socket(getString(R.string.ws_url));
            socket.connect();
        } catch (Exception ignored) {
        }
        lights = List.of(
            getResources().getString(R.string.light_hall),
            getResources().getString(R.string.light_office),
            getResources().getString(R.string.light_led),
            getResources().getString(R.string.light_kitchen),
            getResources().getString(R.string.light_room));
        blinds = List.of(
            getResources().getString(R.string.blind_balcony),
            getResources().getString(R.string.blind_office),
            getResources().getString(R.string.blind_kitchen));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        socket.close();
    }

    @NonNull
    @Override
    public Flow.Publisher<Control> createPublisherForAllAvailable() {
        List<Control> controls = Stream.concat(
                lights.stream().map(this::createStatelessLightControl),
                blinds.stream().map(this::createStatelessBlindControl))
            .collect(Collectors.toList());

        return FlowAdapters.toFlowPublisher(Flowable.fromIterable(controls));
    }

    @NonNull
    @Override
    public Flow.Publisher<Control> createPublisherFor(@NonNull List<String> controlIds) {
        if (socket != null) {
            socket.on(Socket.EVENT_CONNECT, args -> {
                lights.forEach(l -> addLightControl(l, controlIds.contains(l + "Light")));
                blinds.forEach(b -> addBlindControl(b, controlIds.contains(b + "Blind")));
            });
        }
        return FlowAdapters.toFlowPublisher(processor);
    }

    @Override
    public void performControlAction(@NonNull String controlId, @NonNull ControlAction action, @NonNull Consumer<Integer> consumer) {
        String name = controlId.substring(0, controlId.length() - 5);
        if (action instanceof BooleanAction) {
            socket.emit("toggleLight", new JSONObject(Map.of("shelly", name, "on", !((BooleanAction) action).getNewState())));
        } else if (action instanceof FloatAction) {
            float position = ((FloatAction) action).getNewValue();
            socket.emit("blindPosition", new JSONObject(Map.of("shelly", name, "position", position)));
    //        animateBlind(name, position);
        }
        consumer.accept(ControlAction.RESPONSE_OK);
    }

    private Control createStatelessLightControl(String name) {
        String pascalCaseName = name.substring(0, 1).toUpperCase() + name.substring(1);
        return new Control.StatelessBuilder(name + "Light", getIntent())
            .setTitle(pascalCaseName)
            .setSubtitle(pascalCaseName + " Light")
            .setDeviceType(DeviceTypes.TYPE_LIGHT)
            .build();
    }

    private void addLightControl(String name, boolean valid) {
        if (!valid) return;
        socket.emit("light", new JSONObject(Map.of("shelly", name)));

        socket.on("light", args -> {
            Light light = gson.fromJson(args[0].toString(), Light.class);

            if (!light.getShelly().equals(name)) return;

            String pascalCaseName = name.substring(0, 1).toUpperCase() + name.substring(1);
            Control control = new Control.StatefulBuilder(name + "Light", getIntent())
                .setTitle(pascalCaseName)
                .setSubtitle(pascalCaseName + " Light")
                .setDeviceType(DeviceTypes.TYPE_LIGHT)
                .setStatus(Control.STATUS_OK)
                .setControlTemplate(new ToggleTemplate(name + "Light", new ControlButton(light.isOn(), "Toggle")))
                .setAuthRequired(false)
                .build();
            processor.onNext(control);
        });
    }

    private Control createStatelessBlindControl(String name) {
        String pascalCaseName = name.substring(0, 1).toUpperCase() + name.substring(1);
        return new Control.StatelessBuilder(name + "Blind", getIntent())
            .setTitle(pascalCaseName)
            .setSubtitle(pascalCaseName + " Blind")
            .setDeviceType(DeviceTypes.TYPE_BLINDS)
            .build();
    }

    private void addBlindControl(String name, boolean valid) {
        if (!valid) return;

        socket.emit("blind", new JSONObject(Map.of("shelly", name)));

        socket.on("blind", args -> {
            Blind blind = gson.fromJson(args[0].toString(), Blind.class);

            if (!blind.getShelly().equalsIgnoreCase(name)) return;

            String pascalCaseName = name.substring(0, 1).toUpperCase() + name.substring(1);
            Control control = new Control.StatefulBuilder(name + "Blind", getIntent())
                .setTitle(pascalCaseName)
                .setSubtitle(pascalCaseName + " Blind")
                .setDeviceType(DeviceTypes.TYPE_BLINDS)
                .setStatus(Control.STATUS_OK)
                .setControlTemplate(new RangeTemplate(name + "Blind", 0, 100, blind.getPosition(), 1, "%.0f%%"))
                .setCustomColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.orange)))
                .setAuthRequired(false)
                .build();
            processor.onNext(control);
        });
    }

    private PendingIntent getIntent() {
        return PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), PendingIntent.FLAG_IMMUTABLE);
    }

    private void animateBlind(String name, float position) {
        socket.emit("blind", new JSONObject(Map.of("shelly", name)));

        socket.on("blind", args -> {
            Blind blind = gson.fromJson(args[0].toString(), Blind.class);

            if (!blind.getShelly().equalsIgnoreCase(name)) return;

            String pascalCaseName = name.substring(0, 1).toUpperCase() + name.substring(1);
            Control control = new Control.StatefulBuilder(name + "Blind", getIntent())
                .setTitle(pascalCaseName)
                .setSubtitle(pascalCaseName + " Blind")
                .setDeviceType(DeviceTypes.TYPE_BLINDS)
                .setStatus(Control.STATUS_OK)
                .setControlTemplate(new RangeTemplate(name + "Blind", 0, 100, blind.getPosition(), 1, "%.0f%%"))
                .setCustomColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.orange)))
                .setAuthRequired(false)
                .build();
            processor.onNext(control);

            if (blind.getPosition() != position) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> animateBlind(name, position), 500);
            }
        });
    }


}

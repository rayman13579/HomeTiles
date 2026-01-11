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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;

import org.json.JSONObject;
import org.reactivestreams.FlowAdapters;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import at.rayman.hometiles.R;
import at.rayman.hometiles.network.JsonRequest;
import at.rayman.hometiles.network.RestClient;
import at.rayman.hometiles.network.StringRequest;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.ReplayProcessor;

public class DeviceControlsProvider extends ControlsProviderService {

    private final ReplayProcessor<Control> processor = ReplayProcessor.create();

    private List<String> lights;

    private List<String> blinds;

    @Override
    public void onCreate() {
        super.onCreate();
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
        lights.forEach(l -> addLightControl(l, controlIds.contains(l + "Light")));
        blinds.forEach(b -> addBlindControl(b, controlIds.contains(b + "Blind")));
        return FlowAdapters.toFlowPublisher(processor);
    }

    @Override
    public void performControlAction(@NonNull String controlId, @NonNull ControlAction action, @NonNull Consumer<Integer> consumer) {
        String name = controlId.substring(0, controlId.length() - 5);
        if (action instanceof BooleanAction) {
            setLightState(name, ((BooleanAction) action).getNewState())
                .thenCompose(isOn -> getLightState(name))
                .thenAccept(isOn -> updateLightControl(name, isOn));
        } else if (action instanceof FloatAction) {
            int newPosition = Math.round(((FloatAction) action).getNewValue());
            setBlindState(name, newPosition)
                .thenCompose(position -> pollBlindPosition(name, newPosition))
                .thenAccept(position -> updateBlindControl(name, position));
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
        getLightState(name)
            .thenAccept(isOn -> updateLightControl(name, isOn));
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
        getBlindPosition(name)
            .thenAccept(position -> updateBlindControl(name, position));
    }

    private CompletableFuture<Boolean> getLightState(String name) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.url) + "/status?shelly=" + name,
            response -> result.complete(Boolean.valueOf(response)), Throwable::printStackTrace);

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(500, 2, 2));
        RestClient.getInstance(this).addRequest(stringRequest);
        return result;
    }

    private CompletableFuture<Boolean> setLightState(String name, boolean isOn) {
        updateLightControl(name, isOn, Control.STATUS_UNKNOWN);
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        JsonRequest jsonRequest = new JsonRequest(Request.Method.PUT, getString(R.string.url) + "/status",
            new JSONObject(Map.of("shelly", name, "on", isOn)),
            response -> result.complete(isOn),
            error -> updateLightControl(name, isOn, Control.STATUS_ERROR));

        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(500, 2, 2));
        RestClient.getInstance(this).addRequest(jsonRequest);
        return result;
    }

    private void updateLightControl(String name, boolean isOn) {
        updateLightControl(name, isOn, Control.STATUS_OK);
    }

    private void updateLightControl(String name, boolean isOn, int status) {
        String pascalCaseName = name.substring(0, 1).toUpperCase() + name.substring(1);
        Control control = new Control.StatefulBuilder(name + "Light", getIntent())
            .setTitle(pascalCaseName)
            .setSubtitle(pascalCaseName + " Light")
            .setDeviceType(DeviceTypes.TYPE_LIGHT)
            .setStatus(status)
            .setControlTemplate(new ToggleTemplate(name + "Light", new ControlButton(isOn, "Toggle")))
            .setAuthRequired(false)
            .build();
        processor.onNext(control);
    }

    private CompletableFuture<Integer> getBlindPosition(String name) {
        CompletableFuture<Integer> result = new CompletableFuture<>();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.url) + "/position?shelly=" + name,
            response -> result.complete(Integer.valueOf(response)), Throwable::printStackTrace);

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(500, 2, 2));
        RestClient.getInstance(this).addRequest(stringRequest);
        return result;
    }

    private CompletableFuture<Integer> setBlindState(String name, int position) {
        CompletableFuture<Integer> result = new CompletableFuture<>();

        JsonRequest jsonRequest = new JsonRequest(Request.Method.PUT, getString(R.string.url) + "/position",
            new JSONObject(Map.of("shelly", name, "position", position)),
            response -> result.complete(position),
            error -> updateBlindControl(name, position, Control.STATUS_ERROR));

        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(500, 2, 2));
        RestClient.getInstance(this).addRequest(jsonRequest);
        return result;
    }

    private CompletableFuture<Integer> pollBlindPosition(String name, int newPosition) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        pollBlindPosition(future, name, newPosition);
        return future;
    }

    private void pollBlindPosition(CompletableFuture<Integer> future, String name, int newPosition) {
        getBlindPosition(name).thenAccept(currentPosition -> {
            updateBlindControl(name, currentPosition);
            if (currentPosition == newPosition) {
                future.complete(currentPosition);
                return;
            }
            new Handler(Looper.getMainLooper()).postDelayed(() -> pollBlindPosition(future, name, newPosition), 500);
        });
    }

    private void updateBlindControl(String name, int position) {
        updateBlindControl(name, position, Control.STATUS_OK);
    }

    private void updateBlindControl(String name, int position, int status) {
        String pascalCaseName = name.substring(0, 1).toUpperCase() + name.substring(1);
        Control control = new Control.StatefulBuilder(name + "Blind", getIntent())
            .setTitle(pascalCaseName)
            .setSubtitle(pascalCaseName + " Blind")
            .setDeviceType(DeviceTypes.TYPE_BLINDS)
            .setStatus(status)
            .setControlTemplate(new RangeTemplate(name + "Blind", 0, 100, position, 1, "%.0f%%"))
            .setCustomColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), position == 100 ? R.color.yellow : R.color.orange)))
            .setAuthRequired(false)
            .build();
        processor.onNext(control);
    }

    private PendingIntent getIntent() {
        return PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), PendingIntent.FLAG_IMMUTABLE);
    }

}

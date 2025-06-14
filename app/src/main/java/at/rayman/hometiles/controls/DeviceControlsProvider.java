package at.rayman.hometiles.controls;

import android.app.PendingIntent;
import android.content.Intent;
import android.service.controls.Control;
import android.service.controls.ControlsProviderService;
import android.service.controls.DeviceTypes;
import android.service.controls.actions.ControlAction;
import android.service.controls.templates.ControlButton;
import android.service.controls.templates.ToggleTemplate;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.concurrent.Flow;
import java.util.function.Consumer;

public class DeviceControlsProvider extends ControlsProviderService {

    @NonNull
    @Override
    public Flow.Publisher<Control> createPublisherForAllAvailable() {
        Control control = new Control.StatefulBuilder("hallLight", PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), PendingIntent.FLAG_IMMUTABLE))
            .setTitle("Hall")
            .setSubtitle("Hall Light")
            .setDeviceType(DeviceTypes.TYPE_LIGHT)
            .build();

        return subscriber -> {
            subscriber.onNext(control);
            subscriber.onComplete();
        };
    }

    @NonNull
    @Override
    public Flow.Publisher<Control> createPublisherFor(@NonNull List<String> controlIds) {
        Control control = new Control.StatefulBuilder("hallLight", PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), PendingIntent.FLAG_IMMUTABLE))
            .setTitle("Hall")
            .setSubtitle("Hall Light")
            .setDeviceType(DeviceTypes.TYPE_LIGHT)
            .setStatus(Control.STATUS_OK)
            .setControlTemplate(new ToggleTemplate("hallLight", new ControlButton(true, "Slider")))
            .build();

        return subscriber -> {
            subscriber.onSubscribe(new Flow.Subscription() {
                @Override
                public void cancel() {

                }

                @Override
                public void request(long n) {

                }
            });
            subscriber.onNext(control);
            subscriber.onComplete();
        };
    }

    @Override
    public void performControlAction(@NonNull String controlId, @NonNull ControlAction action, @NonNull Consumer<Integer> consumer) {
        consumer.accept(ControlAction.RESPONSE_OK);
    }

}

package at.rayman.hometiles.tile.blinds;

import android.service.quicksettings.Tile;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.slider.Slider;

import java.text.NumberFormat;

import at.rayman.hometiles.R;
import at.rayman.hometiles.tile.model.State;

public abstract class TileBlindsPosition extends TileBlinds {

    @Override
    public void onClick() {
        if (State.ERROR.equals(state)) return;
        keepSocketAlive = true;

        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.slider_dialog, null);
        createSlider(dialogView);
        AlertDialog dialog = new AlertDialog.Builder(this, com.google.android.material.R.style.Theme_Material3_Light_NoActionBar)
            .setView(dialogView)
            .setTitle(getName())
            .setMessage("Change blind position")
            .setPositiveButton("Open", (d, which) -> {
                setState("open");
                disconnectSocket();
            })
            .setNegativeButton("Close", (d, which) -> {
                setState("close");
                disconnectSocket();
            })
            .setNeutralButton("Cancel", (d, which) -> disconnectSocket())
            .setOnCancelListener(d -> disconnectSocket())
            .create();
        showDialog(dialog);
        dialog.getWindow().setLayout(1000, 1400);
    }

    private void createSlider(View dialogView) {
        TextView sliderValue = dialogView.findViewById(R.id.sliderValue);
        sliderValue.setText(position + "%");
        Slider slider = dialogView.findViewById(R.id.slider);
        slider.setValue(position);
        slider.setLabelFormatter(value -> {
            NumberFormat format = NumberFormat.getIntegerInstance();
            return format.format(value) + "%";
        });
        slider.addOnChangeListener((s, v, fromUser) -> sliderValue.setText(Math.round(v) + "%"));
        slider.addOnSliderTouchListener(
            new Slider.OnSliderTouchListener() {
                @Override
                public void onStartTrackingTouch(Slider slider) {
                }

                @Override
                public void onStopTrackingTouch(Slider slider) {
                    setPosition(Math.round(slider.getValue()));
                }
            }
        );
    }

    @Override
    protected int getTileState(State state) {
        switch (state) {
            case OPEN:
            case OPENING:
            case CLOSED:
            case STOPPED:
            case CLOSING:
                return Tile.STATE_INACTIVE;
            case ERROR:
            default:
                return Tile.STATE_UNAVAILABLE;
        }
    }

    @Override
    protected int getIcon(State state) {
        return R.drawable.slider;
    }

    @Override
    protected String getLabel(int position) {
        return "Slider";
    }

    @Override
    protected int getErrorImage() {
        return R.drawable.slider_error;
    }
}

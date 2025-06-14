package at.rayman.hometiles.tile.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

import java.lang.reflect.Type;

public class StateDeserializer implements JsonDeserializer<State> {

	@Override
	public State deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
		return State.forValue(json.getAsString());
	}

}

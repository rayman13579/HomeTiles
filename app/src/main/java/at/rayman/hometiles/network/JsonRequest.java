package at.rayman.hometiles.network;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;

import org.json.JSONObject;

import java.util.Map;

public class JsonRequest extends com.android.volley.toolbox.JsonObjectRequest implements AuthRequest {

    public JsonRequest(int method, String url, @Nullable JSONObject jsonRequest, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return AuthRequest.super.getHeaders();
    }

}

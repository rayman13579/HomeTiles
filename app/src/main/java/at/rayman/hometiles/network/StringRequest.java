package at.rayman.hometiles.network;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;

import java.util.Map;

public class StringRequest extends com.android.volley.toolbox.StringRequest implements AuthRequest {

    public StringRequest(int method, String url, Response.Listener<String> listener, @Nullable Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return AuthRequest.super.getHeaders();
    }

}

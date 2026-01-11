package at.rayman.hometiles.network;

import com.android.volley.AuthFailureError;

import java.util.Map;

public interface AuthRequest {

    default Map<String, String> getHeaders() throws AuthFailureError {
        return Map.of("authorization", "alsdfh4w98rtuofdhasld");
    }

}

package at.rayman.hometiles.util;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class RestClient {

    private static RestClient restClient;
    private RequestQueue requestQueue;
    private static Context ctx;

    private RestClient(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized RestClient getInstance(Context context) {
        if (restClient == null) {
            restClient = new RestClient(context);
        }
        return restClient;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addRequest(Request<T> req) {
        getRequestQueue().add(req);
    }

}

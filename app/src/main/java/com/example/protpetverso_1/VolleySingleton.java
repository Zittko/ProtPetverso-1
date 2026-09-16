package com.example.protpetverso_1;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Gerenciador global da fila de requisições Volley.
 * Garante uma única instância do RequestQueue usando o contexto da aplicação,
 * evitando vazamentos de memória (memory leaks).
 */
public class VolleySingleton {

    private static VolleySingleton instance;
    private RequestQueue requestQueue;
    private static Context ctx;

    private VolleySingleton(Context context) {
        ctx = context.getApplicationContext();
        requestQueue = getRequestQueue();
    }

    public static synchronized VolleySingleton getInstance(Context context) {
        if (instance == null) {
            instance = new VolleySingleton(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
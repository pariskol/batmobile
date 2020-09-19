package gr.kgdev.batmobile.utils;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HTTPClient {

    private ExecutorService EXECUTOR_SERVICE = null;
    private static String BASIC_AUTH = null;
    private OkHttpClient client = new OkHttpClient();
    private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * Executes given runnable, in separate thread from HTTPClient fixed thread pool.
     * HTTPClient's pool is initialized once during 1st call of this function,
     * you can use this pool to execute your requests asynchronously.
     *
     * @param runnable
     */
    public void executeAsync(Runnable runnable) {
        if (EXECUTOR_SERVICE == null)
            EXECUTOR_SERVICE = Executors.newFixedThreadPool(1);
        EXECUTOR_SERVICE.execute(runnable);
    }

    public Object GET(String url) throws Exception {
        checkForCredentials();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization",  BASIC_AUTH)
                .build();

        Response response = client.newCall(request).execute();
        if (response.code() != 200)
            throw new IOException("Status code: " + response.code() + " , " + serialize(response));
        return serialize(response);
    }

    protected Object serialize(Response response) throws Exception {
       return response.body().string();
    }

    private void checkForCredentials() throws IllegalStateException{
        if (BASIC_AUTH == null)
            throw new IllegalStateException("This HTTPClient demands a Basic Authentication header to be set," +
                    " in order this to be done you must first call 'setBasicAuthCredentials' method");
    }

    public Object POST(String url, JSONObject json) throws Exception {
        checkForCredentials();
        json = json == null ? new JSONObject() : json;
        RequestBody body = RequestBody.create(JSON, json.toString());
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization",  BASIC_AUTH)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();

        if (response.code() != 200)
            throw new IOException("Status code: " + response.code() + " , " + serialize(response));
        return serialize(response);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void setBasicAuthCredentials(String username, String password) {
        String userCredentials = username + ":" + password;
        BASIC_AUTH = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));
    }
}

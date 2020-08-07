package gr.kgdev.batmobile.utils;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HTTPClient {

    private static ExecutorService EXECUTOR_SERVICE = null;
    private static String BASIC_AUTH = null;
    private static OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static String BASE_URL = "https://onelineman.eu";
//    public static String BASE_URL = "http://192.168.2.6:8080";

    public static void executeAsync(Runnable runnable) {
        if (EXECUTOR_SERVICE == null)
            EXECUTOR_SERVICE = Executors.newFixedThreadPool(2);
        EXECUTOR_SERVICE.execute(runnable);
    }

    public static String GET(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization",  BASIC_AUTH)
                .build();

        Response response = client.newCall(request).execute();
        if (response.code() != 200)
            throw new IOException("Status code: " + response.code() + " , " + response.body().string());
        return response.body().string();
    }

    public static String POST(String url, JSONObject json) throws IOException {
        json = json == null ? new JSONObject() : json;
        RequestBody body = RequestBody.create(JSON, json.toString());
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization",  BASIC_AUTH)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();

        if (response.code() != 200)
            throw new IOException("Status code: " + response.code() + " , " + response.body().string());
        return response.body().string();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void setBasicAuthCredentials(String username, String password) {
        String userCredentials = username + ":" + password;
        BASIC_AUTH = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));
    }
}

package gr.kgdev.batmobile.utils;

import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class BatmobileHTTPClient extends HTTPClient {

    public static String BASE_URL = "https://onelineman.eu";

    @Override
    public Object GET(String url) throws Exception {
        if (!url.startsWith("http"))
            url = BASE_URL + url;
        return super.GET(url);
    }

    @Override
    public Object POST(String url, JSONObject json) throws Exception {
        if (!url.startsWith("http"))
            url = BASE_URL + url;
        return super.POST(url, json);
    }

    @Override
    protected Object serialize(Response response) throws IOException, JSONException {
        // response.body().string() must be called only once!
        String responseStr = response.body().string();
        if (responseStr.startsWith("["))
            return new JSONArray(responseStr);
        else
            return new JSONObject(responseStr);
    }
}

package gr.kgdev.batmobile.utils;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class BatmobileHttpClient extends HttpClient {

    private String base = "https://onelineman.eu/api";

    private static final MediaType MEDIA_TYPE_WILDCARD = MediaType.parse("*/*");


    public Object upload(String path) throws Exception {
        checkForCredentials();
        File file = new File(path);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(file, MEDIA_TYPE_WILDCARD))
                .build();

        Request request = new Request.Builder()
                .header("Authorization", this.BASIC_AUTH)
                .header("Content-Type", "multipart/form-data")
                .url(base + "/upload")
                .post(requestBody)
                .build();

        try(Response response = client.newCall(request).execute();) {
            if (!response.isSuccessful())
                throw new IOException("Status code: " + response.code() + " , " + response.body().string());
            return serialize(response.body().string());
        }
    }

    @Override
    public Object get(String url) throws Exception {
        if (!url.startsWith("http"))
            url = base + url;
        return super.get(url);
    }

    @Override
    public Object post(String url, JSONObject json) throws Exception {
        if (!url.startsWith("http"))
            url = base + url;
        return super.post(url, json);
    }

    @Override
    protected Object serialize(String response) throws IOException, JSONException {
        if (response.startsWith("["))
            return new JSONArray(response);
        else
            return new JSONObject(response);
    }
}

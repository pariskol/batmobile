package gr.kgdev.simplemessengerapp.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HTTPClient {

    private static ExecutorService service = null;

    public static void executeAsync(Runnable runnable) {
        if (service == null)
            service = Executors.newFixedThreadPool(2);
        service.execute(runnable);
    }

    public static String GET(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setReadTimeout(5000);
        urlConnection.setConnectTimeout(5000);
        urlConnection.setDoOutput(true);
        urlConnection.connect();

        // read the response
        InputStream in = new BufferedInputStream(url.openStream());
        String jsonString = org.apache.commons.io.IOUtils.toString(in, "UTF-8");

        in.close();
        urlConnection.disconnect();

        return jsonString;
    }

    public static String POST(String urlString, JSONObject json) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setReadTimeout(5000);
        urlConnection.setConnectTimeout(5000);
        urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);

        OutputStream out = urlConnection.getOutputStream();
        out.write(json.toString().getBytes("UTF-8"));
        out.close();

        // read the response
        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
        String result = org.apache.commons.io.IOUtils.toString(in, "UTF-8");

        in.close();
        urlConnection.disconnect();

        return result;
    }
}

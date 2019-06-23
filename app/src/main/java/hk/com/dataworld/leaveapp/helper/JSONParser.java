package hk.com.dataworld.leaveapp.helper;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import hk.com.dataworld.leaveapp.DAL.NameValuePair;

/**
 * Created by lclau on 13/4/2015.
 */
public class JSONParser {
    private InputStream is = null;
    private JSONObject jObj = null;
    private String json = "";

    // constructor
    public JSONParser() {
    }

    public JSONObject getJSONFromUrl(String url) {
        return getJSONFromUrl(url, null);
    }

    public JSONObject getJSONFromUrl(String url, ArrayList<NameValuePair> nvp) {
        String json = getJSONStringFromUrl(url, nvp);

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.i("JSON Parser", "Error parsing data " + e.toString());
        }
        // return JSON String
        return jObj;
    }

    public String getJSONStringFromUrl(String url, ArrayList<NameValuePair> nvp) {
        // Making HTTP request
        HttpURLConnection urlConnection = null;
        InputStream is = null;
        try {
            urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.addRequestProperty("Content-Type", "application/json; charset=utf-8;");

            if (nvp != null) {
                JSONObject jsonParam = new JSONObject();
                for (int i = 0; i < nvp.size(); i++) {
                    jsonParam.put(nvp.get(i).getName(), nvp.get(i).getValue() != null ? nvp.get(i).getValue() : JSONObject.NULL);
                }
                byte[] buffer = jsonParam.toString().getBytes("UTF-8");

                // Send POST output.
                DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
                printout.write(buffer, 0, buffer.length);
                printout.flush();
                printout.close();
            }

            is = new BufferedInputStream(urlConnection.getInputStream());
        } catch (Exception e) {
            Log.i("JSONParser", "EXCEPTION 1");
            e.printStackTrace();
        }

        try {
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                is.close();
                json = sb.toString();
            }
        } catch (Exception e) {
            Log.i("Buffer Error", "Error converting result " + e.toString());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return json;
    }
}

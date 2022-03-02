package com.example.tsd;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpReq extends AsyncTask<String , Void ,String> {
    private String server_response, server_error;

    public HttpReq(onPostExecuteListener pListener) {
        this.pListener = pListener;
    }

    interface onPostExecuteListener {
        void postExecute(String resp, String err);
    }
    private final HttpReq.onPostExecuteListener pListener;

    @Override
    protected String doInBackground(String... strings) {

        URL url;
        HttpURLConnection urlConnection = null;
        server_response = "";
        server_error = "";
        try {
            url = new URL("http://192.168.1.10:3000/"+strings[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Content-Type", "application/json; utf-8");
            urlConnection.setRequestProperty("Accept", "application/json");

            if (strings.length>=3) {
                urlConnection.setRequestProperty("Prefer", "return=representation");
                urlConnection.setRequestMethod(strings[1]);
                urlConnection.setDoOutput(true);
                String jsonInputString = strings[2];
                OutputStream os = urlConnection.getOutputStream();
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = urlConnection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                server_response = readStream(urlConnection.getInputStream());
                //Log.v("CatalogClient", server_response);
            }
        } catch (MalformedURLException e) {
            server_error = e.getLocalizedMessage();
            e.printStackTrace();
        } catch (IOException e) {
            server_error = e.getLocalizedMessage();
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        pListener.postExecute(server_response,server_error);
        //Log.e("Response", "" + server_response);
    }

    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }
}
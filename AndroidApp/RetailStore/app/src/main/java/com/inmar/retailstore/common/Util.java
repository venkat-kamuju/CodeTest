package com.inmar.retailstore.common;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Utility class to handle REST API Calls and other common functionality
 */
public class Util {

    private static final String TAG = Util.class.getSimpleName();

    /**
     * Function to fetch data from REST API.
     * @param urlString
     * @return response data from Server in JSON format
     */
    public static String getServerData(String urlString) {

        Log.i(TAG, "getServerData");

        if (urlString == null || urlString.isEmpty()) {
            Log.e(TAG, "Bad arguments");
            return null;
        }

        String responseData = null;

        try {

            // Create connection
            URL url = new URL(urlString);

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                Log.i(TAG, "Connection response ok");

                InputStream responseBody = httpURLConnection.getInputStream();

                responseData = getStringFromInputStream(responseBody);
                //Log.i(TAG, "Server Response:" + responseData);

            } else {
                Log.e(TAG, "Failed establishing HTTP connection");
            }

            httpURLConnection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return responseData;
    }

    /**
     * Function to post/insert new data to Server
     * @param urlString     URL
     * @param paramString   Parameters to post
     * @return Response from server in JSON format
     */
    public static String postServerData(String urlString, String paramString) {

        Log.i(TAG, "postServerData: url=" + urlString + ", Params:" + paramString);

        if (urlString == null || urlString.isEmpty() || paramString == null || paramString.isEmpty()) {
            Log.e(TAG, "Bad arguments");
            return null;
        }

        String responseData = null;

        try {
            // Create connection
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Accept-Charset", "UTF-8");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.connect();

            // Send POST output.
            DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream ());
            printout.writeBytes(paramString);
            printout.flush ();
            printout.close ();

            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
                Log.i(TAG, "Record inserted");
                InputStream responseBody = urlConnection.getInputStream();
                responseData = getStringFromInputStream(responseBody);
            } else {
                Log.e(TAG, "Failed establishing HTTP connection");
            }

            urlConnection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return responseData;
    }

    /**
     * Delete data on server
     * @param urlString URL to delete record
     * @return Response from server in JSON format
     */
    public static String deleteServerData(String urlString) {

        Log.i(TAG, "postServerData: url=" + urlString);

        if (urlString == null || urlString.isEmpty()) {
            Log.e(TAG, "Bad arguments");
            return null;
        }

        String responseData = null;

        try {

            // Create connection
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setDoOutput(true);
            urlConnection.connect();

            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                Log.e(TAG, "Connection response ok");

                InputStream responseBody = urlConnection.getInputStream();

                responseData = getStringFromInputStream(responseBody);
                //Log.i(TAG, "Server Response:" + responseData);

            } else {
                Log.e(TAG, "Failed establishing HTTP connection");
            }

            urlConnection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return responseData;
    }

    /**
     * Read data from InputStream and convert to String
     * @param is Input stream
     * @return
     */
    public static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {

            e.printStackTrace();

        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

    /**
     * Format an URL string with the supplied parameters
     * @param parameters
     * @return
     */
    public static String getQueryStringForParameters(Map<String, String> parameters) {
        StringBuilder parametersAsQueryString = new StringBuilder();
        if (parameters != null) {
            boolean firstParameter = true;

            for (String parameterName : parameters.keySet()) {
                if (!firstParameter) {
                    parametersAsQueryString.append("&");
                }

                parametersAsQueryString.append(parameterName)
                        .append("=")
                        .append(URLEncoder.encode(parameters.get(parameterName)));

                firstParameter = false;
            }
        }
        return parametersAsQueryString.toString();
    }
}

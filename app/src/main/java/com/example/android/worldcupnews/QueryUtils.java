package com.example.android.worldcupnews;

import android.nfc.Tag;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class QueryUtils {

    // Tag for the log messages
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    // Query the Guardian API and return a list of Article objects.
    public static List<Article> fetchArticleData(String requestUrl) {

        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of Articles
        List<Article> articles = extractFeatureFromJson(jsonResponse);

        // Return the list of Articles
        return articles;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // Return if there is no URL
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200) then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the JSON response", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        Log.i("JSONR", jsonResponse);
        return jsonResponse;
    }

    // Convert the InputStream into a String which contains the whole JSON response from the server
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of Artcles parsed from the JSON response
     */
    private static List<Article> extractFeatureFromJson(String articleJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(articleJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding articles to
        List<Article> articles = new ArrayList<>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(articleJSON);

            // Create a JSONObject associated with they key "response"
            JSONObject response = baseJsonResponse.getJSONObject("response");

            // Extract the JSONArray associated with the key called "results",
            JSONArray articlesArray = response.getJSONArray("results");

            // For each article in the articlesArray, create an Article object
            for (int i = 0; i < articlesArray.length(); i++) {

                // Get a single article at position i within the list of articles
                JSONObject currentArticle = articlesArray.getJSONObject(i);

                // Extract the value for the key called "webTitle"
                String title = currentArticle.getString("webTitle");

                // Extract the value for the key called "sectionName"
                String section = currentArticle.getString("sectionName");

                // Extract the value for the key called "webPublicationDate"
                String time = currentArticle.getString("webPublicationDate");

                // Extract the value for the key called "webUrl"
                String url = currentArticle.getString("webUrl");

                // Get the JSONObject associated with the key "tags"
                JSONArray authorArray = currentArticle.getJSONArray("tags");

                // Get the author at the first position in the list
                JSONObject currentAuthor = authorArray.getJSONObject(0);

                // Extract the value for the key called "webTitle"
                String author = currentAuthor.getString("webTitle");

                // Create a new Article object
                Article article = new Article(title, author, section, time, url);

                // Add the new Article to the list of articles
                articles.add(article);
            }

        } catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing the JSON response", e);
        }

        // Return the list of articles
        return articles;
    }

}

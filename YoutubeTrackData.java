package com.ocpjava;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class YoutubeTrackData {

//    private String apiKey = "AIzaSyASj8NTbJQ_kupb2ogSqbzIe2kusP5vNmI";

//    private String apiKey = "AIzaSyAb9Fri4or0rs2fzCrTAYBY5500W-_Ohxk";
    private String apiKey = "AIzaSyD9rduF7I1iagWeHxvU6KCbKZRZmbahk20";

    public String getYoutubeVideoUrl(String searchQuery) throws VideoNotFoundException {
        try {
            URL url = new URL("https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&q="
                    + searchQuery.replace(" ", "+") + "&type=video&key=" + apiKey);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray items = jsonResponse.getJSONArray("items");

                if (items.length() > 0) {
                    JSONObject firstItem = items.getJSONObject(0);
                    JSONObject snippet = firstItem.getJSONObject("snippet");
                    String videoId = firstItem.getJSONObject("id").getString("videoId");

                    return "https://www.youtube.com/watch?v=" + videoId;
                } else {
                    throw new VideoNotFoundException("No videos found for the search query: " + searchQuery);
                }
            } else {
                System.out.println("Error - HTTP response code: " + responseCode);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return "";
    }
}

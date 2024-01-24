package com.ocpjava;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

public class SpotifyPlaylistReader {

    private String clientId = "2f46e974b82140d3ae2d47a661cd0e43";
    private String clientSecret = "87b0a36d22344057986b97528a31e575";

    public List<String> getPlaylistTracks(String playlistUrl) {

        String playlistId = extractPlaylistId(playlistUrl);
        String accessToken = getAccessToken(clientId, clientSecret);
        if (accessToken != null && playlistId != null) {
            return getAllPlaylistTracks(accessToken, playlistId);
        } else {
            System.out.println("Failed to obtain access token or playlist ID.");
        }

        return new ArrayList<>(); // Return an empty list in case of failure
    }

    private List<String> getAllPlaylistTracks(String accessToken, String playlistId) {
        List<String> trackNames = new ArrayList<>();
        String playlistEndpoint = "https://api.spotify.com/v1/playlists/" + playlistId + "/tracks";

        int limit = 100;
        int offset = 0;

        try {
            while (true) {
                String requestUrl = playlistEndpoint + "?offset=" + offset + "&limit=" + limit;

                HttpURLConnection connection = (HttpURLConnection) new URL(requestUrl).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JsonArray items = JsonParser.parseString(response.toString()).getAsJsonObject().getAsJsonArray("items");
                if (items.size() == 0) {
                    break;
                }

                for (JsonElement item : items) {
                    JsonObject track = item.getAsJsonObject().getAsJsonObject("track");
                    trackNames.add(track.get("name").getAsString());
                }

                offset += limit;
            }
        } catch (IOException e) {
            System.err.println("Error while fetching playlist tracks: " + e.getMessage());
        }
        return trackNames;
    }

    private String extractPlaylistId(String playlistUrl) {
        String[] parts = playlistUrl.split("/");
        if (parts.length >= 4) {
            return parts[4].split("\\?")[0];
        }
        return null;
    }

    private String getAccessToken(String clientId, String clientSecret) {
        String tokenEndpoint = "https://accounts.spotify.com/api/token";

        String base64Credentials = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(tokenEndpoint).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Basic " + base64Credentials);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

            String requestBody = "grant_type=client_credentials";
            connection.getOutputStream().write(requestBody.getBytes());

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
            return jsonResponse.get("access_token").getAsString();

        } catch (IOException e) {
            System.err.println("Error obtaining access token: " + e.getMessage());
        }

        return null;
    }
}

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
import java.util.Base64;
import java.util.List;

public class SpotifyPlaylistReader {

    private String clientId = ""; // ur token here
    private String clientSecret = ""; //ur token here

    public List<TrackInfo> getPlaylistTracks(String playlistUrl) {

        String playlistId = extractPlaylistId(playlistUrl);
        String accessToken = getAccessToken(clientId, clientSecret);
        if (accessToken != null && playlistId != null) {
            return getAllPlaylistTracks(accessToken, playlistId);
        } else {
            System.out.println("Failed to obtain access token or playlist ID.");
        }

        return new ArrayList<>(); // Return an empty list in case of failure
    }

    private List<TrackInfo> getAllPlaylistTracks(String accessToken, String playlistId) {
        List<TrackInfo> trackInfoList = new ArrayList<>();
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
                    JsonObject trackObject = item.getAsJsonObject().getAsJsonObject("track");
                    String trackName = trackObject.get("name").getAsString();
                    JsonArray artists = trackObject.getAsJsonArray("artists");

                    // Assuming there is only one artist for simplicity
                    String artistName = artists.get(0).getAsJsonObject().get("name").getAsString();

                    trackInfoList.add(new TrackInfo(trackName, artistName));
                }

                offset += limit;
            }
        } catch (IOException e) {
            System.err.println("Error while fetching playlist tracks: " + e.getMessage());
        }
        return trackInfoList;
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

    // Define a class to hold track information
    protected static class TrackInfo {
        private String trackName;
        private String artistName;

        public TrackInfo(String trackName, String artistName) {
            this.trackName = trackName;
            this.artistName = artistName;
        }

        public String getTrackName() {
            return trackName;
        }

        public String getArtistName() {
            return artistName;
        }
    }
}

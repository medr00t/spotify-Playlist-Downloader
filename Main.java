package com.ocpjava;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JTextField pathTextField = new JTextField();
            JTextField playlistUrlTextField = new JTextField();
            JButton downloadButton = new JButton("Download");

            JFrame frame = new JFrame("YouTube Downloader");
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.setSize(400, 250);
            frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

            frame.add(new JLabel("Enter the Spotify Playlist URL:"));
            frame.add(playlistUrlTextField);
            frame.add(new JLabel("Enter the path where you want to save your downloads:"));
            frame.add(pathTextField);
            frame.add(downloadButton);

            downloadButton.addActionListener(e -> {
                String playlistUrl = playlistUrlTextField.getText();
                String path = pathTextField.getText();
                SpotifyPlaylistReader spotifyReader = new SpotifyPlaylistReader();
                List<SpotifyPlaylistReader.TrackInfo> trackInfoList = spotifyReader.getPlaylistTracks(playlistUrl);
                YoutubeTrackData youtubeData = new YoutubeTrackData();
                String ytDlpPath = "yt-dlp.exe";
                String ffmpegPath = "C:\\Program Files\\ffmpeg-6.1.1-full_build\\bin\\ffmpeg.exe"; // Replace with the actual path

                YouTubeDownloader youTubeDownloader = new YouTubeDownloader(ytDlpPath, ffmpegPath);

                // Existing files in the specified path
                File folder = new File(path);
                File[] existingFiles = folder.listFiles();

                // Check if the existingFiles array is not null to avoid potential issues
                if (existingFiles != null) {
                    List<String> existingTrackNames = extractExistingTrackNames(existingFiles);

                    for (SpotifyPlaylistReader.TrackInfo trackInfo : trackInfoList) {
                        String trackName = trackInfo.getTrackName();

                        // Check if track already exists in the folder
                        if (existingTrackNames.contains(trackName)) {
                            System.out.println("Track already exists: " + trackInfo);
                            continue;  // Skip downloading if track already exists
                        }

                        String searchQuery = trackName + " " + trackInfo.getArtistName() + " official music video";

                        try {
                            String videoUrl = youtubeData.getYoutubeVideoUrl(searchQuery);
                            if (!videoUrl.isEmpty()) {
                                try {
                                    // Updated call to include desired file name pattern
                                    youTubeDownloader.downloadVideo(videoUrl, path, trackName, trackInfo.getArtistName());
                                    System.out.println("Downloaded!");
                                } catch (IOException | InterruptedException ex) {
                                    ex.printStackTrace();
                                }
                            } else {
                                System.out.println("No YouTube video found for track: " + trackInfo);
                            }
                        } catch (VideoNotFoundException ex) {
                            JOptionPane.showMessageDialog(frame, ex.getMessage());
                        }
                    }
                } else {
                    System.out.println("Error: Unable to retrieve existing files in the specified path.");
                }

                System.exit(0);
            });

            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    int choice = JOptionPane.showConfirmDialog(frame,
                            "Are you sure you want to exit?",
                            "Confirmation", JOptionPane.YES_NO_OPTION);
                    if (choice == JOptionPane.YES_OPTION) {
                        System.exit(0);
                    }
                }
            });

            frame.setVisible(true);
        });
    }

    private static List<String> extractExistingTrackNames(File[] existingFiles) {
        List<String> existingTrackNames = new ArrayList<>();
        for (File file : existingFiles) {
            if (file.isFile()) {
                String fileName = file.getName();
                String trackNameFromFileName = extractTrackName(fileName);

                if (trackNameFromFileName != null) {
                    existingTrackNames.add(trackNameFromFileName);
                }
            }
        }
        return existingTrackNames;
    }

    private static String extractTrackName(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex != -1) {
            String potentialTrackName = fileName.substring(0, lastDotIndex);
            String[] parts = potentialTrackName.split(" - ");
            if (parts.length >= 1) {
                return parts[0];
            }
        }
        return null;
    }
}

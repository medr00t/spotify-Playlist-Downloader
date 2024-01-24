package com.ocpjava;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
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

                for (SpotifyPlaylistReader.TrackInfo trackInfo : trackInfoList) {
                    String searchQuery = trackInfo.getTrackName() + " " + trackInfo.getArtistName() + " official music video";

                    try {
                        String videoUrl = youtubeData.getYoutubeVideoUrl(searchQuery);
                        if (!videoUrl.isEmpty()) {
                            try {
                                youTubeDownloader.downloadVideo(videoUrl, path);
                            } catch (IOException | InterruptedException ex) {
                                ex.printStackTrace();
                            }
                            System.out.println("Downloaded!");
                        } else {
                            System.out.println("No YouTube video found for track: " + trackInfo);
                        }
                    } catch (VideoNotFoundException ex) {
                        JOptionPane.showMessageDialog(frame, ex.getMessage());
                    }
                }

                // Terminate the application
                System.exit(0);
            });

            // Add a window listener to force exit
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
}

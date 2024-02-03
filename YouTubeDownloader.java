package com.ocpjava;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class YouTubeDownloader {

    private String ytDlpPath;
    private String ffmpegPath;

    public YouTubeDownloader(String ytDlpPath, String ffmpegPath) {
        this.ytDlpPath = ytDlpPath;
        this.ffmpegPath = ffmpegPath;
    }

    public void downloadVideo(String videoUrl, String path, String trackName, String artistName) throws IOException, InterruptedException {
        try {
            // Updated command to include desired file name pattern
            String[] command = {ytDlpPath, "-x", "--audio-format", "mp3", "--audio-quality", "0", "--ffmpeg-location", ffmpegPath, "--output",
                    path + File.separator + trackName + " - " + artistName + ".%(ext)s", videoUrl};

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(new File(path));

            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = errorReader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("Exited with error code " + exitCode);

            if (exitCode == 0) {
                System.out.println("Audio downloaded successfully in MP3 format with specified options!");
            } else {
                System.out.println("Failed to download the audio. Check the error messages above.");
            }

        } catch (IOException | InterruptedException e) {
            throw e;
        }
    }

}

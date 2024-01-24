import subprocess
import os
import shutil

input_folder = r"//path/input/folder"
output_folder = r"//path/output/folder"

# Ensure the output folder exists
os.makedirs(output_folder, exist_ok=True)

# Specify the full path to the FFmpeg executable
# The FFmpeg folder is on the 'https://github.com/medr00t/spotify-Playlist-Downloader' 
ffmpeg_path = r"C:\Program Files\ffmpeg-6.1.1-full_build\bin\ffmpeg.exe"

for filename in os.listdir(input_folder):
    input_file = os.path.join(input_folder, filename)
    
    # Check if the file is not a directory and move it to the output folder
    if os.path.isfile(input_file):
        output_file = os.path.join(output_folder, f"{os.path.splitext(filename)[0]}.mp3")
        subprocess.run([ffmpeg_path, "-i", input_file, "-vn", "-acodec", "libmp3lame", "-q:a", "2", output_file])

        # If the file does not end with ".webm", move it to the output folder
        if not filename.endswith(".webm"):
            shutil.move(input_file, os.path.join(output_folder, filename))

print("Conversion and file transfer completed.")

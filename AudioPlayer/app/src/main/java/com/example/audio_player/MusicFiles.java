package com.example.audio_player;

import android.media.MediaMetadataRetriever;

public class MusicFiles {
    // (ВСЕ это используется в MusicAdapter)
    private String path;// путь
    private String title;// название
    private String artist; // артист
    private String album; // альбом
    private String duration; // длительность

    public MusicFiles(String path, String title, String artist, String album, String duration) {
        this.path = path;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }



    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getDuration() {
        return duration;
    }

}


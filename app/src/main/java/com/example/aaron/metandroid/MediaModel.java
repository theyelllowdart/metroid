package com.example.aaron.metandroid;

public class MediaModel {
    private final String title;
    private final String uri;
    private final int stop;

    public String getTitle() {
        return title;
    }

    public String getUri() {
        return uri;
    }

    public int getStop() {
        return stop;
    }

    public MediaModel(String title, String uri, int stop) {
        this.title = title;
        this.uri = uri;
        this.stop = stop;
    }
}

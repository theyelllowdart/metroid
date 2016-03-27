package com.example.aaron.metandroid;

public class QueryModel {
    private final String title;
    private final String imageURL;
    private final String mediaTitle;
    private final String mediaURL;
    private final int stopId;
    private final int position;
    private final int artObjectId;

    public QueryModel(String title, String imageURL, String mediaTitle, String mediaURL, int stopId, int position, int artObjectId) {
        this.title = title;
        this.imageURL = imageURL;
        this.mediaTitle = mediaTitle;
        this.mediaURL = mediaURL;
        this.stopId = stopId;
        this.position = position;
        this.artObjectId = artObjectId;
    }

    public String getTitle() {
        return title;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getMediaTitle() {
        return mediaTitle;
    }

    public String getMediaURL() {
        return mediaURL;
    }

    public int getStopId() {
        return stopId;
    }

    public int getPosition() {
        return position;
    }

    public int getArtObjectId() {
        return artObjectId;
    }
}

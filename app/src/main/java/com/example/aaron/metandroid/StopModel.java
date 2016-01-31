package com.example.aaron.metandroid;

import java.util.ArrayList;

public class StopModel implements Comparable<StopModel>{
    private final String title;
    private final String imageURL;
    private final ArrayList<MediaModel> medias = new ArrayList<>();

    public StopModel(String title, String imageURL) {
        this.title = title;
        this.imageURL = imageURL;
    }

    public String getTitle() {
        return title;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void addMedia(MediaModel mediaModel){
        medias.add(mediaModel);
    }

    @Override
    public int compareTo(StopModel another) {
        return this.title.compareTo(another.getTitle());
    }

    public ArrayList<MediaModel> getMedias() {
        return medias;
    }
}

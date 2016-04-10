package com.example.aaron.metandroid;

import java.util.ArrayList;

public class StopModel implements Comparable<StopModel> {
  private final int artObjectId;
  private final int galleryId;
  private final int width;
  private final int height;
  private final String title;
  private final String imageURL;
  private final ArrayList<MediaModel> medias = new ArrayList<>();

  public StopModel(int artObjectId, int galleryId, int width, int height, String title, String imageURL) {
    this.artObjectId = artObjectId;
    this.galleryId = galleryId;
    this.width = width;
    this.height = height;
    this.title = title;
    this.imageURL = imageURL;
  }

  public int getArtObjectId() {
    return artObjectId;
  }

  public int getGalleryId() {
    return galleryId;
  }

  public String getTitle() {
    return title;
  }

  public String getImageURL() {
    return imageURL;
  }

  public void addMedia(MediaModel mediaModel) {
    medias.add(mediaModel);
  }

  @Override
  public int compareTo(StopModel another) {
    return this.title.compareTo(another.getTitle());
  }

  public ArrayList<MediaModel> getMedias() {
    return medias;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }
}

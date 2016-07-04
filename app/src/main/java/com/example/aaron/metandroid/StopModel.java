package com.example.aaron.metandroid;

import com.google.common.collect.ComparisonChain;

import java.util.ArrayList;

public class StopModel implements Comparable<StopModel> {
  private final String artObjectId;
  private final int galleryId;
  private final int width;
  private final int height;
  private final String title;
  private final String imageURL;
  private final ArrayList<MediaModel> medias = new ArrayList<>();

  public StopModel(String artObjectId, int galleryId, int width, int height, String title, String imageURL) {
    this.artObjectId = artObjectId;
    this.galleryId = galleryId;
    this.width = width;
    this.height = height;
    this.title = title;
    this.imageURL = imageURL;
  }

  public String getArtObjectId() {
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

  public boolean isSynthetic() {
    return artObjectId.startsWith("s");
  }


  @Override
  public int compareTo(StopModel another) {
    if (this.isSynthetic() && another.isSynthetic()) {
      return ComparisonChain.start()
          .compareTrueFirst(this.getTitle().startsWith("Overview"), another.getTitle().startsWith("Overview"))
          .compareTrueFirst(this.getTitle().contains("Gallery"), another.getTitle().contains("Gallery"))
          .compare(this.getTitle(), another.getTitle())
          .result();

    } else {
      return ComparisonChain.start()
          .compareTrueFirst(this.isSynthetic(), another.isSynthetic())
          .compare(this.getTitle(), another.getTitle())
          .result();
    }
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

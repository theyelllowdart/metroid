package com.example.aaron.metandroid.model;

import com.google.common.collect.ComparisonChain;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class StopModel implements Comparable<StopModel>, Parcelable {
  private final String artObjectId;
  private final int galleryId;
  private final int width;
  private final int height;
  private final String title;
  private final String imageURL;
  private final ArrayList<MediaModel> medias;

  public StopModel(String artObjectId, int galleryId, int width, int height, String title, String imageURL) {
    this.artObjectId = artObjectId;
    this.galleryId = galleryId;
    this.width = width;
    this.height = height;
    this.title = title;
    this.imageURL = imageURL;
    this.medias = new ArrayList<>();
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


  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.artObjectId);
    dest.writeInt(this.galleryId);
    dest.writeInt(this.width);
    dest.writeInt(this.height);
    dest.writeString(this.title);
    dest.writeString(this.imageURL);
    dest.writeTypedList(this.medias);
  }

  protected StopModel(Parcel in) {
    this.artObjectId = in.readString();
    this.galleryId = in.readInt();
    this.width = in.readInt();
    this.height = in.readInt();
    this.title = in.readString();
    this.imageURL = in.readString();
    this.medias = in.createTypedArrayList(MediaModel.CREATOR);
  }

  public static final Creator<StopModel> CREATOR = new Creator<StopModel>() {
    @Override
    public StopModel createFromParcel(Parcel source) {
      return new StopModel(source);
    }

    @Override
    public StopModel[] newArray(int size) {
      return new StopModel[size];
    }
  };
}

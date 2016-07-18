package com.example.aaron.metandroid.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MediaModel implements Parcelable {
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


  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.title);
    dest.writeString(this.uri);
    dest.writeInt(this.stop);
  }

  protected MediaModel(Parcel in) {
    this.title = in.readString();
    this.uri = in.readString();
    this.stop = in.readInt();
  }

  public static final Creator<MediaModel> CREATOR = new Creator<MediaModel>() {
    @Override
    public MediaModel createFromParcel(Parcel source) {
      return new MediaModel(source);
    }

    @Override
    public MediaModel[] newArray(int size) {
      return new MediaModel[size];
    }
  };
}

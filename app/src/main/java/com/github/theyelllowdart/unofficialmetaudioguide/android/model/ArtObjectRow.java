package com.github.theyelllowdart.unofficialmetaudioguide.android.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ArtObjectRow implements Parcelable {
  private final StopModel model1;
  private final StopModel model2;

  public ArtObjectRow(StopModel model1, StopModel model2) {
    this.model1 = model1;
    this.model2 = model2;
  }

  public StopModel getModel1() {
    return model1;
  }

  public StopModel getModel2() {
    return model2;
  }


  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(this.model1, flags);
    dest.writeParcelable(this.model2, flags);
  }

  protected ArtObjectRow(Parcel in) {
    this.model1 = in.readParcelable(StopModel.class.getClassLoader());
    this.model2 = in.readParcelable(StopModel.class.getClassLoader());
  }

  public static final Parcelable.Creator<ArtObjectRow> CREATOR = new Parcelable.Creator<ArtObjectRow>() {
    @Override
    public ArtObjectRow createFromParcel(Parcel source) {
      return new ArtObjectRow(source);
    }

    @Override
    public ArtObjectRow[] newArray(int size) {
      return new ArtObjectRow[size];
    }
  };
}

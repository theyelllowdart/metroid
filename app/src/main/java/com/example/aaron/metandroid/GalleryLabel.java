package com.example.aaron.metandroid;

public class GalleryLabel {
  private final String text;
  private final float[] coord;
  private final boolean isHorizontal;

  public GalleryLabel(String text, float[] coord, boolean isHorizontal) {
    this.text = text;
    this.coord = coord;
    this.isHorizontal = isHorizontal;
  }

  public String getText() {
    return text;
  }

  public float[] getCoord() {
    return coord;
  }

  public boolean isHorizontal() {
    return isHorizontal;
  }
}

package com.example.aaron.metandroid;

public class ArtObjectLocation {
  private final int id;
  private final int position;
  private float x;
  private float y;

  public ArtObjectLocation(int id, int position, float x, float y) {
    this.id = id;
    this.position = position;
    this.x = x;
    this.y = y;
  }

  public int getPosition() {
    return position;
  }

  public int getId() {
    return id;
  }

  public float getX() {
    return x;
  }

  public float getY() {
    return y;
  }

  public void setX(float x) {
    this.x = x;
  }

  public void setY(float y) {
    this.y = y;
  }
}

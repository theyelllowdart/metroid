package com.example.aaron.metandroid;

public class ArtObjectRow {
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
}

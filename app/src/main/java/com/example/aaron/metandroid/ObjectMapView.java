package com.example.aaron.metandroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ObjectMapView extends ImageView {
  private final RectF gallery = new RectF();
  private final RectF bounds = new RectF();
  private final Matrix newMatrix = new Matrix();

  public ObjectMapView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setGallery(RectF gallery) {
    this.gallery.set(gallery);
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    bounds.set(0, 0, getWidth(), getHeight());
    newMatrix.setRectToRect(gallery, bounds, Matrix.ScaleToFit.CENTER);
    setImageMatrix(newMatrix);

    super.onDraw(canvas);
  }
}

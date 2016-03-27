package com.example.aaron.metandroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.VectorDrawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.io.IOException;
import java.util.*;

public class LargeMapView extends ImageView {

  private final ArrayList<PaintGallery> paintRecs = new ArrayList<>();
  private final Paint textPaint;
  private final float[] imageMatrixValues = new float[9];
  private final Rect clipBounds = new Rect();
  private final RectF clipBoundsF = new RectF();
  private final float[] galleryTextDest = new float[2];
  private final RectF galleryBoundsDest = new RectF();
  private final Matrix verticalTextMatrix = new Matrix();
  private final Matrix horizontalTextMatrix = new Matrix();
  private final VectorDrawable pin;


  public LargeMapView(Context context, AttributeSet attrs) throws IOException {
    super(context, attrs);

    textPaint = new Paint();
    textPaint.setColor(Color.WHITE);
    textPaint.setStyle(Paint.Style.FILL);
    textPaint.setTextAlign(Paint.Align.CENTER);
    // textPaint.setAntiAlias(true);


    final Random random = new Random(0);
    for (GalleryViewRect rect : MyApplication.galleryRectById.values()) {
      final Paint paint = new Paint();
      paint.setColor(random.nextInt());
      paint.setStyle(Paint.Style.FILL);
      paint.setAlpha(90);
      paintRecs.add(new PaintGallery(rect, paint, MyApplication.galleryLabels.get(rect.getId())));
    }

    this.pin = (VectorDrawable) getResources().getDrawable(R.drawable.pin, null);
  }


  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    final float density = getResources().getDisplayMetrics().density;

    getImageMatrix().getValues(imageMatrixValues);
    textPaint.setTextSize(8 * density * imageMatrixValues[Matrix.MSCALE_X]);

    verticalTextMatrix.set(getImageMatrix());
    verticalTextMatrix.postTranslate(imageMatrixValues[Matrix.MSCALE_X] * 10, 0);
    verticalTextMatrix.postRotate(90);

    horizontalTextMatrix.set(getImageMatrix());
    horizontalTextMatrix.postTranslate(0, imageMatrixValues[Matrix.MSCALE_X] * 10);

    canvas.getClipBounds(clipBounds);
    clipBoundsF.set(clipBounds);
    for (PaintGallery paintGallery : paintRecs) {
      final GalleryViewRect rect = paintGallery.getRect();
      final Paint paint = paintGallery.getPaint();
      final GalleryLabel label = paintGallery.getLabel();

      final RectF scaled = rect.getScaled();
      getImageMatrix().mapRect(galleryBoundsDest, scaled);

      if (RectF.intersects(galleryBoundsDest, clipBoundsF)) {
//        canvas.drawRect(galleryBoundsDest, paint);

        if (label.isHorizontal()) {
          horizontalTextMatrix.mapPoints(galleryTextDest, label.getCoord());
          canvas.drawText(label.getText(), galleryTextDest[0], galleryTextDest[1], textPaint);
        } else {
          canvas.save();
          canvas.rotate(-90);
          verticalTextMatrix.mapPoints(galleryTextDest, label.getCoord());
          canvas.drawText(label.getText(), galleryTextDest[0], galleryTextDest[1], textPaint);
          canvas.restore();
        }
      }
    }

    // Polygon test
    {
      Path path = new Path();
      path.moveTo(321 * density, 62 * density);
      path.lineTo(309 * density, 50 * density);
      path.lineTo(318 * density, 41 * density);
      path.lineTo(331 * density, 53 * density);
      path.close();


      Path transformedPath = new Path();
      path.transform(getImageMatrix(), transformedPath);
      //canvas.drawPath(transformedPath, paints.get(0));
    }

    float pointX = 451 * density;
    float pointY = 279 * density;
    float halfPinSize = 5 * density;
    RectF r = new RectF(pointX - halfPinSize, pointY - halfPinSize, pointX + halfPinSize, pointY + halfPinSize);
    RectF transformedR = new RectF();
    getImageMatrix().mapRect(transformedR, r);
    Rect finalR = new Rect();
    transformedR.round(finalR);
    pin.setBounds(finalR);
    pin.draw(canvas);

    TextPaint pinTextPaint = new TextPaint(textPaint);
    pinTextPaint.setColor(Color.RED);
    pinTextPaint.setTextSize(4 * density * imageMatrixValues[Matrix.MSCALE_X]);
    canvas.drawText("10", finalR.centerX(), finalR.centerY() - halfPinSize / 8, pinTextPaint);
  }

  private static class Polygon {
    private int[] polyY, polyX;
    private int polySides;

    public Polygon(int[] px, int[] py, int ps) {
      polyX = px;
      polyY = py;
      polySides = ps;
    }

    /**
     * Checks if the Polygon contains a point.
     *
     * @param x Point horizontal pos.
     * @param y Point vertical pos.
     * @return Point is in Poly flag.
     * @see "http://alienryderflex.com/polygon/"
     */
    public boolean contains(int x, int y) {
      boolean c = false;
      int i, j = 0;
      for (i = 0, j = polySides - 1; i < polySides; j = i++) {
        if (((polyY[i] > y) != (polyY[j] > y))
            && (x < (polyX[j] - polyX[i]) * (y - polyY[i]) / (polyY[j] - polyY[i]) + polyX[i]))
          c = !c;
      }
      return c;
    }
  }

  private static class PaintGallery {
    private final GalleryViewRect rect;
    private final Paint paint;
    private final GalleryLabel label;

    public PaintGallery(GalleryViewRect rect, Paint paint, GalleryLabel label) {
      this.rect = rect;
      this.paint = paint;
      this.label = label;
    }

    public GalleryViewRect getRect() {
      return rect;
    }

    public Paint getPaint() {
      return paint;
    }

    public GalleryLabel getLabel() {
      return label;
    }
  }

}


package com.example.aaron.metandroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.io.IOException;
import java.util.*;

public class LargeMapView extends ImageView {
  private final float density;
  private final ArrayList<PaintGallery> paintRecs = new ArrayList<>();
  private final Paint textPaint;
  private final float[] imageMatrixValues = new float[9];
  private final Rect clipBounds = new Rect();
  private final RectF clipBoundsF = new RectF();
  private final float[] galleryTextDest = new float[2];
  private final RectF galleryBoundsDest = new RectF();
  private final RectF pinBounds = new RectF();
  private final Rect finalPinBounds = new Rect();
  private final Matrix verticalTextMatrix = new Matrix();
  private final Matrix horizontalTextMatrix = new Matrix();
  private final VectorDrawable pinDrawable;
  private ArrayList<DrawPin> pins = new ArrayList<>();
  private Integer pinToPlace = null;
  private PointF pinLocation = new PointF();
  private ArrayList<Rect> pinBoundsList = new ArrayList<>();
  private ArrayList<ArtObjectLocation> locations = new ArrayList<>();


  public LargeMapView(Context context, AttributeSet attrs) throws IOException {
    super(context, attrs);

    density = getResources().getDisplayMetrics().density;

    textPaint = new Paint();
    textPaint.setARGB(255, 88, 126, 146);
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

    this.pinDrawable = (VectorDrawable) getResources().getDrawable(R.drawable.pin, null);
  }

  private class DrawPin {
    int position;
    RectF rect;

    public DrawPin(int position, RectF rect) {
      this.position = position;
      this.rect = rect;
    }

    public int getPosition() {
      return position;
    }

    public void setPosition(int position) {
      this.position = position;
    }

    public RectF getRect() {
      return rect;
    }

    public void setRect(RectF rect) {
      this.rect = rect;
    }
  }

  public ArrayList<ArtObjectLocation> getLocations() {
    return locations;
  }

  public void setPins(List<ArtObjectLocation> locations) {
    this.pins.clear();
    for (ArtObjectLocation location: locations) {
      float halfPinSize = 5 * density;
      RectF rect = new RectF(
          location.getX() - halfPinSize,
          location.getY() - (2 * halfPinSize),
          location.getX() + halfPinSize, location.getY()
      );
      this.pins.add(new DrawPin(location.getPosition(), rect));
    }
    this.locations.clear();
    this.locations.addAll(locations);
    this.invalidate();
  }

  public void clearPins() {
    this.pins.clear();
    this.invalidate();
  }

  public void clearPinToPlace() {
    this.pinToPlace = null;
    this.invalidate();
  }



  public void setPinToPlace(int pinToPlace) {
    this.pinToPlace = pinToPlace;
    this.invalidate();
  }

  public Integer getPinToPlace(){
    return this.pinToPlace;
  }

  public PointF getPinLocation() {
    return pinLocation;
  }

  public Integer getPin(float x, float y) {
    for(int i = 0; i < pins.size(); i++) {
      RectF bound = pins.get(i).getRect();
      if (bound.contains(x, y)) {
        return i;
      }
    }
    return null;
  }

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);

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
//      final Paint paint = paintGallery.getPaint();
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

    pinBoundsList.clear();
    for (DrawPin pin : pins) {
      float pointX;
      float pointY;
      int alpha;
      if (pinToPlace != null) {
        if (pinToPlace == pin.getPosition()) {
          alpha = 255;
        } else {
          alpha = 128;
        }
      } else {
        alpha = 255;
      }

      float halfPinSize;
      if (pinToPlace != null && pinToPlace == pin.getPosition()) {
        halfPinSize = 5 * density * imageMatrixValues[Matrix.MSCALE_X];
        pinBounds.set((getWidth() / 2.0f) - halfPinSize, (getHeight() / 2.0f) - (2 * halfPinSize), (getWidth() / 2.0f) + halfPinSize,  (getHeight() / 2.0f));
        pinLocation.set(pinBounds.centerX(), pinBounds.bottom);
      } else {
        halfPinSize = 5 * density;
        pinBounds.set(pin.getRect());
        getImageMatrix().mapRect(pinBounds);
      }
      pinBounds.round(finalPinBounds);
      pinDrawable.setBounds(finalPinBounds);
      pinBoundsList.add(finalPinBounds);
      pinDrawable.setAlpha(alpha);
      pinDrawable.draw(canvas);

      TextPaint pinTextPaint = new TextPaint(textPaint);
      pinTextPaint.setColor(Color.RED);
      pinTextPaint.setAlpha(alpha);
      pinTextPaint.setTextSize(4 * density * imageMatrixValues[Matrix.MSCALE_X]);
      canvas.drawText(String.valueOf(pin.getPosition()), finalPinBounds.centerX(), finalPinBounds.centerY() - halfPinSize / 8, pinTextPaint);
    }
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


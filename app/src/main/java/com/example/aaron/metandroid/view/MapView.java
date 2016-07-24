package com.example.aaron.metandroid.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.VectorDrawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import com.example.aaron.metandroid.MyApplication;
import com.example.aaron.metandroid.R;
import com.example.aaron.metandroid.model.ArtObjectLocation;
import com.example.aaron.metandroid.model.GalleryLabel;
import com.example.aaron.metandroid.model.GalleryViewRect;
import com.example.aaron.metandroid.model.StopModel;

import java.io.IOException;
import java.util.*;

public class MapView extends ImageView {
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

  private ScaleGestureDetector mScaleDetector;
  private GestureDetector gestureListener;
  private Matrix drawMatrix = new Matrix();

  private OnPinSelectListener pinSelectListener;
  private OnMapSelectListener mapSelectListener;


  public MapView(Context context, AttributeSet attrs) throws IOException {
    super(context, attrs);

    pinSelectListener = (OnPinSelectListener) context;
    mapSelectListener = (OnMapSelectListener) context;

    density = getResources().getDisplayMetrics().density;
    mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    gestureListener = new GestureDetector(context, new GestureListener());

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
      GalleryLabel label = MyApplication.galleryLabels.get(rect.getId());
      if (label == null)
        throw new RuntimeException(String.valueOf(rect.getId()));
      paintRecs.add(new PaintGallery(rect, paint, MyApplication.galleryLabels.get(rect.getId())));
    }

    this.pinDrawable = (VectorDrawable) getResources().getDrawable(R.drawable.pin, null);
  }

  public interface OnPinSelectListener {
    void onPinSelected(StopModel model, int pinNumber);
  }

  public interface OnMapSelectListener {
    void onMapSelected(float x, float y);
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
    for (ArtObjectLocation location : locations) {
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

  public Integer getPinToPlace() {
    return this.pinToPlace;
  }

  public PointF getPinLocation() {
    return pinLocation;
  }

  public Integer getPin(float x, float y) {
    for (int i = 0; i < pins.size(); i++) {
      RectF bound = pins.get(i).getRect();
      if (bound.contains(x, y)) {
        return i;
      }
    }
    return null;
  }

  @Override
  public void onDraw(Canvas canvas) {
    setImageMatrix(drawMatrix);

    super.onDraw(canvas);


    drawMatrix.getValues(imageMatrixValues);
    textPaint.setTextSize(8 * density * imageMatrixValues[Matrix.MSCALE_X]);

    verticalTextMatrix.set(drawMatrix);
    verticalTextMatrix.postTranslate(imageMatrixValues[Matrix.MSCALE_X] * 10, 0);
    verticalTextMatrix.postRotate(90);

    horizontalTextMatrix.set(drawMatrix);
    horizontalTextMatrix.postTranslate(0, imageMatrixValues[Matrix.MSCALE_X] * 10);

    canvas.getClipBounds(clipBounds);
    clipBoundsF.set(clipBounds);
    for (PaintGallery paintGallery : paintRecs) {
      final GalleryViewRect rect = paintGallery.getRect();
      final Paint paint = paintGallery.getPaint();
      final GalleryLabel label = paintGallery.getLabel();

      final RectF scaled = rect.getScaled();
      drawMatrix.mapRect(galleryBoundsDest, scaled);

      if (RectF.intersects(galleryBoundsDest, clipBoundsF)) {
        canvas.drawRect(galleryBoundsDest, paint);

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

    pinBoundsList.clear();
    ArrayList<DrawPinLabel> toDrawPinLabels = new ArrayList<>();
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
        pinBounds.set((getWidth() / 2.0f) - halfPinSize, (getHeight() / 2.0f) - (2 * halfPinSize), (getWidth() / 2.0f) + halfPinSize, (getHeight() / 2.0f));
        pinLocation.set(pinBounds.centerX(), pinBounds.bottom);
      } else {
        halfPinSize = 5 * density;
        pinBounds.set(pin.getRect());
        drawMatrix.mapRect(pinBounds);
      }
      pinBounds.round(finalPinBounds);
      pinDrawable.setBounds(finalPinBounds);
      pinBoundsList.add(finalPinBounds);
      pinDrawable.setAlpha(alpha);

      float pinArea = pin.getRect().height() * pin.getRect().width();
      int intersectsPrevPinCount = 0;
      for (DrawPin prevPin : pins) {
        if (prevPin == pin) {
          break;
        }
        if (RectF.intersects(prevPin.getRect(), pin.getRect())) {
          float dx = Math.min(prevPin.getRect().right, pin.getRect().right) - Math.max(prevPin.getRect().left, pin.getRect().left);
          float dy = Math.min(prevPin.getRect().bottom, pin.getRect().bottom) - Math.max(prevPin.getRect().top, pin.getRect().top);

          if ((dx * dy) / pinArea > .50) {
            intersectsPrevPinCount++;
          }

        }
      }

      int rotation = intersectsPrevPinCount == 0 ? 0 : intersectsPrevPinCount % 2 == 0 ? -35 : 35;
      canvas.save();
      canvas.rotate(rotation, finalPinBounds.centerX(), finalPinBounds.bottom);
      pinDrawable.draw(canvas);
//      canvas.restore();


      TextPaint pinTextPaint = new TextPaint(textPaint);
      pinTextPaint.setColor(Color.RED);
      pinTextPaint.setAlpha(alpha);
      pinTextPaint.setShadowLayer(4f, 1.5f, 1.3f, Color.argb(120, 0, 0, 0));
      pinTextPaint.setTextSize(4 * density * imageMatrixValues[Matrix.MSCALE_X]);

//      canvas.save();

      float textX = finalPinBounds.centerX();
      float texY = finalPinBounds.centerY() - halfPinSize / 8;
//      canvas.rotate(rotation, textX, texY);
      toDrawPinLabels.add(new DrawPinLabel(rotation, finalPinBounds.centerX(), finalPinBounds.bottom, String.valueOf(pin.getPosition()), textX, texY, pinTextPaint));
      canvas.restore();
    }
    for (DrawPinLabel l : toDrawPinLabels) {
      canvas.save();
      canvas.rotate(l.rotation, l.rotationX, l.rotationY);
      canvas.drawText(
          l.title,
          l.x,
          l.y,
          l.paint
      );
      canvas.restore();
    }
  }

  private static class DrawPinLabel {
    int rotation;
    float rotationX;
    float rotationY;

    String title;
    float x;
    float y;
    Paint paint;

    public DrawPinLabel(int rotation, float rotationX, float rotationY, String title, float x, float y, Paint paint) {
      this.rotation = rotation;
      this.rotationX = rotationX;
      this.rotationY = rotationY;
      this.title = title;
      this.x = x;
      this.y = y;
      this.paint = paint;
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


  // Borrowed from
  // http://stackoverflow.com/questions/19418878/implementing-pinch-zoom-and-drag-using-androids-build-in-gesture-listener-and-s
  private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    float lastFocusX;
    float lastFocusY;

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
      lastFocusX = detector.getFocusX();
      lastFocusY = detector.getFocusY();
      return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
      Matrix transformationMatrix = new Matrix();
      float focusX = detector.getFocusX();
      float focusY = detector.getFocusY();

      transformationMatrix.postTranslate(-focusX, -focusY);
      transformationMatrix.postScale(detector.getScaleFactor(), detector.getScaleFactor());

      float focusShiftX = focusX - lastFocusX;
      float focusShiftY = focusY - lastFocusY;
      transformationMatrix.postTranslate((focusX + focusShiftX), focusY + focusShiftY);
      drawMatrix.postConcat(transformationMatrix);
      lastFocusX = focusX;
      lastFocusY = focusY;
      invalidate();
      return true;
    }

  }

  public class GestureListener extends GestureDetector.SimpleOnGestureListener {
    @Override
    public boolean onScroll(MotionEvent downEvent, MotionEvent currentEvent,
                            float distanceX, float distanceY) {
      float speedMultiplier = 1.5f;
      drawMatrix.postTranslate(-distanceX * speedMultiplier, -distanceY * speedMultiplier);
      invalidate();
      return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
      if (pinToPlace == null) {
        Matrix inverse = new Matrix();
        getImageMatrix().invert(inverse);
        float[] absolutePoints = new float[]{e.getX(), e.getY()};
        inverse.mapPoints(absolutePoints);
        float x = absolutePoints[0];
        float y = absolutePoints[1];
        Integer selectedPin = getPin(x, y);
        if (selectedPin != null) {
          pinSelectListener.onPinSelected(null, selectedPin);
        } else {
          mapSelectListener.onMapSelected(x, y);
        }
      }
      return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
      float[] drawMatrixValues = new float[9];
      drawMatrix.getValues(drawMatrixValues);
      float scale = drawMatrixValues[Matrix.MSCALE_X];
      float low = 1.0f;
      float med = 2.0f;
      float high = 4.0f;

      float targetScale;
      if (scale < low) {
        targetScale = low;
      } else if (scale < med) {
        targetScale = med;
      } else if (scale < high) {
        targetScale = high;
      } else {
        targetScale = low;
      }

      float postScaleRatio = targetScale / scale;
      drawMatrix.postScale(postScaleRatio, postScaleRatio, e.getX(), e.getY());
      invalidate();
      return true;
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    mScaleDetector.onTouchEvent(event);
    gestureListener.onTouchEvent(event);
    return true;
  }

  public void zoomToGallery(RectF galleryRect) {
    RectF imageSize = new RectF(0, 0, getWidth(), getHeight());
    Matrix newMatrix = new Matrix();
    float minSize = 100 * density;
    float padding = 10 * density;
    float sizeX = Math.max(minSize, galleryRect.width() + padding);
    float sizeY = Math.max(minSize, galleryRect.height() + padding);
    RectF newViewPort = new RectF(
        (galleryRect.centerX() - sizeX / 2),
        (galleryRect.centerY() - sizeY / 2),
        (galleryRect.centerX() + sizeX / 2),
        (galleryRect.centerY() + sizeY / 2)
    );
    newMatrix.setRectToRect(newViewPort, imageSize, Matrix.ScaleToFit.CENTER);
    drawMatrix = newMatrix;
    invalidate();
  }
}


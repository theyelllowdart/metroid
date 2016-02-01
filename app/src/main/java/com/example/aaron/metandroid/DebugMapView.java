package com.example.aaron.metandroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

public class DebugMapView extends ImageView {

  public final ArrayList<GalleryViewRect> rects;
  private final ArrayList<Paint> paints;

  public DebugMapView(Context context, AttributeSet attrs) throws IOException {
    super(context, attrs);

    rects = new ArrayList<>();
    final float density = getResources().getDisplayMetrics().density;
    try (BufferedReader bufferedReader = new BufferedReader(
        new InputStreamReader(context.getResources().openRawResource(R.raw.shapes)))) {
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        if (!line.trim().isEmpty()) {
          String[] matches = line.split(",");
          rects.add(new GalleryViewRect(
                  Integer.parseInt(matches[0]),
                  Float.parseFloat(matches[1]),
                  Float.parseFloat(matches[2]),
                  Float.parseFloat(matches[3]),
                  Float.parseFloat(matches[4]),
                  density)
          );
        }
      }
    }

    final Random random = new Random(0);
    paints = new ArrayList<>(rects.size());
    for (int i = 0; i < rects.size(); i++) {
      final Paint paint = new Paint();
      paint.setColor(random.nextInt());
      paint.setStyle(Paint.Style.FILL);
      paint.setAlpha(90);
      paints.add(paint);
    }
  }


  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    for (int i = 0; i < rects.size(); i++) {
      final Paint paint = paints.get(i);
      final GalleryViewRect galleryViewRect = rects.get(i);

      final RectF scaled = galleryViewRect.getScaled();
      final RectF transformed = galleryViewRect.getTransformed();
      getImageMatrix().mapRect(transformed, scaled);
      canvas.drawRect(transformed, paint);
    }
    Path path = new Path();
    final float density = getResources().getDisplayMetrics().density;
    path.moveTo(321 * density, 62 * density);
    path.lineTo(309 * density, 50 * density);
    path.lineTo(318 * density, 41 * density);
    path.lineTo(331 * density, 53 * density);
    path.close();


    Path transformedPath = new Path();
    path.transform(getImageMatrix(), transformedPath);
    canvas.drawPath(transformedPath, paints.get(0));
  }

  private class Polygon {
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
}


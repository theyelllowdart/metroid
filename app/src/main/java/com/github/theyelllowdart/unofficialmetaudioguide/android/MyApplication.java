package com.github.theyelllowdart.unofficialmetaudioguide.android;

import android.app.Application;

import com.github.theyelllowdart.unofficialmetaudioguide.android.R;
import com.github.theyelllowdart.unofficialmetaudioguide.android.model.GalleryLabel;
import com.github.theyelllowdart.unofficialmetaudioguide.android.model.GalleryViewRect;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application {

  public static Map<Integer, GalleryViewRect> galleryRectById = new HashMap<>();
  public static Map<Integer, GalleryLabel> galleryLabels = new HashMap<>();

  @Override
  public void onCreate() {
    super.onCreate();

    final float density = getResources().getDisplayMetrics().density;
    try {

      try (BufferedReader bufferedReader = new BufferedReader(
          new InputStreamReader(getResources().openRawResource(R.raw.shapes)))) {
        String line;
        while ((line = bufferedReader.readLine()) != null) {
          if (!line.trim().isEmpty()) {
            String[] matches = line.split(",");
            GalleryViewRect rect = new GalleryViewRect(
                Integer.parseInt(matches[0]),
                Float.parseFloat(matches[1]) * density,
                Float.parseFloat(matches[2]) * density,
                Float.parseFloat(matches[3]) * density,
                Float.parseFloat(matches[4]) * density);
            galleryRectById.put(rect.getId(), rect);
          }
        }
      }

      try (BufferedReader bufferedReader = new BufferedReader(
          new InputStreamReader(getResources().openRawResource(R.raw.labels)))) {
        String line;
        while ((line = bufferedReader.readLine()) != null) {
          if (!line.trim().isEmpty()) {
            String[] matches = line.split(",");

            float[] coord = new float[]{
                Float.parseFloat(matches[1]) * density,
                Float.parseFloat(matches[2]) * density};
            GalleryLabel label = new GalleryLabel(matches[0], coord, true);
            galleryLabels.put(Integer.parseInt(label.getText()), label);

          }
        }
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    // TODO(aaron): Put this into a data verification test
    for (GalleryViewRect rect1 : galleryRectById.values()) {
      for (GalleryViewRect rect2 : galleryRectById.values()) {
        if (rect2.getScaled().intersect(rect1.getScaled()) && rect1.getId() != rect2.getId()) {
          throw new RuntimeException("intersecting shapes " + rect1.getId() + " " + rect2.getId());
        }
      }
    }
  }
}

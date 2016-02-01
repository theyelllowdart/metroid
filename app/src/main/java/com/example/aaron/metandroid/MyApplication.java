package com.example.aaron.metandroid;

import android.app.Application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class MyApplication extends Application {

  public static HashMap<Integer, GalleryViewRect> galleryRectById = new HashMap<>();

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
                Float.parseFloat(matches[1]),
                Float.parseFloat(matches[2]),
                Float.parseFloat(matches[3]),
                Float.parseFloat(matches[4]),
                density);
            galleryRectById.put(rect.getId(), rect);
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // TODO(aaron): Put this into a data verification test
    for (GalleryViewRect rect1 : galleryRectById.values()) {
      for (GalleryViewRect rect2 : galleryRectById.values()) {
        if (rect2.getOriginal().intersect(rect1.getOriginal()) && rect1.getId() != rect2.getId()) {
          throw new RuntimeException("intersecting shapes " + rect1.getId() + " " + rect2.getId());
        }
      }
    }
  }
}

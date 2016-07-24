package com.example.aaron.metandroid.fragment;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.aaron.metandroid.MyApplication;
import com.example.aaron.metandroid.R;
import com.example.aaron.metandroid.model.ArtObjectLocation;
import com.example.aaron.metandroid.model.ArtObjectRow;
import com.example.aaron.metandroid.model.GalleryViewRect;
import com.example.aaron.metandroid.model.MediaModel;
import com.example.aaron.metandroid.model.QueryModel;
import com.example.aaron.metandroid.model.StopModel;
import com.example.aaron.metandroid.util.FeedReaderDbHelper;
import com.example.aaron.metandroid.view.MapView;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class MapActivity extends Activity implements ObjectListFragment.OnObjectSelectListener,
    ObjectDetailFragment.OnMediaSelectListener, ObjectDetailFragment.OnMoveSelectListener,
    ObjectMoveFragment.OnExitMoveModeListener, MapView.OnPinSelectListener, MapView.OnMapSelectListener {

  private Float density;
  private Player player;
  private MapView mapView;
  private Activity mainActivity;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_map);

    density = getResources().getDisplayMetrics().density;

    player = new Player(
        getBaseContext(),
        (SeekBar) findViewById(R.id.seek),
        (Button) findViewById(R.id.play),
        (TextView) findViewById(R.id.time),
        (TextView) findViewById(R.id.audioTitle)
    );

    mapView = (MapView) findViewById(R.id.map);
    mainActivity = this;

    mainActivity.getFragmentManager()
        .beginTransaction()
        .replace(R.id.fragment_container, ObjectListFragment.create(0, null))
        .commit();
  }

  @Override
  public void onObjectSelected(StopModel model, int pinNumber) {
    mainActivity
        .getFragmentManager()
        .beginTransaction()
        .replace(R.id.fragment_container, ObjectDetailFragment.create(model, pinNumber))
        .addToBackStack(null)
        .commit();
  }

  @Override
  public void onMediaSelected(List<MediaModel> models, int position) {
    MediaModel model = models.get(position);
    try {
      List<MediaModel> queue;
      if (position + 1 != models.size()) {
        queue = models.subList(position + 1, models.size());
      } else {
        queue = new ArrayList<>();
      }
      player.play(model.getUri(), model.getTitle(), queue);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void onMediaSelected(StopModel model, int pinNumber) {
    mapView.setPinToPlace(pinNumber);
    mainActivity
        .getFragmentManager()
        .beginTransaction()
        .replace(R.id.fragment_container, ObjectMoveFragment.create(model, pinNumber))
        .addToBackStack(null)
        .commit();
  }

  @Override
  public void onExitMoveMode() {
    mapView.clearPinToPlace();
  }

  @Override
  public void onPinSelected(StopModel model, int pinNumber) {
    Object fragment = mainActivity
        .getFragmentManager()
        .findFragmentById(R.id.fragment_container);
    if (fragment instanceof ObjectListFragment) {
      ((ObjectListFragment) fragment).setSelection(pinNumber);
    } else if (fragment instanceof ObjectDetailFragment) {
      ((ObjectDetailFragment) fragment).setSelection(pinNumber);
    }
  }

  @Override
  public void onMapSelected(float x, float y) {
    float[] coordinates = new float[]{x, y};
    Log.i("tag", x / density + " " + y / density);

    for (GalleryViewRect rect : MyApplication.galleryRectById.values()) {
      if (rect.contains(coordinates[0], coordinates[1])) {
        mapView.clearPins();
        final int gallery = rect.getId();
        SQLiteDatabase db = new FeedReaderDbHelper(getApplicationContext()).getReadableDatabase();
        try (Cursor c = db.rawQuery(
            "SELECT s.objectId, s.title as objectTitle, image, m.title as audioTitle, m.uri as audio, s.id as stop, m.position, width, height " +
                "FROM processed_stop s " +
                "LEFT OUTER JOIN processed_media m ON (s.id = m.stop) " +
                "WHERE gallery = " + rect.getId() + " " +
                "ORDER BY m.stop, m.position ", null)
        ) {
          HashMap<String, ArrayList<QueryModel>> modelsByObjectId = new HashMap<>();
          HashMap<String, ArrayList<QueryModel>> modelsByTitle = new HashMap<>();
          while ((c.moveToNext())) {
            String objectId = c.getString(c.getColumnIndexOrThrow("objectId"));
            String title = c.getString(c.getColumnIndexOrThrow("objectTitle"));
            String image = c.getString(c.getColumnIndexOrThrow("image"));
            int stop = c.getInt(c.getColumnIndex("stop"));

            String audioTitle = c.getString(c.getColumnIndex("audioTitle"));
            String audio = null;
            if (audioTitle == null) {
              audioTitle = "Broken link";
              audio = "broken";
            } else {
              audio = c.getString(c.getColumnIndex("audio"));
            }
            audioTitle = audioTitle + '-' + stop;

            int position = c.getInt(c.getColumnIndex("position"));
            int width = c.getInt(c.getColumnIndex("width"));
            int height = c.getInt(c.getColumnIndex("height"));
            QueryModel model = new QueryModel(title, image, audioTitle, audio, stop, position, objectId, width, height);
            if (objectId.startsWith("s") && !modelsByTitle.containsKey(title)) {
              modelsByTitle.put(title, new ArrayList<QueryModel>());
            }
            if (!objectId.startsWith("s") && !modelsByObjectId.containsKey(objectId)) {
              modelsByObjectId.put(objectId, new ArrayList<QueryModel>());
            }
            ArrayList<QueryModel> models;
            if (objectId.startsWith("s")) {
              models = modelsByTitle.get(title);
            } else {
              models = modelsByObjectId.get(objectId);
            }
            boolean hasAudio = false;
            for (QueryModel q : models) {
              if (q.getMediaURL().equals(audio)) {
                hasAudio = true;
              }
            }
            if (!hasAudio)
              models.add(model);

          }
          ArrayList<StopModel> stopModels = new ArrayList<>();
          for (ArrayList<QueryModel> qs : Iterables.concat(modelsByObjectId.values(), modelsByTitle.values())) {
            StopModel stopModel = new StopModel(qs.get(0).getArtObjectId(), rect.getId(), qs.get(0).getWidth(), qs.get(0).getHeight(), qs.get(0).getTitle(), qs.get(0).getImageURL());
            for (QueryModel q : qs) {
              stopModel.addMedia(new MediaModel(q.getMediaTitle(), q.getMediaURL(), q.getStopId()));
            }
            stopModels.add(stopModel);
          }
          Collections.sort(stopModels);

          ArrayList<ArtObjectRow> rows = new ArrayList<>();
          for (int i = 0; i < stopModels.size(); i++) {
            StopModel stopModel1 = stopModels.get(i);
            StopModel stopModel2 = null;
            int height = stopModel1.getHeight();
            int width = stopModel1.getWidth();
            double ratio = width / (double) height;
            if (ratio <= 1.3) {
              int nextPortrait = -1;
              for (int j = i + 1; j < stopModels.size(); j++) {
                StopModel stopModel = stopModels.get(j);
                if ((stopModel.getWidth() / (double) stopModel.getHeight()) <= 1.3) {
                  nextPortrait = j;
                  break;
                }
              }
              if (nextPortrait != -1) {
                Collections.swap(stopModels, i + 1, nextPortrait);
                stopModel2 = stopModels.get(i + 1);
                i++;
              }
            }
            rows.add(new ArtObjectRow(stopModel1, stopModel2));
          }

          mainActivity.getFragmentManager()
              .beginTransaction()
              .replace(R.id.fragment_container, ObjectListFragment.create(gallery, rows))
              .commit();

          // Find pin Locations
          HashSet<String> artObjectIds = new HashSet<>();
          for (StopModel s : stopModels) {
            artObjectIds.add("'" + s.getArtObjectId() + "'");
          }
          if (!artObjectIds.isEmpty()) {
            try (Cursor cl = db.rawQuery(
                "SELECT objectId, x, y " +
                    "FROM object_location " +
                    "WHERE objectId in (" + Joiner.on(',').join(artObjectIds) + ") ", null)
            ) {
              HashMap<String, PointF> artObjectIdToPoint = new HashMap<>();
              while ((cl.moveToNext())) {
                String objectId = cl.getString(cl.getColumnIndexOrThrow("objectId"));
                float x1 = cl.getFloat(cl.getColumnIndexOrThrow("x"));
                float y1 = cl.getFloat(cl.getColumnIndexOrThrow("y"));
                artObjectIdToPoint.put(objectId, new PointF(x1 * density, y1 * density));
              }
              int unsetLocationCount = 0;
              ArrayList<ArtObjectLocation> pins = new ArrayList<>();
              for (int i = 0; i < stopModels.size(); i++) {
                StopModel s = stopModels.get(i);
                PointF point = artObjectIdToPoint.get(s.getArtObjectId());
                if (point == null) {
                  point = new PointF(rect.getScaled().centerX() + 5 * density * unsetLocationCount, rect.getScaled().centerY());
                  unsetLocationCount++;
                }
                pins.add(new ArtObjectLocation(s.getArtObjectId(), i + 1, point.x, point.y));
              }
              mapView.setPins(pins);
            }
          }
        }

        mapView.zoomToRect(rect.getScaled());
      }
    }
  }
}


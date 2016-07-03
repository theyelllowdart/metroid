package com.example.aaron.metandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import uk.co.senab.photoview.PhotoViewAttacher;


public class MapActivity extends Activity {

  private Float density;
  private MyPlayer myPlayer;
  private PhotoViewAttacher largeMapPhotoView;
  private ListView galleriesView;
  private GalleryAdapter galleryAdapter;
  private TextView galleryHeader;
  private LargeMapView largeMapView;
  private Matrix originalMapMatrix;
  private LinearLayout galleryDetail;
  private LinearLayout moveButtons;
  private LinearLayout missingStopButtons;
  private TextView missingStopAcceptButton;
  private LinearLayout mediaLayout;
  private TextView acceptButton;
  private LinearLayout addStopView;
  private EditText missingStopNumberButton;
  private RequestQueue queue;

  private class MyPhotoViewAttacher extends PhotoViewAttacher {

    public MyPhotoViewAttacher(ImageView imageView) {
      super(imageView);
    }

    @Override
    public void onGlobalLayout() {
      super.onGlobalLayout();
      // HACK(aaron): Capture basematrix
      if (originalMapMatrix == null) {
        originalMapMatrix = getDisplayMatrix();
      }
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    queue = Volley.newRequestQueue(this);
    setContentView(R.layout.activity_map);

    density = getResources().getDisplayMetrics().density;

    myPlayer = new MyPlayer(
        (SeekBar) findViewById(R.id.seek),
        (Button) findViewById(R.id.play),
        (TextView) findViewById(R.id.time),
        (TextView) findViewById(R.id.audioTitle)
    );

    largeMapView = (LargeMapView) findViewById(R.id.largeMap);
    largeMapPhotoView = new MyPhotoViewAttacher(largeMapView);
    largeMapPhotoView.setMaximumScale(20f);
    largeMapPhotoView.setMediumScale(5f);


    galleriesView = (ListView) findViewById(R.id.listView);
    addStopView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.add_stop, null);
    addStopView.setVisibility(View.GONE);
    galleriesView.setFooterDividersEnabled(false);
    galleriesView.addFooterView(addStopView);
    galleryAdapter = new GalleryAdapter(this, android.R.layout.simple_list_item_1);
    galleriesView.setAdapter(galleryAdapter);

    galleryHeader = (TextView) findViewById(R.id.galleryHeader);

    largeMapPhotoView.setOnViewTapListener(new OnMapTap());

    galleryDetail = (LinearLayout) findViewById(R.id.galleryDetail);
    moveButtons = (LinearLayout) findViewById(R.id.moveButtons);
    missingStopButtons = (LinearLayout) findViewById(R.id.missingStopButtons);
    mediaLayout = (LinearLayout) findViewById(R.id.mediaLayout);
    acceptButton = (TextView) findViewById(R.id.accept);
    missingStopAcceptButton = (TextView) findViewById(R.id.missingStopAccept);
    missingStopNumberButton = (EditText) findViewById(R.id.missingStopNumber);
  }


  private class OnMapTap implements PhotoViewAttacher.OnViewTapListener {
    @Override
    public void onViewTap(View view, float v, float v1) {
      addStopView.setVisibility(View.VISIBLE);
      if (largeMapView.getPinToPlace() != null) {
        return;
      }
      Matrix matrix = new Matrix();
      largeMapPhotoView.getDisplayMatrix().invert(matrix);
      float[] coordinates = new float[]{v, v1};
      matrix.mapPoints(coordinates);
      Log.i("tag", coordinates[0] / density + " " + coordinates[1] / density);

      Integer pinPosition = largeMapView.getPin(coordinates[0], coordinates[1]);
      if (pinPosition != null) {
        galleriesView.setSelection(pinPosition);
        return;
      }

      galleryAdapter.clear();
      largeMapView.clearPins();
      galleryHeader.setText("Select a Gallery");


      for (GalleryViewRect rect : MyApplication.galleryRectById.values()) {
        if (rect.contains(coordinates[0], coordinates[1])) {
          final int gallery = rect.getId();
          galleryHeader.setText("Gallery " + rect.getId());
          SQLiteDatabase db = new FeedReaderDbHelper(getApplicationContext()).getReadableDatabase();
          try (Cursor c = db.rawQuery(
              "SELECT s.objectId, s.title as objectTitle, image, m.title as audioTitle, m.uri as audio, m.stop, m.position, width, height " +
                  "FROM processed_stop s " +
                  "INNER JOIN processed_media m ON (s.id = m.stop) " +
                  "WHERE gallery = " + rect.getId() + " " +
                  "AND m.uri IS NOT NULL " +
                  "ORDER BY m.stop, m.position ", null)
          ) {
            HashMap<String, ArrayList<QueryModel>> modelsByObjectId = new HashMap<>();
            HashMap<String, ArrayList<QueryModel>> modelsByTitle = new HashMap<>();
            while ((c.moveToNext())) {
              String objectId = c.getString(c.getColumnIndexOrThrow("objectId"));
              String title = c.getString(c.getColumnIndexOrThrow("objectTitle"));
              String image = c.getString(c.getColumnIndexOrThrow("image"));
              String audioTitle = c.getString(c.getColumnIndex("audioTitle"));
              String audio = c.getString(c.getColumnIndex("audio"));
              int stop = c.getInt(c.getColumnIndex("stop"));
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
            galleryAdapter.addAll(stopModels);
            galleriesView.setSelection(0);


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
                  float x = cl.getFloat(cl.getColumnIndexOrThrow("x"));
                  float y = cl.getFloat(cl.getColumnIndexOrThrow("y"));
                  artObjectIdToPoint.put(objectId, new PointF(x * density, y * density));
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
                largeMapView.setPins(pins);
              }
            }
          }

          // Zoom to gallery on large map
          RectF imageBounds = new RectF(0, 0, largeMapPhotoView.getImageView().getWidth(), largeMapPhotoView.getImageView().getHeight());
          Matrix newMatrix = new Matrix();
          RectF galleryRect = rect.getScaled();
          float minSize = 100 * density;
          float sizeX = Math.max(minSize, galleryRect.width());
          float sizeY = Math.max(minSize, galleryRect.height());
          RectF newViewPort = new RectF(
              (galleryRect.centerX() - sizeX / 2),
              (galleryRect.centerY() - sizeY / 2),
              (galleryRect.centerX() + sizeX / 2),
              (galleryRect.centerY() + sizeY / 2)
          );
          newMatrix.setRectToRect(newViewPort, imageBounds, Matrix.ScaleToFit.CENTER);
          Matrix originalInvertedMatrix = new Matrix();
          originalMapMatrix.invert(originalInvertedMatrix);
          newMatrix.preConcat(originalInvertedMatrix);
          largeMapPhotoView.setDisplayMatrix(newMatrix);


          addStopView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              PointF pinLocation = largeMapView.getPinLocation();
              Matrix matrix = new Matrix();
              largeMapPhotoView.getDisplayMatrix().invert(matrix);
              float[] coordinates = new float[]{pinLocation.x, pinLocation.y};
              matrix.mapPoints(coordinates);
              float x = coordinates[0];
              float y = coordinates[1];
              final ArrayList<ArtObjectLocation> originalLocations = largeMapView.getLocations();
              ArrayList<ArtObjectLocation> newLocations = new ArrayList<>(originalLocations);
              newLocations.add(new ArtObjectLocation("temp", 0, x, y));
              largeMapView.setPins(newLocations);

              largeMapView.setPinToPlace(0);
              galleryDetail.setVisibility(View.GONE);
              missingStopButtons.setVisibility(View.VISIBLE);
              missingStopAcceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  String androidId = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                      Settings.Secure.ANDROID_ID);
                  try {
                    PointF pinLocation = largeMapView.getPinLocation();
                    Matrix matrix = new Matrix();
                    largeMapPhotoView.getDisplayMatrix().invert(matrix);
                    float[] coordinates = new float[]{pinLocation.x, pinLocation.y};
                    matrix.mapPoints(coordinates);

                    float x = coordinates[0] / density;
                    float y = coordinates[1] / density;

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("stop", missingStopNumberButton.getText());
                    jsonObject.put("gallery", gallery);
                    jsonObject.put("x", x);
                    jsonObject.put("y", y);
                    jsonObject.put("user", androidId);
                    jsonObject.put("created", System.currentTimeMillis());
                    JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        "https://glacial-everglades-23026.herokuapp.com/missing-stop",
                        jsonObject,
                        new Response.Listener<JSONObject>() {
                          @Override
                          public void onResponse(JSONObject response) {

                          }
                        },
                        new Response.ErrorListener() {
                          @Override
                          public void onErrorResponse(VolleyError error) {

                          }
                        }
                    );
                    queue.add(request);
                  } catch (JSONException e) {
                    Log.e("upload-missing-stop", e.getMessage(), e);
                  }
                  InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                  imm.hideSoftInputFromWindow(missingStopNumberButton.getWindowToken(), 0);

                  galleryDetail.setVisibility(View.VISIBLE);
                  missingStopButtons.setVisibility(View.GONE);
                  largeMapView.setPins(originalLocations);
                  largeMapView.clearPinToPlace();
                }
              });
            }
          });
        }
      }
    }
  }

  private class GalleryAdapter extends ArrayAdapter<StopModel> {

    public GalleryAdapter(Context context, int resource) {
      super(context, resource);
    }

    class GalleryHolder {
      private final TextView titleView;
      private final ImageView imageView;
      private final ArrayList<TextView> audioTextViews = new ArrayList<>(6);
      private final ArrayList<View> audioDividers = new ArrayList<>(6);

      public GalleryHolder(TextView titleView, ImageView imageView,
                           TextView audio0, TextView audio1, TextView audio2, TextView audio3,
                           TextView audio4, TextView audio5, View divider0, View divider1, View divider2, View divider3, View divider4) {
        this.titleView = titleView;
        this.imageView = imageView;
        this.audioTextViews.add(audio0);
        this.audioTextViews.add(audio1);
        this.audioTextViews.add(audio2);
        this.audioTextViews.add(audio3);
        this.audioTextViews.add(audio4);
        this.audioTextViews.add(audio5);
        this.audioDividers.add(divider0);
        this.audioDividers.add(divider1);
        this.audioDividers.add(divider2);
        this.audioDividers.add(divider3);
        this.audioDividers.add(divider4);
      }

      public TextView getAudio(int i) {
        return audioTextViews.get(i);
      }

      public View getDivider(int i) {
        return audioDividers.get(i);
      }
    }

    @Override
    public boolean isEnabled(int position) {
      return false;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
      final StopModel model = getItem(position);
      if (convertView == null) {
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.gallery, parent, false);
        GalleryHolder holder = new GalleryHolder(
            (TextView) convertView.findViewById(R.id.title),
            (ImageView) convertView.findViewById(R.id.objectImage),
            (TextView) convertView.findViewById(R.id.audio0),
            (TextView) convertView.findViewById(R.id.audio1),
            (TextView) convertView.findViewById(R.id.audio2),
            (TextView) convertView.findViewById(R.id.audio3),
            (TextView) convertView.findViewById(R.id.audio4),
            (TextView) convertView.findViewById(R.id.audio5),
            convertView.findViewById(R.id.audioDivider0),
            convertView.findViewById(R.id.audioDivider1),
            convertView.findViewById(R.id.audioDivider2),
            convertView.findViewById(R.id.audioDivider3),
            convertView.findViewById(R.id.audioDivider4)
        );
        convertView.setTag(holder);
      }
      final GalleryHolder holder = (GalleryHolder) convertView.getTag();
      holder.titleView.setText((position + 1) + ". " + model.getTitle());


      float imageScale = (75.0f * density) / model.getWidth();
      holder.imageView.getLayoutParams().width = Math.round(imageScale * model.getWidth());
      holder.imageView.getLayoutParams().height = Math.round(imageScale * model.getHeight());

      Glide.with(getContext())
          .load(model.getImageURL())
          .diskCacheStrategy(DiskCacheStrategy.RESULT)
          .fitCenter()
          .override(Math.round(imageScale * model.getWidth()), Math.round(imageScale * model.getHeight()))
          .into(holder.imageView);

      int mediaSize = model.getMedias().size();
      for (int i = 0; i < 6; i++) {
        TextView audioTitle = holder.getAudio(i);
        audioTitle.setOnClickListener(null);
        audioTitle.setVisibility(View.GONE);
        if (i < 5) {
          holder.getDivider(i).setVisibility(View.GONE);
        }
        if (i < mediaSize) {
          if (i > 0) {
            holder.getDivider(i - 1).setVisibility(View.VISIBLE);
          }
          audioTitle.setVisibility(View.VISIBLE);
          final MediaModel media = model.getMedias().get(i);
          final ArrayList<MediaModel> queue = new ArrayList<>();
          for (int j = i + 1; j < model.getMedias().size(); j++) {
            queue.add(model.getMedias().get(j));
          }
          audioTitle.setText(media.getTitle());
          audioTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              try {
                myPlayer.play(media.getUri(), media.getTitle(), queue);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            }
          });
        }
      }

      holder.titleView.setOnLongClickListener(new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
          galleryDetail.setVisibility(View.GONE);
          moveButtons.setVisibility(View.VISIBLE);
          largeMapView.setPinToPlace(position + 1);
          acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              PointF pinLocation = largeMapView.getPinLocation();
              Matrix matrix = new Matrix();
              largeMapPhotoView.getDisplayMatrix().invert(matrix);
              float[] coordinates = new float[]{pinLocation.x, pinLocation.y};
              matrix.mapPoints(coordinates);

              float x = coordinates[0] / density;
              float y = coordinates[1] / density;
              SQLiteDatabase db = new FeedReaderDbHelper(getApplicationContext()).getWritableDatabase();
              ContentValues contentValues = new ContentValues();
              contentValues.put("objectId", model.getArtObjectId());
              contentValues.put("x", x);
              contentValues.put("y", y);
              db.insertWithOnConflict("object_location", "objectId", contentValues, SQLiteDatabase.CONFLICT_REPLACE);
              galleryDetail.setVisibility(View.VISIBLE);
              moveButtons.setVisibility(View.GONE);

              String androidId = Settings.Secure.getString(getContext().getContentResolver(),
                  Settings.Secure.ANDROID_ID);
              try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("objectId", model.getArtObjectId());
                jsonObject.put("x", x);
                jsonObject.put("y", y);
                jsonObject.put("user", androidId);
                jsonObject.put("created", System.currentTimeMillis());
                JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    "https://glacial-everglades-23026.herokuapp.com/location",
                    jsonObject,
                    new Response.Listener<JSONObject>() {
                      @Override
                      public void onResponse(JSONObject response) {

                      }
                    },
                    new Response.ErrorListener() {
                      @Override
                      public void onErrorResponse(VolleyError error) {

                      }
                    }
                );
                queue.add(request);
              } catch (JSONException e) {
                Log.e("upload-location", e.getMessage(), e);
              }
              ArrayList<ArtObjectLocation> newLocations = new ArrayList<ArtObjectLocation>();
              for (ArtObjectLocation location : largeMapView.getLocations()) {
                if (location.getId().equals(model.getArtObjectId())) {
                  ArtObjectLocation newLocation = new ArtObjectLocation(
                      location.getId(),
                      location.getPosition(),
                      coordinates[0],
                      coordinates[1]
                  );
                  newLocations.add(newLocation);
                } else {
                  newLocations.add(location);
                }
              }
              largeMapView.setPins(newLocations);
              largeMapView.clearPinToPlace();
            }
          });
          return true;
        }
      });
      return convertView;
    }
  }
}

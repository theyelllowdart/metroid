package com.example.aaron.metandroid;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
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

//            Collections.sort(stopModels);
            galleryAdapter.addAll(rows);
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
                  InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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

  private class GalleryAdapter extends ArrayAdapter<ArtObjectRow> {

    public GalleryAdapter(Context context, int resource) {
      super(context, resource);
    }

    class GalleryHolder2 {
      private final ImageView imageView1;
      private final ImageView imageView2;
      private final RelativeLayout layout1;
      private final RelativeLayout layout2;
      private final TextView pinId1;
      private final TextView pinId2;
      private final TextView title1;
      private final TextView title2;
      private TextView image1Text;

      public GalleryHolder2(ImageView imageView1, ImageView imageView2, RelativeLayout layout1, RelativeLayout layout2, TextView pinId1, TextView pinId2, TextView title1, TextView title2) {
        this.imageView1 = imageView1;
        this.imageView2 = imageView2;
        this.layout1 = layout1;
        this.layout2 = layout2;
        this.pinId1 = pinId1;
        this.pinId2 = pinId2;
        this.title1 = title1;
        this.title2 = title2;
      }
    }

    @Override
    public boolean isEnabled(int position) {
      return false;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
      final ArtObjectRow row = getItem(position);
      if (convertView == null) {
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.gallery2, parent, false);
        GalleryHolder2 holder = new GalleryHolder2(
            (ImageView) convertView.findViewById(R.id.objectImage1),
            (ImageView) convertView.findViewById(R.id.objectImage2),
            (RelativeLayout) convertView.findViewById(R.id.objectImage1Layout),
            (RelativeLayout) convertView.findViewById(R.id.objectImage2Layout),
            (TextView) convertView.findViewById(R.id.objectImage1PinId),
            (TextView) convertView.findViewById(R.id.objectImage2PinId),
            (TextView) convertView.findViewById(R.id.objectImage1Title),
            (TextView) convertView.findViewById(R.id.objectImage2Tilte)
        );
        convertView.setTag(holder);
      }
      final GalleryHolder2 holder = (GalleryHolder2) convertView.getTag();


      int total;
      if (row.getModel2() != null) {
        total = 2;
      } else {
        total = 1;
      }

      if (total == 2) {
        holder.imageView2.setVisibility(View.VISIBLE);
      } else {
        holder.imageView2.setVisibility(View.GONE);
      }


      for (int i = 0; i < total; i++) {
        StopModel model;
        ImageView imageView;
        RelativeLayout layout;
        TextView title;
        TextView pinId;

        int width = 200;
        int height = 200;
        if (i == 0) {
          layout = holder.layout1;
          title = holder.title1;
          pinId = holder.pinId1;
          model = row.getModel1();
          imageView = holder.imageView1;
          if ((model.getWidth() / (double) model.getHeight()) > 1.3) {
            width = width * 2;
          }
        } else {
          layout = holder.layout2;
          title = holder.title2;
          pinId = holder.pinId2;
          model = row.getModel2();
          imageView = holder.imageView2;
        }

        float imageScale = (width * density) / model.getWidth();
        if (total == 2) {
          imageView.getLayoutParams().width = Math.round(200 * density);
        } else {
          imageView.getLayoutParams().width = Math.round(400 * density);
        }
        imageView.getLayoutParams().height = Math.round(height * density);


        layout.getLayoutParams().height = imageView.getLayoutParams().height;
        layout.getLayoutParams().width = imageView.getLayoutParams().width;
        title.setText(model.getTitle());
        pinId.setText(String.valueOf(position + 1));

        Glide.with(getContext())
            .load(model.getImageURL())
            .diskCacheStrategy(DiskCacheStrategy.RESULT)
//            .centerCrop()
            .transform(new CenterTopTranformation(getContext()))
            .override(Math.round(imageScale * model.getWidth()), Math.round(height * density))
            .into(imageView);
      }
      return convertView;
    }
  }

  private class CenterTopTranformation extends BitmapTransformation {

    public CenterTopTranformation(Context context) {
      super(context);
    }

    public CenterTopTranformation(BitmapPool bitmapPool) {
      super(bitmapPool);
    }


    private Bitmap transform(Bitmap recycled, Bitmap toCrop, int width, int height) {
      if (toCrop == null) {
        return null;
      } else if (toCrop.getWidth() == width && toCrop.getHeight() == height) {
        return toCrop;
      }
      // From ImageView/Bitmap.createScaledBitmap.
      final float scale;
      float dx = 0;
      Matrix m = new Matrix();
      if (toCrop.getWidth() * height > width * toCrop.getHeight()) {
        scale = (float) height / (float) toCrop.getHeight();
        dx = (width - toCrop.getWidth() * scale) * 0.5f;
      } else {
        scale = (float) width / (float) toCrop.getWidth();
      }

      m.setScale(scale, scale);
      m.postTranslate((int) (dx + 0.5f), 0);
      final Bitmap result;
      if (recycled != null) {
        result = recycled;
      } else {
        result = Bitmap.createBitmap(width, height, toCrop.getConfig() != null ? toCrop.getConfig() : Bitmap.Config.ARGB_8888);
      }

      // We don't add or remove alpha, so keep the alpha setting of the Bitmap we were given.
      TransformationUtils.setAlpha(toCrop, result);

      Canvas canvas = new Canvas(result);
      Paint paint = new Paint(TransformationUtils.PAINT_FLAGS);
      canvas.drawBitmap(toCrop, m, paint);
      return result;
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
      final Bitmap toReuse = pool.get(outWidth, outHeight, toTransform.getConfig() != null
          ? toTransform.getConfig() : Bitmap.Config.ARGB_8888);

      Bitmap transformed = transform(toReuse, toTransform, outWidth, outHeight);
      if (toReuse != null && toReuse != transformed && !pool.put(toReuse)) {
        toReuse.recycle();
      }
      return transformed;
    }

    @Override
    public String getId() {
      return "yo";
    }
  }
}


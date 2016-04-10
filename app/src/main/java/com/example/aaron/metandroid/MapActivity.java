package com.example.aaron.metandroid;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.google.common.base.Joiner;

import java.io.IOException;
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
  private LinearLayout mediaLayout;
  private TextView acceptButton;

  private class MyPhotoViewAttacher extends PhotoViewAttacher{

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
    galleryAdapter = new GalleryAdapter(this, android.R.layout.simple_list_item_1);
    galleriesView.setAdapter(galleryAdapter);

    galleryHeader = (TextView) findViewById(R.id.galleryHeader);

    largeMapPhotoView.setOnViewTapListener(new OnMapTap());

    galleryDetail = (LinearLayout) findViewById(R.id.galleryDetail);
    moveButtons = (LinearLayout) findViewById(R.id.moveButtons);
    mediaLayout = (LinearLayout) findViewById(R.id.mediaLayout);
    acceptButton = (TextView) findViewById(R.id.accept);
  }



  private class OnMapTap implements PhotoViewAttacher.OnViewTapListener {
    @Override
    public void onViewTap(View view, float v, float v1) {
      if (largeMapView.getPinToPlace() != null){
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
            HashMap<Integer, ArrayList<QueryModel>> modelsByObjectId = new HashMap<>();
            while ((c.moveToNext())) {
              int objectId = c.getInt(c.getColumnIndexOrThrow("objectId"));
              String title = c.getString(c.getColumnIndexOrThrow("objectTitle"));
              String image = c.getString(c.getColumnIndexOrThrow("image"));
              String audioTitle = c.getString(c.getColumnIndex("audioTitle"));
              String audio = c.getString(c.getColumnIndex("audio"));
              int stop = c.getInt(c.getColumnIndex("stop"));
              int position = c.getInt(c.getColumnIndex("position"));
              int width = c.getInt(c.getColumnIndex("width"));
              int height = c.getInt(c.getColumnIndex("height"));
              QueryModel model = new QueryModel(title, image, audioTitle, audio, stop, position, objectId, width, height);
              if (!modelsByObjectId.containsKey(objectId)) {
                modelsByObjectId.put(objectId, new ArrayList<QueryModel>());
              }
              ArrayList<QueryModel> models = modelsByObjectId.get(objectId);
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
            for (ArrayList<QueryModel> qs : modelsByObjectId.values()) {
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
            HashSet<Integer> artObjectIds = new HashSet<>();
            for (StopModel s : stopModels) {
              artObjectIds.add(s.getArtObjectId());
            }
            if (!artObjectIds.isEmpty()) {
              try (Cursor cl = db.rawQuery(
                  "SELECT id, x, y " +
                      "FROM object_location " +
                      "WHERE id in (" + Joiner.on(',').join(artObjectIds) + ") ", null)
              ) {
                HashMap<Integer, PointF> artObjectIdToPoint = new HashMap<>();
                while ((cl.moveToNext())) {
                  int objectId = cl.getInt(cl.getColumnIndexOrThrow("id"));
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
          float minSize = 50 * density;
          float sizeX = Math.max(minSize, galleryRect.width());
          float sizeY = Math.max(minSize, galleryRect.height());
          RectF newViewPort = new RectF(
              (galleryRect.centerX() - sizeX/2),
              (galleryRect.centerY() - sizeY/2),
              (galleryRect.centerX() + sizeX/2),
              (galleryRect.centerY() + sizeY/2)
          );
          newMatrix.setRectToRect(newViewPort, imageBounds, Matrix.ScaleToFit.CENTER);
          Matrix originalInvertedMatrix = new Matrix();
          originalMapMatrix.invert(originalInvertedMatrix);
          newMatrix.preConcat(originalInvertedMatrix);
          largeMapPhotoView.setDisplayMatrix(newMatrix);
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
          .override(Math.round(imageScale * model.getWidth()),  Math.round(imageScale * model.getHeight()))
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
          audioTitle.setText(media.getTitle());
          audioTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              try {
                myPlayer.play(media.getUri(), media.getTitle());
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

              SQLiteDatabase db = new FeedReaderDbHelper(getApplicationContext()).getWritableDatabase();
              ContentValues contentValues = new ContentValues();
              contentValues.put("id", model.getArtObjectId());
              contentValues.put("x", coordinates[0] / density);
              contentValues.put("y", coordinates[1] / density);
              db.insertWithOnConflict("object_location", "id", contentValues, SQLiteDatabase.CONFLICT_REPLACE);
              galleryDetail.setVisibility(View.VISIBLE);
              moveButtons.setVisibility(View.GONE);

//              List<ArtObjectLocation> pins = largeMapView.getPins();
//              for (ArtObjectLocation pin: pins) {
//                if (pin.getId() == model.getArtObjectId()) {
//                  pin.setX(coordinates[0]);
//                  pin.setY(coordinates[1]);
//                }
//              }
//              largeMapView.setPins(pins);
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

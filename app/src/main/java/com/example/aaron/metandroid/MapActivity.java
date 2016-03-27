package com.example.aaron.metandroid;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import uk.co.senab.photoview.PhotoViewAttacher;


public class MapActivity extends Activity {

  private Float density;
  private MyPlayer myPlayer;
  private PhotoViewAttacher largeMapView;
  private ListView galleriesView;
  private GalleryAdapter galleryAdapter;
  private TextView galleryHeader;


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

    largeMapView = new PhotoViewAttacher((LargeMapView) findViewById(R.id.largeMap));
    largeMapView.setMaximumScale(7.0f);

    galleriesView = (ListView) findViewById(R.id.listView);
    galleryAdapter = new GalleryAdapter(this, android.R.layout.simple_list_item_1);
    galleriesView.setAdapter(galleryAdapter);

    galleryHeader = (TextView) findViewById(R.id.galleryHeader);

    largeMapView.setOnViewTapListener(new OnMapTap());
  }

  private class OnMapTap implements PhotoViewAttacher.OnViewTapListener {
    @Override
    public void onViewTap(View view, float v, float v1) {
      galleryAdapter.clear();

      Matrix matrix = new Matrix();
      largeMapView.getDisplayMatrix().invert(matrix);
      float[] coordinates = new float[]{v, v1};
      matrix.mapPoints(coordinates);
      Log.i("tag", coordinates[0] / density + " " + coordinates[1] / density);


      for (GalleryViewRect rect : MyApplication.galleryRectById.values()) {
        if (rect.contains(coordinates[0], coordinates[1])) {
          galleryHeader.setText("Gallery " + rect.getId());
          SQLiteDatabase db = new FeedReaderDbHelper(getApplicationContext()).getReadableDatabase();
          try (Cursor c = db.rawQuery(
              "SELECT s.objectId, s.title as objectTitle, image, m.title as audioTitle, m.uri as audio, m.stop, m.position " +
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
              QueryModel model = new QueryModel(title, image, audioTitle, audio, stop, position);
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
              StopModel stopModel = new StopModel(rect.getId(), qs.get(0).getTitle(), qs.get(0).getImageURL());
              for (QueryModel q : qs) {
                stopModel.addMedia(new MediaModel(q.getMediaTitle(), q.getMediaURL(), q.getStopId()));
              }
              stopModels.add(stopModel);
            }
            Collections.sort(stopModels);
            galleryAdapter.addAll(stopModels);
            galleriesView.setSelection(0);
          }
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
    public View getView(int position, View convertView, ViewGroup parent) {
      StopModel model = getItem(position);
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
      holder.imageView.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
      //scroll to center of gallery
//      GalleryViewRect rect = MyApplication.galleryRectById.get(model.getGalleryId());
//      holder.mapView.setGallery(rect.getScaled());

      Glide.with(getContext()).load(model.getImageURL()).transform(new BitmapTransformation(getContext()) {
        @Override
        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
          Matrix m = new Matrix();
          Float scale = (75.0f * density) / toTransform.getWidth();
          //holder.imageView.getLayoutParams().height = new Float(toTransform.getHeight() * scale).intValue();
          m.setScale(scale, scale);
          if (toTransform.getWidth() == outWidth && toTransform.getHeight() == outHeight) {
            return toTransform;
          }
          return Bitmap.createBitmap(toTransform, 0, 0, toTransform.getWidth(), toTransform.getHeight(), m, true);
        }

        @Override
        public String getId() {
          return "scaled123";
        }
      }).diskCacheStrategy(DiskCacheStrategy.RESULT).into(holder.imageView);

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

      return convertView;
    }
  }
}

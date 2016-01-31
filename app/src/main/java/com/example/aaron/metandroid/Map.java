package com.example.aaron.metandroid;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
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


public class Map extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        final ListView listView = (ListView) findViewById(R.id.listView);
        final Float density = getResources().getDisplayMetrics().density;

        final Activity activity = this;
        final ArrayAdapter<StopModel> listAdapter = new ArrayAdapter<StopModel>(this, android.R.layout.simple_list_item_1) {

            class GalleryHolder {
                private final TextView titleView;
                private final ImageView imageView;
                private final ArrayList<TextView> audioTextViews = new ArrayList<>(6);

                public GalleryHolder(TextView titleView, ImageView imageView, TextView audio0, TextView audio1, TextView audio2, TextView audio3, TextView audio4, TextView audio5) {
                    this.titleView = titleView;
                    this.imageView = imageView;
                    this.audioTextViews.add(audio0);
                    this.audioTextViews.add(audio1);
                    this.audioTextViews.add(audio2);
                    this.audioTextViews.add(audio3);
                    this.audioTextViews.add(audio4);
                    this.audioTextViews.add(audio5);
                }

                public TextView getAudio(int i) {
                    return audioTextViews.get(i);
                }
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
                            (TextView) convertView.findViewById(R.id.audio5)
                    );
                    convertView.setTag(holder);
                }
                final GalleryHolder holder = (GalleryHolder) convertView.getTag();
                holder.titleView.setText(model.getTitle());
                holder.imageView.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
//                holder.imageView.getLayoutParams().width = 75 * density.intValue();
//                holder.imageView.requestLayout();


                Glide.with(activity).load(model.getImageURL()).transform(new BitmapTransformation(activity) {
                    @Override
                    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
                        Matrix m = new Matrix();
                        Float scale = (75.0f * density) / toTransform.getWidth();
//                        holder.imageView.getLayoutParams().height = new Float(toTransform.getHeight() * scale).intValue();
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
                    TextView view = holder.getAudio(i);
                    view.setOnClickListener(null);
                    if (i < mediaSize) {
                        view.setVisibility(View.VISIBLE);
                        final MediaModel media = model.getMedias().get(i);
                        view.setText("Stop " + media.getStop() + ": " + media.getTitle());
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MediaPlayer mediaPlayer = new MediaPlayer();
                                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                try {
                                    mediaPlayer.setDataSource(media.getUri());
                                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                        @Override
                                        public void onPrepared(MediaPlayer mp) {
                                            mp.start();
                                        }
                                    });
                                    mediaPlayer.prepareAsync();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                    } else {
                        view.setVisibility(View.GONE);
                    }
                }

                return convertView;
            }
        };
        listView.setAdapter(listAdapter);
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.eng_7686);
//                mediaPlayer.start();
//            }
//        });


        final DebugMapView imageView = (DebugMapView) findViewById(R.id.imageView);
        final PhotoViewAttacher photoView = new PhotoViewAttacher(imageView);
        photoView.setMaximumScale(7.0f);

        final TextView galleryHeader = (TextView) findViewById(R.id.galleryHeader);

        // TODO(aaron): Put this into a data verification test
        for (GalleryViewRect rect1 : imageView.rects) {
            for (GalleryViewRect rect2 : imageView.rects) {
                if (rect2.getOriginal().intersect(rect1.getOriginal()) && rect1.getId() != rect2.getId()) {
                    throw new RuntimeException("intersecting shapes " + rect1.getId() + " " + rect2.getId());
                }
            }
        }

        photoView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float v, float v1) {
                Matrix matrix = new Matrix();
                photoView.getDisplayMatrix().invert(matrix);
                float[] coordinates = new float[]{v, v1};
                matrix.mapPoints(coordinates);
                matrix.postScale(photoView.getScale(), photoView.getScale());
                matrix.postTranslate(v, v1);
                float realX = coordinates[0] / density;
                float realY = coordinates[1] / density;
                Log.e("tag", realX + " " + realY);


                listAdapter.clear();
                for (GalleryViewRect rect : imageView.rects) {
                    if (rect.getOriginal().contains(realX, realY)) {
                        // TODOD(aaron): set header when gallery was found
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
//                                Glide.with(activity).load(qs.get(0).getImageURL()).fitCenter().preload(100 * density.intValue(), 100 * density.intValue());
                                StopModel stopModel = new StopModel(qs.get(0).getTitle(), qs.get(0).getImageURL());
                                for (QueryModel q : qs) {
                                    stopModel.addMedia(new MediaModel(q.getMediaTitle(), q.getMediaURL(), q.getStopId()));
                                }
                                stopModels.add(stopModel);
                            }
                            Collections.sort(stopModels);
                            listAdapter.addAll(stopModels);
                            listView.setSelection(0);
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //scaleGestureDetector.onTouchEvent(ev);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

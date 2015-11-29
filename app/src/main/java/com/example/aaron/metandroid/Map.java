package com.example.aaron.metandroid;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Matrix;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import java.lang.reflect.Method;
import java.sql.Array;
import java.util.logging.Logger;

import uk.co.senab.photoview.PhotoViewAttacher;


public class Map extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        String[] codeLearnChapters = new String[]{"Android Introduction", "Android Setup/Installation", "Android Hello World", "Android Layouts/Viewgroups", "Android Activity & Lifecycle", "Intents in Android"};
        ArrayAdapter<String> codeLearnArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, codeLearnChapters);
        ListView codeLearnLessons = (ListView) findViewById(R.id.listView);
        codeLearnLessons.setAdapter(codeLearnArrayAdapter);
        codeLearnLessons.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.eng_7686);
                mediaPlayer.start();
            }
        });


        final ImageView imageView = (ImageView) findViewById(R.id.imageView);
        final PhotoViewAttacher photoView = new PhotoViewAttacher(imageView);
        photoView.setMaximumScale(7.0f);




        final VectorDrawable drawable = (VectorDrawable) imageView.getDrawable().mutate();

//        final ImageView overlayView = (ImageView)findViewById(R.id.overlay);
//        overlayView.setImageMatrix(photoView.getDisplayMatrix());


//        View overlay = (View) findViewById(R.id.overlay);
//        int opacity = 200; // from 0 to 255
//        overlay.
//        overlay.setBackgroundColor(opacity * 0x1000000); // black with a variable alpha
//        overlay.invalidate(); // update the view


//        imageView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                float x = event.getX();
//                float y = event.getY();
//                float[] coordinates = new float[]{x, y};
//                Matrix matrix = new Matrix();
//                Matrix viewMatrix = imageView.getImageMatrix();
//                viewMatrix.invert(matrix);
//                matrix.mapPoints(coordinates);
//                float density = getResources().getDisplayMetrics().density;
//                float realX = coordinates[0]/density;
//                float realY = coordinates[1]/density;
//                overlayView.invalidate();
//                return true;
//            }
//        });

        SQLiteDatabase db = new FeedReaderDbHelper(getApplicationContext()).getReadableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(GalleryContract.FeedEntry.COLUMN_NAME_ID, 1);
        values.put(GalleryContract.FeedEntry.COLUMN_NAME_TITLE, "hello");

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
//        newRowId = db.insert(
//                GalleryContract.FeedEntry.TABLE_NAME,
//                null,
//                values);

        Cursor c = db.query(GalleryContract.FeedEntry.TABLE_NAME,
                new String[]{GalleryContract.FeedEntry.COLUMN_NAME_ID},
                null,
                null,
                null,
                null,
                null,
                null);
        c.moveToFirst();
        for (int i = 0; i < c.getCount(); i++) {
            int a = c.getInt(c.getColumnIndexOrThrow(GalleryContract.FeedEntry.COLUMN_NAME_ID));
            int b = a + 1;
        }
        c.close();


        photoView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float v, float v1) {
                float i = photoView.getScale();
                float j = imageView.getScaleX();
                int x = 0;
                int h = drawable.getIntrinsicHeight();
                Matrix matrix = new Matrix();
                photoView.getDisplayMatrix().invert(matrix);
                float[] coordinates = new float[]{v, v1};
                matrix.mapPoints(coordinates);
                matrix.postScale(photoView.getScale(), photoView.getScale());
                matrix.postTranslate(v, v1);
                float density = getResources().getDisplayMetrics().density;
                float realX = coordinates[0] / density;
                float realY = coordinates[1] / density;
                int a = 0;
                Log.e("tag", realX + " " + realY);
//                overlayView.invalidate();
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

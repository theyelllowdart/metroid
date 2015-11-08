package com.example.aaron.metandroid;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import uk.co.senab.photoview.PhotoViewAttacher;


public class Map extends Activity {

    public int pxToDp(int px) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }

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
//        final VectorDrawable drawable = (VectorDrawable) imageView.getDrawable().mutate();
//
//
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
//                int pxToDp = pxToDp(Math.round(x));
//                return true;
//            }
//        });
//        photoView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
//            @Override
//            public void onViewTap(View view, float v, float v1) {
//                float i = photoView.getScale();
//                float j = imageView.getScaleX();
//                int x = 0;
//                int h = drawable.getIntrinsicHeight();
//                Matrix matrix = new Matrix();
//                photoView.getDisplayMatrix().invert(matrix);
//                float[] coordinates = new float[]{v, v1};
//                matrix.mapPoints(coordinates);
//                matrix.postScale(photoView.getScale(), photoView.getScale());
//                matrix.postTranslate(v, v1);
//                int a = 0;
//            }
//        });
//        photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
//            @Override
//            public void onPhotoTap(View view, float xPercent, float yPercent) {
//                int x = Math.round(xPercent * imageView.getMaxWidth());
//                int y = Math.round(yPercent * imageView.getMaxHeight());
//                int intrinsic = drawable.getIntrinsicWidth();
//                Log.i("setOnPhotoTapListener", "x: " + x + " y: " + y);
//
//            }
//        });
//        photoView.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
//            @Override
//            public boolean onSingleTapConfirmed(MotionEvent e) {
//                float x = e.getX(e.getActionIndex());
//                float y = e.getY(e.getActionIndex());
//                float[] coordinates = new float[]{x, y};
//                Matrix matrix = new Matrix();
//                photoView.getDrawMatrix().invert(matrix);
//                //matrix.postTranslate(photoView.getScrollX(), photoView.getScaleY());
//                matrix.mapPoints(coordinates);
//                return false;
//            }
//
//            @Override
//            public boolean onDoubleTap(MotionEvent e) {
//                return false;
//            }
//
//            @Override
//            public boolean onDoubleTapEvent(MotionEvent e) {
//                return false;
//            }
//        });
//        try {
//            new VectorDrawableExtension(drawable).setFillAlpha("myPath", 0.1f);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
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

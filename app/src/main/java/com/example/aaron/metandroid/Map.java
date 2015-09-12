package com.example.aaron.metandroid;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import java.lang.reflect.Method;

import uk.co.senab.photoview.PhotoViewAttacher;


public class Map extends Activity {
    private ImageView imageView;
    private ScaleGestureDetector scaleGestureDetector;
    private PhotoViewAttacher mAttacher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        imageView = (ImageView) findViewById(R.id.imageView);
//        scaleGestureDetector = new ScaleGestureDetector(this,new ScaleListener());
        mAttacher = new PhotoViewAttacher(imageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
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

//    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
//        private Matrix matrix = new Matrix();
//        private Float scaleFactor = 1.f;
//
//        @Override
//        public boolean onScale(ScaleGestureDetector detector) {
//            scaleFactor *= detector.getScaleFactor();
//            // Don't let the object get too small or too large.
//            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));
//            Log.i("ScaleListener", scaleFactor.toString());
//            matrix.setScale(scaleFactor, scaleFactor);
//            imageView.setImageMatrix(matrix);
//            return true;
//        }
//    }
}

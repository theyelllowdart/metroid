package com.example.aaron.metandroid.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.example.aaron.metandroid.R;
import com.example.aaron.metandroid.model.StopModel;
import com.example.aaron.metandroid.view.LargeMapView;

public class MapFragment extends Fragment {
  private OnPinSelectListener pinSelectListener;
  private OnMapSelectListener mapSelectListener;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    float density = getResources().getDisplayMetrics().density;

    View view = inflater.inflate(R.layout.map, container, false);
    final LargeMapView mapView = (LargeMapView) view.findViewById(R.id.largeMap);

//    MyGestureListener gestureListener = new MyGestureListener(mapView);
//    final GestureDetector gdt = new GestureDetector(getActivity(), gestureListener);

//    mapView.setOnTouchListener(new View.OnTouchListener() {
//      @Override
//      public boolean onTouch(View v, MotionEvent event) {
//        return gdt.onTouchEvent(event);
//      }
//    });

//    mapView.setOnTouchListener(new View.OnTouchListener() {
//      private float mLastTouchX;
//      private float mLastTouchY;
//      private float mPosX;
//      private float mPosY;
//
//      @Override
//      public boolean onTouch(View v, MotionEvent ev) {
//        final int action = ev.getAction();
//        switch (action) {
//          case MotionEvent.ACTION_DOWN: {
//            final float x = ev.getX();
//            final float y = ev.getY();
//
//            // Remember where we started
//            mLastTouchX = x;
//            mLastTouchY = y;
//            break;
//          }
//
//          case MotionEvent.ACTION_MOVE: {
//            final float x = ev.getX();
//            final float y = ev.getY();
//
//            // Calculate the distance moved
//            final float dx = x - mLastTouchX;
//            final float dy = y - mLastTouchY;
//
//            // Move the object
//            mPosX += dx;
//            mPosY += dy;
//
//            // Remember this touch position for the next move event
//            mLastTouchX = x;
//            mLastTouchY = y;
//
//            mapView.setTranslationX(mPosX);
//            mapView.setTranslationY(mPosY);
//
//
//            // Invalidate to request a redraw
//            mapView.invalidate();
//            break;
//          }
//        }
//
//        return true;
//      }
//    });

//    mapView.setOnDragListener(new View.OnDragListener() {
//      @Override
//      public boolean onDrag(View v, DragEvent event) {
//        return false;
//      }
//    });

    return view;
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    pinSelectListener = (OnPinSelectListener) activity;
    mapSelectListener = (OnMapSelectListener) activity;
  }

  public interface OnPinSelectListener {
    void onPinSelected(StopModel model, int pinNumber);
  }

  public interface OnMapSelectListener {
    void onMapSelected(float x, float y);
  }

  private static class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
//    private Rect mContentRect;
//    private RectF mCurrentViewport = new RectF(0, 0, 400, 400);
//    private final View view;
//
//    private MyGestureListener(View view) {
//      this.view = view;
//    }
//
//
//    @Override
//    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//      mCurrentViewport.set(view.getClipBounds());
//
//      float viewportOffsetX = distanceX * mCurrentViewport.width() / mContentRect.width();
//      float viewportOffsetY = -distanceY * mCurrentViewport.height() / mContentRect.height();
//
//      // Updates the viewport, refreshes the display.
//      float x = mCurrentViewport.left + viewportOffsetX;
//      float y = mCurrentViewport.bottom + viewportOffsetY;
//
//      float curWidth = mCurrentViewport.width();
//      float curHeight = mCurrentViewport.height();
//      x = Math.max(0, Math.min(x, view.getWidth() - curWidth));
//      y = Math.max(0 + curHeight, Math.min(y, view.getHeight()));
//
//      mCurrentViewport.set(x, y - curHeight, x + curWidth, y);
//      Rect newClipBounds = new Rect();
//      mCurrentViewport.round(newClipBounds);
//      view.setClipBounds(newClipBounds);
//      view.invalidate();
//
//      // Invalidates the View to update the display.
////      ViewCompat.postInvalidateOnAnimation(this);
//
//      return true;
//    }


  }
}

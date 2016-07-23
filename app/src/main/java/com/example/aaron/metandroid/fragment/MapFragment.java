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
}

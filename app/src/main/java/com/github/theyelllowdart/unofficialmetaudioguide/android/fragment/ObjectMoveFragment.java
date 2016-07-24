package com.github.theyelllowdart.unofficialmetaudioguide.android.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.theyelllowdart.unofficialmetaudioguide.android.R;
import com.github.theyelllowdart.unofficialmetaudioguide.android.model.StopModel;

public class ObjectMoveFragment extends Fragment {

  private OnExitMoveModeListener onExitMoveModeStateListener;
  private int pinNumber;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    onExitMoveModeStateListener = (OnExitMoveModeListener) activity;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.move_pin, container, false);
    pinNumber = getArguments().getInt("pinNumber");
    return view;
  }

  static ObjectMoveFragment create(StopModel stopModel, int pinNumber) {
    Bundle bundle = new Bundle();
    bundle.putInt("pinNumber", pinNumber);
    ObjectMoveFragment fragment = new ObjectMoveFragment();
    fragment.setArguments(bundle);
    return fragment;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    onExitMoveModeStateListener.onExitMoveMode();
  }

  public interface OnExitMoveModeListener {
    void onExitMoveMode();
  }
}

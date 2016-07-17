package com.example.aaron.metandroid;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ObjectDetailFragment extends Fragment {

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View a = inflater.inflate(R.layout.detail_frag, container, false);
    ((TextView) a.findViewById(R.id.detailText)).setText(getArguments().getString("textToShow"));
    return a;
  }

  static ObjectDetailFragment create(String textToShow) {
    Bundle bundle = new Bundle();
    bundle.putString("textToShow", textToShow);
    ObjectDetailFragment fragment = new ObjectDetailFragment();
    fragment.setArguments(bundle);
    return fragment;
  }
}

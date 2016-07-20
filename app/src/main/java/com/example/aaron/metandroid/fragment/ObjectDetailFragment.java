package com.example.aaron.metandroid.fragment;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.aaron.metandroid.R;
import com.example.aaron.metandroid.model.MediaModel;
import com.example.aaron.metandroid.model.StopModel;

import java.util.List;

public class ObjectDetailFragment extends ListFragment {

  private OnMediaSelectListener mediaSelectCallback;
  private OnMoveSelectListener moveSelectCallback;
  private StopModel model;
  private int pinNumber;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    mediaSelectCallback = (OnMediaSelectListener) activity;
    moveSelectCallback = (OnMoveSelectListener) activity;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.detail_frag, container, false);

    model = getArguments().getParcelable("stopModel");
    pinNumber =  getArguments().getInt("pinNumber");

    ((TextView) view.findViewById(R.id.detailText)).setText(model.getTitle());
    ((Button) view.findViewById(R.id.movePin)).setOnClickListener(new OnMoveObjectViewClickListener());

    MediaAdapter mediaAdapter = new MediaAdapter(
        this.getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, model.getMedias());
    setListAdapter(mediaAdapter);

    return view;
  }

  static ObjectDetailFragment create(StopModel stopModel, int pinNumber) {
    Bundle bundle = new Bundle();
    bundle.putParcelable("stopModel", stopModel);
    bundle.putInt("pinNumber", pinNumber);
    ObjectDetailFragment fragment = new ObjectDetailFragment();
    fragment.setArguments(bundle);
    return fragment;
  }

  public class MediaAdapter extends ArrayAdapter<MediaModel> {

    public MediaAdapter(Context context, int resource, List<MediaModel> objects) {
      super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      if (convertView == null) {
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.media_row, parent, false);
      }
      ((TextView) convertView.findViewById(R.id.mediaTitle)).setText(getItem(position).getTitle());
      return convertView;
    }

  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    mediaSelectCallback.onMediaSelected(model.getMedias(), position);
  }

  public interface OnMediaSelectListener {
    void onMediaSelected(List<MediaModel> models, int position);
  }

  public interface OnMoveSelectListener {
    void onMediaSelected(StopModel model, int pinNumber);
  }

  private class OnMoveObjectViewClickListener implements View.OnClickListener {
    @Override
    public void onClick(View v) {
      moveSelectCallback.onMediaSelected(model, pinNumber);
    }
  }
}

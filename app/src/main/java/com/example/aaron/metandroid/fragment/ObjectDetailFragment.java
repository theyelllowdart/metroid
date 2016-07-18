package com.example.aaron.metandroid.fragment;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.aaron.metandroid.R;
import com.example.aaron.metandroid.model.MediaModel;
import com.example.aaron.metandroid.model.StopModel;

import java.util.List;

public class ObjectDetailFragment extends ListFragment {

  private OnMediaSelectListener selectCallback;
  private float density;
  private List<MediaModel> mediaModels;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      selectCallback = (OnMediaSelectListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement OnMediaSelectListener");
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View a = inflater.inflate(R.layout.detail_frag, container, false);

    StopModel model = getArguments().getParcelable("stopModel");
    mediaModels = model.getMedias();

    ((TextView) a.findViewById(R.id.detailText)).setText(model.getTitle());

    MediaAdapter mediaAdapter = new MediaAdapter(
        this.getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, model.getMedias());
    setListAdapter(mediaAdapter);

    return a;
  }

  static ObjectDetailFragment create(StopModel stopModel) {
    Bundle bundle = new Bundle();
    bundle.putParcelable("stopModel", stopModel);
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
      ((TextView)convertView.findViewById(R.id.mediaTitle)).setText(getItem(position).getTitle());
      return convertView;
    }

  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    selectCallback.onMediaSelected(mediaModels, position);
  }

  public interface OnMediaSelectListener {
    void onMediaSelected(List<MediaModel> models, int position);
  }
}

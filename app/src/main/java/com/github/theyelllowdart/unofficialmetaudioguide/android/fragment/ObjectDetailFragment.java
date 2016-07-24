package com.github.theyelllowdart.unofficialmetaudioguide.android.fragment;

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

import com.github.theyelllowdart.unofficialmetaudioguide.android.R;
import com.github.theyelllowdart.unofficialmetaudioguide.android.model.MediaModel;
import com.github.theyelllowdart.unofficialmetaudioguide.android.model.StopModel;

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
    float density = getResources().getDisplayMetrics().density;

    View view = inflater.inflate(R.layout.detail_frag, container, false);

    model = getArguments().getParcelable("stopModel");
    pinNumber = getArguments().getInt("pinNumber");

//    ((TextView) view.findViewById(R.id.detailPinNumber)).setText(String.valueOf(pinNumber));
    ((TextView) view.findViewById(R.id.detailText)).setText(model.getTitle());
    ((Button) view.findViewById(R.id.movePin)).setOnClickListener(new OnMoveObjectViewClickListener());


//    ImageView imageView = (ImageView) view.findViewById(R.id.detailImage);
//    int width = 200;
//    if ((model.getWidth() / (double) model.getHeight()) > 1.3) {
//      width = width * 2;
//    }
//    int height = 200;
//    float imageScale = (width * density) / model.getWidth();
//    imageView.getLayoutParams().width = Math.round(400 * density);
//    imageView.getLayoutParams().height = Math.round(height * density);
//
//    Glide.with(getActivity().getBaseContext())
//        .load(model.getImageURL())
//        .diskCacheStrategy(DiskCacheStrategy.RESULT)
////            .centerCrop()
//        .transform(new CenterTopTranformation(getActivity().getBaseContext()))
//        .override(Math.round(imageScale * model.getWidth()), Math.round(height * density))
//        .into(imageView);

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

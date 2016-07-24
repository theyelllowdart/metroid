package com.github.theyelllowdart.unofficialmetaudioguide.android.fragment;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.theyelllowdart.unofficialmetaudioguide.android.R;
import com.github.theyelllowdart.unofficialmetaudioguide.android.model.ArtObjectRow;
import com.github.theyelllowdart.unofficialmetaudioguide.android.model.StopModel;
import com.github.theyelllowdart.unofficialmetaudioguide.android.util.CenterTopTranformation;

import java.util.ArrayList;

public class ObjectListFragment extends ListFragment {
  private OnObjectSelectListener selectCallback;
  private float density;


  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      selectCallback = (OnObjectSelectListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement OnObjectSelectListener");
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    density = getResources().getDisplayMetrics().density;
    View view = inflater.inflate(R.layout.object_list, container, false);

//    ((TextView) a.findViewById(R.id.galleryHeader)).setText(String.valueOf(getArguments().getInt("galleryId")));

//    addStopView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.add_stop, null);
//    addStopView.setVisibility(View.GONE);

//    galleriesView.setFooterDividersEnabled(false);
//    galleriesView.addFooterView(addStopView);

    ListView listView = (ListView) view.findViewById(android.R.id.list);
    View header = inflater.inflate(R.layout.gallery_header, listView, false);
    if (getArguments().containsKey("galleryId")) {
      String galleryId = String.valueOf(getArguments().getInt("galleryId"));
      ((TextView) header.findViewById(R.id.galleryListHeader)).setText("Gallery " + galleryId);
    }
    listView.addHeaderView(header, null, false);
    View footer = inflater.inflate(R.layout.gallery_footer, listView, false);
    listView.addFooterView(footer, null, true);


    GalleryAdapter galleryAdapter = new GalleryAdapter(this.getActivity().getApplicationContext(), android.R.layout.simple_list_item_1);
    setListAdapter(galleryAdapter);

    ArrayList<ArtObjectRow> rows = getArguments().getParcelableArrayList("rows");
    if (rows != null) {
      galleryAdapter.addAll(rows);
    }

    return view;
  }

  static ObjectListFragment create(int galleryId, ArrayList<ArtObjectRow> rows) {
    Bundle bundle = new Bundle();
    bundle.putInt("galleryId", galleryId);
    bundle.putParcelableArrayList("rows", rows);
    ObjectListFragment fragment = new ObjectListFragment();
    fragment.setArguments(bundle);
    return fragment;
  }


  private class GalleryAdapter extends ArrayAdapter<ArtObjectRow> {

    public GalleryAdapter(Context context, int resource) {
      super(context, resource);
    }

    class GalleryHolder2 {
      private final ImageView imageView1;
      private final ImageView imageView2;
      private final RelativeLayout layout1;
      private final RelativeLayout layout2;
      private final TextView pinId1;
      private final TextView pinId2;
      private final TextView title1;
      private final TextView title2;

      public GalleryHolder2(ImageView imageView1, ImageView imageView2, RelativeLayout layout1, RelativeLayout layout2, TextView pinId1, TextView pinId2, TextView title1, TextView title2) {
        this.imageView1 = imageView1;
        this.imageView2 = imageView2;
        this.layout1 = layout1;
        this.layout2 = layout2;
        this.pinId1 = pinId1;
        this.pinId2 = pinId2;
        this.title1 = title1;
        this.title2 = title2;
      }
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
      final ArtObjectRow row = getItem(position);
      if (convertView == null) {
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.gallery2, parent, false);
        GalleryHolder2 holder = new GalleryHolder2(
            (ImageView) convertView.findViewById(R.id.objectImage1),
            (ImageView) convertView.findViewById(R.id.objectImage2),
            (RelativeLayout) convertView.findViewById(R.id.objectImage1Layout),
            (RelativeLayout) convertView.findViewById(R.id.objectImage2Layout),
            (TextView) convertView.findViewById(R.id.objectImage1PinId),
            (TextView) convertView.findViewById(R.id.objectImage2PinId),
            (TextView) convertView.findViewById(R.id.objectImage1Title),
            (TextView) convertView.findViewById(R.id.objectImage2Tilte)
        );
        convertView.setTag(holder);
      }
      final GalleryHolder2 holder = (GalleryHolder2) convertView.getTag();


      int total;
      if (row.getModel2() != null) {
        total = 2;
      } else {
        total = 1;
      }

      if (total == 2) {
        holder.imageView2.setVisibility(View.VISIBLE);
      } else {
        holder.imageView2.setVisibility(View.GONE);
      }


      for (int i = 0; i < total; i++) {
        final StopModel model;
        ImageView imageView;
        RelativeLayout layout;
        TextView title;
        final TextView pinId;

        int width = 200;
        int height = 200;
        if (i == 0) {
          layout = holder.layout1;
          title = holder.title1;
          pinId = holder.pinId1;
          model = row.getModel1();
          imageView = holder.imageView1;
          if ((model.getWidth() / (double) model.getHeight()) > 1.3) {
            width = width * 2;
          }
        } else {
          layout = holder.layout2;
          title = holder.title2;
          pinId = holder.pinId2;
          model = row.getModel2();
          imageView = holder.imageView2;
        }


        float imageScale = (width * density) / model.getWidth();
        if (total == 2) {
          imageView.getLayoutParams().width = Math.round(200 * density);
        } else {
          imageView.getLayoutParams().width = Math.round(400 * density);
        }
        imageView.getLayoutParams().height = Math.round(height * density);


        layout.getLayoutParams().height = imageView.getLayoutParams().height;
        layout.getLayoutParams().width = imageView.getLayoutParams().width;
        title.setText(model.getTitle());
        pinId.setText(String.valueOf(position + 1));

        Glide.with(getContext())
            .load(model.getImageURL())
            .diskCacheStrategy(DiskCacheStrategy.RESULT)
//            .centerCrop()
            .transform(new CenterTopTranformation(getContext()))
            .override(Math.round(imageScale * model.getWidth()), Math.round(height * density))
            .into(imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            selectCallback.onObjectSelected(model, position);
          }
        });
      }
      return convertView;
    }
  }

  public interface OnObjectSelectListener {
    void onObjectSelected(StopModel model, int position);
  }
}
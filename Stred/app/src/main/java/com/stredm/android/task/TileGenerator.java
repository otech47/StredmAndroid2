package com.stredm.android.task;

import android.content.Context;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.stredm.android.EventPagerActivity;
import com.stredm.android.Model;
import com.stredm.android.R;
import com.stredm.android.util.HttpUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by oscarlafarga on 9/18/14.
 */
public class TileGenerator {

    private static final String serverRoot = "http://stredm.com/";
    private Context context;
    private Geocoder geo;
    private HttpUtils apiCall;

    public TileGenerator(Context context) {
        this.context = context;
        geo = new Geocoder(context, Locale.getDefault());
        apiCall = new HttpUtils(context);
    }

    public List<View> modelsToViews(Model[] models, View parentView) {
        List<View> views = new ArrayList<View>();
        LayoutInflater inflater = ((EventPagerActivity) parentView.getContext()).getLayoutInflater();
        for(Model element : models) {
            View eventTile = inflater.inflate(R.layout.event_tile, null);
            String imageUrl = serverRoot + "/images/" + element.landing_image;
//            DownloadImageTask imageTask = new DownloadImageTask()
            String uri = "@drawable/logo.png";
            int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());
            ((ImageView) eventTile.findViewById(R.id.image)).setImageResource(imageResource);
            ((TextView) eventTile.findViewById(R.id.event)).setText(element.event);
            ((TextView) eventTile.findViewById(R.id.date)).setText(element.start_date.substring(0, element.start_date.indexOf("T")));
            ((TextView) eventTile.findViewById(R.id.city)).setText(element.address);
            ((TextView) eventTile.findViewById(R.id.type)).setText("Hear Sets");
            ((ViewGroup) parentView).addView(eventTile);
        }
        return views;
    }

}

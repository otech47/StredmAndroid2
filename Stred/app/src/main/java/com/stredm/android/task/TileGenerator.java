package com.stredm.android.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.stredm.android.EventPagerActivity;
import com.stredm.android.Model;
import com.stredm.android.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oscarlafarga on 9/18/14.
 */
public class TileGenerator {

    private static final String serverRoot = "http://stredm.com/";
    private Context context;
    public ImageCache imageCache;
    public int imagesLoaded = 0;
    public ViewPager eventViewPager;

    public TileGenerator(Context context, ViewPager eventViewPager) {
        this.context = context;
        imageCache = new ImageCache();
        this.eventViewPager = eventViewPager;
    }

    public void setImageBackground(String imageUrl, ImageView imageView) {
        if(imageCache.getBitmapFromMemCache(imageUrl) == null) {
            DownloadImageTask imageTask = new DownloadImageTask(context, imageCache, imageView, this);
            imageTask.execute(imageUrl);
        }
        else {
            Bitmap image = imageCache.getBitmapFromMemCache(imageUrl);
            onDownloadImage(imageView, image);
        }
    }

    public void onDownloadImage(ImageView imageView, Bitmap image) {
        imagesLoaded++;
        imageView.setImageBitmap(image);
        Log.v("setting image bitmap", image.toString());
        if(imagesLoaded >= 3) {
            eventViewPager.setVisibility(View.VISIBLE);
        }
    }


    public View modelsToEventTiles(List<Model> models, View rootView) {
        List<View> views = new ArrayList<View>();
        View parentView = rootView.findViewById(R.id.eventsList);
        LayoutInflater inflater = ((EventPagerActivity) parentView.getContext()).getLayoutInflater();
        for(Model element : models) {
            View eventTile = inflater.inflate(R.layout.event_tile, null);
            String imageUrl = serverRoot + "images/" + element.landing_image;
            ImageView imageView = ((ImageView) eventTile.findViewById(R.id.image));
            Log.v("Getting Image For ", element.event);
            setImageBackground(imageUrl, imageView);
            ((TextView) eventTile.findViewById(R.id.event)).setText(element.event.toUpperCase());
            ((TextView) eventTile.findViewById(R.id.date)).setText(element.start_date.substring(0, element.start_date.indexOf("T")));
            ((TextView) eventTile.findViewById(R.id.city)).setText(element.address);
            ((TextView) eventTile.findViewById(R.id.type)).setText("featured");
            ((ViewGroup) parentView).addView(eventTile);
        }
        return parentView;
    }

    public View modelsToEventSearchTiles(List<Model> models, View rootView) {
        List<View> views = new ArrayList<View>();
        View parentView = rootView.findViewById(R.id.searchResults);
        LayoutInflater inflater = ((EventPagerActivity) parentView.getContext()).getLayoutInflater();
        for(Model element : models) {
            View eventTile = inflater.inflate(R.layout.event_search_tile, null);
            String imageUrl = serverRoot + "images/" + element.landing_image;
            ImageView imageView = ((ImageView) eventTile.findViewById(R.id.resultImage));
            Log.v("Getting Image For ", element.event);
            setImageBackground(imageUrl, imageView);
            ((TextView) eventTile.findViewById(R.id.eventText)).setText(element.event);
            ((TextView) eventTile.findViewById(R.id.dateText)).setText(element.start_date.substring(0, element.start_date.indexOf("T")));
            ((TextView) eventTile.findViewById(R.id.locationText)).setText(element.address);
            ((ViewGroup) parentView).addView(eventTile);
        }
        return parentView;
    }

}

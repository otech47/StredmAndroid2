package com.setmine.android;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.setmine.android.object.Track;
import com.setmine.android.util.TimeUtils;

public class ListTrackAdapter extends ListAdapter<Track> {

	public boolean isPlaylist = false;
	public int playingPosition = -1;

	public ListTrackAdapter(Context c, List<Track> resources) {
		super(c, resources);
	}

	public ListTrackAdapter(Context c, List<Track> resources, int pos) {
		super(c, resources);
		playingPosition = pos;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TrackHolder holder;

		if (convertView == null) {
			if (position == playingPosition) {
				convertView = inflater.inflate(R.layout.list_track_item, null);
			} else {
				convertView = inflater.inflate(R.layout.list_track_item, null);
			}
			holder = new TrackHolder();
			holder.position = position;
			holder.textView = (TextView) convertView
					.findViewById(R.id.list_track_item_text);
			holder.timeStamp = (TextView) convertView
					.findViewById(R.id.list_track_item_timestamp);
			convertView.setTag(holder);
		} else {
			holder = (TrackHolder) convertView.getTag();
		}

		Track t = mResources.get(position);
		String title = t.getTrackName();
		String timestamp = t.getStartTime();
		TimeUtils utils = new TimeUtils();
		int timeMS = utils.timerToMilliSeconds(timestamp);
		timestamp = utils.milliSecondsToTimer(timeMS);

		holder.textView.setText(title);
		holder.timeStamp.setText(timestamp);
		return convertView;
	}
}

package com.setmine.android;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.setmine.android.object.Set;

import java.util.List;

public class ListSetAdapter extends ListAdapter<Set> {

	public boolean isPlaylist = false;
	public int playingPosition = -1;

	public ListSetAdapter(Context c, List<Set> resources) {
		super(c, resources);
	}

	public ListSetAdapter(Context c, List<Set> resources, int pos) {
		super(c, resources);
		playingPosition = pos;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			if (position == playingPosition) {
				convertView = inflater.inflate(R.layout.list_set_item, null);
			} else {
				convertView = inflater.inflate(R.layout.list_set_item, null);
			}
			holder = new ViewHolder();
			holder.position = position;
			holder.textView = (TextView) convertView
					.findViewById(R.id.list_set_item_text);
			holder.imageView = (ImageView) convertView
					.findViewById(R.id.list_set_item_image);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Set s = mResources.get(position);
//		new DownloadImageTask(holder, this).execute(s.getArtistImage());
		String title = s.getArtist() + "\r\n" + s.getEvent();
		holder.textView.setText(title);
		return convertView;
	}
}

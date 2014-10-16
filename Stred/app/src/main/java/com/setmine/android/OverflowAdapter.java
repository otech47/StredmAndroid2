package com.setmine.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class OverflowAdapter extends BaseAdapter implements
		android.widget.ListAdapter {

	private final int[] list = new int[] { R.drawable.ic_action_search,
			R.drawable.ic_action_shuffle };
	private final Context context;

	public OverflowAdapter(Context context) {
		this.context = context;
	}

	@Override
	public int getCount() {
		return list.length;
	}

	@Override
	public Object getItem(int position) {
		return list[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		convertView = inflater.inflate(R.xml.overflow_list_item, null);
		ImageView ib = (ImageView) convertView
				.findViewById(R.id.overflow_list_item_button);
		ib.setImageResource(list[position]);

		return convertView;
	}
}

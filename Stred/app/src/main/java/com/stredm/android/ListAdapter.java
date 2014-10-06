package com.stredm.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListPopupWindow;

import com.stredm.android.object.Set;
import com.stredm.android.task.GetSetsTask;

import java.util.List;

public class ListAdapter<T> extends BaseAdapter implements BitmapCache,
		OnTaskCompleted<Set>, OverflowInterface {
	protected Context context;
	protected List<T> mResources;
	protected LayoutInflater inflater;
	protected GetSetsTask getSetsTask;
	protected int listPosition;

	public ListAdapter(Context c, List<T> resources) {
		context = c;
		mResources = resources;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return mResources.size();
	}

	@Override
	public Object getItem(int position) {
		return mResources.get(position);
	}

	@Override
	public long getItemId(int position) {
		return Integer.valueOf(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return convertView;
	}

	@Override
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	@Override
	public Bitmap getBitmapFromMemCache(String key) {
		return mMemoryCache.get(key);
	}

	public OnClickListener setListPopupWindow(int viewPosition) {
		final ListPopupWindow listPopupWindow;
		listPopupWindow = new ListPopupWindow(context);

		BaseAdapter adapter = new OverflowAdapter(context);
		listPopupWindow.setAdapter(adapter);

		final int pos = viewPosition;
		return new OnClickListener() {

			@Override
			public void onClick(View v) {
				listPopupWindow.setAnchorView(v);
				listPopupWindow.setWidth(150);
				listPopupWindow.setModal(true);
				listPopupWindow
						.setOnItemClickListener(new AdapterView.OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> arg0,
									View v, int position, long id) {
								listPopupWindow.dismiss();
								listPosition = pos;
								if (position == 0) {
									openSetsActivity();
								} else {
									startTask();
								}
							}
						});
				listPopupWindow
						.setPromptPosition(ListPopupWindow.POSITION_PROMPT_BELOW);
				listPopupWindow.show();
			}
		};

	}

	@Override
	public void startTask() {
	}

	@Override
	public void cancelTask() {
		if (getSetsTask != null) {
			getSetsTask.cancel(true);
		}
	}

	@Override
	public void onTaskCompleted(List<Set> list) {
		//((SetMineMainActivity) context).shufflePlayer(list);
	}

	@Override
	public void onTaskFailed() {
	}

	@Override
	public void openSetsActivity() {
	}

	@Override
	public String getResourceFromPosition() {
		return null;
	}

}
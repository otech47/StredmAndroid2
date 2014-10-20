package com.setmine.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.setmine.android.adapter.ListTrackAdapter;
import com.setmine.android.object.Set;

public class TracklistActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tracklist);

		SetMineApplication sa = ((SetMineApplication) getApplicationContext());
		ListView listview = (ListView) findViewById(R.id.listview);
		ListTrackAdapter listAdapter = null;
		int pos = handleIntentPosition(getIntent());
		final Set currentSet;
		if (handleIntentIsShuffle(getIntent())) {
			currentSet = sa.getPlaylistShuffled().get(pos);
		} else {
			currentSet = sa.getPlaylist().get(pos);
		}
		listAdapter = new ListTrackAdapter(this, currentSet.getTracklist(), pos);
		listview.setAdapter(listAdapter);
		listview.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				Intent resultIntent = new Intent();
				resultIntent.putExtra("track", position);
				setResult(RESULT_OK, resultIntent);
				finish();
			}
		});

	}

	private boolean handleIntentIsShuffle(Intent intent) {
		Bundle extras = intent.getExtras();
		boolean isShuffle = false;
		if (extras != null) {
			isShuffle = extras.getBoolean("isShuffle");
		}
		return isShuffle;
	}

	private int handleIntentPosition(Intent intent) {
		Bundle extras = intent.getExtras();
		int position = -1;
		if (extras != null) {
			position = extras.getInt("position");
		}
		return position;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		onBackPressed();
		return true;
	}
}

package com.stredm.android;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.stredm.android.object.Set;

public class DatabaseHandler extends SQLiteOpenHelper {

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "setsManager";

	// Contacts table name
	private static final String TABLE_SETS = "sets";

	private final Context context;
	// Contacts Table Columns names
	private static final String KEY_ID = "id";
	private static final String KEY_SET_ID = "set_id";
	private static final String KEY_ARTIST = "artist";
	private static final String KEY_EVENT = "event";
	private static final String KEY_GENRE = "genre";
	private static final String KEY_IMAGE = "image";
	private static final String KEY_SONG = "song";
	private static final String KEY_IS_RADIOMIX = "is_radiomix";

	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_SETS + " ("
				+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_SET_ID + " TEXT,"
				+ KEY_ARTIST + " TEXT," + KEY_EVENT + " TEXT," + KEY_GENRE
				+ " TEXT," + KEY_IMAGE + " TEXT," + KEY_SONG + " TEXT,"
				+ KEY_IS_RADIOMIX + " INTEGER" + ")";
		db.execSQL(CREATE_CONTACTS_TABLE);
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETS);

		// Create tables again
		onCreate(db);
	}

	public List<Set> getAllSets() {
		List<Set> setList = new ArrayList<Set>();

		SQLiteDatabase db = this.getReadableDatabase();

		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_SETS + " WHERE 1";
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				Set set = new Set();
				set.setId(cursor.getString(1));
				set.setArtist(cursor.getString(2));
				set.setEvent(cursor.getString(3));
				set.setGenre(cursor.getString(4));
				set.setImage(cursor.getString(5));
				set.setSongURL(cursor.getString(6));
				set.setIsRadiomix((cursor.getInt(7) == 1) ? true : false);
				set.setIsDownloaded(true);

				setList.add(set);
			} while (cursor.moveToNext());
		}

		return setList;
	}

	public Set getSet(String pos) {
		Set set = new Set();

		SQLiteDatabase db = this.getReadableDatabase();

		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_SETS + " WHERE "
				+ KEY_ID + " = " + pos;
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				set.setId(cursor.getString(1));
				set.setArtist(cursor.getString(2));
				set.setEvent(cursor.getString(3));
				set.setGenre(cursor.getString(4));
				set.setImage(cursor.getString(5));
				set.setSongURL(cursor.getString(6));
				set.setIsRadiomix((cursor.getInt(7) == 1) ? true : false);
				set.setIsDownloaded(true);
			} while (cursor.moveToNext());
		}

		return set;
	}

	public void initialize() {

		Resources res = context.getResources();
		Bitmap pic = BitmapFactory
				.decodeResource(res, R.drawable.logo);
		int height = (int) res
				.getDimension(android.R.dimen.notification_large_icon_height);
		int width = (int) res
				.getDimension(android.R.dimen.notification_large_icon_width);
		pic = Bitmap.createScaledBitmap(pic, width, height, false);

		String dir = context.getExternalFilesDir(null).toString();
		File dest = new File(dir, "icon.png");
		String iconPath = dir + "/icon.png";

		try {
			FileOutputStream out;
			out = new FileOutputStream(dest);
			pic.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		SQLiteDatabase db = this.getReadableDatabase();

		String CREATE_EMPTY_SLOTS = "INSERT OR IGNORE INTO " + TABLE_SETS + "("
				+ KEY_ID + "," + KEY_SET_ID + "," + KEY_ARTIST + ","
				+ KEY_EVENT + "," + KEY_GENRE + "," + KEY_IMAGE + ","
				+ KEY_SONG + "," + KEY_IS_RADIOMIX + ")"
				+ " VALUES(1, '9999', 'Empty Slot', '1', null, '" + iconPath
				+ "', null, 0)," + " (2, '9999', 'Empty Slot', '2', null, '"
				+ iconPath + "', null, 0)";
		db.execSQL(CREATE_EMPTY_SLOTS);

	}

	public void overwrite() {
		SQLiteDatabase db = this.getReadableDatabase();

		String CREATE_EMPTY_SLOTS = "INSERT OR REPLACE INTO "
				+ TABLE_SETS
				+ "("
				+ KEY_ID
				+ ","
				+ KEY_SET_ID
				+ ","
				+ KEY_ARTIST
				+ ","
				+ KEY_EVENT
				+ ","
				+ KEY_GENRE
				+ ","
				+ KEY_IMAGE
				+ ","
				+ KEY_SONG
				+ ","
				+ KEY_IS_RADIOMIX
				+ ")"
				+ " VALUES(1, '9999', 'Empty Slot', '1', 'music', 'http://stredm.com/favicon.ico', null, 0),"
				+ " (2, '9999', 'Empty Slot', '2', 'music', 'http://stredm.com/favicon.ico', null, 0)";
		db.execSQL(CREATE_EMPTY_SLOTS);

	}

	public int updateSet(Set set, String slot) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_SET_ID, set.getId());
		values.put(KEY_ARTIST, set.getArtist());
		values.put(KEY_EVENT, set.getEvent());
		values.put(KEY_GENRE, set.getGenre());
		values.put(KEY_IMAGE, set.getImage());
		values.put(KEY_SONG, set.getSongURL());
		values.put(KEY_IS_RADIOMIX, set.isRadiomix());

		// updating row
		return db.update(TABLE_SETS, values, KEY_ID + " = ?",
				new String[] { slot });
	}

	public void cleanupFiles() {
		File dir = context.getExternalFilesDir(null);
		List<Set> sets = getAllSets();
		File[] files = dir.listFiles();
		for (File f : files) {
			boolean deleteThisFile = true;
			for (Set s : sets) {
				String filePath = f.getPath();
				if (filePath.equals(s.getSongURL())
						|| filePath.equals(s.getImage())) {
					deleteThisFile = false;
				}
				if (!deleteThisFile) {
					break;
				}
			}
			if (deleteThisFile) {
				f.delete();
			}
		}
	}

}

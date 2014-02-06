package com.example.kekeo_gimun.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.kekeo_gimun.db.RoomContract.RoomEntry;

public class RoomDbHelper extends SQLiteOpenHelper{
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "Room.db";
	private static final String TEXT_TYPE = " TEXT";
	private static final String INTEGER_TYPE = " INTEGER";
	private static final String COMMA_SEP = ",";
	private static final String SQL_CREATE_ENTRIES =
			"CREATE TABLE " + RoomEntry.TABLE_NAME + " (" +
							RoomEntry._ID + " INTEGER PRIMARY KEY," +
							RoomEntry.COLUMN_NAME_PROFILE_ID + INTEGER_TYPE + COMMA_SEP +
							RoomEntry.COLUMN_NAME_ROOM_NAME + TEXT_TYPE + COMMA_SEP +
							RoomEntry.COLUMN_NAME_ROOM_ID + INTEGER_TYPE +
			" )";
	
	private static final String SQL_DELETE_ENTRIES =
			"DROP TABLE IF EXISTS " + RoomEntry.TABLE_NAME;
	
	public RoomDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ENTRIES);
	}
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(SQL_DELETE_ENTRIES);
		onCreate(db);

	}
	
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);

	}
}

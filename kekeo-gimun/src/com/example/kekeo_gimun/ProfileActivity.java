package com.example.kekeo_gimun;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kekeo_gimun.db.RoomContract.RoomEntry;
import com.example.kekeo_gimun.db.RoomDbHelper;

public class ProfileActivity extends Activity{
	
	private String mPhone;
	private int mRoomId;
	private String mName;
	private int mImageId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		mName = getIntent().getExtras().getString("name");
		mPhone = getIntent().getExtras().getString("phone");
		mRoomId = getIntent().getExtras().getInt("room_id");
		mImageId = getIntent().getExtras().getInt("image");
		TextView view = (TextView) findViewById(R.id.profile_name);
		view.setText(mName);
		
		TextView call = (TextView) findViewById(R.id.call);
		call.setText(mPhone);
		
		ImageView close = (ImageView) findViewById(R.id.close);
		close.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		findViewById(R.id.call).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent callIntent = new Intent(Intent.ACTION_CALL);
				callIntent.setData(Uri.parse("tel:03777778888"));
				startActivity(callIntent);
			}
		});
		
		findViewById(R.id.chat).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Insert
				RoomDbHelper mDbHelper = new RoomDbHelper(ProfileActivity.this);
				SQLiteDatabase db = mDbHelper.getWritableDatabase();
				ContentValues values = new ContentValues();
				values.put(RoomEntry.COLUMN_NAME_ROOM_NAME, mName);
				values.put(RoomEntry.COLUMN_NAME_ROOM_ID, mRoomId);
				long newId = db.insert(RoomEntry.TABLE_NAME, null, values);
				Intent intent = new Intent(ProfileActivity.this,
								RoomActivity.class);
				intent.putExtra("name", mName);
				intent.putExtra("room_id", mRoomId);
				startActivity(intent);
			}
		});
		
		ImageView imageView = (ImageView) findViewById(R.id.profile);
		imageView.setImageResource(mImageId);
		imageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ProfileActivity.this,
						ImageActivity.class);
				intent.putExtra("image", mImageId);
				startActivity(intent);
			}
		});
	}

}

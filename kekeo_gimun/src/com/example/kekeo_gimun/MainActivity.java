package com.example.kekeo_gimun;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kekeo_gimun.db.RoomContract.RoomEntry;
import com.example.kekeo_gimun.db.RoomDbHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.games.multiplayer.realtime.RoomEntity;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class MainActivity extends FragmentActivity {

	private ListView mFriend;
	private ListView mChat;
	private View mSearch;
	private View mMore;
	private ImageView mProfile;
	
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    String SENDER_ID = "779290698029";

    private static final String TAG = "MainActivity";

    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context;

    String regid;
    
    private RoomAdapter mRoomCursorAdapter;
    
    public static final String[] people = {
    	"jongbae", "jineui", "gimun", "jungin", "hyungchul", "seunghwan", "jeahyung"
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Select
		mDbHelper = new RoomDbHelper(this);
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		
		Cursor c = db.query(
				RoomEntry.TABLE_NAME,	// The table to query
				null,					// The columns to return
				null,                   // The columns for the WHERE clause
				null,                   // The values for the WHERE clause
				null,                   // don't group the rows
				null,                   // don't filter by row groups
				null                    // The sort order
				);
		
		setContentView(R.layout.activity_main);
		findViewById(R.id.random_button).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int randomIndex = (int) Math.floor(Math.random() * (double) people.length);
				Toast.makeText(MainActivity.this, "¥Á√∑µ«ºÃΩ¿¥œ¥Ÿ! " + people[randomIndex], Toast.LENGTH_LONG).show();
			}
		});
		
		mFriend = (ListView) findViewById(R.id.friend);
		mChat = (ListView) findViewById(R.id.chat);
		
		final String[] ROOM_FROM_COLUMNS ={
				RoomEntry.COLUMN_NAME_ROOM_ID,
				RoomEntry.COLUMN_NAME_ROOM_NAME,
		};
		
		final int[] ROOM_TO_IDS = {
				R.id.text,
				R.id.name
		};
		
		mRoomCursorAdapter = new RoomAdapter(
				this,
				R.layout.room_item,
				c,
				ROOM_FROM_COLUMNS, ROOM_TO_IDS,
				0);
		
		mChat.setAdapter(mRoomCursorAdapter);
		mChat.setItemsCanFocus(false);
		mChat.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				Intent intent = new Intent(MainActivity.this, RoomActivity.class);
				Cursor cursor = mRoomCursorAdapter.getCursor();
				if (cursor.moveToPosition(position)) {
					String name = cursor.getString(cursor.getColumnIndex(
								  RoomEntry.COLUMN_NAME_ROOM_NAME));
					int roomId = cursor.getInt(cursor.getColumnIndex(
								 RoomEntry.COLUMN_NAME_ROOM_NAME));
					intent.putExtra("name", name);
					intent.putExtra("room_id", roomId);
				}
				startActivity(intent);
			}
		});
		mSearch = findViewById(R.id.search);
		mMore = findViewById(R.id.more);
		findViewById(R.id.friend_button).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						mFriend.setVisibility(View.VISIBLE);
						mChat.setVisibility(View.GONE);
						mSearch.setVisibility(View.GONE);
						mMore.setVisibility(View.GONE);
					}
				});

		findViewById(R.id.chat_button).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						mFriend.setVisibility(View.GONE);
						mChat.setVisibility(View.VISIBLE);
						mSearch.setVisibility(View.GONE);
						mMore.setVisibility(View.GONE);
						updateRoomCursor();
					}
				});
		
		findViewById(R.id.search_button).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						mFriend.setVisibility(View.GONE);
						mChat.setVisibility(View.GONE);
						mSearch.setVisibility(View.VISIBLE);
						mMore.setVisibility(View.GONE);
					}
				});

		findViewById(R.id.more_button).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						mFriend.setVisibility(View.GONE);
						mChat.setVisibility(View.GONE);
						mSearch.setVisibility(View.GONE);
						mMore.setVisibility(View.VISIBLE);
					}
				});

		setPerson();

		mProfile = (ImageView) findViewById(R.id.profile);
		mProfile.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dispatchTakePictureIntent();
			}
		});
		
		context = getApplicationContext();
		
		if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            //if (TextUtils.isEmpty(regid)) {
                registerInBackground();
            //}
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
	}
	
	private String getRegistrationId(Context context) {
	    final SharedPreferences prefs = getGCMPreferences(context);
	    String registrationId = prefs.getString(PROPERTY_REG_ID, "");
	    if (registrationId.isEmpty()) {
	        Log.i(TAG, "Registration not found.");
	        return "";
	    }
	    int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
	    int currentVersion = getAppVersion(context);
	    if (registeredVersion != currentVersion) {
	        Log.i(TAG, "App version changed.");
	        return "";
	    }
	    return registrationId;
	}
	
	private SharedPreferences getGCMPreferences(Context context) {
		return getSharedPreferences(MainActivity.class.getSimpleName(),
				Context.MODE_PRIVATE);
	}
	
	private static int getAppVersion(Context context) {
	    try {
	        PackageInfo packageInfo = context.getPackageManager()
	                .getPackageInfo(context.getPackageName(), 0);
	        return packageInfo.versionCode;
	    } catch (NameNotFoundException e) {
	        throw new RuntimeException("Could not get package name: " + e);
	    }
	}
	@Override
	protected void onResume() {
	    super.onResume();
	    checkPlayServices();
	}
	private boolean checkPlayServices() {
	    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	    if (resultCode != ConnectionResult.SUCCESS) {
	        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	            //GooglePlayServicesUtil.getErrorDialog(resultCode, this,
	            //        PLAY_SERVICES_RESOLUTION_REQUEST).show();
	        } else {
	            Log.i(TAG, "This device is not supported.");
	            finish();
	        }
	        return false;
	    }
	    return true;
	}
	
	public static String names[] = new String[] { "Seungwon", "Jongbae", "Wooseok",
			"Jinwoo" };

	private static String texts[] = new String[] { "Hi", "Hello", "Nice to meet you",
			"Me too." };

	private Integer pics[] = new Integer[] { R.drawable.profile,
			R.drawable.profile2, R.drawable.profile3, R.drawable.profile4 };

	private TextView mName;
	private ImageView mPic;
	private TextView mText;
	
	private SimpleCursorAdapter mCursorAdapter;
	
	private ArrayList<Integer> mProfilePics;
	private RoomDbHelper mDbHelper;
	
	private static final String[] PROJECTION =
		{
			Contacts._ID,
			Contacts.LOOKUP_KEY,
			/* Build.VERSION.SDK_INT
				>= Build.VERSION_CODES.HONEYCOMB ?
						Contacts.DISPLAY_NAME_PRIMARY :
						Contacts.DISPLAY_NAME*/
			ContactsContract.CommonDataKinds.Phone._ID,
			ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
			ContactsContract.CommonDataKinds.Phone.NUMBER,
						
		};
	
	private static final String[] FROM_COLUMNS = {
		/* Build.VERSION.SDK_INT
			>= Build.VERSION_CODES.HONEYCOMB ?
					Contacts.DISPLAY_NAME_PRIMARY :
					Contacts.DISPLAY_NAME*/
		ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
		ContactsContract.CommonDataKinds.Phone.NUMBER,
	};
	
	private static final int[] TO_IDS = {
		R.id.name,
		R.id.text
	};

	private void setPerson() {
		mFriend = (ListView) findViewById(R.id.friend);

		mCursorAdapter = new FriendAdapter(this, R.layout.friend_item, null, FROM_COLUMNS, TO_IDS, 0);
		mFriend.setAdapter(mCursorAdapter);
		
		getSupportLoaderManager().initLoader(0, null, new LoaderCallbacks<Cursor>() {

			@Override
			public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
				return new CursorLoader(
						MainActivity.this,
						//Contacts.CONTENT_URI,
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						PROJECTION, null, null, null);
			}

			@Override
			public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
				int count = cursor.getCount();
				mProfilePics = new ArrayList<Integer>();
				for (int i = 0; i < count; ++i){
					int resId = R.drawable.profile;
					double r = Math.random();
					if (r < 0.1) {
						resId = R.drawable.profile;
					} else if (r < 0.2) {
						resId = R.drawable.profile2;
					} else if (r < 0.3) {
						resId = R.drawable.profile3;
					} else if (r < 0.4){
						resId = R.drawable.profile4;
					} else if (r < 0.5) {
						resId = R.drawable.profile5;
					} else if (r < 0.6) {
						resId = R.drawable.profile6;
					} else if (r < 0.7) {
						resId = R.drawable.profile7;
					} else if (r < 0.8) {
						resId = R.drawable.profile8;
					} else if (r < 0.9) {
						resId = R.drawable.profile9;
					} else {
						resId = R.drawable.profile10;
					}
					mProfilePics.add(resId);
				}
				mCursorAdapter.swapCursor(cursor);
			}
			
			@Override
			public void onLoaderReset(Loader<Cursor> arg0) {
				mCursorAdapter.swapCursor(null);
			}
		});
		
		mFriend.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long arg3) {
				Intent intent = new Intent(MainActivity.this,
								ProfileActivity.class);
				Cursor cursor = mCursorAdapter.getCursor();
				if (cursor.moveToPosition(position)) {
					String name = cursor.getString(cursor.getColumnIndex(
									ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
					String phone = cursor.getString(cursor.getColumnIndex(
									ContactsContract.CommonDataKinds.Phone.NUMBER));
					intent.putExtra("name", name);
					intent.putExtra("room_id", position + 1);
					intent.putExtra("phone", phone);
					intent.putExtra("image", mProfilePics.get(position));
				}
				startActivity(intent);
			}
		});
	}
	private class FriendAdapter extends SimpleCursorAdapter {
		
		public FriendAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
		}
		
		@Override
		public void bindView(View view, Context arg1, Cursor cursor) {
			super.bindView(view, arg1, cursor);
			ImageView image = (ImageView) view.findViewById(R.id.pic);
			image.setImageResource(mProfilePics.get(cursor.getPosition()));
		}
	}
	
	private class RoomAdapter extends SimpleCursorAdapter {
		
		public RoomAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
		}
		
		@Override
		public void bindView(View view, Context arg1, Cursor cursor) {
			super.bindView(view, arg1, cursor);
			final int id = cursor.getInt(cursor.getColumnIndex(RoomEntry._ID));
			view.findViewById(R.id.close).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					SQLiteDatabase db = mDbHelper.getWritableDatabase();
					db.delete(RoomEntry.TABLE_NAME, RoomEntry._ID + " = ?", new String[] {
							String.valueOf(id)
					});
					updateRoomCursor();
				}
			});
		}
	}
	
	private void updateRoomCursor() {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		
		Cursor c = db.query(
				RoomEntry.TABLE_NAME,	// The table to query
				null,					// The columns to return
				null,                   // The columns for the WHERE clause
				null,                   // The values for the WHERE clause
				null,                   // don't group the rows
				null,                   // don't filter by row groups
				null                    // The sort order
				);
		mRoomCursorAdapter.swapCursor(c);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	static final int REQUEST_IMAGE_CAPTURE = 1;

	private void dispatchTakePictureIntent() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			File photoFile = null;
			try {
				photoFile = createImageFile();
			} catch (IOException ex) {
				// Error occurred while creating the File
			}
			if (photoFile != null) {
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(photoFile));
				startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
			setPic();
		}
	}
	
	private void setPic() {    
		int targetW = mProfile.getWidth();    
		int targetH = mProfile.getHeight();    
		
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();    
		bmOptions.inJustDecodeBounds = true;    
		
		BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);    
		int photoW = bmOptions.outWidth;    
		int photoH = bmOptions.outHeight;   
		int scaleFactor = Math.min(photoW/targetW, photoH/targetH);    
		
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		// bmOptions.inPurgeable = true;
		Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions); 
		mProfile.setImageBitmap(bitmap);
	}

	String mCurrentPhotoPath;

	private File createImageFile() throws IOException {
		String imageFileName = "profile";
		File storageDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		File image = new File(storageDir.toString(), imageFileName + ".jpg");
		mCurrentPhotoPath = image.getAbsolutePath();
		return image;
	}
	
	private void registerInBackground() {
	    new AsyncTask<Void, Void, String>() {
	        @Override
	        protected String doInBackground(Void... params) {
	            String msg = "";
	            try {
	                if (gcm == null) {
	                    gcm = GoogleCloudMessaging.getInstance(context);
	                }
	                regid = gcm.register(SENDER_ID);
	                msg = "Device registered, registration ID=" + regid;

	                sendRegistrationIdToBackend();

	                storeRegistrationId(context, regid);
	            } catch (IOException ex) {
	                msg = "Error :" + ex.getMessage();
	            }
	            return msg;
	        }

	        @Override
	        protected void onPostExecute(String msg) {
	        	System.out.println("reg id : " + msg);
	            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	        }
	    }.execute(null, null, null);
	}
	
	private void sendRegistrationIdToBackend() {
			// Your implementation here.
		}
	
	private void storeRegistrationId(Context context, String regId) {
	    final SharedPreferences prefs = getGCMPreferences(context);
	    int appVersion = getAppVersion(context);
	    Log.i(TAG, "Saving regId on app version " + appVersion);
	    SharedPreferences.Editor editor = prefs.edit();
	    editor.putString(PROPERTY_REG_ID, regId);
	    editor.putInt(PROPERTY_APP_VERSION, appVersion);
	    editor.commit();
	}
}
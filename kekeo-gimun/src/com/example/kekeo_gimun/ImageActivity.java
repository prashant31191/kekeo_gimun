package com.example.kekeo_gimun;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

public class ImageActivity extends Activity{
	
	private int mImageId;
	private ImageView mImageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image);
		mImageId = getIntent().getExtras().getInt("image");
		mImageView = (ImageView) findViewById(R.id.image);
		mImageView.setImageResource(mImageId);
	}
}

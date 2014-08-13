package com.sohee.app.frameani;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {

//	private static final String TAG = "MainActivity";

	private FrameAnimationEx anim = null;
	private ImageView animView;
	private RelativeLayout mainLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mainLayout = (RelativeLayout) findViewById(R.id.main);
		animView = (ImageView) findViewById(R.id.anim);

		setAnimation();
	}

	private void setAnimation() {
		anim = new FrameAnimationEx(this, animView, R.anim.anim_01_single_tab);

		mainLayout.invalidate();
		if (anim != null)
			anim.start();
	}
}

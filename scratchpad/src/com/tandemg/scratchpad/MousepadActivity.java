package com.tandemg.scratchpad;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.Button;

public class MousepadActivity extends Activity {
	private static final String TAG = "MousepadActivity";

	private int mLastX;
	private int mLastY;

	private int mLastScrollX;
	private int mLastScrollY;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mousepad);
		// keep screen on while app is running
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mLastX = 0;
		mLastY = 0;

		View v = findViewById(R.id.touchPad);

		v.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return touchpadOnTouch(event);
			}
		});

		v = findViewById(R.id.viewTouchpadScrollVertical);

		v.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return touchpadScrollVerticalOnTouch(event);
			}
		});

		v = findViewById(R.id.viewTouchpadScrollHorizontal);

		v.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return touchpadScrollHorizontalOnTouch(event);
			}
		});

		Button button = (Button) findViewById(R.id.buttonTouchpadLMB);

		button.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return buttonMouseOnTouch(R.id.buttonTouchpadLMB, event,
						(byte) 0);
			}
		});

		button = (Button) findViewById(R.id.buttonTouchpadRMB);

		button.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return buttonMouseOnTouch(R.id.buttonTouchpadRMB, event,
						(byte) 1);
			}
		});

		button = (Button) findViewById(R.id.buttonTouchpadMMB);

		button.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return buttonMouseOnTouch(R.id.buttonTouchpadMMB, event,
						(byte) 2);
			}
		});

		Log.v(TAG, "activity created");
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Log.d(TAG, "orientation changed to LANDSCAPE ("
					+ newConfig.orientation + ")");
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			Log.d(TAG, "orientation changed to PORTRAIT ("
					+ newConfig.orientation + ")");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.mousepad, menu);
		return true;
	}

	private boolean touchpadOnTouch(final MotionEvent event) {
		int Xvalue;
		int Yvalue;

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			mLastX = Math.round(event.getX());
			mLastY = Math.round(event.getY());
			break;

		case MotionEvent.ACTION_MOVE:
			Xvalue = Math.round(event.getX()) - mLastX;
			Yvalue = Math.round(event.getY()) - mLastY;
			Xvalue = Math.round((Xvalue * 3) / 2);
			Yvalue = Math.round((Yvalue * 3) / 2);

			TCPClient.getInstance().notifyMouseMove(Xvalue, Yvalue);
			mLastX = Math.round(event.getX());
			mLastY = Math.round(event.getY());
			break;
		}
		return true;
	}

	private boolean buttonMouseOnTouch(int id, MotionEvent event, byte button) {
		View v = findViewById(id);
		int ev = event.getAction() & MotionEvent.ACTION_MASK;
		if (ev == MotionEvent.ACTION_DOWN) {
			v.setPressed(true);
			TCPClient.getInstance().notifyMouseButtonPress(button);
		} else if (ev == MotionEvent.ACTION_UP) {
			TCPClient.getInstance().notifyMouseButtonRelease(button);
			v.setPressed(false);
		}
		return true;
	}

	private boolean touchpadScrollVerticalOnTouch(final MotionEvent event) {
		int Yvalue;

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastScrollY = Math.round(event.getY());
			break;

		case MotionEvent.ACTION_MOVE:
			Yvalue = Math.round(event.getY()) - mLastScrollY;
			Yvalue = -Math.round(Yvalue / 3);

			TCPClient.getInstance().notifyMouseVScroll(Yvalue);
			mLastScrollY = Math.round(event.getY());
			break;
		}
		return true;
	}

	private boolean touchpadScrollHorizontalOnTouch(final MotionEvent event) {
		int Xvalue;

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastScrollX = Math.round(event.getX());
			break;

		case MotionEvent.ACTION_MOVE:
			Xvalue = Math.round(event.getX()) - mLastScrollX;
			Xvalue = Math.round(Xvalue / 3);

			TCPClient.getInstance().notifyMouseHScroll(Xvalue);
			mLastScrollX = Math.round(event.getX());
			break;
		}
		return true;
	}
}

package com.tandemg.scratchpad;

import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.AbstractWheelAdapter;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;

public class MousepadActivity extends Fragment {
	private static final String TAG = "MousepadActivity";

	private int mLastX;
	private int mLastY;

	private int mLastScrollX;
	private int mLastScrollY;
	private int sendYScroll;
	private int sendXScroll;

	private ViewGroup rootView;

	private long timestamp;

	// timeout in milliseconds to emulate left button press
	private static final int TOUCH_LEFT_BUTTON_EMU_TIMEOUT = 100;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		int scrollingWheelScaling = 1;
		// Inflate the layout containing a title and body text.
		rootView = (ViewGroup) inflater.inflate(R.layout.activity_mousepad,
				container, false);

		sendYScroll = 0;
		sendXScroll = 0;
		mLastX = 0;
		mLastY = 0;

		View v = rootView.findViewById(R.id.touchPad);
		v.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return touchpadOnTouch(event);
			}
		});

		v = rootView.findViewById(R.id.viewTouchpadScrollVertical);
		initWheel(R.id.viewTouchpadScrollVertical, scrollingWheelScaling);
		v.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				touchpadScrollVerticalOnTouch(event);
				return false;
			}
		});

		v = rootView.findViewById(R.id.viewTouchpadScrollHorizontal);
		scrollingWheelScaling = 10;
		v.setScaleY(scrollingWheelScaling);
		initWheel(R.id.viewTouchpadScrollHorizontal, scrollingWheelScaling);
		v.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				touchpadScrollHorizontalOnTouch(event);
				return false;
			}
		});

		Button button = (Button) rootView.findViewById(R.id.buttonTouchpadLMB);
		button.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return buttonMouseOnTouch(R.id.buttonTouchpadLMB, event,
						(byte) 0);
			}
		});

		button = (Button) rootView.findViewById(R.id.buttonTouchpadRMB);
		button.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return buttonMouseOnTouch(R.id.buttonTouchpadRMB, event,
						(byte) 1);
			}
		});

		button = (Button) rootView.findViewById(R.id.buttonTouchpadMMB);
		button.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return buttonMouseOnTouch(R.id.buttonTouchpadMMB, event,
						(byte) 2);
			}
		});

		return rootView;
	}

	private void initWheel(int id, int scale) {
		WheelView wheel = (WheelView) rootView.findViewById(id);
		wheel.setViewAdapter(new MyAbstractWheelAdapter(this.getActivity(),
				scale));
		wheel.setCurrentItem(16);
		wheel.enableOnReleaseMoveTo(true);
		wheel.setOnReleaseMoveTo(16);
		wheel.setCyclic(false);
		wheel.setInterpolator(new AnticipateOvershootInterpolator());
	}

	class MyAbstractWheelAdapter extends AbstractWheelAdapter {
		int scale = 1;
		final int IMAGE_WIDTH = 20;
		final int IMAGE_HEIGHT = 20;
		Context context;

		MyAbstractWheelAdapter(Context context, int scale) {
			super();
			this.scale = scale;
			this.context = context;
		}

		@Override
		public int getItemsCount() {
			return 31;
		}

		@Override
		public View getItem(int index, View cachedView, ViewGroup parent) {
			ImageView img;
			if (cachedView != null) {
				img = (ImageView) cachedView;
			} else {
				img = new ImageView(context);
			}
			if (index == (getItemsCount() / 2) + 1) {
				img.setImageBitmap(loadImage(R.drawable.button9));
			} else {
				img.setImageBitmap(loadImage(R.drawable.empty));
			}

			return img;
		}

		private Bitmap loadImage(int id) {
			Bitmap bitmap = BitmapFactory.decodeResource(
					rootView.getResources(), id);
			Bitmap scaled = Bitmap.createScaledBitmap(bitmap, IMAGE_WIDTH,
					IMAGE_HEIGHT / scale, true);
			bitmap.recycle();
			return scaled;
		}

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
			Log.v(TAG, "orientation changed to LANDSCAPE ("
					+ newConfig.orientation + ")");
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			Log.v(TAG, "orientation changed to PORTRAIT ("
					+ newConfig.orientation + ")");
		}
	}

	private boolean touchpadOnTouch(final MotionEvent event) {
		int Xvalue;
		int Yvalue;

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			mLastX = Math.round(event.getX());
			mLastY = Math.round(event.getY());
			timestamp = System.currentTimeMillis();
			break;

		case MotionEvent.ACTION_MOVE:
			Xvalue = Math.round(event.getX()) - mLastX;
			Yvalue = Math.round(event.getY()) - mLastY;
			Xvalue = Math.round((Xvalue * 3) / 2);
			Yvalue = Math.round((Yvalue * 3) / 2);

			((ScratchpadActivity) getActivity()).getTcpService()
					.notifyMouseMove(Xvalue, Yvalue);
			mLastX = Math.round(event.getX());
			mLastY = Math.round(event.getY());
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if (System.currentTimeMillis() - timestamp < TOUCH_LEFT_BUTTON_EMU_TIMEOUT) {
				((ScratchpadActivity) getActivity()).getTcpService()
						.notifyMouseButtonPressLEFT();
				((ScratchpadActivity) getActivity()).getTcpService()
						.notifyMouseButtonReleaseLEFT();
			}
			break;
		}
		return true;
	}

	private boolean buttonMouseOnTouch(int id, MotionEvent event, byte button) {
		View v = getView().findViewById(id);
		int ev = event.getAction() & MotionEvent.ACTION_MASK;
		if (ev == MotionEvent.ACTION_DOWN) {
			v.setPressed(true);
			((ScratchpadActivity) getActivity()).getTcpService()
					.notifyMouseButtonPress(button);
		} else if (ev == MotionEvent.ACTION_UP) {
			((ScratchpadActivity) getActivity()).getTcpService()
					.notifyMouseButtonRelease(button);
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
			sendYScroll = ++sendYScroll % 4;
			if (0 == sendYScroll) {
				((ScratchpadActivity) getActivity()).getTcpService()
						.notifyMouseVScroll(Yvalue);
			}
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

			sendXScroll = ++sendXScroll % 4;
			if (0 == sendXScroll) {
				((ScratchpadActivity) getActivity()).getTcpService()
						.notifyMouseHScroll(Xvalue);
			}
			mLastScrollX = Math.round(event.getX());
			break;
		}
		return true;
	}
}

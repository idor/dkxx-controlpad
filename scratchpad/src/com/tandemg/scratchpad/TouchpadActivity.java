package com.tandemg.scratchpad;

import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class TouchpadActivity extends Fragment implements IScartchpadClient {

	private static final int INPUT_TYPE_TOUCH = 0;
	private static final int INPUT_TYPE_MOUSE = 1;
	private ScratchpadGLSurfaceView mGLView = null;
	private static final String TAG = "TouchpadActivity";
	private int mHeight, mWidth;
	private Boolean mDimensionsSentToClient = false;
	private int mInputType = INPUT_TYPE_TOUCH;

	private ViewGroup rootView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout containing a title and body text.
		rootView = (ViewGroup) inflater.inflate(R.layout.activity_touchpad,
				container, false);

		// create scratchpad dinamiccaly
		mGLView = new ScratchpadGLSurfaceView(getActivity());
		mGLView.setClient(this);

		// attach the scratch pad to its view object
		LinearLayout ln = (LinearLayout) rootView.findViewById(R.id.surface);
		ln.addView(mGLView);

		return rootView;
	}

	@Override
	public void onPause() {
		super.onPause();
		// The following call pauses the rendering thread.
		// If your OpenGL application is memory intensive,
		// you should consider de-allocating objects that
		// consume significant memory here.
		mGLView.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		// The following call resumes a paused rendering thread.
		// If you de-allocated) graphic objects for onPause()
		// this is a good place to re-allocate them.
		mGLView.onResume();
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
		mDimensionsSentToClient = false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void onClick_InputType(View v) {
		Log.v(TAG, "input type toggle");
		switch (mInputType) {
		case INPUT_TYPE_TOUCH:
			break;
		case INPUT_TYPE_MOUSE:
			break;

		default:
			Log.e(TAG, "input tyep unknown");
		}
	}

	@Override
	public String clientName() {
		return TAG;
	}

	public void handleEvent_KeyUp() {
	}

	public void handleEvent_KeyDown() {
	}

	public void handleEvent_Motion(MotionEvent event) {
		if (mDimensionsSentToClient != true) {
			LinearLayout ln = (LinearLayout) rootView
					.findViewById(R.id.surface);
			mHeight = ln.getMeasuredHeight();
			mWidth = ln.getMeasuredWidth();
			((ScratchpadActivity) getActivity()).getTcpService()
					.notifyDimensions(mHeight, mWidth);
			mDimensionsSentToClient = true;
			Log.v(TAG,
					"dimensions received from view, height: "
							+ String.valueOf(mHeight) + ", width: "
							+ String.valueOf(mWidth));
		}
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			((ScratchpadActivity) getActivity()).getTcpService().notifyDown(
					event.getX(), event.getY(), event.getPressure());
			break;
		case MotionEvent.ACTION_MOVE:
			((ScratchpadActivity) getActivity()).getTcpService().notifyMove(
					event.getX(), event.getY(), event.getPressure());
			break;
		case MotionEvent.ACTION_UP:
			((ScratchpadActivity) getActivity()).getTcpService().notifyUp(
					event.getX(), event.getY(), event.getPressure());
			break;
		case MotionEvent.ACTION_CANCEL:
			((ScratchpadActivity) getActivity()).getTcpService().notifyCancel(
					event.getX(), event.getY(), event.getPressure());
			break;
		default:
			Log.e(TAG, "event type not supported");
		}
	}
}

package com.tandemg.scratchpad;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class ScratchpadGLSurfaceView extends GLSurfaceView {

	private final ScratchpadGLRenderer mRenderer;
	private IScartchpadClient mClient = null;

	// private static final String TAG = "ScratchpadGLSurfaceView";

	public ScratchpadGLSurfaceView(Context context) {
		super(context);

		// Create an OpenGL ES 2.0 context.
		setEGLContextClientVersion(2);
		// Set the Renderer for drawing on the GLSurfaceView
		mRenderer = new ScratchpadGLRenderer();
		setRenderer(mRenderer);
		// Render the view only when there is a change in the drawing data
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}

	public void setClient(IScartchpadClient client) {
		mClient = client;
	}

	@Override
	public boolean onKeyDown(int n, KeyEvent e) {
		// Log.v(TAG, new Exception().getStackTrace()[0].getMethodName() +
		// ", n= "
		// + n + ", event = " + e);
		mClient.handleEvent_KeyDown();
		return true;
	}

	@Override
	public boolean onKeyUp(int n, KeyEvent e) {
		// Log.v(TAG, new Exception().getStackTrace()[0].getMethodName() +
		// ", n= "
		// + n + ", event = " + e);
		mClient.handleEvent_KeyUp();
		return true;
	}

	@Override
	public boolean onTrackballEvent(MotionEvent e) {
		// Log.v(TAG, new Exception().getStackTrace()[0].getMethodName()
		// + ", event = " + e);
		mClient.handleEvent_Motion(e);
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		// MotionEvent reports input details from the touch screen
		// and other input controls. In this case, you are only
		// interested in events where the touch position changed.

		// Log.v(TAG, new Exception().getStackTrace()[0].getMethodName()
		// + ", event = " + e);

		mClient.handleEvent_Motion(e);

		return true;
	}
}

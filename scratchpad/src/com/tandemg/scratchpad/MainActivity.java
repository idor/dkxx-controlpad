package com.tandemg.scratchpad;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class MainActivity extends Activity implements IScartchpadClient {

	private static final int INPUT_TYPE_TOUCH = 0;
	private static final int INPUT_TYPE_MOUSE = 1;
	private ScratchpadGLSurfaceView mGLView = null;
	private TCPClient mTcpClient = null;
	private Thread mClientThread = null;
	private static final String TAG = "MainActivity";
	private int mHeight, mWidth;
	private Boolean mDimensionsSentToClient = false;
	private int mInputType = INPUT_TYPE_TOUCH;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// keep screen on while app is running
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// create scratchpad dinamiccaly
		mGLView = new ScratchpadGLSurfaceView(this);
		mGLView.setClient(this);

		// attach the scratch pad to its view object
		LinearLayout ln = (LinearLayout) this.findViewById(R.id.surface);
		ln.addView(mGLView);

		// connect to the server
		mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
			@Override
			// here the messageReceived method is implemented
			public void messageReceived(String message) {
				// this method calls the onProgressUpdate
				Log.d(TAG, "message received from TCPClient: " + message);
			}
		});
		mClientThread = new Thread(mTcpClient);
		mClientThread.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		// The following call pauses the rendering thread.
		// If your OpenGL application is memory intensive,
		// you should consider de-allocating objects that
		// consume significant memory here.
		mGLView.onPause();
	}

	@Override
	protected void onResume() {
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
	protected void onDestroy() {
		try {
			mTcpClient.stopClient();
		} catch (Exception e) {
			Log.e(TAG, "Error when destroying activity: " + e.toString(), e);
		} finally {
			super.onDestroy();
		}
	}

	public void onClick_Back(View v) {
		try {
			if (mTcpClient == null) {
				throw new Exception("client does not exist");
			}
			if (mClientThread == null) {
				throw new Exception("connection thread was not created yet");
			}
			if (mClientThread.isAlive() != true) {
				throw new Exception("connection thread not running");
			}
			if (!mTcpClient.connected()) {
				throw new Exception("client is not connected");
			}
			mTcpClient.notifyBack();
		} catch (Exception e) {
			Log.e(TAG, "Error: " + e.toString(), e);
			e.printStackTrace();
		} finally {
		}
	}

	public void onClick_InputType(View v) {
		Log.v(TAG, "input type toggle");
		try {
			switch (mInputType) {
			case INPUT_TYPE_TOUCH:
				break;
			case INPUT_TYPE_MOUSE:
				break;

			default:
				throw new Exception("input tyep unknown");
			}
		} catch (Exception e) {
			Log.e(TAG, "Error: " + e.toString(), e);
			e.printStackTrace();
		} finally {
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
		try {
			if (mTcpClient == null) {
				throw new Exception("client does not exist");
			}
			if (mClientThread == null) {
				throw new Exception("connection thread was not created yet");
			}
			if (mClientThread.isAlive() != true) {
				throw new Exception("connection thread not running");
			}
			if (!mTcpClient.connected()) {
				throw new Exception("client is not connected");
			}
			if (mDimensionsSentToClient != true) {
				LinearLayout ln = (LinearLayout) this
						.findViewById(R.id.surface);
				mHeight = ln.getMeasuredHeight();
				mWidth = ln.getMeasuredWidth();
				mTcpClient.notifyDimensions(mHeight, mWidth);
				mDimensionsSentToClient = true;
				Log.v(TAG,
						"dimensions received from view, height: "
								+ String.valueOf(mHeight) + ", width: "
								+ String.valueOf(mWidth));
			}
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mTcpClient.notifyDown(event.getX(), event.getY(),
						event.getPressure());
				break;
			case MotionEvent.ACTION_MOVE:
				mTcpClient.notifyMove(event.getX(), event.getY(),
						event.getPressure());
				break;
			case MotionEvent.ACTION_UP:
				mTcpClient.notifyUp(event.getX(), event.getY(),
						event.getPressure());
				break;
			default:
				throw new Exception("event type not supported");
			}
		} catch (Exception e) {
			Log.e(TAG, "Error: " + e.toString(), e);
		} finally {
		}
	}
}

package com.tandemg.scratchpad;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.WindowManager;

public class MainActivity extends Activity implements IScartchpadClient {
	
	private GLSurfaceView mGLView = null;
	private TCPClient mTcpClient = null;
	private Thread mClientThread = null;
	private static final String TAG = "MainActivity";
	private int mHeight, mWidth;
	private Boolean one_time_dimensions_sent = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		mGLView = new ScratchpadGLSurfaceView(this, this);
        setContentView(mGLView);
        
		// connect to the server
        
        /*
        connection = new ConnectTask();
        connection.execute("");*/
        mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
			@Override
			//here the messageReceived method is implemented
			public void messageReceived(String message) {
			    //this method calls the onProgressUpdate
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
        // If you de-allocated graphic objects for onPause()
        // this is a good place to re-allocate them.
        mGLView.onResume();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Log.d(TAG, "orientation changed to LANDSCAPE (" + newConfig.orientation + ")");
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
			Log.d(TAG, "orientation changed to PORTRAIT (" + newConfig.orientation + ")");
		}
        one_time_dimensions_sent = false;
    }
    
	@Override
    protected void onDestroy() {
    	try {
    		mTcpClient.stopClient();
    	} catch(Exception e ) {
    		Log.e(TAG, "Error when destroying activity: " + e.toString(), e);
    	} finally {
    		super.onDestroy();
    	}
    }
    
    public class ConnectTask extends AsyncTask<String,String,TCPClient> { 
		@Override
		protected TCPClient doInBackground(String... message) {
			//we create a TCPClient object and
			mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
				@Override
				//here the messageReceived method is implemented
				public void messageReceived(String message) {
				    //this method calls the onProgressUpdate
				    Log.d(TAG, "message received from TCPClient: " + message);
				    publishProgress(message);
				}
            });
			mTcpClient.run();
			return null;
		}
 
		@Override
		protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
		}
	}

    @Override
    public String clientName() {
    	return TAG;
    }
    
	public void	handleEvent_KeyUp(){
		
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
			if (mClientThread.isAlive() != true)
			{
				throw new Exception("connection thread not running");
			}
			if(!mTcpClient.connected())
			{
				throw new Exception("client is not connected");
			}
			if(one_time_dimensions_sent != true)
			{
				Rect rect = new Rect();
				mGLView.getWindowVisibleDisplayFrame(rect);
				mHeight = rect.height();
				mWidth = rect.width();
				mTcpClient.notifyDimensions(mHeight, mWidth);
				one_time_dimensions_sent = true;
				Log.v(TAG, "dimensions received from view, height: " + String.valueOf(mHeight) + ", width: " + String.valueOf(mWidth));
			}
			switch(event.getAction())
			{
			case MotionEvent.ACTION_DOWN:
				mTcpClient.notifyDown(event.getX(), event.getY(), event.getPressure());
				break;
			case MotionEvent.ACTION_MOVE:
				mTcpClient.notifyMove(event.getX(), event.getY(), event.getPressure());
				break;
			case MotionEvent.ACTION_UP:
				mTcpClient.notifyUp(event.getX(), event.getY(), event.getPressure());
				break;
			default:
				throw new Exception("event type not supported");
			}
		}
		catch (Exception e) { 
			Log.e(TAG, "Error: " + e.toString(), e);
		} finally {
			
		}
	}
}



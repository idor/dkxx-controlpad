package com.tandemg.scratchpad;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;

public class MainActivity extends Activity implements IScartchpadClient {
	
	private GLSurfaceView mGLView = null;
	private TCPClient mTcpClient = null;
	private ConnectTask connection = null;
	private static final String TAG = "MainActivity";
	private int mHeight, mWidth;
	private Boolean one_time_dimensions_sent = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		mGLView = new ScratchpadGLSurfaceView(this, this);
        setContentView(mGLView);
        
        mHeight = mGLView.getHeight();
        mWidth = mGLView.getWidth();
        Log.v(TAG, "dimensions received from view, height: " + String.valueOf(mHeight) + ", width: " + String.valueOf(mWidth));
        
		// connect to the server
        connection = new ConnectTask();
        connection.execute("");
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
    protected void onDestroy() {
    	
    	mTcpClient.stopClient();
    	super.onDestroy();
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
    public String myname() {
    	return TAG;
    }
    
	public void	handleEvent_KeyUp(){
		
	}
	
	public void handleEvent_KeyDown() {
		
	}
	
	public void handleEvent_Trackball() {
		
	}
	
	public void handleEvent_Touch(MotionEvent event) {
//		String msg = new String();		
		try {	
			if (mTcpClient == null) {
				throw new Exception("client does not exist");
			}
			switch(event.getAction())
			{
			case MotionEvent.ACTION_DOWN:
				if(one_time_dimensions_sent != true)
				{
					mTcpClient.notifyDimensions(mHeight, mWidth);
					one_time_dimensions_sent = true;
				}
				mTcpClient.notifyDown(event.getX(), event.getY(), event.getPressure());
				/*msg = "D " + 
						String.valueOf(event.getX()) + " " +
						String.valueOf(event.getY()) + " " +
						String.valueOf(event.getPressure());*/
				break;
			case MotionEvent.ACTION_MOVE:
				mTcpClient.notifyDown(event.getX(), event.getY(), event.getPressure());
				/*msg = "M " + 
						String.valueOf(event.getX()) + " " +
						String.valueOf(event.getY()) + " " +
						String.valueOf(event.getPressure());*/
				break;
			case MotionEvent.ACTION_UP:
				mTcpClient.notifyDown(event.getX(), event.getY(), event.getPressure());
				/*msg = "U " + 
						String.valueOf(event.getX()) + " " +
						String.valueOf(event.getY()) + " " +
						String.valueOf(event.getPressure());*/
				break;
			default:
				throw new Exception("event type not supported");
			}
/*			//sends the message to the server
			if (mTcpClient != null) {
				Log.d(TAG, "message to send: " + msg);
				mTcpClient.sendMessage(msg);
			} else {
				throw new Exception("client does not exist");
			}*/
		}
		catch (Exception e) { 
			Log.e(TAG, "Error: " + e.toString(), e);
		} finally {
			
		}
	}
}



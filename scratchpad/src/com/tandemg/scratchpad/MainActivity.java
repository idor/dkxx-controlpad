package com.tandemg.scratchpad;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity implements IScartchpadClient {
	
	private GLSurfaceView mGLView;
	private ScratchpadSocket mSocket;
	private static final String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mGLView = new ScratchpadGLSurfaceView(this, this);
        setContentView(mGLView);
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
    public String myname() {
    	return "MainActivity";
    }
    
	public void	handleEvent_KeyUp(){
		
	}
	
	public void handleEvent_KeyDown() {
		
	}
	
	public void handleEvent_Trackball() {
		
	}
	
	public void handleEvent_Touch(float x, float y) {
		
	}
}



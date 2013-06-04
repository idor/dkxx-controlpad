package com.tandemg.scratchpad;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class ScratchpadGLSurfaceView extends GLSurfaceView {

    private final ScratchpadGLRenderer mRenderer;
    private IScartchpadClient mClient;
    private static final String TAG = "ScratchpadGLSurfaceView";

    public ScratchpadGLSurfaceView(Context context, IScartchpadClient client) {
        super(context);
        
        mClient = client;

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView

		mRenderer = new ScratchpadGLRenderer();
        setRenderer(mRenderer);


        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        
        Log.v(TAG, mClient.myname());
 
    }

    @Override
    public boolean onKeyDown(int n, KeyEvent e) {
    	Log.v(TAG, new Exception().getStackTrace()[0].getMethodName() + ", n= " + n + ", event = " + e );
    	mClient.handleEvent_KeyDown();
    	return true;
    }
    
    @Override
    public boolean onKeyUp(int n, KeyEvent e) {
    	Log.v(TAG, new Exception().getStackTrace()[0].getMethodName() + ", n= " + n + ", event = " + e );
    	mClient.handleEvent_KeyUp();
    	return true;
    }
    
    @Override
    public boolean onTrackballEvent(MotionEvent e) {
    	Log.v(TAG, new Exception().getStackTrace()[0].getMethodName() + ", event = " + e );
    	mClient.handleEvent_Trackball();
    	return true;
    }
    	
	@Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.
		
//		Log.v(TAG, new Exception().getStackTrace()[0].getMethodName() + ", event = " + e );
//		switch( e.getAction())
//		{
//		case MotionEvent.ACTION_DOWN:
//			Log.d(TAG, "D " + e.getX() + " " + e.getY() + " " + e.getPressure());
//			break;
//		case MotionEvent.ACTION_MOVE:
//			Log.d(TAG, "M " + e.getX() + " " + e.getY() + " " + e.getPressure());
//			break;
//		case MotionEvent.ACTION_UP:
//			Log.d(TAG, "U " + e.getX() + " " + e.getY() + " " + e.getPressure());
//			break;
//		}
		
		mClient.handleEvent_Touch(e);
		
		return true;
	}
}








/*

V/ScratchpadGLSurfaceView(31501): onTouchEvent, event = MotionEvent { action=ACTION_DOWN, id[0]=0, x[0]=642.5, y[0]=887.0, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=446343164, downTime=446343164, deviceId=6, source=0x1002 }
/dev/input/event2: EV_ABS       ABS_MT_TRACKING_ID   000057ff            
/dev/input/event2: EV_ABS       ABS_MT_POSITION_X    00000505            
/dev/input/event2: EV_ABS       ABS_MT_POSITION_Y    00000812            
/dev/input/event2: EV_ABS       ABS_MT_PRESSURE      0000003b            
/dev/input/event2: EV_ABS       ABS_MT_TOUCH_MAJOR   00000004            
/dev/input/event2: EV_SYN       SYN_REPORT           00000000


V/ScratchpadGLSurfaceView(31501): onTouchEvent, event = MotionEvent { action=ACTION_MOVE, id[0]=0, x[0]=641.19226, y[0]=886.34607, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=1, eventTime=446343176, downTime=446343164, deviceId=6, source=0x1002 }
/dev/input/event2: EV_ABS       ABS_MT_POSITION_X    00000503            
/dev/input/event2: EV_ABS       ABS_MT_POSITION_Y    00000811            
/dev/input/event2: EV_SYN       SYN_REPORT           00000000


V/ScratchpadGLSurfaceView(31501): onTouchEvent, event = MotionEvent { action=ACTION_UP, id[0]=0, x[0]=641.19226, y[0]=886.34607, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=446343213, downTime=446343164, deviceId=6, source=0x1002 }             
/dev/input/event2: EV_ABS       ABS_MT_TRACKING_ID   ffffffff            
/dev/input/event2: EV_SYN       SYN_REPORT           00000000





 */

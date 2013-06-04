package com.tandemg.scratchpad;

import android.view.MotionEvent;

public interface IScartchpadClient {
	
	public String myname();
	
	public void		handleEvent_KeyUp();
	public void 	handleEvent_KeyDown();
	public void 	handleEvent_Trackball();
	public void		handleEvent_Touch(MotionEvent e);
}

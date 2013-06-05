package com.tandemg.scratchpad;

import android.view.MotionEvent;

public interface IScartchpadClient {
	
	public String clientName();	
	public void	handleEvent_KeyUp();
	public void handleEvent_KeyDown();
	public void	handleEvent_Motion(MotionEvent e);
}

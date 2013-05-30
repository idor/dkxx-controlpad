package com.tandemg.scratchpad;

public interface IScartchpadClient {
	/*
	enum MessageType{
		MSG_TYPE_EVENT_KEY_UP,
		MSG_TYPE_EVENT_KEY_DOWN,
		MSG_TYPE_EVENT_TRACKBALL,
		MSG_TYPE_EVENT_TOUCH,
		
		MSG_TYPE_NUM_OF_EVENTS,
	}
	
	public class Message {
		public MessageType	type;
		public float 		x;
		public float 		y;
	}*/
	
	public String myname();
	
	public void		handleEvent_KeyUp();
	public void 	handleEvent_KeyDown();
	public void 	handleEvent_Trackball();
	public void		handleEvent_Touch(float x, float y);
	
	/*
	public boolean 	connected();
	public int 		connect();
	public int 		disconnect();
	public int 		findServer();
	public int 		sendMessage(Message msg);
	*/
}

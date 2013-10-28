package com.tandemg.scratchpad.communications;

public class MessageTypes {

	// Android messages
	public static final String MSG_BACK = "B";
	public static final String MSG_HOME = "H";

	// touch messages
	public static final String MSG_TOUCH_DOWN = "D";
	public static final String MSG_TOUCH_MOVE = "M";
	public static final String MSG_TOUCH_UP = "U";
	public static final String MSG_TOUCH_CANCEL = "C";
	public static final String MSG_TOUCH_DIMENSIONS = "d";

	// mouse messages
	public static final String MSG_MOUSE_CMD = "m";
	// mouse sub-commands
	public static final int EV_MOVE = 0;
	public static final int EV_BTN_LEFT_PRESS = 1;
	public static final int EV_BTN_LEFT_RELEASE = 2;
	public static final int EV_BTN_RIGHT_PRESS = 3;
	public static final int EV_BTN_RIGHT_RELEASE = 4;
	public static final int EV_BTN_MIDDLE_PRESS = 5;
	public static final int EV_BTN_MIDDLE_RELEASE = 6;
	public static final int EV_SCROLL_HORIZ = 7;
	public static final int EV_SCROLL_VERT = 8;

	// brightness Message
	public static final String POST_BRIGHTNESS = "P";
	public static final String GET_BRIGHTNESS = "G";
}

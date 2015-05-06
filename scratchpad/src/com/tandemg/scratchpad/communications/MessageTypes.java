package com.tandemg.scratchpad.communications;

public class MessageTypes {
	// protocol versions
	public static final String MSG_GET_PROTOCOL_VERSION = "V";

	// Android messages
	public static final String MSG_BACK = "B";
	public static final String MSG_HOME = "H";
	public static final String MSG_OPTIONS = "O";
	public static final String MSG_INTENT = "I";
	public static final String MSG_BROADCAST_INTENT = "b";
	public static final String MSG_SERVICE_INTENT = "s";
	// touch messages
	public static final String MSG_TOUCH_DOWN = "D";
	public static final String MSG_TOUCH_MOVE = "M";
	public static final String MSG_TOUCH_UP = "U";
	public static final String MSG_TOUCH_CANCEL = "C";
	public static final String MSG_TOUCH_DIMENSIONS = "d";
	// location messages
	public static final String MSG_LOCATION_READY = "L";
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

	// battery Message
	public static final String GET_BATTERY = "S";

	// keyboard message
	public static final String MSG_KEYBOARD_CLICK = "k";

	// Volume message
	public static final String MSG_GENERIC_KEYBOARD_BTN = "v";
	public static final String MSG_GENERIC_KEYBOARD_BTN_VOLUMEDOWN = "94";
	public static final String MSG_GENERIC_KEYBOARD_BTN_VOLUMEUP = "93";

	// Power Button
	public static final String EV_PWR_BTN_PRESS = "w";
	public static final String EV_PWR_BTN_RELEASE = "W";
}

package com.tandemg.scratchpad;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import android.util.Log;

public class TCPClient implements Runnable {

	private static final int EV_MOVE = 0;
	private static final int EV_BTN_LEFT_PRESS = 1;
	private static final int EV_BTN_LEFT_RELEASE = 2;
	private static final int EV_BTN_RIGHT_PRESS = 3;
	private static final int EV_BTN_RIGHT_RELEASE = 4;
	private static final int EV_BTN_MIDDLE_PRESS = 5;
	private static final int EV_BTN_MIDDLE_RELEASE = 6;
	private static final int EV_SCROLL_HORIZ = 7;
	private static final int EV_SCROLL_VERT = 8;

	private static final String TAG = "TCPClient";
	private static TCPClient instance = null;
	private String serverMessage;

	public static final String SERVERIP = "192.168.43.140";
	public static final int SERVERPORT = 2301;
	private Socket mSocket = null;
	private OnMessageReceived mMessageListener = null;
	private Thread mClientThread = null;
	private boolean mRun = false;

	private OutputStreamWriter out;
	private InputStreamReader in;

	private void doTest() {
		Log.v(TAG, "searching for nodes");
		try {
			for (Enumeration<NetworkInterface> ifaces = NetworkInterface
					.getNetworkInterfaces(); ifaces.hasMoreElements();) {
				NetworkInterface iface = ifaces.nextElement();
				System.out.println(iface.getName() + ":");
				for (Enumeration<InetAddress> addresses = iface
						.getInetAddresses(); addresses.hasMoreElements();) {
					InetAddress address = addresses.nextElement();
					Log.v(TAG, "  address: " + address.getHostAddress());
					// if(address.isAnyLocalAddress())
					// Log.v(TAG, "  local address" + address);
					// else
					// Log.v(TAG, "  NOT local address" + address);
				}
			}

			InetAddress[] inetAddress = null;
			List<String> hostList = new ArrayList<String>();
			NetworkInterface nif = NetworkInterface.getByName("wlan0");
			Log.v(TAG, "nif: " + nif.toString());
			String host = nif.getInetAddresses().toString();
			Log.v(TAG, "host address: " + host);
			inetAddress = InetAddress.getAllByName("192.168.2.3");

			for (int i = 0; i < inetAddress.length; i++) {
				hostList.add(inetAddress[i].getClass() + " -\n"
						+ inetAddress[i].getHostName() + "\n"
						+ inetAddress[i].getHostAddress());
				Log.v(TAG,
						inetAddress[i].getClass() + " -\n"
								+ inetAddress[i].getHostName() + "\n"
								+ inetAddress[i].getHostAddress());
			}
		} catch (Exception e) {
			Log.e(TAG,
					"general error while searching for client: " + e.toString(),
					e);
		}
		Log.v(TAG, "finished searching");
	}

	public static synchronized TCPClient getInstance() {
		if (instance == null) {
			instance = new TCPClient();
		}
		return instance;
	}

	private TCPClient() {
		mMessageListener = new TCPClient.OnMessageReceived() {

			@Override
			public void messageReceived(String message) {
				Log.d(TAG, "message received from TCPClient: " + message);
			}
		};

		mClientThread = new Thread(this);
		mClientThread.start();
	}

	/**
	 * Sends the message entered by client to the server
	 * 
	 * @param message
	 *            text entered by client
	 */
	public void sendMessage(String message) {
		try {
			if (mClientThread == null) {
				throw new Exception("connection thread was not created yet");
			}
			if (mClientThread.isAlive() != true) {
				throw new Exception("connection thread not running");
			}
			if (connected() != true) {
				throw new Exception("client is not connected");
			}
			if (out != null) {
				try {
					out.write(message + "\n");
					out.flush();
					Log.d(TAG, "Sent.");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "Error: " + e.toString(), e);
		} finally {
		}
	}

	public void stopClient() throws IOException {
		mRun = false;
		if (mSocket != null)
			mSocket.close();
		mSocket = null;
	}

	public String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
		}
		return null;
	}

	public static String getBroadcast() throws SocketException {
		System.setProperty("java.net.preferIPv4Stack", "true");
		for (Enumeration<NetworkInterface> niEnum = NetworkInterface
				.getNetworkInterfaces(); niEnum.hasMoreElements();) {
			NetworkInterface ni = niEnum.nextElement();
			if (!ni.isLoopback()) {
				for (InterfaceAddress interfaceAddress : ni
						.getInterfaceAddresses()) {
					return interfaceAddress.getBroadcast().toString()
							.substring(1);
				}
			}
		}
		return null;
	}

	public void run() {
		mRun = true;
		try {
			// Log.d(TAG, "local ip address: " + getLocalIpAddress());
			// Log.d(TAG, "broadcast: " + getBroadcast());
			// doTest();
			InetAddress serverAddr = InetAddress.getByName(SERVERIP);
			Log.e(TAG, "C: Connecting...");
			// create a socket to make the connection with the server
			mSocket = new Socket(serverAddr, SERVERPORT);
			Log.d(TAG, "Connected");
			try {
				out = new OutputStreamWriter(mSocket.getOutputStream());
				in = new InputStreamReader(mSocket.getInputStream());
				// out = new PrintWriter(new BufferedWriter(new
				// OutputStreamWriter(mSocket.getOutputStream())), true);
				// in = new BufferedReader(new
				// InputStreamReader(mSocket.getInputStream()));
				while (mRun) {
					// serverMessage = in.readLine();
					char arr[] = new char[256];
					int n;
					try {
						n = in.read(arr, 0, 256);
					} catch (IOException e) {
						continue;
					}
					serverMessage = String.copyValueOf(arr, 0, n); // =
																	// arr.toString();
					if (serverMessage != null && mMessageListener != null) {
						// call the method messageReceived from MyActivity class
						mMessageListener.messageReceived(serverMessage);
					}
					serverMessage = null;
				}
			} catch (Exception e) {
				Log.e(TAG,
						"general error while waiting for server data: "
								+ e.toString(), e);
			} finally {
				// the socket must be closed. It is not possible to reconnect to
				// this socket
				// after it is closed, which means a new socket instance has to
				// be created.
				Log.i(TAG, "Closing socket");
				if (in != null) {
					in.close();
					in = null;
				}
				if (out != null) {
					out.close();
					out = null;
				}
				if (mSocket != null) {
					mSocket.close();
					mSocket = null;
				}
			}
		} catch (IOException e) {
			Log.e(TAG, "TCP Client has stopped running duo to an IO error: "
					+ e.toString(), e);
		} catch (Exception e) {
			Log.e(TAG,
					"TCP Client has stopped running duo to a general error: "
							+ e.toString(), e);
		}
	}

	public boolean connected() {
		return mSocket.isConnected();
	}

	public void notifyDown(float x, float y, float pressure) {
		this.sendMessage("D " + String.valueOf(x) + " " + String.valueOf(y)
				+ " " + String.valueOf(pressure));
	}

	public void notifyMove(float x, float y, float pressure) {
		this.sendMessage("M " + String.valueOf(x) + " " + String.valueOf(y)
				+ " " + String.valueOf(pressure));
	}

	public void notifyUp(float x, float y, float pressure) {
		this.sendMessage("U " + String.valueOf(x) + " " + String.valueOf(y)
				+ " " + String.valueOf(pressure));
	}

	public void notifyDimensions(int height, int width) {
		this.sendMessage("d " + String.valueOf(height) + " "
				+ String.valueOf(width));
	}

	public void notifyBack() {
		this.sendMessage("B");
	}

	public void notifyHome() {
		this.sendMessage("H");
	}

	public void notifyMouseMove(final int Xvalue, final int Yvalue) {
		this.sendMessage("m " + String.valueOf(EV_MOVE) + " "
				+ String.valueOf(Xvalue) + " " + String.valueOf(Yvalue));
	}

	public void notifyMouseButtonPressLEFT() {
		this.sendMessage("m " + String.valueOf(EV_BTN_LEFT_PRESS) + " 0 0");
	}

	public void notifyMouseButtonReleaseLEFT() {
		this.sendMessage("m " + String.valueOf(EV_BTN_LEFT_RELEASE) + " 0 0");
	}

	public void notifyMouseButtonPressRIGHT() {
		this.sendMessage("m " + String.valueOf(EV_BTN_RIGHT_PRESS) + " 0 0");
	}

	public void notifyMouseButtonReleaseRIGHT() {
		this.sendMessage("m " + String.valueOf(EV_BTN_RIGHT_RELEASE) + " 0 0");
	}

	public void notifyMouseButtonPress(final byte button) {
		switch (button) {
		case 0:
			this.sendMessage("m " + String.valueOf(EV_BTN_LEFT_PRESS) + " 0 0");
			break;
		case 1:
			this.sendMessage("m " + String.valueOf(EV_BTN_RIGHT_PRESS) + " 0 0");
			break;
		case 2:
			this.sendMessage("m " + String.valueOf(EV_BTN_MIDDLE_PRESS)
					+ " 0 0");
			break;
		default:
			Log.e(TAG, "wrong button argument: " + String.valueOf(button));
		}
	}

	public void notifyMouseButtonRelease(final byte button) {
		switch (button) {
		case 0:
			this.sendMessage("m " + String.valueOf(EV_BTN_LEFT_RELEASE)
					+ " 0 0");
			break;
		case 1:
			this.sendMessage("m " + String.valueOf(EV_BTN_RIGHT_RELEASE)
					+ " 0 0");
			break;
		case 2:
			this.sendMessage("m " + String.valueOf(EV_BTN_MIDDLE_RELEASE)
					+ " 0 0");
			break;
		default:
			Log.e(TAG, "wrong button argument: " + String.valueOf(button));
		}
	}

	public void notifyMouseHScroll(final int Xvalue) {
		this.sendMessage("m " + String.valueOf(EV_SCROLL_HORIZ) + " "
				+ String.valueOf(Xvalue) + " 0");
	}

	public void notifyMouseVScroll(final int Yvalue) {
		this.sendMessage("m " + String.valueOf(EV_SCROLL_VERT) + " "
				+ String.valueOf(Yvalue) + " 0");
	}

	// Declare the interface. The method messageReceived(String message) will
	// must be implemented in the MyActivity
	// class at on asynckTask doInBackground
	public interface OnMessageReceived {
		public void messageReceived(String message);
	}
}

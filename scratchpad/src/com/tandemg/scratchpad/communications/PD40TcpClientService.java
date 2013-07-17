package com.tandemg.scratchpad.communications;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.tandemg.scratchpad.R;
import com.tandemg.scratchpad.ScratchpadActivity;

public class PD40TcpClientService extends Service {

	private static final String TAG = "PD40TcpClientService";
	private static final int PING_TIMEOUT = 1000;
	private static final String SERVER_DEFAULT_IP = "192.168.43.140";
	private static final int SERVER_DEFAULT_PORT = 2301;
	private static final int CONNECT_TIMEOUT = 5000;
	private static final int DISCONNECT_TIMEOUT = 5000;
	private static final int SOCKET_READ_TIMEOUT_MS = 500;
	private static final int SERVICE_THREAD_SLEEP_TIMEOUT = 5000;
	private static final int SERVICE_THREAD_PING_ERROR_TIMEOUT = 1000;
	private static final int SERVICE_THREAD_CONNECT_ERROR_TIMEOUT = 2500;
	private static final int SERVICE_THREAD_START_LISTENER_ERROR_TIMEOUT = 500;

	private Object lock = null;
	private boolean connected = false;
	private String serverIp = null;
	private int serverPort = -1;

	private NotificationManager mNotificationManager = null;
	private NotificationCompat.Builder mNotificationBuilder = null;
	private static final int mNotificationId = R.id.notification_bar;

	private final IBinder mBinder = new PD40TcpClientServiceBinder();

	public interface DataHandler {
		public void handleData(String data);
	}

	private List<DataHandler> mHandlers = null;
	private Socket mSocket = null;
	private OutputStreamWriter out = null;
	private InputStreamReader in = null;
	private Thread mClientThread = null;
	private Thread mSocketListenerThread = null;
	private boolean mServiceRunning = false;
	private boolean mSocketListenrRunning = false;

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class PD40TcpClientServiceBinder extends Binder {
		public PD40TcpClientService getService() {
			return PD40TcpClientService.this;
		}
	}

	public PD40TcpClientService() {
		lock = new Object();
		mHandlers = new ArrayList<PD40TcpClientService.DataHandler>();
	}

	@Override
	public void onCreate() {
		// Display a notification about us starting. We put an icon in the
		// status bar.
		setupNotification();
		showNotificationStarted();

		startServiceThread();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "Received start id " + startId + ": " + intent);
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "PD40 TCP client service destroyed");
		showNotificationStopped();

		stopServiceThread();
		// Cancel the persistent notification.
		mNotificationManager.cancel(mNotificationId);

		// Tell the user we stopped.
		Toast.makeText(this, R.string.pd40tcp_service_stopped,
				Toast.LENGTH_SHORT).show();
	}

	public void addDataHandler(DataHandler handler) throws Exception {
		synchronized (this) {
			if (!mHandlers.add(handler))
				throw new Exception("could not add data handler");
		}
	}

	public void remDataHandler(DataHandler handler)
			throws IndexOutOfBoundsException {
		synchronized (this) {
			mHandlers.remove(mHandlers.indexOf(handler));
		}
	}

	private void notifyHandlers(final String text) {
		synchronized (this) {
			for (DataHandler h : mHandlers) {
				h.handleData(text);
			}
		}
	}

	protected boolean ping(final String ip) {
		try {
			InetAddress addr = InetAddress.getByName(ip);
			return addr.isReachable(PING_TIMEOUT);
		} catch (IOException e) {
			Log.e(TAG, "ping failed: " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	protected Vector<String> listAvailableIps() {
		Vector<String> ret = new Vector<String>();
		// TODO: search network here
		return ret;
	}

	protected boolean startServiceThread() {
		synchronized (lock) {
			mServiceRunning = true;
		}
		mClientThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					synchronized (lock) {
						if (!mServiceRunning)
							break;
					}
					Vector<String> availableIps = listAvailableIps();
					if (availableIps.size() == 0) {
						availableIps.add(SERVER_DEFAULT_IP);
					}
					for (int i = 0; i < availableIps.size(); i++) {
						synchronized (lock) {
							if (!mServiceRunning)
								break;
						}
						showNotificationSearching();
						String ip = availableIps.get(i);
						if (ip == null) {
							Log.e(TAG, "invalid available ip list");
							continue;
						}
						if (!ping(ip)) {
							Log.e(TAG, "ping failed to: " + ip);
							sleep(SERVICE_THREAD_PING_ERROR_TIMEOUT);
							continue;
						}
						if (!connect(ip, SERVER_DEFAULT_PORT)) {
							Log.e(TAG, "connection failed to: " + ip);
							sleep(SERVICE_THREAD_CONNECT_ERROR_TIMEOUT);
							continue;
						}
						showNotificationConnected();
						if (!startListenerThread()) {
							Log.e(TAG, "listener thread could not be started");
							// TODO: add ip to blacklist
							disconnect();
							sleep(SERVICE_THREAD_START_LISTENER_ERROR_TIMEOUT);
							continue;
						}
						waitListenerThread();
						disconnect();
						showNotificationDisconnected();
						boolean sleep;
						synchronized (lock) {
							sleep = mServiceRunning;
						}
						if (sleep)
							sleep(SERVICE_THREAD_SLEEP_TIMEOUT);
					}
				}
				Log.i(TAG, "service thread exited");
			}
		});
		mClientThread.start();
		return true;
	}

	protected void stopServiceThread() {
		synchronized (lock) {
			mServiceRunning = false;
			mSocketListenrRunning = false;
		}
		stopListenerThread();
		disconnect();
		if (mClientThread.isAlive()) {
			try {
				Log.i(TAG, "waiting on service thread (stop)");
				mClientThread.join(DISCONNECT_TIMEOUT);
				Log.i(TAG, "service thread joined (stop)");
			} catch (InterruptedException e) {
				Log.e(TAG, "stopServiceThread interrupted");
				e.printStackTrace();
			}
		}
	}

	protected boolean startListenerThread() {
		if (!connected())
			return false;
		synchronized (lock) {
			mSocketListenrRunning = true;
			mSocketListenerThread = new Thread(new Runnable() {
				@Override
				public void run() {
					char arr[] = new char[256];
					while (connected()) {
						synchronized (lock) {
							if (!mSocketListenrRunning)
								break;
						}
						int n;
						try {
							n = in.read(arr, 0, 256);
						} catch (SocketTimeoutException e) {
							continue;
						} catch (IOException e) {
							Log.e(TAG,
									"IO Exception during read: " + e.toString());
							e.printStackTrace();
							continue;
						}
						if (n < 0) {
							Log.e(TAG,
									"socket read returned with error, result: "
											+ String.valueOf(n));
							Log.i(TAG,
									"assuming socket closed on the other hand, break listener");
							break;
						}
						String msg = String.copyValueOf(arr, 0, n);
						if (msg != null) {
							// notify listeners
							Log.v(TAG, "message received: " + msg);
							notifyHandlers(msg);
						}
					}
					Log.i(TAG, "listener thread exited");
				}
			});
			mSocketListenerThread.start();
			return true;
		}
	}

	protected void waitListenerThread() {
		if (mSocketListenerThread.isAlive()) {
			try {
				Log.i(TAG, "waiting on listener thread (wait)");
				mSocketListenerThread.join();
				Log.i(TAG, "listener thread joined (during wait)");
			} catch (InterruptedException e) {
				Log.e(TAG, "waitListenerThread interrupted");
				e.printStackTrace();
			}
		}
	}

	protected void stopListenerThread() {
		synchronized (lock) {
			mSocketListenrRunning = false;
			if (mSocketListenerThread == null)
				return;
		}
		if (mSocketListenerThread.isAlive()) {
			disconnect();
			try {
				Log.i(TAG, "waiting on listener thread (stop)");
				mSocketListenerThread.join(DISCONNECT_TIMEOUT);
				Log.i(TAG, "listener thread joined (during stop)");
			} catch (InterruptedException e) {
				Log.e(TAG, "waitListenerThread interrupted");
				e.printStackTrace();
			}
		}
	}

	protected boolean connect(final String ip, final int port) {
		if (connected())
			return false;
		if (!ping(ip))
			return false;
		synchronized (lock) {
			Log.i(TAG, "connecting to server");
			try {
				InetAddress serverAddr = InetAddress.getByName(ip);
				InetSocketAddress addr = new InetSocketAddress(serverAddr, port);
				mSocket = new Socket();
				mSocket.setKeepAlive(true);
				mSocket.setSoTimeout(SOCKET_READ_TIMEOUT_MS);
				mSocket.setTcpNoDelay(true);
				mSocket.connect(addr, CONNECT_TIMEOUT);
				setServerSettings(ip, port);
				connected(true);
				out = new OutputStreamWriter(mSocket.getOutputStream());
				in = new InputStreamReader(mSocket.getInputStream());
			} catch (UnknownHostException e) {
				Log.e(TAG, "UnknownHostException on IP: " + ip);
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				Log.e(TAG, "IOException during connection: " + e.toString());
				e.printStackTrace();
				return false;
			}
			Log.i(TAG, "connected to server");
		}
		return true;
	}

	protected boolean disconnect() {
		if (!connected())
			return true;
		Log.i(TAG, "disconnecting socket");
		synchronized (lock) {
			try {
				Log.d(TAG, "shutdown socket i/o");
				if (out != null) {
					out.close();
					out = null;
				}
				if (in != null) {
					in.close();
					in = null;
				}
				Log.d(TAG, "closing socket");
				if (mSocket != null) {
					mSocket.close();
					mSocket = null;
				}
			} catch (IOException e) {
				Log.e(TAG, "could not close client socket");
				Log.d(TAG, e.toString(), e);
				e.printStackTrace();
				return false;
			}
		}
		connected(false);
		Log.i(TAG, "socket disconnected");
		return true;
	}

	private void sendMessage(final String text) {
		if (!connected())
			return;
		synchronized (lock) {
			if (out != null) {
				try {
					out.write(text + "\n");
					out.flush();
					Log.v(TAG, "sent: " + text);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Show a notification while this service is running.
	 */
	private void setupNotification() {
		synchronized (lock) {
			mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationBuilder = new NotificationCompat.Builder(this);
			mNotificationBuilder
					.setContentTitle(getText(R.string.pd40tcp_service_label));
			// Creates an explicit intent for an Activity in your app
			Intent resultIntent = new Intent(this, ScratchpadActivity.class);

			// The stack builder object will contain an artificial back stack
			// for the started Activity. This ensures that navigating backward
			// from the Activity leads out of your application to the Home
			// screen.
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
			// Adds the back stack for the Intent (but not the Intent itself)
			stackBuilder.addParentStack(ScratchpadActivity.class);
			// Adds the Intent that starts the Activity to the top of the stack
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
					0, PendingIntent.FLAG_UPDATE_CURRENT);
			mNotificationBuilder.setContentIntent(resultPendingIntent);
		}
	}

	private void showNotificationStarted() {
		synchronized (lock) {
			if (mNotificationManager == null || mNotificationBuilder == null)
				return;
			mNotificationBuilder.setSmallIcon(R.drawable.ic_stat_okay);
			mNotificationBuilder
					.setContentText(getText(R.string.pd40tcp_service_started));
			mNotificationManager.notify(mNotificationId,
					mNotificationBuilder.getNotification());
		}
	}

	private void showNotificationSearching() {
		synchronized (lock) {
			if (mNotificationManager == null || mNotificationBuilder == null)
				return;
			mNotificationBuilder.setSmallIcon(R.drawable.ic_stat_cloud);
			mNotificationBuilder
					.setContentText(getText(R.string.pd40tcp_service_searching));
			mNotificationManager.notify(mNotificationId,
					mNotificationBuilder.getNotification());
		}
	}

	private void showNotificationConnected() {
		synchronized (lock) {
			if (mNotificationManager == null || mNotificationBuilder == null)
				return;
			mNotificationBuilder.setSmallIcon(R.drawable.ic_stat_mouse);
			mNotificationBuilder
					.setContentText(getText(R.string.pd40tcp_service_connected));
			mNotificationManager.notify(mNotificationId,
					mNotificationBuilder.getNotification());
		}
	}

	private void showNotificationDisconnected() {
		synchronized (lock) {
			if (mNotificationManager == null || mNotificationBuilder == null)
				return;
			mNotificationBuilder.setSmallIcon(R.drawable.ic_stat_error);
			mNotificationBuilder
					.setContentText(getText(R.string.pd40tcp_service_disconnected));
			mNotificationManager.notify(mNotificationId,
					mNotificationBuilder.getNotification());
		}
	}

	private void showNotificationStopped() {
		synchronized (lock) {
			if (mNotificationManager == null || mNotificationBuilder == null)
				return;
			mNotificationBuilder.setSmallIcon(R.drawable.ic_stat_restart);
			mNotificationBuilder
					.setContentText(getText(R.string.pd40tcp_service_stopped));
			mNotificationManager.notify(mNotificationId,
					mNotificationBuilder.getNotification());
		}
	}

	protected synchronized boolean connected() {
		synchronized (lock) {
			if (mSocket != null)
				return mSocket.isConnected();
			return connected;
		}
	}

	private synchronized void connected(final boolean state) {
		synchronized (lock) {
			connected = state;
		}
	}

	private synchronized void setServerSettings(final String ip, final int port) {
		synchronized (lock) {
			serverIp = ip;
			serverPort = port;
		}
	}

	protected synchronized String serverIp() {
		synchronized (lock) {
			return serverIp;
		}
	}

	protected synchronized int serverPort() {
		synchronized (lock) {
			return serverPort;
		}
	}

	public void notifyDown(float x, float y, float pressure) {
		this.sendMessage(MessageTypes.MSG_TOUCH_DOWN + " " + String.valueOf(x)
				+ " " + String.valueOf(y) + " " + String.valueOf(pressure));
	}

	public void notifyMove(float x, float y, float pressure) {
		this.sendMessage(MessageTypes.MSG_TOUCH_MOVE + " " + String.valueOf(x)
				+ " " + String.valueOf(y) + " " + String.valueOf(pressure));
	}

	public void notifyUp(float x, float y, float pressure) {
		this.sendMessage(MessageTypes.MSG_TOUCH_UP + " " + String.valueOf(x)
				+ " " + String.valueOf(y) + " " + String.valueOf(pressure));
	}

	public void notifyCancel(float x, float y, float pressure) {
		this.sendMessage(MessageTypes.MSG_TOUCH_CANCEL + " "
				+ String.valueOf(x) + " " + String.valueOf(y) + " "
				+ String.valueOf(pressure));
	}

	public void notifyDimensions(int height, int width) {
		this.sendMessage(MessageTypes.MSG_TOUCH_DIMENSIONS + " "
				+ String.valueOf(height) + " " + String.valueOf(width));
	}

	public void notifyBack() {
		this.sendMessage(MessageTypes.MSG_BACK);
	}

	public void notifyHome() {
		this.sendMessage(MessageTypes.MSG_HOME);
	}

	public void notifyMouseMove(final int Xvalue, final int Yvalue) {
		this.sendMessage(MessageTypes.MSG_MOUSE_CMD + " "
				+ String.valueOf(MessageTypes.EV_MOVE) + " "
				+ String.valueOf(Xvalue) + " " + String.valueOf(Yvalue));
	}

	public void notifyMouseButtonPressLEFT() {
		this.sendMessage(MessageTypes.MSG_MOUSE_CMD + " "
				+ String.valueOf(MessageTypes.EV_BTN_LEFT_PRESS) + " 0 0");
	}

	public void notifyMouseButtonReleaseLEFT() {
		this.sendMessage(MessageTypes.MSG_MOUSE_CMD + " "
				+ String.valueOf(MessageTypes.EV_BTN_LEFT_RELEASE) + " 0 0");
	}

	public void notifyMouseButtonPressRIGHT() {
		this.sendMessage(MessageTypes.MSG_MOUSE_CMD + " "
				+ String.valueOf(MessageTypes.EV_BTN_RIGHT_PRESS) + " 0 0");
	}

	public void notifyMouseButtonReleaseRIGHT() {
		this.sendMessage(MessageTypes.MSG_MOUSE_CMD + " "
				+ String.valueOf(MessageTypes.EV_BTN_RIGHT_RELEASE) + " 0 0");
	}

	public void notifyMouseButtonPress(final byte button) {
		switch (button) {
		case 0:
			this.sendMessage(MessageTypes.MSG_MOUSE_CMD + " "
					+ String.valueOf(MessageTypes.EV_BTN_LEFT_PRESS) + " 0 0");
			break;
		case 1:
			this.sendMessage(MessageTypes.MSG_MOUSE_CMD + " "
					+ String.valueOf(MessageTypes.EV_BTN_RIGHT_PRESS) + " 0 0");
			break;
		case 2:
			this.sendMessage(MessageTypes.MSG_MOUSE_CMD + " "
					+ String.valueOf(MessageTypes.EV_BTN_MIDDLE_PRESS) + " 0 0");
			break;
		default:
			Log.e(TAG, "wrong button argument: " + String.valueOf(button));
		}
	}

	public void notifyMouseButtonRelease(final byte button) {
		switch (button) {
		case 0:
			this.sendMessage(MessageTypes.MSG_MOUSE_CMD + " "
					+ String.valueOf(MessageTypes.EV_BTN_LEFT_RELEASE) + " 0 0");
			break;
		case 1:
			this.sendMessage(MessageTypes.MSG_MOUSE_CMD + " "
					+ String.valueOf(MessageTypes.EV_BTN_RIGHT_RELEASE)
					+ " 0 0");
			break;
		case 2:
			this.sendMessage(MessageTypes.MSG_MOUSE_CMD + " "
					+ String.valueOf(MessageTypes.EV_BTN_MIDDLE_RELEASE)
					+ " 0 0");
			break;
		default:
			Log.e(TAG, "wrong button argument: " + String.valueOf(button));
		}
	}

	public void notifyMouseHScroll(final int Xvalue) {
		this.sendMessage(MessageTypes.MSG_MOUSE_CMD + " "
				+ String.valueOf(MessageTypes.EV_SCROLL_HORIZ) + " "
				+ String.valueOf(Xvalue) + " 0");
	}

	public void notifyMouseVScroll(final int Yvalue) {
		this.sendMessage(MessageTypes.MSG_MOUSE_CMD + " "
				+ String.valueOf(MessageTypes.EV_SCROLL_VERT) + " "
				+ String.valueOf(Yvalue) + " 0");
	}

	private static void sleep(final long timeout) {
		try {
			Thread.sleep(timeout);
		} catch (InterruptedException e) {
			Log.e(TAG, "tcp service thread sleep interrupted");
			e.printStackTrace();
		}
	}
}

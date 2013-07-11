package com.tandemg.scratchpad.communications;

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

	private Object lock = null;
	private boolean connected = false;
	private String serverIp = null;
	private long serverPort = -1;

	private NotificationManager mNotificationManager = null;
	private NotificationCompat.Builder mNotificationBuilder = null;
	private static final int mNotificationId = R.id.notification_bar;

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new LocalBinder();

	public PD40TcpClientService() {
		lock = new Object();
		serverIp("192.168.43.140");
		serverPort(2301);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		public PD40TcpClientService getService() {
			return PD40TcpClientService.this;
		}
	}

	@Override
	public void onCreate() {
		// Display a notification about us starting. We put an icon in the
		// status bar.
		setupNotification();
		showNotificationStarted();
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
		showNotificationStopped();
		// Cancel the persistent notification.
		mNotificationManager.cancel(mNotificationId);

		// Tell the user we stopped.
		Toast.makeText(this, R.string.pd40tcp_service_stopped,
				Toast.LENGTH_SHORT).show();
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
			// for
			// the started Activity.
			// This ensures that navigating backward from the Activity leads out
			// of
			// your application to the Home screen.
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
			mNotificationBuilder.setSmallIcon(R.drawable.stat_sample);
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
			mNotificationBuilder.setSmallIcon(R.drawable.stat_neutral);
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
			mNotificationBuilder.setSmallIcon(R.drawable.stat_happy);
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
			mNotificationBuilder.setSmallIcon(R.drawable.stat_sad);
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
			mNotificationBuilder.setSmallIcon(R.drawable.stat_sample);
			mNotificationBuilder
					.setContentText(getText(R.string.pd40tcp_service_stopped));
			mNotificationManager.notify(mNotificationId,
					mNotificationBuilder.getNotification());
		}
	}

	protected boolean ping(final String ip) {
		return false;
	}

	protected String[] listAvailableIps() {
		return null;
	}

	protected boolean connect(final String ip, final int port) {
		if (!ping(ip))
			return false;
		return true;
	}

	protected boolean disconnect() {
		if (!connected())
			return true;
		synchronized (lock) {
			serverIp = null;
			serverPort = -1;
		}
		return false;
	}

	protected synchronized boolean connected() {
		synchronized (lock) {
			return connected;
		}
	}

	private synchronized void connected(final boolean state) {
		if (state) {
			showNotificationConnected();
		} else {
			showNotificationDisconnected();
		}
		synchronized (lock) {
			connected = state;
		}
	}

	protected synchronized String serverIp() {
		synchronized (lock) {
			return serverIp;
		}
	}

	private synchronized void serverIp(final String ip) {
		synchronized (lock) {
			serverIp = ip;
		}
	}

	protected synchronized long serverPort() {
		synchronized (lock) {
			return serverPort;
		}
	}

	private synchronized void serverPort(final long port) {
		synchronized (lock) {
			serverPort = port;
		}
	}
}

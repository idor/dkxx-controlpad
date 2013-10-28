package com.tandemg.scratchpad.location;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class PD40LocationService extends Service implements LocationListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private static final String TAG = "PD40LocationService";

	// Milliseconds per second
	private static final int MILLISECONDS_PER_SECOND = 1000;
	// Update frequency in seconds
	public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
	// Update frequency in milliseconds
	private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND
			* UPDATE_INTERVAL_IN_SECONDS;
	// The fastest update frequency, in seconds
	private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
	// A fast frequency ceiling in milliseconds
	private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND
			* FASTEST_INTERVAL_IN_SECONDS;
	private final IBinder mBinder = new PD40LocationServiceBinder();

	private boolean mGooglePlayServicesAvailable = false;

	private LocationClient mLocationClient = null;
	private Location mCurrentLocation = null;
	private boolean mGPSTurnedOn = false;
	private LocationRequest mLocationRequest = null;
	private boolean mUpdatesRequested = true;

	public class PD40LocationServiceBinder extends Binder {
		public PD40LocationService getService() {
			return PD40LocationService.this;
		}
	}

	public PD40LocationService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.v(TAG, "onBind");
		return mBinder;
	}

	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate");
		super.onCreate();

		if (servicesConnected()) {
			mLocationClient = new LocationClient(this, this, this);
			mLocationClient.connect();

			// Create the LocationRequest object
			mLocationRequest = LocationRequest.create();
			// Use high accuracy
			mLocationRequest
					.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
			// Set the update interval to 5 seconds
			mLocationRequest.setInterval(UPDATE_INTERVAL);
			// Set the fastest update interval to 1 second
			mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
		}
	}

	@Override
	public void onDestroy() {
		Log.v(TAG, "onDestroy");

		if (mLocationClient != null) {
			if (mLocationClient.isConnected()) {
				mLocationClient.removeLocationUpdates(this);
			}
			mLocationClient.disconnect();
		}
		if (mGPSTurnedOn) {
			Log.i(TAG, "turn off gps (back to normal)");
		}
		super.onDestroy();
	}

	private boolean servicesConnected() {
		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode == ConnectionResult.SUCCESS) {
			// In debug mode, log the status
			Log.d(TAG, "Google Play services is available.");
			mGooglePlayServicesAvailable = true;
		} else {
			Log.e(TAG, "could not connect to Google Play services, error "
					+ String.valueOf(resultCode));
			mGooglePlayServicesAvailable = false;
		}
		return mGooglePlayServicesAvailable;
	}

	@Override
	public void onConnected(Bundle arg0) {
		Log.i(TAG, "connected to Google Play services");
		if (mUpdatesRequested) {
			mCurrentLocation = mLocationClient.getLastLocation();
			mLocationClient.requestLocationUpdates(mLocationRequest, this);
		}
	}

	@Override
	public void onDisconnected() {
		Log.i(TAG, "disconnected to Google Play services");
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.e(TAG, "failed to connect to Google Play services, result: "
				+ String.valueOf(connectionResult));
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "location changed: " + location.toString());
		mCurrentLocation = location;
		Time t = new Time();
		Log.v(TAG, "provider: " + location.getProvider().toString());
		t.set(location.getTime());
		Log.v(TAG, "time: " + t.hour + ":" + t.minute + "." + t.second);
		Log.v(TAG, "accuracy: " + location.getAccuracy());
		Log.v(TAG, "latitude: " + location.getLatitude());
		Log.v(TAG, "longitude: " + location.getLongitude());
		Log.v(TAG, "altitude: " + location.getAltitude());
		Log.v(TAG, "bearing: " + location.getBearing());
		Log.v(TAG, "speed: " + location.getSpeed());
	}

	public final Location getLocation() {
		return mCurrentLocation;
	}

	// @Override
	// public void onProviderDisabled(String provider) {
	// Log.d(TAG, "provider disabled: " + provider.toString());
	// }
	//
	// @Override
	// public void onProviderEnabled(String provider) {
	// Log.d(TAG, "provider enabled: " + provider.toString());
	// }
	//
	// @Override
	// public void onStatusChanged(String provider, int status, Bundle extras) {
	// Log.d(TAG,
	// "status changed, provider: " + provider.toString() +
	// ", status: " + String.valueOf(status) +
	// ", extras: " + extras.toString());
	// }
}

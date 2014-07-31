package com.tandemg.scratchpad;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.tandemg.scratchpad.communications.PD40TcpClientService;
import com.tandemg.scratchpad.communications.PD40TcpClientService.PD40TcpClientServiceBinder;
import com.tandemg.scratchpad.location.PD40LocationService;
import com.tandemg.scratchpad.location.PD40LocationService.PD40LocationServiceBinder;

public class ScratchpadActivity extends FragmentActivity {
	private static final int KEYBOARD_DOUBLE_TAP_TIMEOUT = 500;
	private static final String TAG = "ScratchpadActivity";
	private static final int brightnessTimeout = 5000;
	private Fragment mouse = new MousepadActivity();
	private Fragment quickLaunch = new QuickLaunchActivity();
	private Fragment wifiScanner = new ScanWifiActivity();
	private boolean mTcpServiceBound = false;
	private ServiceConnection mTcpClientConnection = null;
	private PD40TcpClientService mTcpClientService = null;
	private boolean mLocationServiceBound = false;
	private ServiceConnection mLocationConnection = null;
	private PD40LocationService mLocationService = null;
	public DataHandler handler = null;
	private Thread brightnessDeamonThread = null;
	private long keyboardTimestamp = 0;
	/**
	 * The pager widget, which handles animation and allows swiping horizontally
	 * to access previous and next wizard steps.
	 */
	private NonSwipeableViewPager mPager;

	/**
	 * The pager adapter, which provides the pages to the view pager widget.
	 */
	private PagerAdapter mPagerAdapter;
	private long mBatteryUpadateTimestamp = 0;
	final private long mBatteryUpdateTimeout = 9999;

	public ScratchpadActivity() {
		handler = new DataHandler();
		mTcpClientConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.v(TAG, "tcp client service connected");
				// We've bound to LocalService, cast the IBinder and get
				// LocalService instance
				PD40TcpClientServiceBinder binder = (PD40TcpClientServiceBinder) service;
				mTcpClientService = binder.getService();

				try {// create and add local handler to the TCP client instance
					mTcpClientService.addDataHandler(handler);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mTcpServiceBound = true;
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.v(TAG, "tcp client service disconnected");
				mTcpServiceBound = false;
				mTcpClientService = null;
			}
		};

		Log.v(TAG, "ScratchpadActivity object created");
	}

	private class DataHandler
			implements
			com.tandemg.scratchpad.communications.PD40TcpClientService.DataHandler {
		public void handleData(String data) {
			String[] temp = data.split(" ");
			try {
				switch (temp[0].charAt(0)) {
				case 'G':
					recievedGlassBrightness(Integer.parseInt(temp[1]));
					break;
				case 'S':
					recievedGlassBatteryStatus(Integer.parseInt(temp[1]));
					recievedGlassBatteryState(Integer.parseInt(temp[2]));
					/* update battery-timestamp every battert status event */
					mBatteryUpadateTimestamp = System.currentTimeMillis();
					break;
				case 'P':
					/*
					 * TODO: add action on POST request returned, it's syntax
					 * is: "P [1|0 - 1 for success ] [int-currentValue]"
					 */
					break;
				}
			} catch (NumberFormatException e) {
				Log.d(TAG, "Failed to Parse the expected int: " + temp[1]
						+ " or " + temp[2]);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scratchpad);

		Intent intent;
		intent = new Intent(this, PD40TcpClientService.class);
		bindService(intent, mTcpClientConnection, Context.BIND_AUTO_CREATE);

		// Show the Up button in the action bar.
		setupActionBar();

		// keep screen on while app is running
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Instantiate a ViewPager and a PagerAdapter.
		mPager = (NonSwipeableViewPager) findViewById(R.id.pager);
		mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
		mPager.setAdapter(mPagerAdapter);
		mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				// When changing pages, reset the action bar actions since they
				// are dependent on which page is currently active. An
				// alternative approach is to have each fragment expose actions
				// itself (rather than the activity exposing actions), but for
				// simplicity, the activity provides the actions in this sample.
				invalidateOptionsMenu();
			}
		});
		VertSeekBar brightnessBar = (VertSeekBar) findViewById(R.id.brightness_bar);
		brightnessBar.setProgress(50);
		getGlassBrightness();
		setBatteryOnLongClickListener();

		brightnessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				setGlassBrightness(progress);
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
			}

		});

		brightnessDeamonThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(2000, 0);
					while (true) {
						getGlassBrightness();
						Thread.sleep(brightnessTimeout / 2, 0);
						getGlassBatteryStatus();
						Thread.sleep(brightnessTimeout / 2, 0);
						/*
						 * a simple watchdog timer, mBatteryUpadateTimestamp is
						 * updated every battery status update. if for 10
						 * seconds battery will not get an update, thus the
						 * screen indication is faulty, and will be cleared.
						 */
						if (System.currentTimeMillis()
								- mBatteryUpadateTimestamp > mBatteryUpdateTimeout) {
							Log.e(TAG,
									"Scratchpad did not recieve battery status for 10 seconds, clearing battery status indication");
							clearGlassBatteryStatusOrState();
						}
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		brightnessDeamonThread.start();
	}

	@Override
	public void onDestroy() {
		if (mLocationServiceBound) {
			Log.v(TAG, "unbind location service");
			unbindService(mLocationConnection);
		}
		if (mTcpServiceBound) {
			Log.v(TAG, "unbind tcp service");
			unbindService(mTcpClientConnection);
		}
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		Log.d(TAG, "onStart, with Version: v" + getAppVersionName());
		super.onStart();
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "stop");
		super.onStop();
	}

	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "pause");
		super.onPause();
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "resume");
		super.onResume();
	}

	@Override
	protected void onRestart() {
		Log.d(TAG, "restart");
		super.onRestart();
	}

	public String getAppVersionName() {
		try {
			return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Error - NameNotFound";
		}
	}

	@Override
	public void onBackPressed() {
		if (mPager.getCurrentItem() != 0) {// return to main fragment on back
											// pressed
			mPager.setCurrentItem(0);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.activity_screen_slide, menu);
		menu.findItem(R.id.action_bar_settings).setEnabled(true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_bar_settings) {
			if (mPager.getCurrentItem() == 1) {
				mPager.setCurrentItem(0);
				mTcpClientService.setIconConnected(R.drawable.ic_stat_mouse);
			} else {
				mPager.setCurrentItem(1);
				mTcpClientService.setIconConnected(R.drawable.ic_stat_mouse);
				// maybe find more suitable icon for this state?
				return true;
			}
		} else { // here for forward-backward compatibility propuse
			mPager.setCurrentItem(0);
			mTcpClientService.setIconConnected(R.drawable.ic_stat_mouse);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Log.v(TAG, "orientation changed to LANDSCAPE ("
					+ newConfig.orientation + ")");
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			Log.v(TAG, "orientation changed to PORTRAIT ("
					+ newConfig.orientation + ")");
		}
	}

	public void onClick_Back(View v) {
		try {
			mTcpClientService.notifyBack();
		} catch (Exception e) {
			Log.e(TAG, "Error: " + e.toString(), e);
			e.printStackTrace();
		} finally {
		}
	}

	public void onClick_Home(View v) {
		try {
			mTcpClientService.notifyHome();
		} catch (Exception e) {
			Log.e(TAG, "Error: " + e.toString(), e);
			e.printStackTrace();
		} finally {
		}
	}

	public void onClick_Options(View v) {

		try {
			mTcpClientService.notifyOptions();
		} catch (Exception e) {
			Log.e(TAG, "Error: " + e.toString(), e);
			e.printStackTrace();
		} finally {
		}

	}

	public void onClick_Keyboard(View v) {
		/*
		 * currently we support only specific one-char values. no full ASCII
		 * support, no Upper-Cases for instance.
		 */

		if (System.currentTimeMillis() - keyboardTimestamp > KEYBOARD_DOUBLE_TAP_TIMEOUT) {
			keyboardTimestamp = System.currentTimeMillis();
			String message = "Double-click for keyboard input...";
			Toast.makeText(this.getApplicationContext(), message,
					Toast.LENGTH_SHORT).show();
			return;
		}
		keyboardTimestamp = 0;
		AlertDialog.Builder alert = new AlertDialog.Builder(this); // android.R.style.Theme_Dialog
		alert.setTitle("Push string");
		alert.setMessage("");

		final EditText input = new EditText(this);
		alert.setView(input);
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// TODO: make sure somehow data was transfered correctly?
				Editable value = input.getText();
				mTcpClientService.notifyKeyboardChar(value.toString());
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});
		AlertDialog dialog = alert.create();
		dialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		dialog.show();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int keyCode = event.getKeyCode();
		if (event.getAction() != KeyEvent.ACTION_DOWN) {
			// don't responde to actions other then Down
			return super.dispatchKeyEvent(event);
		}
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP:
			mTcpClientService.notifyVolumeUp();
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			mTcpClientService.notifyVolumeDown();
			return true;
		default:
			return super.dispatchKeyEvent(event);
		}
	}

	/**
	 * A simple pager adapter that represents 5 {@link ScreenSlidePageFragment}
	 * objects, in sequence.
	 */
	private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
		public ScreenSlidePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		private static final String TAG = "ScreenSlidePagerAdapter";

		@Override
		public Fragment getItem(int position) {
			Log.e(TAG, "going to position: " + Integer.toString(position));
			switch (position) {
			case 1:
				if (quickLaunch == null) {
					Log.d(TAG, "quick launch null");
					quickLaunch = new QuickLaunchActivity();
				}
				return (Fragment) quickLaunch;
			case 2:
				if (wifiScanner == null) {
					Log.d(TAG, "wifiScanner was null");
					wifiScanner = new ScanWifiActivity();
				}
				return (Fragment) wifiScanner;
			case 0:
			default:
				if (mouse == null) {
					Log.d(TAG, "mouse was null");
					mouse = new MousepadActivity();
				}
				return (Fragment) mouse;
			}
		}

		@Override
		public int getCount() {
			return 3;
		}
	}

	public PD40TcpClientService getTcpService() {
		synchronized (this) {
			return mTcpClientService;
		}
	}

	public PD40LocationService getLocationService() {
		synchronized (this) {
			return mLocationService;
		}
	}

	public void recievedGlassBrightness(int value) {
		VertSeekBar brightnessBar = (VertSeekBar) findViewById(R.id.brightness_bar);
		brightnessBar.setProgress((value * 100) / 255 + 1);
		runOnUiThread(new Runnable() { // only propose is to set the thumb on
										// the vertical seekbar
			public void run() {
				VertSeekBar brightnessBar = (VertSeekBar) findViewById(R.id.brightness_bar);
				brightnessBar.onSizeChanged(brightnessBar.getWidth(),
						brightnessBar.getHeight(), 0, 0);
			}
		});
	}

	public void recievedGlassBatteryStatus(int value) {
		final TextView batteryTextView = (TextView) findViewById(R.id.battery_status);
		final int tmpValue = value;
		runOnUiThread(new Runnable() {
			public void run() {
				if (tmpValue > 99) { // value range [0,100]
					batteryTextView.setText("100");
					return;
				}
				Integer.toString(tmpValue);
				batteryTextView.setText(Integer.toString(tmpValue) + "%");
			}
		});
	}

	public void clearGlassBatteryStatusOrState() {
		final TextView batteryTextView = (TextView) findViewById(R.id.battery_status);
		runOnUiThread(new Runnable() {
			public void run() {
				batteryTextView.setText("");
			}
		});
	}

	public void recievedGlassBatteryState(int value) {
		final TextView batteryTextView = (TextView) findViewById(R.id.battery_status);
		final int tmpValue = value;
		runOnUiThread(new Runnable() {
			public void run() {
				if (tmpValue == 1) {
					batteryTextView.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
					return;
				}
				batteryTextView.setPaintFlags(Paint.LINEAR_TEXT_FLAG);
			}
		});
	}

	public String getServerIp() {
		return mTcpClientService.serverIp();
	}

	public void getGlassBrightness() {
		try {
			if (mTcpClientService != null)
				mTcpClientService.notifyBrightnessGet();
		} catch (Exception e) {
			Log.e(TAG, "Error: " + e.toString(), e);
			e.printStackTrace();
		}
	}

	public void setBatteryOnLongClickListener() {
		final TextView batteryTextView = (TextView) findViewById(R.id.battery_status);
		batteryTextView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				mPager.setCurrentItem(2);
				return false;
			}
		});
	}

	public void getGlassBatteryStatus() {
		try {
			if (mTcpClientService != null)
				mTcpClientService.notifyBatteryGet();
		} catch (Exception e) {
			Log.e(TAG, "Error: " + e.toString(), e);
			e.printStackTrace();
		}
	}

	public void setGlassBrightness(int value) {
		// TODO: this will be called by the seekBar.onChangeListener
		int brightnessValue = (255 * value) / 100;
		try {
			mTcpClientService.notifyBrightnessSet(brightnessValue);
		} catch (Exception e) {
			Log.e(TAG, "Error: " + e.toString(), e);
			e.printStackTrace();
		} finally {
		}
		return;
	}
}

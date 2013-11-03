package com.tandemg.scratchpad;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.tandemg.scratchpad.communications.PD40TcpClientService;
import com.tandemg.scratchpad.communications.PD40TcpClientService.PD40TcpClientServiceBinder;
import com.tandemg.scratchpad.location.PD40LocationService;
import com.tandemg.scratchpad.location.PD40LocationService.PD40LocationServiceBinder;

public class ScratchpadActivity extends FragmentActivity {

	private static final String TAG = "ScratchpadActivity";
	private static final int brightnessTimeout = 5000;
	private Fragment touch = new TouchpadActivity();
	private Fragment mouse = new MousepadActivity();

	private boolean mTcpServiceBound = false;
	private ServiceConnection mTcpClientConnection = null;
	private PD40TcpClientService mTcpClientService = null;
	private boolean mLocationServiceBound = false;
	private ServiceConnection mLocationConnection = null;
	private PD40LocationService mLocationService = null;
	public DataHandler handler = null;
	private Thread brightnessDeamonThread = null;
	/**
	 * The pager widget, which handles animation and allows swiping horizontally
	 * to access previous and next wizard steps.
	 */
	private ViewPager mPager;

	/**
	 * The pager adapter, which provides the pages to the view pager widget.
	 */
	private PagerAdapter mPagerAdapter;

	public ScratchpadActivity() {
		handler = new DataHandler();
		mLocationConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.v(TAG, "Location service connected");
				PD40LocationServiceBinder binder = (PD40LocationServiceBinder) service;
				mLocationService = binder.getService();
				mLocationServiceBound = true;
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.v(TAG, "Location service disconnected");
				mLocationServiceBound = false;
				mLocationService = null;
			}
		};
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
			switch (temp[0].charAt(0)) {
			case 'G':
				recievedGlassBrightness(Integer.parseInt(temp[1]));
				break;
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scratchpad);

		Intent intent;

		intent = new Intent(this, PD40LocationService.class);
		bindService(intent, mLocationConnection, Context.BIND_AUTO_CREATE);

		intent = new Intent(this, PD40TcpClientService.class);
		bindService(intent, mTcpClientConnection, Context.BIND_AUTO_CREATE);

		// Show the Up button in the action bar.
		setupActionBar();

		// keep screen on while app is running
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Instantiate a ViewPager and a PagerAdapter.
		mPager = (ViewPager) findViewById(R.id.pager);
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
						Thread.sleep(brightnessTimeout, 0);
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
		Log.d(TAG, "start");
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

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.activity_screen_slide, menu);

		menu.findItem(R.id.action_previous).setEnabled(
				mPager.getCurrentItem() > 0);
		menu.findItem(R.id.action_next).setEnabled(
				(mPager.getCurrentItem() != mPagerAdapter.getCount() - 1));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_previous) {
			// Go to the previous step in the wizard. If there is no previous
			// step, setCurrentItem will do nothing.
			mPager.setCurrentItem(mPager.getCurrentItem() - 1);
			mTcpClientService.setIconConnected(R.drawable.ic_stat_mouse);
			return true;
		} else if (item.getItemId() == R.id.action_next) {
			// Advance to the next step in the wizard. If there is no next step,
			// setCurrentItem will do nothing.
			mPager.setCurrentItem(mPager.getCurrentItem() + 1);
			mTcpClientService.setIconConnected(R.drawable.ic_stat_droid);
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
			if (position == 0) {
				if (mouse == null) {
					Log.d(TAG, "mouse was null");
					mouse = new MousepadActivity();
				}
				return (Fragment) mouse;
			} else {
				if (touch == null) {
					Log.d(TAG, "touch was null");
					touch = new TouchpadActivity();
				}
				return (Fragment) touch;
			}
		}

		@Override
		public int getCount() {
			return 2;
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

	public void getGlassBrightness() {
		// TODO: in this ctx the scratchpad seekBar will be updated
		try {
			if (mTcpClientService != null)	mTcpClientService.notifyBrightnessGet();
		} catch (Exception e) {
			Log.e(TAG, "Error: " + e.toString(), e);
			e.printStackTrace();
		}
	}

	public void setGlassBrightness(int value) {
		// TODO: this will be called by the seekBar.onChangeListener
		int brightnessValue = (255 * value) / 100;
		if (brightnessValue == 255)
			brightnessValue--;
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
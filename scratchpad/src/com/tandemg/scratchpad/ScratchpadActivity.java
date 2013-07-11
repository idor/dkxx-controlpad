package com.tandemg.scratchpad;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.tandemg.scratchpad.communications.PD40TcpClientService;
import com.tandemg.scratchpad.communications.TCPClient;

public class ScratchpadActivity extends FragmentActivity {

	private static final String TAG = "ScratchpadActivity";

	private Fragment touch = new TouchpadActivity();
	private Fragment mouse = new MousepadActivity();

	public ScratchpadActivity() {
		Log.v(TAG, "ScratchpadActivity object created");
	}

	/**
	 * The pager widget, which handles animation and allows swiping horizontally
	 * to access previous and next wizard steps.
	 */
	private ViewPager mPager;

	/**
	 * The pager adapter, which provides the pages to the view pager widget.
	 */
	private PagerAdapter mPagerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scratchpad);
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
				// are dependent
				// on which page is currently active. An alternative approach is
				// to have each
				// fragment expose actions itself (rather than the activity
				// exposing actions),
				// but for simplicity, the activity provides the actions in this
				// sample.
				invalidateOptionsMenu();
			}
		});
		startService(new Intent(ScratchpadActivity.this,
				PD40TcpClientService.class));
	}

	@Override
	public void onDestroy() {
		stopService(new Intent(ScratchpadActivity.this,
				PD40TcpClientService.class));
		super.onDestroy();
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

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
		switch (item.getItemId()) {
		/*
		 * case android.R.id.home: // Navigate "up" the demo structure to the
		 * launchpad activity. // See
		 * http://developer.android.com/design/patterns/navigation.html // for
		 * more. NavUtils.navigateUpTo(this, new Intent(this,
		 * MainActivity.class)); return true;
		 */

		case R.id.action_previous:
			// Go to the previous step in the wizard. If there is no previous
			// step,
			// setCurrentItem will do nothing.
			mPager.setCurrentItem(mPager.getCurrentItem() - 1);
			return true;

		case R.id.action_next:
			// Advance to the next step in the wizard. If there is no next step,
			// setCurrentItem
			// will do nothing.
			mPager.setCurrentItem(mPager.getCurrentItem() + 1);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Log.d(TAG, "orientation changed to LANDSCAPE ("
					+ newConfig.orientation + ")");
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			Log.d(TAG, "orientation changed to PORTRAIT ("
					+ newConfig.orientation + ")");
		}
	}

	public void onClick_Back(View v) {
		try {
			TCPClient.getInstance().notifyBack();
		} catch (Exception e) {
			Log.e(TAG, "Error: " + e.toString(), e);
			e.printStackTrace();
		} finally {
		}
	}

	public void onClick_Home(View v) {
		try {
			TCPClient.getInstance().notifyHome();
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
			Log.d(TAG, "get item #" + String.valueOf(position));
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
}

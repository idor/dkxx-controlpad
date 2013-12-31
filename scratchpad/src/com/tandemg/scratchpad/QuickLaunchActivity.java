package com.tandemg.scratchpad;

import java.util.Vector;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class QuickLaunchActivity extends Fragment {
	private static final String TAG = "QuickLaunchActivity";

	private Vector<String[]> intentStrings;
	private ViewGroup rootView;
	private SharedPreferences pref;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		OnClickListener mClickListener = new OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onQuickLauncherClick(v);
			}
		};
		intentStrings = new Vector<String[]>();
		rootView = (ViewGroup) inflater.inflate(R.layout.activity_quicklaunch,
				container, false);

		pref = this.getActivity().getSharedPreferences("quick_launcher_config",
				Context.MODE_WORLD_WRITEABLE);

		for (int x = 0; x < 5; x++) { // in future, to insert default values,
										// remove this for, and call each
										// 'getString separetly'
			intentStrings.add(pref.getString(Integer.toString(x), "")
					.split(","));
		}
		Button b = (Button) rootView.findViewById(R.id.quickLauncherButton0);
		b.setText(intentStrings.get(0)[0]);
		b.setOnClickListener(mClickListener);

		b = (Button) rootView.findViewById(R.id.quickLauncherButton1);
		b.setText(intentStrings.get(1)[0]);
		b.setOnClickListener(mClickListener);

		b = (Button) rootView.findViewById(R.id.quickLauncherButton2);
		b.setText(intentStrings.get(2)[0]);
		b.setOnClickListener(mClickListener);

		b = (Button) rootView.findViewById(R.id.quickLauncherButton3);
		b.setText(intentStrings.get(3)[0]);
		b.setOnClickListener(mClickListener);

		b = (Button) rootView.findViewById(R.id.quickLauncherButton4);
		b.setText(intentStrings.get(4)[0]);
		b.setOnClickListener(mClickListener);
		return rootView;
	}

	public void onQuickLauncherClick(View view) { // should improve input
													// validation, strings are
													// dangerous
		String[] tmp = null;
		if (view.getId() == R.id.quickLauncherButton0) {
			tmp = intentStrings.get(0);
		} else if (view.getId() == R.id.quickLauncherButton1) {
			tmp = intentStrings.get(1);
		} else if (view.getId() == R.id.quickLauncherButton2) {
			tmp = intentStrings.get(2);
		} else if (view.getId() == R.id.quickLauncherButton3) {
			tmp = intentStrings.get(3);
		} else if (view.getId() == R.id.quickLauncherButton4) {
			tmp = intentStrings.get(4);
		}
		if (tmp != null) {
			((ScratchpadActivity) getActivity()).getTcpService()
					.notifyStartIntent(tmp[1], tmp[2], tmp[3]);
			Log.e(TAG, "Intent string was empty, no app was launched");
		} else {
			Log.e(TAG, "Intent Launched. Packege: " + tmp[1] + ", Activity: "
					+ tmp[2] + ", Action: " + tmp[3]);
		}

	}
}

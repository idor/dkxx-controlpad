package com.tandemg.scratchpad;

import java.util.Vector;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

public class QuickLaunchActivity extends Fragment {
	private static final String TAG = "QuickLaunchActivity";

	private Vector<String[]> intentStrings;
	private ViewGroup rootView;

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

		SharedPreferences pref;
		pref = this.getActivity().getSharedPreferences("quick_launcher_config",
				Context.MODE_WORLD_WRITEABLE);

		// Text,Packege,Activity,Action
		SharedPreferences.Editor prefEditor = pref.edit();

		String tmp = "ADE-P1,PIC1,com.example.hellopanoramagl,MainActivity";
		intentStrings.add(pref.getString("1", tmp).split(","));
		prefEditor.putString("1", pref.getString("1", tmp)); // will generate
																// the file
																// template on
																// first run
		tmp = "ADE-P2,PIC2,com.example.hellopanoramagl,MainActivity";
		intentStrings.add(pref.getString("2", tmp).split(","));

		tmp = "ADE-P3,PIC3,com.example.hellopanoramagl,MainActivity";
		intentStrings.add(pref.getString("3", tmp).split(","));

		tmp = "Lumus Demo,ACTION_MAIN,com.tandemg.pd40demo,MainActivity";
		intentStrings.add(pref.getString("4", tmp).split(","));

		tmp = "Image Capture,android.media.action.IMAGE_CAPTURE, , ";
		intentStrings.add(pref.getString("5", tmp).split(","));

		tmp = "GyroCompass,ACTION_MAIN,fi.finwe.gyrocompass,Compass";
		intentStrings.add(pref.getString("6", tmp).split(","));

		tmp = "DynamicArrow,ACTION_MAIN,com.example.dynamicarrows,MainActivity";
		intentStrings.add(pref.getString("7", tmp).split(","));

		tmp = "Gestures client,ACTION_MAIN,eyesight.service.client,EyeSightClientActivity";
		intentStrings.add(pref.getString("8", tmp).split(","));

		tmp = "Gestures Demo,ACTION_MAIN,air.ESGlassesCallnderanimation05,AppEntry";
		intentStrings.add(pref.getString("9", tmp).split(","));
		prefEditor.commit();

		// bind Buttons to text and clickListeners.
		Button b = (Button) rootView.findViewById(R.id.quickLauncherButtonADE1);
		b.setText(intentStrings.get(0)[0]);
		b.setOnClickListener(mClickListener);
		// bind Buttons to text and clickListeners.
		b = (Button) rootView.findViewById(R.id.quickLauncherButtonADE2);
		b.setText(intentStrings.get(1)[0]);
		b.setOnClickListener(mClickListener);
		// bind Buttons to text and clickListeners.
		b = (Button) rootView.findViewById(R.id.quickLauncherButtonADE3);
		b.setText(intentStrings.get(2)[0]);
		b.setOnClickListener(mClickListener);

		b = (Button) rootView.findViewById(R.id.quickLauncherButton1);
		b.setText(intentStrings.get(3)[0]);
		b.setOnClickListener(mClickListener);

		b = (Button) rootView.findViewById(R.id.quickLauncherButton2);
		b.setText(intentStrings.get(4)[0]);
		b.setOnClickListener(mClickListener);

		b = (Button) rootView.findViewById(R.id.quickLauncherButton3);
		b.setText(intentStrings.get(5)[0]);
		b.setOnClickListener(mClickListener);

		b = (Button) rootView.findViewById(R.id.quickLauncherButton4);
		b.setText(intentStrings.get(6)[0]);
		b.setOnClickListener(mClickListener);

		b = (Button) rootView.findViewById(R.id.quickLauncherButton5);
		b.setText(intentStrings.get(7)[0]);
		b.setOnClickListener(mClickListener);

		b = (Button) rootView.findViewById(R.id.quickLauncherButton6);
		b.setText(intentStrings.get(8)[0]);
		b.setOnClickListener(mClickListener);

		/* set shutdown button functionality */
		Button btn = (Button) rootView.findViewById(R.id.quickLauncherShutdown);
		btn.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					Log.d("Pressed", "Button pressed");
					((ScratchpadActivity) getActivity()).getTcpService()
							.notifyPowerButtonDown();
					return true;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					Log.d("Released", "Button released");
					((ScratchpadActivity) getActivity()).getTcpService()
							.notifyPowerButtonUp();
					return true;
				}
				return false;
			}
		});
		return rootView;
	}

	public void onQuickLauncherClick(View view) { // should improve input
													// validation, strings are
													// dangerous
		String[] tmp = null;
		if (view.getId() == R.id.quickLauncherButtonADE1) {
			tmp = intentStrings.get(0);
		} else if (view.getId() == R.id.quickLauncherButtonADE2) {
			tmp = intentStrings.get(1);
		} else if (view.getId() == R.id.quickLauncherButtonADE3) {
			tmp = intentStrings.get(2);
		} else if (view.getId() == R.id.quickLauncherButton1) {
			tmp = intentStrings.get(3);
		} else if (view.getId() == R.id.quickLauncherButton2) {
			tmp = intentStrings.get(4);
		} else if (view.getId() == R.id.quickLauncherButton3) {
			tmp = intentStrings.get(5);
		} else if (view.getId() == R.id.quickLauncherButton4) {
			tmp = intentStrings.get(6);
		} else if (view.getId() == R.id.quickLauncherButton5) {
			tmp = intentStrings.get(7);
		} else if (view.getId() == R.id.quickLauncherButton6) {
			tmp = intentStrings.get(8);
		}
		if (tmp != null) {
			((ScratchpadActivity) getActivity()).getTcpService()
					.notifyStartIntent(tmp[1], tmp[2], tmp[3]);
			Log.i(TAG, "Intent Launched. Action: " + tmp[1] + ", Packege: "
					+ tmp[2] + ", Activity: " + tmp[3]);
		} else {
			Log.e(TAG, "Intent string was empty, no app was launched");

		}

	}
}

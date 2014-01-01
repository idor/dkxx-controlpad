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
import android.widget.EditText;
import android.widget.Toast;

public class TechnicianActivity extends Fragment {
	private static final String TAG = "TechnicianActivity";

	private Vector<String[]> intentStrings;
	private ViewGroup rootView;
	SharedPreferences pref;
	SharedPreferences.Editor prefEditor;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		intentStrings = new Vector<String[]>();
		rootView = (ViewGroup) inflater.inflate(R.layout.activity_technician,
				container, false);

		bindButtonsToConfigFile();

		return rootView;
	}

	private void bindButtonsToConfigFile() {
		OnClickListener mClickListener = new OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onQuickLauncherClick(v);
			}
		};

		pref = this.getActivity().getSharedPreferences("quick_launcher_config",
				Context.MODE_WORLD_WRITEABLE);
		// Text,Packege,Activity,Action
		prefEditor = pref.edit();

		String tmp = "ADE,ACTION_MAIN,com.example.hellopanoramagl,MainActivity";
		intentStrings.add(pref.getString("0", tmp).split(","));
		prefEditor.putString("0", pref.getString("0", tmp)); // will generate
																// the file
																// template on
																// first run

		tmp = "Lumus Demo,ACTION_MAIN,com.tandemg.pd40demo,MainActivity";
		intentStrings.add(pref.getString("1", tmp).split(","));

		tmp = "Image Capture,android.media.action.IMAGE_CAPTURE, , ";
		intentStrings.add(pref.getString("2", tmp).split(","));

		tmp = "GyroCompass,ACTION_MAIN,fi.finwe.gyrocompass,Compass";
		intentStrings.add(pref.getString("3", tmp).split(","));

		tmp = "Skype,ACTION_MAIN,com.skype.raider,Main";
		intentStrings.add(pref.getString("4", tmp).split(","));
		prefEditor.commit();

		// bind Buttons to text and clickListeners.
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
	}

	public void onQuickLauncherClick(View view) {
		Vector<String> intentFieldsVector = new Vector<String>();
		intentFieldsVector.add(((EditText) rootView
				.findViewById(R.id.editQuicklaunchText)).getEditableText()
				.toString());
		intentFieldsVector.add(((EditText) rootView
				.findViewById(R.id.editQuicklaunchAction)).getEditableText()
				.toString());
		intentFieldsVector.add(((EditText) rootView
				.findViewById(R.id.editQuicklaunchPackage)).getEditableText()
				.toString());
		intentFieldsVector.add(((EditText) rootView
				.findViewById(R.id.editQuicklaunchActivityName))
				.getEditableText().toString());

		String buttonId;
		switch (view.getId()) {
		case R.id.quickLauncherButton0:
			buttonId = "0";
			break;
		case R.id.quickLauncherButton1:
			buttonId = "1";
			break;
		case R.id.quickLauncherButton2:
			buttonId = "2";
			break;
		case R.id.quickLauncherButton3:
			buttonId = "3";
			break;
		case R.id.quickLauncherButton4:
		default:
			buttonId = "4";
		}

		if (!intentFieldsVector.elementAt(0).isEmpty()) {
			if (!intentFieldsVector.elementAt(1).isEmpty()) {
				prefEditor.putString(buttonId, intentFieldsVector.elementAt(0)
						+ "," + intentFieldsVector.elementAt(1) + ","
						+ intentFieldsVector.elementAt(2) + ","
						+ intentFieldsVector.elementAt(3));
				prefEditor.commit();
				bindButtonsToConfigFile();
			} else {
				Toast.makeText(this.getActivity(),
						"Action is missing - Aborting", Toast.LENGTH_SHORT)
						.show();
				Log.i(TAG, "Action is missing while assigning Button #"
						+ buttonId);
			}
		} else {
			Toast.makeText(this.getActivity(), "Label is missing - Aborting",
					Toast.LENGTH_SHORT).show();
			Log.i(TAG, "Label is missing while assigning Button #" + buttonId);
		}

	}
}

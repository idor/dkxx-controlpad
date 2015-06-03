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
import android.view.View.OnLongClickListener;
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
		final Context ctx = this.getActivity();
		OnLongClickListener mLongClickListener = new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				onQuickLauncherLongClick(v);
				return true;
			}
		};
		OnClickListener mClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(ctx, "Hold Key to bind action", Toast.LENGTH_SHORT).show();
			}
		};

		pref = this.getActivity().getSharedPreferences("quick_launcher_config",
				Context.MODE_WORLD_WRITEABLE);
		// Text,Packege,Activity,Action
		prefEditor = pref.edit();

		String tmp = "Lumus Demo,ACTION_MAIN,com.tandemg.pd40demo,MainActivity";
		intentStrings.add(pref.getString("4", tmp).split(","));

		tmp = "Image Capture,android.media.action.IMAGE_CAPTURE, , ";
		intentStrings.add(pref.getString("5", tmp).split(","));

		tmp = "GyroCompass,ACTION_MAIN,fi.finwe.gyrocompass,Compass";
		intentStrings.add(pref.getString("6", tmp).split(","));

		tmp = "DynamicArrow,ACTION_MAIN,com.example.dynamicarrows,MainActivity";
		intentStrings.add(pref.getString("7", tmp).split(","));
		prefEditor.commit();

		// bind Buttons to text and clickListeners.
		Button b = (Button) rootView.findViewById(R.id.quickLauncherButton1);
		b.setText(intentStrings.get(0)[0]);
		b.setOnClickListener(mClickListener);
		b.setOnLongClickListener(mLongClickListener);

		b = (Button) rootView.findViewById(R.id.quickLauncherButton2);
		b.setText(intentStrings.get(1)[0]);
		b.setOnClickListener(mClickListener);
		b.setOnLongClickListener(mLongClickListener);

		b = (Button) rootView.findViewById(R.id.quickLauncherButton3);
		b.setText(intentStrings.get(2)[0]);
		b.setOnClickListener(mClickListener);
		b.setOnLongClickListener(mLongClickListener);

		b = (Button) rootView.findViewById(R.id.quickLauncherButton4);
		b.setText(intentStrings.get(3)[0]);
		b.setOnClickListener(mClickListener);
		b.setOnLongClickListener(mLongClickListener);
	}

	public void onQuickLauncherLongClick(View view) {
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
		case R.id.quickLauncherButton1:
			buttonId = "4";
			break;
		case R.id.quickLauncherButton2:
			buttonId = "5";
			break;
		case R.id.quickLauncherButton3:
			buttonId = "6";
			break;
		case R.id.quickLauncherButton4:
		default:
			buttonId = "7";
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
						"Action is missing", Toast.LENGTH_SHORT)
						.show();
				Log.i(TAG, "Action is missing while assigning Button #"
						+ buttonId);
			}
		} else {
			Toast.makeText(this.getActivity(), "Label is missing",
					Toast.LENGTH_SHORT).show();
			Log.i(TAG, "Label is missing while assigning Button #" + buttonId);
		}

	}
}

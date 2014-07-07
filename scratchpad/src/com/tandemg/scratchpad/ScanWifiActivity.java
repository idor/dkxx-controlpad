package com.tandemg.scratchpad;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Vector;

import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;
import com.tandemg.scratchpad.communications.NsdHelper;

import android.app.ListFragment;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ScanWifiActivity extends ListFragment {
	NsdHelper mNsdHelper = null;
	ViewGroup viewGroup = null;
	private boolean mIsDiscoveryRunning = false;
	protected BaseAdapter mListAdapter = null;
	private Vector<NsdServiceInfo> mNsdList = null;
	static final String TAG = "ScanWifiActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mNsdHelper = new NsdHelper(this.getActivity());
		initializeListAdapter();
		this.setListAdapter(mListAdapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LayoutInflater inf = (LayoutInflater) getActivity().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		viewGroup = (ViewGroup) inf.inflate(R.layout.activity_scan_wifi, null);
		viewGroup.findViewById(R.id.button_rescan).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						restartNsdDiscovery(v);
					}
				});
		return viewGroup;
	}

	@Override
	public void onResume() {
		super.onResume();
		listUpdateNotify();
	}

	@Override
	public void onPause() {
		super.onPause();
		this.stopNsdDiscovery();
	}

	private void initializeListAdapter() {
		if (mListAdapter != null) {
			Log.d(TAG, "ListAdapter already exist, not touching");
		} else {
			mNsdList = new Vector<NsdServiceInfo>();
			mListAdapter = new wifiBaseAdapter();
		}
	}

	final static String SERVICE_TYPE = "_pd40._tcp.";
	NsdManager mNsdManager = null;
	NsdManager.DiscoveryListener mDiscoveryListener = null;
	NsdManager.ResolveListener mResolveListener = null;
	NsdServiceInfo mService = null;

	protected NsdManager.DiscoveryListener initializeDiscoveryListener() {
		// Instantiate a new DiscoveryListener
		mDiscoveryListener = mDiscoveryListener != null ? mDiscoveryListener
				: new NsdManager.DiscoveryListener() {

					// Called as soon as service discovery begins.
					@Override
					public void onDiscoveryStarted(String regType) {
						mIsDiscoveryRunning = true;
						Log.d(TAG, "Service discovery started");
					}

					@Override
					public void onServiceFound(NsdServiceInfo service) {
						try {
							// A service was found! Do something with it.
							Log.d(TAG, "Service discovery success: " + service);
							if (!service.getServiceType().equals(SERVICE_TYPE)) {
								// Service type is the string containing the
								// protocol and
								// transport layer for this service.
								Log.d(TAG,
										"Unknown Service Type: "
												+ service.getServiceType());
							} else if (service.getServiceName()
									.contains("DK40")) {
								mNsdHelper.resolveService(service);
							}
						} catch (Exception e) {
							Log.e(TAG, e.getMessage());
						}
					}

					@Override
					public void onServiceLost(NsdServiceInfo service) {
						// When the network service is no longer available.
						// Internal bookkeeping code goes here.
						Log.e(TAG, "SSSSSSSSSSSSsservice lost" + service);
						removeItemsFromList(service);
					}

					@Override
					public void onDiscoveryStopped(String serviceType) {
						mIsDiscoveryRunning = false;
						Log.i(TAG, "Discovery stopped: " + serviceType);
					}

					@Override
					public void onStartDiscoveryFailed(String serviceType,
							int errorCode) {
						Log.e(TAG, "Discovery failed: Error code:" + errorCode);
						mNsdManager.stopServiceDiscovery(this);
					}

					@Override
					public void onStopDiscoveryFailed(String serviceType,
							int errorCode) {
						Log.e(TAG, "Discovery failed: Error code:" + errorCode);
						mNsdManager.stopServiceDiscovery(this);
					}
				};
		return mDiscoveryListener;
	}

	protected NsdManager.ResolveListener initializeResolveListener() {
		mResolveListener = mResolveListener != null ? mResolveListener
				: new NsdManager.ResolveListener() {
					@Override
					public void onResolveFailed(NsdServiceInfo serviceInfo,
							int errorCode) {
						// Called when the resolve fails. Use the error code to
						// debug.
						Log.e(TAG, "Resolve failed" + errorCode);
					}

					@Override
					public void onServiceResolved(NsdServiceInfo serviceInfo) {
						Log.e(TAG, "Resolve Succeeded. " + serviceInfo);
						mService = serviceInfo;
						InetAddress host = mService.getHost();
						int port = mService.getPort();
						// addItemsToList(serviceInfo);
						new PingAsyncTask().execute(serviceInfo);
					}
				};
		return mResolveListener;
	}

	private void restartNsdDiscovery(View v) {
		Iterator<NsdServiceInfo> iter = mNsdList.iterator();
		while (iter.hasNext()) {
			NsdServiceInfo tmpService = iter.next();
			new PingAsyncTask().execute(tmpService); // add or remove accaceble
														// nodes.
		}
		listUpdateNotify();
		// Log.d(TAG, "You clicked on re-scan while scanning :)");
		stopNsdDiscovery();
		startNsdDiscovery();
	}

	private void listUpdateNotify() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mListAdapter.notifyDataSetChanged();
				if (mNsdList.isEmpty()) {
					viewGroup.findViewById(R.id.no_devices_found)
							.setVisibility(View.VISIBLE);
				} else {
					viewGroup.findViewById(R.id.no_devices_found)
							.setVisibility(View.GONE);
				}

			}
		});
	}

	private void startNsdDiscovery() {
		// mNsdHelper.is
		mNsdHelper.initializeNsd();
		mNsdHelper.setDiscoveryListener(initializeDiscoveryListener());
		mNsdHelper.setResolveListener(initializeResolveListener());
		mNsdHelper.discoverServices();
	}

	private void stopNsdDiscovery() {
		try {
			mNsdHelper.stopDiscovery();
		} catch (java.lang.IllegalArgumentException e) {
		}
	}

	private class PingAsyncTask extends
			AsyncTask<NsdServiceInfo, Integer, Boolean> {

		NsdServiceInfo mNsdServiveInfo;

		@Override
		protected Boolean doInBackground(NsdServiceInfo... nsi) {
			int count = nsi.length;
			if (count != 1) {
				Log.e(TAG, "found more then one NsdServiceInfo, dropping");
			}
			mNsdServiveInfo = nsi[0];
			InetAddress tmpIP = mNsdServiveInfo.getHost();
			boolean ret;
			try {
				ret = tmpIP.isReachable(10000);
				return ret;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				addItemsToList(mNsdServiveInfo);
			} else {
				removeItemsFromList(mNsdServiveInfo);
			}
		}
	}

	protected void addItemsToList(final NsdServiceInfo serviceInfo) {
		// this runs on UI thread using pingAsyncTask class

		/*
		 * for (NsdServiceInfo si:mNsdList) { if
		 * (si.getServiceName().equals(serviceInfo.getServiceName())) { // don't
		 * add this item to list return; } }
		 */

		Iterator<NsdServiceInfo> iter = mNsdList.iterator();
		while (iter.hasNext()) {
			NsdServiceInfo tmpService = iter.next();
			if (tmpService.getHost().equals(serviceInfo.getHost())) {
				iter.remove();
			}
		}
		mNsdList.add(serviceInfo);
		listUpdateNotify();
	}

	protected void removeItemsFromList(final NsdServiceInfo serviceInfo) {
		Iterator<NsdServiceInfo> iter = mNsdList.iterator();
		while (iter.hasNext()) {
			NsdServiceInfo tmpService = iter.next();
			if (tmpService.getHost().equals(serviceInfo.getHost())) {
				iter.remove();
			}
		}
		listUpdateNotify();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// go to list, get the one on place position, and connect to it's ip.
		Toast.makeText(getActivity(), ((TextView) v).getText(), 1).show();
		mListAdapter.getItemId(position);
	}

	public class wifiBaseAdapter extends BaseAdapter {
		@Override
		public long getItemId(int position) {
			return 1;
		}

		@Override
		public Object getItem(int position) {
			return mNsdList.get(position);
		}

		@Override
		public int getCount() {
			return mNsdList.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			final NsdServiceInfo item = mNsdList.get(position);
			if (v == null) {
				LayoutInflater li = (LayoutInflater) getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = li.inflate(R.layout.generic_list_item, null);
			}
			if (item != null) {
				TextView textView = (TextView) v;
				if (textView != null) {
					textView.setText(item.getServiceName().replace("-", " - "));
					textView.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							updatePd40DeviceIp(item);
						}
					});
				}
			}
			return v;
		}
	}

	public void updatePd40DeviceIp(NsdServiceInfo info) {
		String ip = info.getHost().toString().substring(1);
		Toast.makeText(getActivity(), "Chosen device IP: " + ip, 0).show();
		((ScratchpadActivity) getActivity()).getTcpService()
				.restartServiceThreadWithIp(ip);
	}
}
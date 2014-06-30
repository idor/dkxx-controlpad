/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tandemg.scratchpad.communications;

import java.io.IOException;
import java.net.ServerSocket;

import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.net.nsd.NsdManager;
import android.util.Log;

public class NsdHelper {

	Context mContext;

	public NsdManager mNsdManager;
	NsdManager.ResolveListener mResolveListener;
	NsdManager.DiscoveryListener mDiscoveryListener;
	NsdManager.RegistrationListener mRegistrationListener;
	public static final String TAG = "NsdHelper";
	public static final String SERVICE_TYPE = "_pd40._tcp.";
	public String mServiceName = "TBD";

	public NsdHelper(Context context) {
		mContext = context;
		mNsdManager = (NsdManager) context
				.getSystemService(Context.NSD_SERVICE);
	}

	public void initializeNsd() {
	}

	public void setResolveListener(NsdManager.ResolveListener l) {
		mResolveListener = l;
	}

	public void setDiscoveryListener(NsdManager.DiscoveryListener l) {
		mDiscoveryListener = l;
	}

	public void resolveService(NsdServiceInfo info) {
		mNsdManager.resolveService(info, mResolveListener);
	}

	public void registerService() {
		try {
			this.registerService((new ServerSocket(0)).getLocalPort());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void registerService(int port) {
		NsdServiceInfo serviceInfo = new NsdServiceInfo();
		serviceInfo.setPort(port);
		serviceInfo.setServiceName(mServiceName);
		serviceInfo.setServiceType(SERVICE_TYPE);
		mNsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD,
				mRegistrationListener);

	}

	public void discoverServices() {
		mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD,
				mDiscoveryListener);
	}

	public void stopDiscovery() {
		mNsdManager.stopServiceDiscovery(mDiscoveryListener);
	}

	public void tearDown() {
		mNsdManager.unregisterService(mRegistrationListener);
	}
}

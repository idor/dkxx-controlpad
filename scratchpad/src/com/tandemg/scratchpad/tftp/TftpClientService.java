package com.tandemg.scratchpad.tftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.net.tftp.TFTP;
import org.apache.commons.net.tftp.TFTPClient;

import com.tandemg.scratchpad.ScratchpadActivity;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class TftpClientService extends IntentService {

	private final static String TAG = "TftpClientService";
	private static final int SERVER_DEFAULT_PORT = 1069;

	public TftpClientService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "onHandleIntent");
		Log.d(TAG, intent.getAction());
		sendFile(intent);
	}

	public void sendFile(Intent intent) {
		FileInputStream input = null;
		int transferMode = TFTP.BINARY_MODE;
		String hostname = intent.getStringExtra("serverHostName");
		String localFilename = intent.getStringExtra("localFileName");
		String remoteFilename = intent.getStringExtra("remoteFileName");
		TFTPClient tftpClient = new TFTPClient();

		Log.d(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());

		// Try to open local file for reading
		try {
			input = new FileInputStream(localFilename);
		} catch (IOException e) {
			tftpClient.close();
			Log.e(TAG, "Error: could not open local file for reading.");
			Log.e(TAG, e.getMessage());
			return;
		}

		// Try to send local file via TFTP
		try {
			tftpClient.open(SERVER_DEFAULT_PORT);
			tftpClient.sendFile(remoteFilename, transferMode, input,
					InetAddress.getByName(hostname), SERVER_DEFAULT_PORT);
		} catch (UnknownHostException e) {
			Log.e(TAG, "Error: could not resolve hostname.");
			Log.e(TAG, e.getMessage());
			return;
		} catch (IOException e) {
			System.err
					.println("Error: I/O exception occurred while sending file.");
			Log.e(TAG, e.getMessage());
			return;
		} finally {
			// Close local socket and input file
			tftpClient.close();
			try {
				if (input != null) {
					input.close();
					Log.d(TAG, "file sent successfully");
				}
			} catch (IOException e) {
				Log.e(TAG, "Error: error closing file.");
				Log.e(TAG, e.getMessage());
			}
		}
	}

	public void receiveFile(Intent intent) {
		/*
		 * flow: start a socket using the port from the intent data, receive a
		 * file. save it quit.
		 */
		int transferMode = TFTP.BINARY_MODE;
		String hostname = intent.getStringExtra("name"); // TODO: tbd
		String localFilename = intent.getStringExtra("name"); // TODO: tbd
		String remoteFilename = intent.getStringExtra("name"); // TODO: tbd
		TFTPClient tftpClient = new TFTPClient();
		FileOutputStream output = null;

		File file = new File(localFilename);
		if (file.exists()) {
			Log.e(TAG, "Error: " + localFilename
					+ " already exists. not overriting");
			return;
		}

		// Try to open local file for writing
		try {
			output = new FileOutputStream(file);
		} catch (IOException e) {
			tftpClient.close();
			Log.e(TAG, "Error: could not open local file for writing.");
			Log.e(TAG, e.getMessage());
		}
		// Try to receive remote file via TFTP
		try {
			tftpClient.receiveFile(remoteFilename, transferMode, output,
					hostname);
		} catch (UnknownHostException e) {
			Log.e(TAG, "Error: could not resolve hostname.");
			// Log.e(TAG,e.getMessage());
			return;
		} catch (IOException e) {
			System.err
					.println("Error: I/O exception occurred while receiving file.");
			Log.e(TAG, e.getMessage());
			return;
		} finally {
			// Close local socket and output file
			tftpClient.close();
			try {
				if (output != null) {
					output.close();
				}
			} catch (IOException e) {
				Log.e(TAG, "Error: error closing file.");
				Log.e(TAG, e.getMessage());
			}
		}
	}

}
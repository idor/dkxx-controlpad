package com.tandemg.scratchpad;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.CharBuffer;

import android.util.Log;

public class TCPClient {

	private String serverMessage;

	public static final String SERVERIP = "192.168.43.140";
	public static final int SERVERPORT = 2301;
	private Socket mSocket = null;
	private OnMessageReceived mMessageListener = null;
	private boolean mRun = false;

//	private PrintWriter out;
//	private BufferedReader in;
	private OutputStreamWriter out;
	private InputStreamReader in;

	private static final String TAG = "TCPClient";
 
	/**
	 *  Constructor of the class. OnMessagedReceived listens for the messages received from server
	 */
	public TCPClient(OnMessageReceived listener) {
		mMessageListener = listener;
	}
 
	/**
	 * Sends the message entered by client to the server
	 * @param message text entered by client
	 */
    public void sendMessage(String message){
		if (out != null /*&& !out.checkError()*/) {
			try {
//				out.write(message.toCharArray(), 0, message.length());
				out.write(message + "\n");
//				out.println(message);
				out.flush();
				Log.d(TAG, "Sent.");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
 
	public void stopClient(){
		mRun = false;
		in.notify();		
	}
 
    public void run() { 
        mRun = true;
        try {        	
            InetAddress serverAddr = InetAddress.getByName(SERVERIP);            
            Log.e(TAG, "C: Connecting...");
            //create a socket to make the connection with the server
            mSocket = new Socket(serverAddr, SERVERPORT);
            Log.d(TAG, "Connected"); 
            try {
            		out = new OutputStreamWriter(mSocket.getOutputStream());
            		in = new InputStreamReader(mSocket.getInputStream());
//                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream())), true);
//                    in = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                    while (mRun) {
                    	//serverMessage = in.readLine();
                    	char arr[] = new char [256];
                    	int n = in.read(arr, 0, 256);
                    	Log.d(TAG, "read " + String.valueOf(n) + " bytes from socket");
                    	serverMessage = String.copyValueOf(arr, 0, n); //= arr.toString();
                        if(serverMessage != null && mMessageListener != null) {
                            //call the method messageReceived from MyActivity class
                            mMessageListener.messageReceived(serverMessage);
                        }
                        serverMessage = null;
                    }
                    Log.d(TAG, "RESPONSE FROM SERVER: S: Received Message: '" + serverMessage + "'");
            } catch (Exception e) { 
            	Log.e(TAG, "general error while waiting for server data: " + e.toString(), e); 
            } finally {
				//the socket must be closed. It is not possible to reconnect to this socket
				// after it is closed, which means a new socket instance has to be created.
            	Log.e(TAG, "Closing socket");
				if( in != null ) {
					in.close();
					in = null;
				}
				if( out != null ) {
					out.close();
					out = null;
				}
				mSocket.close();
				mSocket = null;
            }
		} catch(IOException e) {
			Log.e(TAG, "TCP Client has stopped running duo to an IO error: " + e.toString(), e);
		} catch (Exception e) { 
			Log.e(TAG, "TCP Client has stopped running duo to a general error: " + e.toString(), e); 
		}
    }
    
    public void notifyDown(float x, float y, float pressure) {
    	this.sendMessage("D " + 
				String.valueOf(x) + " " +
				String.valueOf(y) + " " +
				String.valueOf(pressure));
    }
    public void notifyMove(float x, float y, float pressure) {
    	this.sendMessage("M " + 
				String.valueOf(x) + " " +
				String.valueOf(y) + " " +
				String.valueOf(pressure));
    }
    public void notifyUp(float x, float y, float pressure) {
    	this.sendMessage("U " + 
				String.valueOf(x) + " " +
				String.valueOf(y) + " " +
				String.valueOf(pressure));
    }
    public void notifyDimensions(float height, float width) {
    	this.sendMessage("d " + 
				String.valueOf(height) + " " +
				String.valueOf(width));
    }
 
	//Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
	//class at on asynckTask doInBackground
	public interface OnMessageReceived {
		public void messageReceived(String message);
	}
}

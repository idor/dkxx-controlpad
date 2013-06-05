package com.tandemg.scratchpad;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import android.util.Log;

public class TCPClient implements Runnable {

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
	
	private void doTest(){
		Log.v(TAG, "searching for nodes");
		try {			
			for (Enumeration<NetworkInterface> ifaces = 
		               NetworkInterface.getNetworkInterfaces();
		             ifaces.hasMoreElements(); )
	        {
	            NetworkInterface iface = ifaces.nextElement();
	            System.out.println(iface.getName() + ":");
	            for (Enumeration<InetAddress> addresses =
	                   iface.getInetAddresses();
	                 addresses.hasMoreElements(); )
	            {
	                InetAddress address = addresses.nextElement();
	                if(address.isAnyLocalAddress())
	                	Log.v(TAG, "  local address" + address);
	                else
	                	Log.v(TAG, "  NOT local address" + address);
	            }
	        }			
			
			InetAddress[] inetAddress = null;
			List<String> hostList = new ArrayList<String>();
			NetworkInterface nif = NetworkInterface.getByName("wlan0");
			Log.v(TAG, "nif: " + nif.toString());
			String host = nif.getInetAddresses().toString();
			Log.v(TAG, "host address: " + host);
			inetAddress = InetAddress.getAllByName(host);			
			
			for(int i = 0; i < inetAddress.length; i++){
				hostList.add(inetAddress[i].getClass() + " -\n"
						+ inetAddress[i].getHostName() + "\n"
						+ inetAddress[i].getHostAddress());
				Log.v(TAG, inetAddress[i].getClass() + " -\n"
						+ inetAddress[i].getHostName() + "\n"
						+ inetAddress[i].getHostAddress());
			}
		} catch(Exception e) {
			Log.e(TAG, "general error while searching for client: " + e.toString(), e);
		}
		Log.v(TAG, "finished searching");
 	}
 
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
 
	public void stopClient() throws IOException {
		mRun = false;
		if(mSocket != null)
			mSocket.close();
		mSocket = null;
	}
 
    public void run() { 
        mRun = true;
        try {
        	doTest();
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
                    	int n;
                    	try {
                    		n = in.read(arr, 0, 256);
                    	} catch(IOException e) {
                    		continue;
                    	}
                    	serverMessage = String.copyValueOf(arr, 0, n); //= arr.toString();
                        if(serverMessage != null && mMessageListener != null) {
                            //call the method messageReceived from MyActivity class
                            mMessageListener.messageReceived(serverMessage);
                        }
                        serverMessage = null;
                    }
            } catch (Exception e) { 
            	Log.e(TAG, "general error while waiting for server data: " + e.toString(), e); 
            } finally {
				//the socket must be closed. It is not possible to reconnect to this socket
				// after it is closed, which means a new socket instance has to be created.
            	Log.i(TAG, "Closing socket");
				if( in != null ) {
					in.close();
					in = null;
				}
				if( out != null ) {
					out.close();
					out = null;
				}
				if( mSocket != null ) {
					mSocket.close();
					mSocket = null;
				}
            }
		} catch(IOException e) {
			Log.e(TAG, "TCP Client has stopped running duo to an IO error: " + e.toString(), e);
		} catch (Exception e) { 
			Log.e(TAG, "TCP Client has stopped running duo to a general error: " + e.toString(), e); 
		}
    }
    
    public boolean connected() {
    	return mSocket.isConnected();
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
    public void notifyDimensions(int height, int width) {
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

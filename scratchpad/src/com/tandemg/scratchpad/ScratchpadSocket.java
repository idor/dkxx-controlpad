/*package com.tandemg.scratchpad;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import android.util.Log;

public class ScratchpadSocket {
	 
    private String serverMessage;
    public static final String SERVERIP = "10.255.1.30"; //your computer IP address
    public static final int SERVERPORT = 2301;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
 
    PrintWriter out;
    BufferedReader in;
    
    
	private Socket nsocket;
    private InputStream nis; //Network Input Stream
    private OutputStream nos; //Network Output Stream
    
    
    private String TAG = "ScratchpadSocket";
    
    
 
    *//**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     *//*
    public ScratchpadSocket(OnMessageReceived listener) {
        mMessageListener = listener;
    }
 
    *//**
     * Sends the message entered by client to the server
     * @param message text entered by client
     *//*
    public void sendMessage(String message){
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
        }
    }
 
    public void stopClient(){
        mRun = false;
    }
 
    public void run() {
 
        mRun = true;
        
        try {
			SocketAddress sockaddr = new InetSocketAddress("10.255.1.30", 2301);
			nsocket = new Socket();
			Log.i("AsyncTask", "socket created");
			nsocket.connect(sockaddr, SOCKET_TIMEOUT); //10 second connection timeout
			if (nsocket.isConnected()) {
				nis = nsocket.getInputStream();
				nos = nsocket.getOutputStream();
				Log.i("AsyncTask", "doInBackground: Socket created, streams assigned");
				Log.i("AsyncTask", "doInBackground: Waiting for inital data...");
				String cmd = "D 1 1 1";
				nos.write(cmd.getBytes());
             }
		} catch (IOException e) {
			e.printStackTrace();
			Log.i("AsyncTask", "doInBackground: IOException " + e.toString());
		} catch (Exception e) {
			e.printStackTrace();
			Log.i("AsyncTask", "doInBackground: Exception " + e.toString());
		} finally {
            try {
				nis.close();
				nos.close();
				nsocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
            }
            Log.i("AsyncTask", "doInBackground: Finished");
        }
        
        
        
        
        
        
        
        
        
        
        
        
        
 
        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVERIP);
 
            Log.e("TCP Client", "C: Connecting...");
 
            //create a socket to make the connection with the server
            nsocket = new Socket(serverAddr, SERVERPORT);

 
            try {
 
                //send the message to the server
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
 
                Log.e("TCP Client", "C: Sent.");
 
                Log.e("TCP Client", "C: Done.");
 
                //receive the message which the server sends back
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 
                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    serverMessage = in.readLine();
 
                    if (serverMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(serverMessage);
                    }
                    serverMessage = null;
 
                }
 
 
                Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");
 
 
            } catch (Exception e) {
 
                Log.e("TCP", "S: Error", e);
 
            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }
 
        } catch (Exception e) {
 
            Log.e("TCP", "C: Error", e);
 
        }
 
    }
 
    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}
*/
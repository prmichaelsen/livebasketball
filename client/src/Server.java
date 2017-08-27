package com.patrickmichaelsen.livebasketball;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class Server {
	private Socket clientSocket = null; 
	private DataOutputStream outToServer = null;
	private BufferedReader inFromServer = null;
	private String host = null;
	private int port = 0;
	private boolean isConnected = false;

	public Server(String host, int port){
		this.host = host;
		this.port = port;
	}

	public void connect(){
		try{
			clientSocket = new Socket(host, port);
			isConnected = true;
			System.out.println("Connected to TCP server!");
		} catch ( UnknownHostException e){
			e.printStackTrace();
		} catch ( IOException e){
			e.printStackTrace();
		}
		if(clientSocket != null){
			try{
				outToServer = new DataOutputStream(clientSocket.getOutputStream());
				inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			} catch ( IOException e){
				e.printStackTrace();
			} 
		} 
	}

	public boolean isConnected(){ return isConnected; }

	public String readLine(){
		String line = null;
		if( inFromServer != null){
			try{
				line = inFromServer.readLine();
			} catch ( IOException e ){
				e.printStackTrace(); 
			}
		}
		if( line == null ){
			isConnected = false; 
		}
		return line;
	}

	public void close(){
		if(clientSocket != null){
			try{
				clientSocket.close();
			} catch (IOException e){
				e.printStackTrace();
			} 
			clientSocket = null;
		}
	} 
}

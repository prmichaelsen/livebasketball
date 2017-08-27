package com.patrickmichaelsen.livebasketball;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.ServerSocket;
import java.net.Socket;

public class Client{
	private ServerSocket welcomeSocket;
	private Socket connectionSocket;
	private BufferedReader inFromClient;
	private DataOutputStream outToClient;

	public Client(ServerSocket socket){
		welcomeSocket = socket;
	}

	public boolean waitForConnection() throws IOException{
		if(welcomeSocket != null){
			Socket connectionSocket = welcomeSocket.accept();
			inFromClient =
				new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			return true;
		} 
		return false;
	}

	public String readLineFromClient() throws IOException{
		if(inFromClient != null){ 
			String line = inFromClient.readLine();
			return line; 
		}
		else throw new IOException("inFromClient never initialized");
	}

	public void writeToClient(String line) throws IOException{
		if(outToClient != null){
			outToClient.writeBytes(line); 
		}
		else throw new IOException("outToClient never initialized");
	} 
} 

import java.io.*;
import java.net.*;

class Main {
 public static void main(String argv[]) throws Exception {
  String sentence;
  String modifiedSentence;
  BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
  Socket clientSocket = new Socket("localhost", 6789);
  DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
  BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
  //sentence = inFromUser.readLine();
  //outToServer.writeBytes(sentence + '\n');
	while(true){ 
		modifiedSentence = inFromServer.readLine();
		System.out.println("FROM SERVER: " + modifiedSentence);
		//since the above is a json, we can use
		//gson/json to get back our match objects
		//exactly. Or, we can use a new object, for instance
		//notification object, to just get pertintent info
	}
  //clientSocket.close(); //we cannot close the socket bc of the for loop
	//this needs fixing some day
 }
}

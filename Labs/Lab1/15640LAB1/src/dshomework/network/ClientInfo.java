package dshomework.network;


import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class ClientInfo {
	
	//unique id given by server to clients
	public int clientId; 
	
	//handles commands from server to client
	public Thread clientHandler;
	
	// a list of processes running on the server
	public ArrayList processes;
	
	// location tuple of ip and port
	public Location location;
	
	// port where the client is receiving from server
	public int receiverPort; 
	
	//time since last message
	public long lastSeen = 0; 
	
	//true for process manager 
	boolean processManager; 
	
	ClientInfo(int id, Thread ch, Socket clientSocket, int receiverPort){
		this.clientId = id;
		this.clientHandler = ch;
		this.processes = new ArrayList<Object>();
		this.location = new Location(clientSocket.getInetAddress().toString(),clientSocket.getPort());
		this.receiverPort = receiverPort;
		java.util.Date currentDate = new java.util.Date();
		this.lastSeen = currentDate.getTime();
		processManager = false;
	}
	
	public void setProcessManager(){
		this.processManager =true;
	}
	
	public String toString(){
		return " clientId "+clientId+" clientHandler "+clientHandler+" processes "+displayProcesses(this.processes);
	}
	
	public String displayProcesses(ArrayList ob){
		String s="";
		for(int i=0; i<processes.size();i++)
			s = s + processes.get(i)+",";
		return s;
	}


}

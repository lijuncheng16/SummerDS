package dshomework.network;


/**
 * class derived from the official Java documentation available at:
 * http://docs.oracle.com/javase/tutorial/networking/sockets/examples/EchoClient.java
 * I have modified the program by removing the implicit try-with-resources block
 * and replacing it with a traditional try-catch block and a finally
 * block to explicitly free all the resources that were being freed implicitly before.
 * I also extended their client to be runnable so that multiple clients can be instantiated.
 */


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.io.Serializable;


public class Client {
	public Location location;
	public static int clientKey = -1;
	//public static ClientProcessMap processes;
	private static int processID = 0;
	public static ConcurrentHashMap<String, MigratableProcess> processes;
	
	public int getSocketNumber(){
		return location.socketNumber;
	}
	
	public int receiverPort = 6666;
	
	//method to display all processese running on this client
	public static void displayProcesses(){
		System.out.println("Processes running on client "+Client.clientKey+" are as follows:" );
		for(String key: processes.keySet()){
			System.out.print(processes.get(key).getName()+" ");
		}
		System.out.println();
	}
	
	
	public static void main(String args[]) throws UnknownHostException, IOException, ClassNotFoundException{
		//check input
		if (args.length != 0) {
	        System.err.println("FAILURE. Usage: java Client");
	        System.err.println("Exiting");
	        System.exit(0);
	    }
		//for storing all the rpocesses at this client 
		
				/* Try to connect to server */
		        String hostName = Server.HOSTNAME;
		        int portNumber = Server.INITIAL_PORT;
		        
		        //create a new receiver port for a client to receive messages from other clients
		        ServerSocket receiverSocket = new ServerSocket(0);
		        int receiverPort = receiverSocket.getLocalPort();
		        Thread t = new ClientsideReceiver(receiverSocket);
		        //start the receiver
		        t.start();
	        
	            Socket echoSocket = new Socket(hostName, portNumber);
	            //open print stream
	            PrintWriter outToServer = new PrintWriter(echoSocket.getOutputStream(), true);
	            // open in stream
	            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
	            		echoSocket.getInputStream()));
	            
	            //tell the server the location of my clientside receiving socket
	            // where all other clients can send serialized objects
	            outToServer.println("MyReceiver "+ receiverPort);
	            
	            // now, server should send the unique client key for this client
	            //String mixedClientKey = inFromServer.readLine();
	            String firstInput = inFromServer.readLine();
	            String[] clientKeyArray = firstInput.split(" ");
	            //if the first received message does not contain key, then exit
	            if(!clientKeyArray[0].equalsIgnoreCase("YOURKEY")||clientKeyArray.length<1){
	            	System.out.println("Client failed to receive key from server. This client is exiting.");
	            	System.out.println(firstInput);
	            	System.exit(-1);
	            }
	            System.out.println("Connection established. This client's ID is "+Integer.parseInt(clientKeyArray[1]));
	            Client.clientKey = Integer.parseInt(clientKeyArray[1]);
	            processes = new ConcurrentHashMap<String,MigratableProcess>();
	            
	            try{
	            // create a new client hearbeat thread for this client
	            ClientHeartbeat chb = new ClientHeartbeat(Integer.parseInt(clientKeyArray[1]));
	            Thread chbt = new Thread(chb);
	            chbt.start();
	            } catch(Exception e){
	            	System.out.println("Error while parsing unique key given by server. Client is exiting");
	            	System.exit(-1);
	            }
	            
	            // standard input stream for user input
	            BufferedReader stdUserInput = new BufferedReader(new InputStreamReader(System.in));
	            // store input
	            String userInput;
	            while ((userInput = stdUserInput.readLine()) != "") {
	            	outToServer.println(userInput);
	                System.out.println("echo: " + inFromServer.readLine());
	            }
	}

}


class ClientsideReceiver extends Thread{
	public DataInputStream inputStream=null;
	public PrintWriter printStream = null;
	public ServerSocket receiverSocket;
	private int threadIdentifier;
	
	public ClientsideReceiver(ServerSocket newReceiverSocket) {
		this.receiverSocket = newReceiverSocket;
	}
	
	@Override
	public void run(){
		try{
			System.out.println("Client side receiver started on port .."+receiverSocket.getLocalPort());
			//start to receive connections from other clients
		    while (true) {

			            //accept a new client connection by listening to port
		    			System.out.println("Waiting for client");
			            Socket clientSocket = receiverSocket.accept();    
			            ObjectInputStream inobj = new ObjectInputStream(clientSocket.getInputStream());
			            MigratableProcess newObj = (MigratableProcess)inobj.readObject();
			            System.out.println("Object received. Starting at client ");
			            //fff.suspend();
			            
			            Thread t = new Thread(newObj);
			            System.out.println("Thread Id for thread is: "+ t.getId());
			            //t.getId();
			            Client.processes.put(newObj.getName(), newObj);
			            t.start();
			            Client.displayProcesses();
	            }

		} catch(IOException e){
			try {
				receiverSocket.close();
			} catch (IOException e1) {
				System.out.print("Failure Encountered and also could not close Clientside Receiver");
				e1.printStackTrace();
			}
			System.out.println("Thread ended for client");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
			
	}// end of ClientsideReceiver run
}

class ClientHeartbeat extends Thread{
	public int clientKey =-1; 
	
	public ClientHeartbeat(){
		
	}
	
	public ClientHeartbeat(int clientKey){
		this.clientKey=clientKey;
		
	}
	
	@Override 
	public void run() {
		//Socket heartbeatSocket;
		//PrintWriter outToServer;
		while(true){
			try{
				//open up a socket for heartbeat to the server
				Socket heartbeatSocket = new Socket(Server.HOSTNAME, Server.HEARTBEAT_PORT);
		        //open print stream not in use
				PrintWriter outToServer = new PrintWriter(heartbeatSocket.getOutputStream(), true);
		        // open in stream - not being used
		        //BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
		        //heartbeatSocket.getInputStream()));
		        outToServer.println("HEARTBEAT "+ clientKey);
				System.out.println("SENT = HEARTBEAT "+ clientKey +" Now sending process map..");
				outToServer.println(Client.processes);
				//include a safe time buffer
				Thread.sleep(30);
				heartbeatSocket.close();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// Auto-generated catch block
					// dont kill the process, catch exception and ignore it
				}
			} catch(Exception e){
				System.out.println("Process Manager could not contact client. Retrying.");
				continue;
			}
		}

	}
}

class ClientProcessMap implements java.io.Serializable{

	/**
	 * Autogenerated
	 */
	private static final long serialVersionUID = -6942022907249520529L;
	
	public int clientKey;
	public ConcurrentHashMap<String, Runnable> processList;
	
	
	public ClientProcessMap(int c){
		clientKey = c;
		processList = new ConcurrentHashMap<String, Runnable>();
	}
	
	
	
}

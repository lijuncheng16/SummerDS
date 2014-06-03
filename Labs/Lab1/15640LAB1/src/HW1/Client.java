package HW1;


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
import java.util.Arrays;
import java.util.HashMap;
import java.io.Serializable;

import processmanager.MigratableProcess;

public class Client {
	public Location location;
	public static int clientKey = -1;
	private static HashMap<Integer, ProcessInfo> processMap = new HashMap<Integer, ProcessInfo>();
	private static int processID = 0;
	public static ProcessHashMap processes;
	
	public int getSocketNumber(){
		return location.socketNumber;
	}
	
	public int receiverPort = 6666;
	
	public static void main(String args[]) throws UnknownHostException, IOException, ClassNotFoundException{
		//check input
		if (args.length != 0) {
	        System.err.println("FAILURE. Usage: java Client");
	        System.err.println("Exiting");
	        System.exit(0);
	    }
		//for storing all the rpocesses at this client 
		processes = new ProcessHashMap();
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
	}//end fo main
	
	        /*
	         *  Prepare to read message from server 
	         * 
			String str = null;
			String[] args = null;
			while ((str = inFromServer.readLine()) != null) {
				args = str.split(" ");

				if (args[0].equals("launch"))
					launch(args);
				//	 suspend a process given process ID 
				else if (args[0].equalsIgnoreCase("suspend")) {

					//check the process ID 
					int migrateProcessID = -1;
					try {
						migrateProcessID = Integer.parseInt(args[1]);
					} catch (NumberFormatException e) {
						System.err.println("error in process ID format");
						continue;
					}

					MigratableProcess mpWrite = processMap
							.get(migrateProcessID).process;

					if (mpWrite == null) {
						System.err.println("wrong process ID");
						continue;
					}

					mpWrite.suspend();
					processMap.get(migrateProcessID).status = ProcessStatus.SUSPENDED;

					//write the suspended process into a file 
					FileOutputStream outputFile = new FileOutputStream(args[1]
							+ args[2] + args[3] + ".obj");
					ObjectOutputStream outputObj = new ObjectOutputStream(
							outputFile);
					outputObj.writeObject(mpWrite);
					outputObj.flush();
					outputObj.close();
					outputFile.close();

					// acknowledge back to master server 
					outToServer.write("finish suspending\n");
					outToServer.flush();

					// remove the process from process list 
					processMap.remove(migrateProcessID);
				}
                
				
				 /* resume a suspended process by reading from an *.obj file
				 * previously dumped by another slave server
				 */
	            /*
				else if (args[0].equals("resume")) {
					//read the *.obj file 
					FileInputStream inputFile = new FileInputStream(args[1]
							+ args[2] + args[3] + ".obj");
					ObjectInputStream inputObj = new ObjectInputStream(inputFile);
					MigratableProcess mpRead = (MigratableProcess) inputObj.readObject();
					inputObj.close();
					inputFile.close();
					
					// run the process 
					Thread newThread = new Thread(mpRead);
					newThread.start();

					// add this newly started process to the process list 
					ProcessInfo processInfo = new ProcessInfo();
					processInfo.process = mpRead;
					processInfo.status = ProcessStatus.RUNNING;
					processID++;
					processMap.put(processID, processInfo);
				}

				// iterate through the process list and send back to server 
	/*			else if (str.equals("processlist")) {
					for (Map.Entry<Integer, ProcessInfo> entry : processMap
							.entrySet()) {
						if (entry.getValue().process.finalize())
							outToServer.write("#"
									+ entry.getKey()
									+ "\t"
									+ entry.getValue().process.getClass()
											.getSimpleName() + " "
									+ ProcessStatus.TERMINATED + "\n");
						else
							outToServer.write("#"
									+ entry.getKey()
									+ "\t"
									+ entry.getValue().process.getClass()
											.getSimpleName() + " "
									+ entry.getValue().status + "\n");
						outToServer.flush();
					}

					outToServer.write("process list finish\n");
					outToServer.flush();
				}


			}
		*/



		/**
		 * Instantiate a new process
		 * 
		 * @param args
		 */
		public static void launch(String[] args) {
			MigratableProcess newProcess = null;
			try {
				Class<MigratableProcess> processClass = (Class<MigratableProcess>) Class.forName(args[1]);
				Constructor<?> processConstructor = processClass
						.getConstructor(String[].class);
				Object[] processArgs = { Arrays.copyOfRange(args, 2, args.length) };
				newProcess = (MigratableProcess) processConstructor
						.newInstance(processArgs);
			} catch (ClassNotFoundException e) {
				System.out.println("Could not find class " + args[1]);
				e.printStackTrace();
				return;
			} catch (SecurityException e) {
				System.out.println("Security Exception getting constructor for "
						+ args[1]);
				return;
			} catch (NoSuchMethodException e) {
				System.out.println("Could not find proper constructor for "
						+ args[1]);
				return;
			} catch (IllegalArgumentException e) {
				System.out.println("Illegal arguments for " + args[1]);
				return;
			} catch (InstantiationException e) {
				System.out.println("Instantiation Exception for " + args[1]);
				return;
			} catch (IllegalAccessException e) {
				System.out.println("IIlegal access exception for " + args[1]);
				return;
			} catch (InvocationTargetException e) {
				System.out.println("Invocation target exception for " + args[1]);
				return;
			} catch (Exception e) {
				System.err.println(e.toString());
			}

			Thread newThread = new Thread(newProcess);
			newThread.start();

			/* add this newly started process to the process list */
			ProcessInfo processInfo = new ProcessInfo();
			//processInfo.process = newProcess; TODO
			//processInfo.status = ProcessStatus.RUNNING;
			processID++;
			processMap.put(processID, processInfo);
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
			//start to receive connections from other clients
		    while (true) {

			            //accept a new client connection by listening to port
			            Socket clientSocket = receiverSocket.accept();    
			            // TODO implement handling of serialized object
	
	            }

		} catch(IOException e){
			try {
				receiverSocket.close();
			} catch (IOException e1) {
				System.out.print("Failure Encountered and also could not close Clientside Receiver");
				e1.printStackTrace();
			}
			System.out.println("Thread ended for client");
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
				System.out.println("SENT = HEARTBEAT "+ clientKey);
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


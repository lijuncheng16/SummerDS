package HW1;

//import SlaveServer;
//import SlaveServiceException;

import java.io.BufferedReader;
import java.util.Scanner;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import com.sun.xml.internal.fastinfoset.util.StringArray;

import network.Client;
import network.Server;
import network.StatusMessages;

public class ProcessManager {
	
	// message code when sending messages to server
	public static int messageCode = 0;
	// flag indicating that a message is ready to be sent
	public static boolean sendMessage=false;
	// actual message to be sent
	public static String messageContent ="";
	
	
	public static void main(String args[]){
		ContactServer.log("Console for Process Management.");
		Thread contact = new Thread(new ContactServer());
		contact.start();	
	}

}

class ContactServer extends Thread {
	
	//choice from user
	public static int choice = 0;
	
	public int clientKey=-1;
	
	//for user input
	Scanner sc= new Scanner(System.in); 
	
	//make printing stuff easier in the console by wrapping function name
	static void log(String a){
		System.out.println(a);
	}
	
	//run method for Contacting server for user console
	@Override
	public void run() {
		String hostName = Server.HOSTNAME;
        int portNumber = Server.INITIAL_PORT;

        try {
        	
        	//try to connect to server
            Socket echoSocket = new Socket(hostName, portNumber);
            //open print stream
            PrintWriter outToServer = new PrintWriter(echoSocket.getOutputStream(), true);
            // open in stream
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
 
            //tell the server that this is the process manager
            outToServer.println("MyReceiver "+-1+" PROCESSMANAGER");
            
            //
            
            //take first input from server
            String firstInput = inFromServer.readLine();
            String[] clientKeyArray = firstInput.split(" ");
            //if the first received message does not contain key, then exit
            if(!clientKeyArray[0].equalsIgnoreCase("YOURKEY")||clientKeyArray.length<1){
            	System.out.println("Process Manager failed to receive key from server. This process manager is exiting.");
            	System.out.println(firstInput);
            	System.exit(-1);
            }
            //launch a thread for heartebat of process manager
            try{
	            // create a new client hearbeat thread for this client
            	ProcessManagerHeartbeat pmhb = new ProcessManagerHeartbeat(Integer.parseInt(clientKeyArray[1]));
	            Thread pmhbt = new Thread(pmhb);
	            pmhbt.start();
	            } catch(Exception e){
	            	System.out.println("Error while parsing unique key given by server. Client is exiting");
	            	System.exit(-1);
	            }
            
            String reply;
            
            while(true){
    			log("Press: 1.List Clients 2. List Processes 3.Remove Process 4.Migrate Process: 5.Create Process");
    			choice = sc.nextInt();
    			switch(choice){
    			case(1): 
    				System.out.println("Process manager sending list request");
    				outToServer.println("ProcessManager "+StatusMessages.LIST_CLIENTS);
                	//reply = inFromServer.readLine();
    				//System.out.println("echo: Process manager received: " + reply);
    				// set the message so that server knows it is process manager
    				// ProcessManager.messageCode = StatusMessages.LIST_PROCESSES;
    				// ProcessManager.messageContent = StatusMessages.LIST_PROCESSES+"";
    				//  ProcessManager.sendMessage = true;
    				break;
    			
    			case(2): 
    				System.out.println("Process manager sending suspend request");
    				int pid;
    				log("Enter process id to be suspended");
    				pid = sc.nextInt();
					outToServer.println("ProcessManager TRYSUSPEND "+pid);
	            	reply = inFromServer.readLine();
	            	if(reply.equals("notokay"))
	            		log("echo: Process manager received: " + reply);
	            	else{
	            		outToServer.println("ProcessManager SUSPEND "+pid); 
	            	}
    				break;
    			
    			case(3):
    				System.out.println("Available processes are: "); // TODO
    				System.out.print("Enter the ID of the process to be deleted: ");
    				break;
    			
    			
    			case(4): break;
    			
    			case(5):
    				log(" The available clients are: ");
    				Server.displayClients();
    				log("The available processes are: ");
    				// TODO Server.displayProcesses();
    				
    				//@referred to http://stackoverflow.com/questions/9886266/is-there-a-way-to-instantiate-a-class-by-name-in-java
    				Class<?> userClass = Class.forName("GrepProcess");
    				Constructor<?> constructorNew = userClass.getConstructor(StringArray.class);
    				Object instance = constructorNew.newInstance();
    				
    				break;
    			
    			default: break;
    			}
            }
        
        }catch(Exception e){}
	}

       
        	
        
	}//end of run






class ProcessManagerHeartbeat extends Thread{
	public int clientKey =-1; 
	
	public ProcessManagerHeartbeat(){
		
	}
	
	public ProcessManagerHeartbeat(int clientKey){
		this.clientKey=clientKey;
		
	}
	
	@Override 
	public void run() {
		PrintWriter outToServer;
		while(true){
			try{
				//open up a socket for heartbeat to the server
				Socket heartbeatSocket = new Socket(Server.HOSTNAME, Server.HEARTBEAT_PORT);
		        //open print stream - not in use
		        outToServer = new PrintWriter(heartbeatSocket.getOutputStream(), true);
		        outToServer.println("HEARTBEAT "+ this.clientKey);
				//System.out.println("SENT = HEARTBEAT "+ this.clientKey);
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

	}//end of run
}

package HW1;


public class StatusMessages {
	
	public final static int END_MESSAGE = -1;
	public final static int LAUNCH = 0;
	public final static int MIGRATE = 1;
	public final static int SUSPEND = 2;
	public final static int REMOVE = 3;
	public final static int LIST_CLIENTS = 4;
	public final static int LIST_PROCESSES = 5;


	/* Message Formats used in the project
	 * 
	 * 1. SERVER TO CLIENT 
	 * 		a. "MIGRATE process pid clientp ip clientport port"  //pid=int, ip=string, port=int
	 * 		b. "LAUNCH client c process p " //p is java reflections class type
	 * 		c. "REMOVE process pid"
	 * 		d. "YOURKEY key" // key is the unique identifier for the lcient required for client identification and hearbeats
	 * 
	 * 2. CLIENT TO SERVER
	 * 		a. 
	 * 
	 * 3. CONSOLE TO SERVER
	 * 		a. "ProcessManager LIST_PROCESSES" // display all the existing processes at the server
	 * 		b. "ProcessManager SUSPEND pid" // suspend message with id=pid
	 * 		c. "ProcessManager TRYSUSPEND pid" // check if given process id exists on the server. Returns okay or notokay
	 * 
	 * 4. SERVER TO CONSOLE
	 * 		a. "okay" // when process to be suspended exists on the server
	 * 		b. "notokay" // when process to be suspended exists on the server
	 * 
	 * 5. CLIENT TO CLIENT
	 * 		a. ???? implement serialized objects here
	 * 
	 * 
	 * SPECIAL MESSAGES:
	 * first messages sent:
	 * client to server: MyReceiver int //int is port number
	 * server to client: YOURKEY int //int is unique client key
	 * 
	 * heartbeat
	 * client to server: HEARTBEAT int //int is unique client key 
	 * 
	 */

}

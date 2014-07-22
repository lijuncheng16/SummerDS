package mapReduce.frameWork;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import mapReduce.util.StaffUtils;

/**
 * Defines a generic worker server in the distributed system. Each
 * {@link WorkerServer} listens for incoming connections on a distinct host/port
 * address, and waits for others to send {@link WorkerCommand} objects for it to
 * execute remotely.
 *
 * Refer to recitation 13 for an idea of how this class should be implemented
 * (you are allowed to copy the code from recitation 13).
 */
public class WorkerServer extends Thread {
    private final int mPort;
    
    private final int POOL_SIZE = 4;
    private final ExecutorService wExecutor;
    
    /**
     * The {@link WorkerServer} constructor.
     *
     * @param workerPort The port to listen on.
     */
    public WorkerServer(int workerPort) {
        mPort = workerPort;
        wExecutor = Executors.newFixedThreadPool(POOL_SIZE);
    }

    @Override
    public void run() {
    	ServerSocket serverSocket = null;
    	try {
			serverSocket = new ServerSocket(mPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	while (true) {
    		try {
    			System.out.println("WorkerServer: "
						+ serverSocket.toString() + "starts to listen.");
    			Socket masterSocket = serverSocket.accept();
    			System.out.println("WorkerServer: "
						+ masterSocket.toString() + " connects.");
    			ObjectInputStream in = new ObjectInputStream(masterSocket.getInputStream());
    			WorkerCommand task = (WorkerCommand)in.readObject();
    			task.setSocket(masterSocket);
    			wExecutor.execute(task);
    		} catch (IOException e) {
                System.err.println("Error while listening for incoming connections.");
                break;
            } catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
    	}
    	

		wExecutor.shutdown();
    }

    /********************************************************************/
    /***************** STAFF CODE BELOW. DO NOT MODIFY. *****************/
    /********************************************************************/

    /**
     * Starts a worker server on a distinct port. This information can be either
     * specified as command line arguments or via system properties specified in
     * the <code>workers.properties</code> file (if no command line arguments
     * are specified).
     */
    public static void main(String[] args) {
        List<WorkerServer> servers = StaffUtils.makeWorkerServers(args);
        for (WorkerServer server : servers) {
            server.start();
        }
    }

}

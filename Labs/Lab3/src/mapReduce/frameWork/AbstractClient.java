package mapReduce.frameWork;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import mapReduce.wordcount.WordCountClient;
import mapReduce.wordprefix.WordPrefixClient;

/**
 * An abstract client class used primarily for code reuse between the
 * {@link WordCountClient} and {@link WordPrefixClient}.
 */
public abstract class AbstractClient {
    private final String mMasterHost;
    private final int mMasterPort;

    /**
     * The {@link AbstractClient} constructor.
     *
     * @param masterHost The host name of the {@link MasterServer}.
     * @param masterPort The port that the {@link MasterServer} is listening on.
     */
    public AbstractClient(String masterHost, int masterPort) {
        mMasterHost = masterHost;
        mMasterPort = masterPort;
    }

    protected abstract MapTask getMapTask();

    protected abstract ReduceTask getReduceTask();

    public void execute() {
        final MapTask mapTask = getMapTask();
        final ReduceTask reduceTask = getReduceTask();

        // Submit the map/reduce task to the master and wait for the task
        // to complete.
        Socket socket = null;
        ObjectInputStream in = null;
        try {
			socket = new Socket(mMasterHost, mMasterPort);
			System.out.println("ClientServer: "
					+ "connects to" + socket.toString());
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			
			out.writeObject(mapTask);
			out.writeObject(reduceTask);
			
			// wait for the masterserver to return result.
			in = new ObjectInputStream(socket.getInputStream());
			Object obj = in.readObject();
			if (!(obj instanceof ReplyDone)) {
				System.out.println("Fail...That's really sad.");
			} else {
				System.out.println("Success!");
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
			}
		}
        
    }

}

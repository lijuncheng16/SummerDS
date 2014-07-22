package mapReduce.frameWork;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * The abstract template workercommand sender class. All workercommand from
 * master to worker are sent by the subclasses of SendWorker. SendWorker establishes
 * the connection with the target workerserver and send the workercommand
 * to them, if the target workerserver crashes during the communication, this
 * worker's status is marked as fail. It returns a T object as the result of
 * the workercommand delivery. All subclasses need to implement the readResult,
 * getSuccessResult, getFailureResult methods. Which should be in specific concern
 * of specific senders. Subclasses includes {@link CleanSendWorker}, 
 * {@link MapSendWorker}, {@link ShuffleSendWorker}, and {@link ReduceSendWorker}.
 *
 * @param <T>
 */
public abstract class SendWorker<T> implements Callable<T> {
	private final WorkerInfo workerInfo;
	private final WorkerCommand work;
	private final Map<WorkerInfo, Boolean> wStatus;

	/**
	 * Constructor.
	 * @param w
	 * @param work
	 * @param wStatus
	 */
	public SendWorker(WorkerInfo w, WorkerCommand work,
			Map<WorkerInfo, Boolean> wStatus) {
		workerInfo = w;
		this.work = work;
		this.wStatus = wStatus;
	}

	@Override
	public T call() {
		Socket socket = null;
		try {
			// first send the execute command.
			ObjectOutputStream out = null;
			socket = new Socket(workerInfo.getHost(),
					workerInfo.getPort());
			out = new ObjectOutputStream(socket.getOutputStream());

			out.writeObject(work);
			out.flush();

			ObjectInputStream in = null;
			in = new ObjectInputStream(socket.getInputStream());
			
			// block until the remote socket has written the result or
			// crashed.
			Object reply = in.readObject();
			readResult(reply);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (InvalidClassException e) {
		} catch (NotSerializableException e) {
			System.err.println("work Not serializable!");
		} catch (IOException e) {
			System.out.println("SendWorker: "+workerInfo.getName() + " has crashed.");
			wStatus.put(workerInfo, false);
			return getFailureResult();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (socket != null) {
					socket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return getSuccessResult();
	}
	
	protected WorkerInfo getReceiver() {
		return workerInfo;
	}
	
	/**
	 * Interpret the result sent from the remote side. The subclasses have
	 * to override this.
	 * @param reply
	 * @throws IOException
	 */
	abstract public void readResult(Object reply) throws IOException;
	
	/**
	 * Return the success delivery result. The subclasses have to override
	 * this.
	 * @return <T>
	 */
	abstract public T getSuccessResult();
	
	/**
	 * Return the failed delivery result. The subclasses have to override
	 * this.
	 * @return <T>
	 */
	abstract public T getFailureResult();
}

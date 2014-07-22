package mapReduce.frameWork;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import mapReduce.util.WorkerStorage;

/**
 * Execute the clean job in the workerserver.
 *
 */
public class ExecuteClean extends WorkerCommand {

	private static final long serialVersionUID = 1980860335312607233L;
	private final WorkerInfo worker;

	public ExecuteClean(WorkerInfo w) {
		this.worker = w;
	}
	
	@Override
	public void run() {
		Socket mSocket = getSocket();
		String dirName = WorkerStorage.getIntermediateResultsDirectory(worker.getName());
		
		File tgtDir = new File(dirName);
		
		if (!tgtDir.exists() || !tgtDir.isDirectory()) {
			throw new IllegalArgumentException();
		} else if (!tgtDir.canRead()) {
            throw new IllegalStateException();
        }
		
		// deleted all the files under the intermediate results directory.
		for (File file : tgtDir.listFiles()) {
			file.delete();
		}
		
		// write back the done signal.
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(mSocket.getOutputStream());
			out.writeObject(new ReplyCleanDone());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				mSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}

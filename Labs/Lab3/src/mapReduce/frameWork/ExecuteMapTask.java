package mapReduce.frameWork;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;

import mapReduce.util.WorkerStorage;

/**
 * Execute the map task. Sent by the masterserver to the map workers.
 *
 */
public class ExecuteMapTask extends WorkerCommand {
	private static final long serialVersionUID = 8937077500465707097L;
	
	private final MapTask mTask;
	private final List<Partition> partitions;
	private final String workerName;

	public ExecuteMapTask(MapTask task, List<Partition> partitions,
			String workerName) {
		this.mTask = task;
		this.partitions = partitions;
		this.workerName = workerName;
	}

	@Override
	public void run() {
		Socket mSocket = getSocket();
		FileOutputStream out = null;
		FileInputStream in = null;
		String dirName = WorkerStorage
				.getIntermediateResultsDirectory(workerName);
		String outFileName = "";

		// for each of the partition, write the intermediate results
		for (Partition p : partitions) {
			Iterator<File> iter = p.iterator();
			outFileName = dirName + "\\" + p.getPartitionName()
					+ ".txt";
			FileInputStream check = null;
			try { // if the file is already mapped, pass.
				check = new FileInputStream(outFileName);
			} catch (FileNotFoundException e) { // if file is not mapped, map it.
				while (iter.hasNext()) {
					try {
						in = new FileInputStream(iter.next());
						out = new FileOutputStream(outFileName, true);
						Emitter emitter = new EmitterImpl(out);
						mTask.execute(in, emitter);
					} catch (IOException e1) {
						System.err
								.println("Cannot open the files in the worker server.");
					} finally {
						try {
							if (in != null) {
								in.close();
							}
							if (out != null) {
								out.close();
							}
						} catch (IOException e1) {
						}
					}
				}
			} finally {
				if (check != null) {
					try {
						check.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		// write back the result.
		ObjectOutputStream reply;
		try {
			reply = new ObjectOutputStream(mSocket.getOutputStream());
			reply.writeObject(new ReplyMapDone());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			mSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

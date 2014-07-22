package mapReduce.frameWork;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mapReduce.util.KeyValuePair;
import mapReduce.util.WorkerStorage;

/**
 * WorkerCommand class that execute data sending task during shuffle.
 * ExecuteDataGetTask objects are sent from the reduce worker to other workers
 * that holds the map results the reduce worker wants.
 * 
 */
public class ExecuteDataGetTask extends WorkerCommand {
	private static final long serialVersionUID = -8135896376320371266L;
	// records the partition-sections relation given by masterserver.
	private final Map<Partition, Set<Integer>> partitions;
	private final WorkerInfo dataSender;
	// as the divident of the hashcode.
	private final int workerServerNum;

	public ExecuteDataGetTask(WorkerInfo w, int workerServerNum) {
		dataSender = w;
		partitions = new HashMap<Partition, Set<Integer>>();
		this.workerServerNum = workerServerNum;
	}

	@Override
	public void run() {
		Socket mSocket = getSocket();
		FileInputStream in = null;

		// get the result of all the key/value pairs in the selected sections.
		String mappedFileDir = WorkerStorage
				.getIntermediateResultsDirectory(dataSender.getName());
		List<KeyValuePair> result = new ArrayList<KeyValuePair>();
		for (Partition p : partitions.keySet()) {
			String parName = mappedFileDir + "\\" + p.getPartitionName()
					+ ".txt";
			BufferedReader reader = null;
			try {
				in = new FileInputStream(parName);
				reader = new BufferedReader(new InputStreamReader(in));

				String line = null;
				while (true) {
					try {
						line = reader.readLine();
						if (line == null) {
							break;
						}
						String[] v = line.split("\\W+");
						if (v.length != 2) {
							System.err
									.println("Wrong contents in the mapped result, something has go wrong!");
							throw new IOException();
						}
						if (partitions.get(p).contains(
								Math.abs(v[0].hashCode() % workerServerNum))) {
							KeyValuePair unit = new KeyValuePair(v[0], v[1],
									Integer.parseInt(p.getPartitionName()));
							result.add(unit);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} catch (FileNotFoundException e) { // the control flow has gone
												// wrong. no such file in
												// sender.
				System.err.println(dataSender.getName()
						+ ": No such mapped partition in the receiver!");
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
					}
				}
			}
		}

		// send the result back.
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(mSocket.getOutputStream());
			for (KeyValuePair pair : result) {
				out.writeObject(pair);
				out.reset();
			}
			out.writeObject(new ReplyDataSendDone());
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

	/**
	 * Add the contents that this ExecuteDataGetTask object need to send.
	 * @param p
	 * 		the partition of the contents.
	 * @param s
	 * 		the sections in the partition which should be sent.
	 */
	public void addPartitionWork(Partition p, Set<Integer> s) {
		if (partitions.containsKey(p)) {
			for (Integer i : s) {
				if (!partitions.get(p).contains(i)) {
					partitions.get(p).add(i);
				}
			}
		} else {
			partitions.put(p, s);
		}
	}

	/**
	 * Partitionwork getter.
	 * @return
	 */
	public Map<Partition, Set<Integer>> getPartitionWork() {
		return partitions;
	}

}

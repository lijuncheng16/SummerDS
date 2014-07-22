package mapReduce.frameWork;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mapReduce.util.WorkerStorage;

/**
 * Execute the reduce task. Sent by the masterserver to reduceservers.
 *
 */
public class ExecuteReduceTask extends WorkerCommand {
	private static final long serialVersionUID = -4562286881268315503L;
	private final ReduceTask mTask;
	private final String workerName;

	public ExecuteReduceTask(ReduceTask task, String workerName) {
		this.mTask = task;
		this.workerName = workerName;
	}

	@Override
	public void run() {
		Socket mSocket = getSocket();
		FileOutputStream out = null;
		FileInputStream in = null;

		String dirInter = WorkerStorage
				.getIntermediateResultsDirectory(workerName);
		String inFileName = "";
		String dirName = WorkerStorage.getFinalResultsDirectory(workerName);
		String outFileName = "";
		
		// recorder for all the key-values.
		Map<String, List<String>> keyValues = new HashMap<String, List<String>>();
		
		// get the key/value pairs for all the partitions
		for (int i = 0; i < 10; i++) {
			inFileName = dirInter + "\\shuffle" + (i+1) + ".txt";
			BufferedReader reader = null;
			try {
				in = new FileInputStream(inFileName);
				reader = new BufferedReader(new InputStreamReader(in));
				
				while(true) {
					String line = reader.readLine();
					if (line == null) {
						break;
					}
					String[] s = line.split("\\W+");
					if (!keyValues.containsKey(s[0])) {
						List<String> list = new ArrayList<String>();
						list.add(s[1]);
						keyValues.put(s[0], list);
					} else {
						keyValues.get(s[0]).add(s[1]);
					}
				}
			} catch (FileNotFoundException e) {
				System.err.println(workerName+": in reduce phase, found shuffle files not complete.");
				
			} catch (IOException e) {
				System.err.println("Readline in Reduce Task error!");
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		// write the final reduce result.
		try {
			outFileName = dirName + "\\result.txt";
			out = new FileOutputStream(outFileName);
			Emitter emitter = new EmitterImpl(out);
			
			for (String key : keyValues.keySet()) {
				Iterator<String> iter = keyValues.get(key).iterator();
				mTask.execute(key, iter, emitter);
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
		
		// write back the result.
		ObjectOutputStream reply;
		try {
			reply = new ObjectOutputStream(mSocket.getOutputStream());
			reply.writeObject(new ReplyReduceDone(outFileName));
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


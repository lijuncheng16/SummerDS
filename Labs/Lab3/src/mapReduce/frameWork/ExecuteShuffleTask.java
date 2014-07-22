package mapReduce.frameWork;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import mapReduce.util.KeyValuePair;
import mapReduce.util.WorkerStorage;

/**
 * Execute shuffle command. Sent from the masterserver to map workers.

 * 
 */
public class ExecuteShuffleTask extends WorkerCommand {
	private static final long serialVersionUID = 6753800866209722807L;

	// The partition-worker map given by the masterserver.
	private final Map<Partition, WorkerInfo> pHolder;
	// The sections of keys that this worker wants.
	private final Set<Integer> sections;
	private final WorkerInfo worker;
	// records the failed workers detected during the networking.
	private final List<WorkerInfo> failedWorkers;
	private final int workerServerNum;

	private final int POOL_SIZE = 3;

	public ExecuteShuffleTask(Map<Partition, WorkerInfo> pHolder,
			Set<Integer> s, WorkerInfo worker, int workerServerNum) {
		this.pHolder = pHolder;
		sections = s;
		this.worker = worker;
		failedWorkers = new ArrayList<WorkerInfo>();
		this.workerServerNum = workerServerNum;
	}

	@Override
	public void run() {
		final ExecutorService mExecutor;
		mExecutor = Executors.newFixedThreadPool(POOL_SIZE);

		Socket masterSocket = getSocket();
		// Construct the ExecuteReduceTask for each worker server.
		File workerInterDir = new File(
				WorkerStorage.getIntermediateResultsDirectory(worker.getName()));
		Map<WorkerInfo, ExecuteDataGetTask> workerLoad = new HashMap<WorkerInfo, ExecuteDataGetTask>();

		for (Partition p : pHolder.keySet()) {
			FileInputStream check = null;
			BufferedReader checkReader = null;
			String pName = workerInterDir + "\\shuffle" + p.getPartitionName()
					+ ".txt";
			String cName = workerInterDir + "\\shuffle" + p.getPartitionName()
					+ "config.txt";
			try {
				// build the working sections.
				check = new FileInputStream(pName);
				FileInputStream checkConfig = new FileInputStream(cName);
				String config = null;
				// read the config line of file.
				checkReader = new BufferedReader(new InputStreamReader(
						checkConfig));
				try {
					config = checkReader.readLine();
				} catch (IOException e) {
				}

				// get the sections that has already got
				List<String> sNames;
				if (config != null) {
					sNames = Arrays.asList(config.split("\\W+"));
				} else {
					sNames = new ArrayList<String>();
					config = "";
				}

				// filter the already got ones.
				Set<Integer> sSet = new HashSet<Integer>();
				sSet.addAll(sections);
				Iterator<Integer> iter = sSet.iterator();
				while (iter.hasNext()) {
					int i = iter.next();
					for (String s : sNames) {
						if (i == Integer.parseInt(s)) {
							iter.remove();
							break;
						}
					}
				}

				// build the task.
				if (sSet.size() != 0) {
					if (workerLoad.containsKey(pHolder.get(p))) {
						workerLoad.get(pHolder.get(p))
								.addPartitionWork(p, sSet);
					} else {
						ExecuteDataGetTask task = new ExecuteDataGetTask(
								pHolder.get(p), workerServerNum);
						task.addPartitionWork(p, sSet);
						workerLoad.put(pHolder.get(p), task);
					}
				}

			} catch (FileNotFoundException e) {
				// the partition is not received yet.
				if (workerLoad.containsKey(pHolder.get(p))) {
					workerLoad.get(pHolder.get(p))
							.addPartitionWork(p, sections);
				} else {
					ExecuteDataGetTask task = new ExecuteDataGetTask(
							pHolder.get(p), workerServerNum);
					task.addPartitionWork(p, sections);
					workerLoad.put(pHolder.get(p), task);
				}

			} finally {
				if (check != null) {
					try {
						check.close();
					} catch (IOException e) {
					}
				}
				if (checkReader != null) {
					try {
						checkReader.close();
					} catch (IOException e) {
					}
				}
			}
		}

		// send ExecuteDataGetTask to each of the worker server.
		// multi-threading.
		List<Future<WorkerInfo>> results = new ArrayList<Future<WorkerInfo>>();
		for (WorkerInfo w : workerLoad.keySet()) {
			Callable<WorkerInfo> call = new SenderWorker(w, workerLoad.get(w));
			Future<WorkerInfo> submit = mExecutor.submit(call);
			results.add(submit);
		}

		// check the result.
		for (Future<WorkerInfo> future : results) {
			try {
				WorkerInfo r = future.get();
				if (r != null) {
					failedWorkers.add(r);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}

		// Write the reponse back to the masterserver.
		ReplyShuffleDone reply = new ReplyShuffleDone();
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(masterSocket.getOutputStream());
			if (failedWorkers.size() != 0) {
				reply.setList(failedWorkers);
			}
			out.writeObject(reply);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// The Callable class used by the run() method.
	private class SenderWorker implements Callable<WorkerInfo> {
		private WorkerInfo receiver;
		private ExecuteDataGetTask task;

		public SenderWorker(WorkerInfo w, ExecuteDataGetTask task) {
			receiver = w;
			this.task = task;
		}

		@Override
		public WorkerInfo call() {
			// Receive the results, labeled by their partition.
			Map<Integer, List<KeyValuePair>> result = new HashMap<Integer, List<KeyValuePair>>();
			Socket workerSocket = null;
			try {
				workerSocket = new Socket(receiver.getHost(),
						receiver.getPort());
				System.out.println("WorkerServer " + worker.getName()
						+ ": connects to " + receiver.getName() + ".");
				ObjectOutputStream out = new ObjectOutputStream(
						workerSocket.getOutputStream());

				out.writeObject(task);
				ObjectInputStream in = new ObjectInputStream(
						workerSocket.getInputStream());

				while (true) {
					Object obj = in.readObject();
					if (obj instanceof KeyValuePair) {
						KeyValuePair pair = (KeyValuePair) obj;
						int parNum = pair.getPar();
						if (result.containsKey(parNum)) {
							result.get(parNum).add(pair);
						} else {
							List<KeyValuePair> list = new ArrayList<KeyValuePair>();
							list.add(pair);
							result.put(parNum, list);
						}
					} else if (obj instanceof ReplyDataSendDone) {
						break;
					} else {
						throw new IOException();
					}
				}
			} catch (IOException e) { // get data failure, return the failed
										// server.
				System.out
						.println("WorkerServer: Cannot connects to the assigned server");
				return receiver;

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				try {
					if (workerSocket != null) {
						workerSocket.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// for each of the partition results, write a file to record it.
			for (int i : result.keySet()) {
				BufferedReader checker = null;
				String workerDir = WorkerStorage
						.getIntermediateResultsDirectory(worker.getName());
				String configFileName = workerDir + "//shuffle" + i
						+ "config.txt";
				String config = "";
				try {
					FileInputStream in = new FileInputStream(configFileName);
					checker = new BufferedReader(new InputStreamReader(in));
					config = checker.readLine();

					List<String> pNames = new ArrayList<String>();
					if (config != null) {
						pNames.addAll(Arrays.asList(config.split("\\s*,\\s*")));
					}

					// sanity check, detect redundant work
					for (Integer sec : sections) {
						if (pNames.contains(sec.toString())) {
							System.err
									.println("Redundant Work done in the Shuffle.");
						} else {
							pNames.add(sec.toString());
						}
					}

					config = "";
					for (String s : pNames) {
						config += s + " ";
					}
				} catch (IOException e) {
					config = "";
					for (Integer sec : sections) {
						config += sec.toString() + " ";
					}
				} finally {
					if (checker != null) {
						try {
							checker.close();
						} catch (IOException e) {
						}
					}
				}

				// renew the config
				PrintWriter p = null;
				try {
					FileOutputStream outConfig = new FileOutputStream(
							configFileName);
					p = new PrintWriter(outConfig);
					p.println(config);
					p.flush();
				} catch (IOException e) {
				} finally {
					if (p != null) {
						p.close();
					}
				}

				// output the key value pair.
				FileOutputStream out = null;
				String outFileName = workerDir + "//shuffle" + i + ".txt";
				try {
					out = new FileOutputStream(outFileName, true);
					Emitter emitter = new EmitterImpl(out);
					List<KeyValuePair> rstList = result.get(i);
					for (KeyValuePair pair : rstList) {
						emitter.emit(pair.getKey(), pair.getValue());
					}
				} catch (IOException e) {
				} finally {
					if (out != null) {
						try {
							out.close();
						} catch (IOException e) {
						}
					}
				}
			}
			// everything is fine, nothing fails.
			return null;
		}
	}
}

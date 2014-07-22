package mapReduce.frameWork;


import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import mapReduce.util.StaffUtils;

/**
 * This class represents the "master server" in the distributed map/reduce
 * framework. The {@link MasterServer} is in charge of managing the entire
 * map/reduce computation from beginning to end. The {@link MasterServer}
 * listens for incoming client connections on a distinct host/port address, and
 * is passed an array of {@link WorkerInfo} objects when it is first initialized
 * that provides it with necessary information about each of the available
 * workers in the system (i.e. each worker's name, host address, port number,
 * and the set of {@link Partition}s it stores). A single map/reduce computation
 * managed by the {@link MasterServer} will typically behave as follows:
 *
 * <ol>
 * <li>Wait for the client to submit a map/reduce task.</li>
 * <li>Distribute the {@link MapTask} across a set of "map-workers" and wait for
 * all map-workers to complete.</li>
 * <li>Distribute the {@link ReduceTask} across a set of "reduce-workers" and
 * wait for all reduce-workers to complete.</li>
 * <li>Write the locations of the final results files back to the client.</li>
 * </ol>
 */
public class MasterServer extends Thread {
    private final int mPort;
    private final List<WorkerInfo> mWorkers;

	private static final int POOL_SIZE = 4;
	private final ExecutorService mExecutor;

	// records the status of every workerserver.
	private final Map<WorkerInfo, Boolean> wStatus;
	// for every partition, keeps a list of workerserver that has its data.
	private final Map<Partition, List<WorkerInfo>> partitionHolders;

	// records the decision of partition's target worker.
	private final Map<Partition, WorkerInfo> partitionHolder;
	// records the decision of reduce worker work.
	private final Map<WorkerInfo, Set<Integer>> reduceWorkerLoad;

	// the final result keeper.
	private final List<MapReduceResultContainer> containerList;

	// lock for controlling the number of map/reduce task.
	private final Object lock = new Object();

	/**
	 * The {@link MasterServer} constructor.
	 * 
	 * @param masterPort
	 *            The port to listen on.
	 * @param workers
	 *            Information about each of the available workers in the system.
	 */
	public MasterServer(int masterPort, List<WorkerInfo> workers) {
		mPort = masterPort;
		mWorkers = workers;
		mExecutor = Executors.newFixedThreadPool(POOL_SIZE);

		// Initialize the worker statuses.
		wStatus = new HashMap<WorkerInfo, Boolean>();
		for (WorkerInfo w : mWorkers) {
			wStatus.put(w, true);
		}

		// Initialize the partition holder info.
		partitionHolders = new HashMap<Partition, List<WorkerInfo>>();
		for (WorkerInfo w : mWorkers) {
			List<Partition> partitions = w.getPartitions();
			for (Partition p : partitions) {
				// String pName = p.getPartitionName();
				if (partitionHolders.containsKey(p)) {
					partitionHolders.get(p).add(w);
				} else {
					List<WorkerInfo> list = new ArrayList<WorkerInfo>();
					list.add(w);
					partitionHolders.put(p, list);
				}
			}
		}

		// Random choose the worker for every partition.
		partitionHolder = new HashMap<Partition, WorkerInfo>();
		for (Partition p : partitionHolders.keySet()) {
			List<WorkerInfo> list = partitionHolders.get(p);
			int i = (new Random()).nextInt(list.size());
			WorkerInfo worker = list.get(i);
			partitionHolder.put(p, worker);
		}

		// Initialize reduce worker
		reduceWorkerLoad = new HashMap<WorkerInfo, Set<Integer>>();
		for (WorkerInfo w : mWorkers) {
			Set<Integer> s = new HashSet<Integer>();
			s.add(mWorkers.indexOf(w));
			reduceWorkerLoad.put(w, s);
		}

		containerList = new ArrayList<MapReduceResultContainer>();
	}

	@Override
	public void run() {
		try {
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket(mPort);
			} catch (IOException e) {
				System.err.println("Could not open server socket on port "
						+ mPort + ".");
				return;
			}

			while (true) {
				try {
					System.out.println("MasterServer: "
							+ serverSocket.toString() + "starts to listen.");
					Socket clientSocket = serverSocket.accept();
					System.out.println("MasterServer: "
							+ clientSocket.toString() + "connects.");
					Runnable work = new WorkerCommandHandler(clientSocket);
					mExecutor.execute(work);
				} catch (IOException e) {
					System.err
							.println("Error while listening for incoming connections.");
					break;
				}
			}

			try {
				serverSocket.close();
			} catch (IOException e) {
				// Ignore because we're about to exit anyway.
			}
		} finally {
			mExecutor.shutdown();
		}
	}

	private class WorkerCommandHandler implements Runnable {
		private final Socket mSocket;
		private final ExecutorService wExecutor;

		public WorkerCommandHandler(Socket socket) {
			mSocket = socket;
			wExecutor = Executors.newFixedThreadPool(4);
		}

		@Override
		public void run() {
			synchronized (lock) { // only one map/reduce can be processed at one
									// time
				ObjectInputStream clientIn;
				MapTask mapTask = null;
				ReduceTask reduceTask = null;

				try {
					clientIn = new ObjectInputStream(mSocket.getInputStream());
					mapTask = (MapTask) clientIn.readObject();
					reduceTask = (ReduceTask) clientIn.readObject();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} finally {
				}

				// First send the clean task to the workerservers. so no
				// previous intermediate results would impact this one.
				List<Future<Integer>> ilist = new ArrayList<Future<Integer>>();
				for (WorkerInfo w : mWorkers) {
					WorkerCommand task = new ExecuteClean(w);
					Callable<Integer> worker = new CleanSendWorker(w, task,
							wStatus);
					Future<Integer> submit = wExecutor.submit(worker);
					ilist.add(submit);
				}

				// The ilist is only for formatting concern. It is of no
				// meaning.
				for (Future<Integer> future : ilist) {
					try {
						future.get();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}

				while (true) {
					System.out.println("The map/reduce procedure starts.");
					// records the progress of completion of map of every
					// partition.
					List<Partition> progress = new ArrayList<Partition>();
					Map<WorkerInfo, List<Partition>> workerLoad = new HashMap<WorkerInfo, List<Partition>>();
					for (Partition p : partitionHolder.keySet()) {
						progress.add(p);
					}

					// loop until all partition has been mapped.
					while (progress.size() != 0) {
						// renew the partition holder, delete the crashed
						// servers.
						for (Partition p : progress) {
							if (wStatus.get(partitionHolder.get(p)) == false) {
								List<WorkerInfo> wList = partitionHolders
										.get(p);
								for (WorkerInfo w : wList) {
									if (wStatus.get(w) == true) {
										partitionHolder.put(p, w);
									}
								}
							}
						}

						// get the partitions for each worker
						for (Partition p : partitionHolder.keySet()) {
							if (workerLoad.containsKey(partitionHolder.get(p))) {
								workerLoad.get(partitionHolder.get(p)).add(p);
							} else {
								List<Partition> list = new ArrayList<Partition>();
								list.add(p);
								workerLoad.put(partitionHolder.get(p), list);
							}
						}

						// for each workerServer, send the request
						List<Future<WorkerInfo>> list = new ArrayList<Future<WorkerInfo>>();
						for (WorkerInfo w : workerLoad.keySet()) {
							WorkerCommand task = new ExecuteMapTask(mapTask,
									workerLoad.get(w), w.getName());
							Callable<WorkerInfo> worker = new MapSendWorker(w,
									task, wStatus);
							Future<WorkerInfo> submit = wExecutor
									.submit(worker);
							list.add(submit);
						}

						// renew the progress
						for (Future<WorkerInfo> future : list) {
							try {
								WorkerInfo w = future.get();
								if (w != null) {
									List<Partition> pList = workerLoad.get(w);
									for (Partition p : pList) {
										progress.remove(p);
									}
								}
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (ExecutionException e) {
								e.printStackTrace();
							}
						}

					}

					// Map phase ends, Shuffle phase begins.
					System.out
							.println("Map work done, please check the intermidiate results.");

					// Renew the reduceWorkerLoad
					Set<Integer> work = null;
					WorkerInfo failedW = null;
					for (WorkerInfo w : reduceWorkerLoad.keySet()) {
						if (wStatus.get(w) == false) {
							work = reduceWorkerLoad.get(w);
							failedW = w;
							break;
						}
					}
					if (failedW != null) {
						reduceWorkerLoad.remove(failedW);
						for (WorkerInfo w : reduceWorkerLoad.keySet()) {
							reduceWorkerLoad.get(w).addAll(work);
							break;
						}
					}

					// for each workerServer, send the request
					List<Future<List<WorkerInfo>>> list = new ArrayList<Future<List<WorkerInfo>>>();
					for (WorkerInfo w : reduceWorkerLoad.keySet()) {
						ExecuteShuffleTask task = new ExecuteShuffleTask(
								partitionHolder, reduceWorkerLoad.get(w), w,
								mWorkers.size());
						Callable<List<WorkerInfo>> worker = new ShuffleSendWorker(
								w, task, wStatus);
						Future<List<WorkerInfo>> submit = wExecutor
								.submit(worker);
						list.add(submit);
					}

					// check the result
					Boolean shuffleFinished = true;
					for (Future<List<WorkerInfo>> future : list) {
						try {
							List<WorkerInfo> failedList = future.get();
							if (failedList.size() != 0) {
								for (WorkerInfo w : failedList) {
									wStatus.put(w, false);
								}
								shuffleFinished = false;
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (ExecutionException e) {
							e.printStackTrace();
						}
					}

					// Some workers failed in the shuffle process, restart the
					// task again.
					if (!shuffleFinished) {
						continue;
					}

					// Shuffle phase ends, reduce phase starts.
					System.out
							.println("Shuffle phase is done, please check the shuffle result.");

					// for each of the reduce worker, send the worker command.
					List<Future<ReduceResult>> rlist = new ArrayList<Future<ReduceResult>>();
					for (WorkerInfo w : reduceWorkerLoad.keySet()) {
						WorkerCommand task = new ExecuteReduceTask(reduceTask,
								w.getName());
						Callable<ReduceResult> worker = new ReduceSendWorker(w,
								task, wStatus);
						Future<ReduceResult> submit = wExecutor.submit(worker);
						rlist.add(submit);
					}

					// collect the result, restart if some server crashes
					List<MapReduceResultContainer> cList = new ArrayList<MapReduceResultContainer>();
					Boolean pass = true;
					for (Future<ReduceResult> future : rlist) {
						try {
							ReduceResult w = future.get();
							if (w.getFileName() == null) { // some server has
															// crashed.
								wStatus.put(w.getWorker(), false);
								pass = false;
								break;
							} else {
								cList.add(new MapReduceResultContainer(w
										.getWorker().getHost(), w.getWorker()
										.getPort(), w.getFileName()));
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (ExecutionException e) {
							e.printStackTrace();
						}
					}
					if (pass == false) {
						continue;
					}

					containerList.addAll(cList);

					// All phases are done, reduce work finished. Please check
					// the final results.
					System.out
							.println("All phases are done! Please check the final result!");

					break;
				}

				// write back the reponse to the clientserver.
				ObjectOutputStream response = null;
				try {
					response = new ObjectOutputStream(mSocket.getOutputStream());
					response.writeObject(new ReplyDone(containerList));
				} catch (NotSerializableException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						mSocket.close();
					} catch (IOException e) {
					}
				}

				try {
					wExecutor.shutdown();
					wExecutor.awaitTermination(10, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
				}
			}
		}
	}

    /********************************************************************/
    /***************** STAFF CODE BELOW. DO NOT MODIFY. *****************/
    /********************************************************************/

    /**
     * Starts the master server on a distinct port. Information about each
     * available worker in the distributed system is parsed and passed as an
     * argument to the {@link MasterServer} constructor. This information can be
     * either specified via command line arguments or via system properties
     * specified in the <code>master.properties</code> and
     * <code>workers.properties</code> file (if no command line arguments are
     * specified).
     */
    public static void main(String[] args) {
        StaffUtils.makeMasterServer(args).start();
    }

}

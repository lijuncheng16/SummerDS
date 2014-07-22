package mapReduce.frameWork;

/**
 * Reduce result wrapper. Stores the worker and filename inside.
 *
 */
public class ReduceResult {
	private final WorkerInfo worker;
	private final String fileName;
	
	
	public ReduceResult(WorkerInfo receiver, String fileName) {
		this.fileName = fileName;
		this.worker = receiver;
	}

	/**
	 * filename getter.
	 * @return
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * workerInfo getter.
	 * @return
	 */
	public WorkerInfo getWorker() {
		return worker;
	}

}

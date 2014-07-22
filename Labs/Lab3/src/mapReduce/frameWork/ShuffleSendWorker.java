package mapReduce.frameWork;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The sender of {@link ExecuteShuffleTask}, which executes the shuffle work.
 * 
 * 
 */
public class ShuffleSendWorker extends SendWorker<List<WorkerInfo>> {
	// a recorder of the failedworkers.
	private final List<WorkerInfo> failedWorkers;

	public ShuffleSendWorker(WorkerInfo w, WorkerCommand work,
			Map<WorkerInfo, Boolean> wStatus) {
		super(w, work, wStatus);
		failedWorkers = new ArrayList<WorkerInfo>();
	}

	/**
	 * Interpret the response. Extract all the failed workers from the response.
	 */
	@Override
	public void readResult(Object reply) throws IOException {
		if (!(reply instanceof ReplyShuffleDone)) {
			System.err.println("Unknown result from ExecuteShuffleTask");
			throw new IOException();
		}
		ReplyShuffleDone result = (ReplyShuffleDone) reply;
		failedWorkers.addAll(result.getList());
	}

	/**
	 * return the failedworkers.
	 */
	@Override
	public List<WorkerInfo> getSuccessResult() {
		return failedWorkers;
	}

	/**
	 * same as {@link #getSuccessResult()}.
	 */
	@Override
	public List<WorkerInfo> getFailureResult() {
		return failedWorkers;
	}

}

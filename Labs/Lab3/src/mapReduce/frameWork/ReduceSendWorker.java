package mapReduce.frameWork;

import java.io.IOException;
import java.util.Map;

/**
 * The sender of {@link ExecuteReduceTask}, which do the reduce work. SendWorker
 * returns a {@link ReduceResult} object which rewraps the reduce result sent
 * from the workerserver as {@link ReplyReduceDone} object.
 * 
 */
public class ReduceSendWorker extends SendWorker<ReduceResult> {

	private ReplyReduceDone result = null;

	public ReduceSendWorker(WorkerInfo w, WorkerCommand work,
			Map<WorkerInfo, Boolean> wStatus) {
		super(w, work, wStatus);
	}

	/**
	 * Interpret the result.
	 */
	@Override
	public void readResult(Object reply) throws IOException {
		if (!(reply instanceof ReplyReduceDone)) {
			System.out.println("Reduce workerserver returns unknown result");
			throw new IOException();
		}
		result = (ReplyReduceDone) reply;
	}

	/**
	 * Extract the filename from the result, wraps it with the information of
	 * the worker as the reduceresult.
	 */
	@Override
	public ReduceResult getSuccessResult() {
		return new ReduceResult(getReceiver(), result.getFileName());
	}

	/**
	 * The same as {@link #getSuccessResult}, but the result is null.
	 */
	@Override
	public ReduceResult getFailureResult() {
		return new ReduceResult(getReceiver(), null);
	}

}

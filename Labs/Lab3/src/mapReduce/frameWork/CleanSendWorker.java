package mapReduce.frameWork;

import java.io.IOException;
import java.util.Map;

/**
 * The sender of {@link ExecuteClean}. Which clean the intermediate results
 * folder of the specific worker. This command would be sent after the
 * map/reduce job is done form masterserver to workerserver. The return type is
 * Integer but actually it has no meaning, just format conforming.
 * 
 */
public class CleanSendWorker extends SendWorker<Integer> {

	public CleanSendWorker(WorkerInfo w, WorkerCommand work,
			Map<WorkerInfo, Boolean> wStatus) {
		super(w, work, wStatus);
	}

	/**
	 * do nothing.
	 */
	@Override
	public void readResult(Object reply) throws IOException {
	}

	/**
	 * do nothing.
	 */
	@Override
	public Integer getSuccessResult() {
		return null;
	}

	/**
	 * return null, because the program would end anyway.
	 */
	@Override
	public Integer getFailureResult() {
		return null;
	}

}

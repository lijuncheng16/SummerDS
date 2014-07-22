package mapReduce.frameWork;

import java.io.IOException;
import java.util.Map;

/**
 * The sender of {@link ExecuteMapTask}, which do the map worker. The return
 * type is WorkerInfo class. Which tells whether the map work is normally done.
 * 
 */
public class MapSendWorker extends SendWorker<WorkerInfo> {

	public MapSendWorker(WorkerInfo w, WorkerCommand work,
			Map<WorkerInfo, Boolean> wStatus) {
		super(w, work, wStatus);
		// TODO Auto-generated constructor stub
	}

	/**
	 * The ExecuteMapTask workercommand should return a {@link ReplyMapDone}
	 * object. Throw an exception if the return type is not the right class.
	 */
	@Override
	public void readResult(Object reply) throws IOException {
		if (!(reply instanceof ReplyMapDone)) {
			System.out.println("Map workerserver returns unknown result");
			throw new IOException();
		}
	}

	/**
	 * If success, get the successful worker and tell the masterserver this part
	 * of the work has been done.
	 */
	@Override
	public WorkerInfo getSuccessResult() {
		return getReceiver();
	}

	/**
	 * If failure, return nothing.
	 */
	@Override
	public WorkerInfo getFailureResult() {
		return null;
	}
}

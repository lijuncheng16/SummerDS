package mapReduce.frameWork;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Reply class sent by shuffleworker when shuffle is done. Containing a
 * list of failed workers that the shuffleworker detected.
 *
 */
public class ReplyShuffleDone implements Serializable{
	private static final long serialVersionUID = 1906861523502161087L;
	private final List<WorkerInfo> failedList;
	
	public ReplyShuffleDone() {
		failedList = new ArrayList<WorkerInfo>();
	}
	
	/**
	 * set the list of the failed workers.
	 * @param l
	 */
	public void setList(List<WorkerInfo> l) {
		failedList.addAll(l);
	}
	
	/**
	 * Failed workerlist getter.
	 * @return
	 */
	public List<WorkerInfo> getList() {
		return failedList;
	}
}

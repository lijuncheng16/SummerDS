package mapReduce.frameWork;

import java.io.Serializable;
import java.util.List;

/**
 * A reply class that is sent from masterserver to clientserver when
 * the map/reduce job is done. This class contains a list of 
 * {@link MapReduceResultContainer} objects which wraps the map/reduce
 * result information.
 *
 */
public class ReplyDone implements Serializable{
	private static final long serialVersionUID = -5909085122867650833L;
	private final List<MapReduceResultContainer> result;
	
	public ReplyDone(List<MapReduceResultContainer> containerList) {
		// TODO Auto-generated constructor stub
		result = containerList;
	}

	/**
	 * Get the result.
	 * @return
	 * 		{@link MapReduceResultContainer} list.
	 */	
	public List<MapReduceResultContainer> getFileList() {
		return result;
	}
}

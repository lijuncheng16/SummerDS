package mapReduce.frameWork;

import java.io.Serializable;

/**
 * A reply class sent by reduceworker when reduce is done. It contains the
 * full-path name of the output file.
 * 
 */
public class ReplyReduceDone implements Serializable {
	private static final long serialVersionUID = 8838274958410624917L;
	String resultFileName;

	public ReplyReduceDone(String outFileName) {
		resultFileName = outFileName;
	}

	/**
	 * filename getter.
	 * @return
	 */
	public String getFileName() {
		return resultFileName;
	}
}

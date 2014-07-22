package mapReduce.frameWork;

import java.io.Serializable;

/**
 * A data wrapper class. One MapReduceResultContainer instance holds the
 * host string, port number and the full-path filename of the result in
 * the server.
 *
 */
public class MapReduceResultContainer implements Serializable{
	private static final long serialVersionUID = 4803096077034971071L;
	private final String host;
	private final int port;
	private final String outFileName;
	
	public MapReduceResultContainer(String host, int port, String outFileName) {
		this.host = host;
		this.port = port;
		this.outFileName = outFileName;
	}
	
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getFileName() {
		return outFileName;
	}
}

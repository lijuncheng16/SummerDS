package mapReduce.frameWork;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * The concrete class which implements the {@link Emitter}
 * interface.
 * 
 *
 */
public class EmitterImpl implements Emitter{
	PrintWriter out;
	
	public EmitterImpl(FileOutputStream out) {
		this.out = new PrintWriter(out);
	}
	
	@Override
	public void emit(String key, String value) throws IOException {
		out.println(key + " " + value);
		out.flush();
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

}

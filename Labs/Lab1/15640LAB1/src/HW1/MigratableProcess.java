package HW1;
import java.lang.Runnable;
import java.lang.Thread;
import java.io.Serializable;


public interface MigratableProcess extends Runnable, Serializable{
	public final String DEFAULT_PROCESS_NAME = "Process name not assigned"; 
	public String processName = DEFAULT_PROCESS_NAME;
	
	//must be called before object is serialized
	// so that it can enter known safe state
	public void suspend(); 
	
	public void migrate();
	
	public void remove();
	
	// this should at least return class name and 
	// a set of params with which the method was called 
	public String toString(String paramArray[]);
	

}

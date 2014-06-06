package dshomework.network;
import java.lang.Runnable;
import java.lang.Thread;
import java.io.Serializable;


public abstract class MigratableProcess implements Runnable, Serializable{

	String processName;
	
	//must be called before object is serialized
	// so that it can enter known safe state
	public abstract void suspend(); 
	
	public abstract void migrate();
	
	public abstract void remove();
	
	public void setName(String name){
		this.processName = name;
		
	}
	
	public String getName(){
		return this.processName;
	}
	
	
	// this should at least return class name and 
	// a set of params with which the method was called 
	public String toString(){
		return "";
	};
	
	
	
	

}

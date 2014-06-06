package dshomework.network;




import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;


public class TestClient {
	public static void main(String args[]) throws Exception{
		/*
		//
		String processInform = "GrepProcess of C:\\input.txt C:\\output\\output.txt";
		System.out.println(processInform);
//		processInform.replaceFirst(regex, replacement)
		System.out.println(processInform.substring(0,processInform.indexOf(' '))); // "72"
		String [] ok = processInform.substring(processInform.indexOf(' ')+1).split(" "); // "tocirah sneab"
		
		/*GrepProcess r = new GrepProcess(arr);
		Thread rp = new Thread(r);
		rp.start();
		*/
		String[] arrv = {"encrypt","C:input.txt","C:/output/output.txt"};
		// @referred to http://stackoverflow.com/questions/2126714/java-get-all-variable-names-in-a-class
		Class<?> userClass = Class.forName("processmanager.EncryptProcess");
		Constructor<?> constructorNew = userClass.getConstructor(String[].class);
		MigratableProcess instance = (MigratableProcess)constructorNew.newInstance((Object)arrv);
		Thread pp = new Thread(instance);
		pp.start();  //comment this to launch
		
		String hostName = "localhost";
        int portNumber = Server.INITIAL_PORT;      
        Thread.sleep(2000); //comment this to launch and uncomment to migrate
        Socket echoSocket = new Socket(hostName, portNumber);

        ObjectOutputStream outObj = new ObjectOutputStream(echoSocket.getOutputStream());
        Thread.sleep(1000); //comment this to launch and uncomment to migrate
        instance.suspend(); //comment this to launch and uncomment to migrate
        //delete process from here once 
        pp=null;
        outObj.writeObject(instance);
        //provide timeout so that object is sent smoothly
        Thread.sleep(1000);
        
	}
	
	
}
/*
class TestObject implements MigratableProcess {
	/**
	 * 
	 
	private static final long serialVersionUID = -4566509411963559780L;
	public int int1=1;
	public int int2=2;
	private volatile boolean suspending;
	
	public TestObject(){
		
	}
	public TestObject(int a, int b){
		int1 =0;
		int2=b;
	}
	
	@Override
	public void run(){
		while(!suspending){
			System.out.println(int1++);
			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
				e.printStackTrace();	
		}	
	}
	}

	@Override
	public void suspend() {
		this.suspending=!this.suspending;
		
	}
	@Override
	public void migrate() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String toString(String[] paramArray) {
		// TODO Auto-generated method stub
		return null;
	}}
*/
//
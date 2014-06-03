package HW1;



import grepprocess.GrepProcess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import processmanager.MigratableProcess;

public class TestClient {
	public static void main(String args[]) throws Exception{
		String[] arr = {"grepprocess","C:/test.txt","C:/javastuff/output.txt"};
		GrepProcess r = new GrepProcess(arr);
		Thread rp = new Thread(r);
		rp.start();
		
		String hostName = "localhost";
        int portNumber = Server.INITIAL_PORT;      
        Thread.sleep(2000);
        Socket echoSocket = new Socket(hostName, portNumber);
        //open print stream
        //PrintWriter outToServer = new PrintWriter(echoSocket.getOutputStream(), true);
        // open in stream
        //BufferedReader inFromServer = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));

        // standard input stream for user input
        //BufferedReader stdUserInput = new BufferedReader(new InputStreamReader(System.in));
        // store input
        
        ObjectOutputStream outObj = new ObjectOutputStream(echoSocket.getOutputStream());
        //Thread.sleep(1000);
        r.suspend();
        //rp.stop();
        rp=null;
        outObj.writeObject(r);
        
        Thread.sleep(1000);
        //Thread.sleep(1000);
        //System.out.println("Resume");
        //Field field = r.getClass().getDeclaredField("suspending");
        //field.setAccessible(true);
        //field.setBoolean(r, false);
        
        //
        //rp.stop();
        //echoSocket.close();
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
//reflectoin part was done from http://stackoverflow.com/questions/2126714/java-get-all-variable-names-in-a-class TODO
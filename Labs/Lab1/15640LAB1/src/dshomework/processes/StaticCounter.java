package dshomework.processes;


import java.awt.Toolkit;
import java.util.Timer;
import java.util.TimerTask;

import dshomework.network.MigratableProcess;

public class StaticCounter extends MigratableProcess{
	private volatile boolean suspending;
	Toolkit toolkit;
	Timer timer;
	private String query;
	private int interval;
	int counter = 0;
	
	
	public StaticCounter(String args[]) throws Exception
	{
		
			System.out.println("from grep: arr len ="+args.length);
			System.out.println("usage: GrepProcess <queryString> <inputFile> <outputFile>");
			throw new Exception("Invalid Arguments");
		
	}
	
	public void run() {
		// Auto-generated method stub
		while (!suspending) {
			 System.out.println(counter);
			 try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Auto-generated catch block
				e.printStackTrace();
			}
			    counter++;
		}
		suspending = false;
	}

	@Override
	public void suspend() {
		suspending = true;
		while (suspending);
		
	}

	@Override
	public void migrate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}



}
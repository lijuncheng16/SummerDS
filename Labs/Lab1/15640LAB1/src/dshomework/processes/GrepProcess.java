package dshomework.processes;

import java.io.PrintStream;
import java.io.EOFException;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.Thread;
import java.lang.InterruptedException;

import dshomework.network.TransactionalFileInputStream;
import dshomework.network.TransactionalFileOutputStream;



public class GrepProcess extends MigratableProcess
{
	private TransactionalFileInputStream  inFile;
	private TransactionalFileOutputStream outFile;
	private String query;
	public int i = 0;
	private volatile boolean suspending;
	String[] passedArgs;

	public GrepProcess(String args[]) throws Exception
	{
		if (args.length != 3) {
			System.out.println("from grep: arr len ="+args.length);
			System.out.println("usage: GrepProcess <queryString> <inputFile> <outputFile>");
			throw new Exception("Invalid Arguments");
		}
		passedArgs=args;
		query = args[0];
		inFile = new TransactionalFileInputStream(args[1]);
		outFile = new TransactionalFileOutputStream(args[2], false);
	}

	public void run()
	{
		PrintStream out = new PrintStream(outFile);
		DataInputStream in = new DataInputStream(inFile);

		try {
			while (!suspending) {
				@SuppressWarnings("deprecation")
				String line = in.readLine();
				System.out.println(line+"+READ");
				if (line == null) {
				  System.exit(1);
				}
				
				if (line.contains(query)) {
					out.println(line);
					//System.out.println(line);
					//System.out.println(i++);
				}
				
				// Make grep take longer so that we don't require extremely large files for interesting results
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// ignore it
				}
			}
		} catch (EOFException e) {
			//End of File
		} catch (IOException e) {
			System.out.println ("GrepProcess: Error: " + e);
		}

	
		suspending = false;
	}

	public void suspend()
	{
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
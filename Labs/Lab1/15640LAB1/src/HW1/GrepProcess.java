package HW1;


import java.io.InputStream;
import java.io.PrintStream;
import java.io.EOFException;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.Thread;
import java.lang.InterruptedException;

public class GrepProcess implements MigratableProcess
{
	private TransactionalFileInputStream  inFile;
	private TransactionalFileOutputStream outFile;
	private String query;
	public  int intCount=0;

	private volatile boolean suspending;

	public GrepProcess(String args[]) throws Exception
	{
		if (args.length != 3) {
			System.out.println("usage: GrepProcess <queryString> <inputFile> <outputFile>");
			//throw new Exception("Invalid Arguments");
		}
		
		query = args[0];
		inFile = new TransactionalFileInputStream(args[1]);
		
		//we assume that this implmentation is same to the FileoutStream implementation
		// which takes a boolean for an append option
		// @param - true implies file will be written from the end
		// @param - false implies file will be written from the beginning 
		outFile = new TransactionalFileOutputStream(args[2], false);
	}


	public void run()
	{
		PrintStream out = new PrintStream(outFile);
		//replace deprecated methods
		BufferedReader br = new BufferedReader(new InputStreamReader(inFile));

		try {
			while (!suspending) {
				String line = br.readLine();
				System.out.println("Line read :"+line);
				Thread.sleep(400);
				
				if (line!=null && line.contains(query)) {
					out.println(line);
				}

					if (line == null) break;
				
				// Make grep take longer so that we don't require extremely large files for interesting results
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// ignore it
				}
			}
		} catch (EOFException e) {
			//End of File
		} catch (IOException e) {
			System.out.println ("GrepProcess: Error: " + e);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		suspending = false;
	}//end of run

	public void suspend()
	{
		suspending = true;
		System.out.println("Nonnative suspend");
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
	
	

	
	@Override
	public String toString(String[] paramArray) {
		// TODO Auto-generated method stub
		return null;
	}
	

}
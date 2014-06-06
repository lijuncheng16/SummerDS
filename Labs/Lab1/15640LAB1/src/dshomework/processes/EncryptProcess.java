package dshomework.processes;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;

import dshomework.network.MigratableProcess;
import dshomework.network.TransactionalFileInputStream;
import dshomework.network.TransactionalFileOutputStream;




public class EncryptProcess extends MigratableProcess {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2817852151280585348L;
	/**
	 * 
	 */
	private String mode;
	private String inputFile;
	private String outputFile;
	private volatile boolean suspending = false;
	private boolean finished = false;
	private int count;

	/* transactionalIO */
	private TransactionalFileInputStream inStream;
	private TransactionalFileOutputStream outStream;


	public EncryptProcess(String[] args) throws Exception {
		if (args.length != 3
				|| (!args[0].equals("encrypt") && !args[0].equals("decrypt"))) {
			System.out
					.println("usage: EncryptProcess <option> <inputfile> <outputfile>");
			System.out.println("options:");
			System.out.println("\to encrypt: encrypting the input file and output");
			System.out.println("\to decrypt: decrypting the input file and output");
			throw new Exception("Invalid arguments");
		}

		if (!new File(args[1]).isFile()) {
			System.out.println("Not a valid file");
			throw new Exception("Invalid arguments");
		}
		mode = args[0];
		inputFile = args[1];
		outputFile = args[2];
		count = 0;

		inStream = new TransactionalFileInputStream(inputFile);
		outStream = new TransactionalFileOutputStream(outputFile,false);

	}

	
	@Override
	public void run() {
		suspending = false;
		DataInputStream in = new DataInputStream(inStream);
		DataOutputStream out = new DataOutputStream(outStream);

		char current = '\0';
		char newchar = '\0';
		while (!suspending && !finished) {

			try {
				current = in.readChar();
			} catch (EOFException eof) {
				System.out.println("Finished processing");
				finished = true;
				break;
			} catch (IOException eio) {
				eio.printStackTrace();
			}

			if (mode.equals("encrypt")) {
				newchar = (char) (current + 8);
			} else if (mode.equals("decrypt")){
				newchar = (char) (current - 8);
			}

			try {
				out.writeChar(newchar);
				count++;
			} catch (IOException e1) {
				
				e1.printStackTrace();
			}
			try {
				Thread.sleep(100);		// Slow down the process to allow migration
			} catch (InterruptedException e1) {
				
				e1.printStackTrace();
			}
			
		}

		try {
			inStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			outStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		suspending = false;
	}

	@Override
	public void suspend() {
		suspending = true;
		while (suspending && !finished);
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

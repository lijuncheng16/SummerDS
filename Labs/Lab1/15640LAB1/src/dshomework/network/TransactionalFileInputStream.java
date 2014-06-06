package dshomework.network;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
 
 public class TransactionalFileInputStream extends InputStream implements java.io.Serializable{
	public String fileName;
	int offset=0;
	int i;
	char c;
	public TransactionalFileInputStream(String fileName){
		offset=0;
		this.fileName = fileName;
		
	}
 
 	@Override //this method was done while referring to http://www.tutorialspoint.com/java/io/inputstream_read.htm
 	public int read()  {
 		//declare variables
 		int a =-1;
 		FileInputStream fileInput = null;
 		try{
 			//open file
	 		File dir = new File(fileName);
	 		//open an write stream 
	 		fileInput = new FileInputStream(dir);
	 		//skip to the correct location
	 		fileInput.skip(offset);
	 		//increment offset and read the new character;
	 		offset = offset + 1;
	 		 a =  fileInput.read();
	 	}
 		catch(Exception e){
 			e.printStackTrace();
 		}finally{
 				try {
 					//no matter what, close the output stream
 					fileInput.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

 		}
 		//return the read value 
		return a;
 		
 	}
 
 
 }
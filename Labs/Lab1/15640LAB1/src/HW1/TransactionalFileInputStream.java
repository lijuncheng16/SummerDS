package HW1;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
 
 public class TransactionalFileInputStream extends InputStream implements java.io.Serializable{
	public String fileName;
	int offset;
	int i;
	char c;
	public TransactionalFileInputStream(String fileName){
		offset=0;
		this.fileName = fileName;
		
	}

 
 	@Override //this method was done while referring to http://www.tutorialspoint.com/java/io/inputstream_read.htm
 	public int read()  {
 		RandomAccessFile raf = null;
 		//create new input stream
 		int p = -1;
 		// @referred to: http://tutorials.jenkov.com/java-io/file.html
 		try{
 		File dir = new File(fileName);
 		raf = new RandomAccessFile(dir, "r");
 		raf.seek(offset);
 		//read till end of stream
 		
 		try{
 			p = raf.readInt();
 			offset = offset + 1;
 		}catch(EOFException e){
 			System.out.println("EOF EXCEPTION");
 			System.out.println("Read int : "+p);
 			return p;
 			
 		}
 		//raf.close();
 		
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}finally{
 			try {
				raf.close();
				System.out.println("Transactional input closed");
				return p;
			} catch (IOException e) {	
				System.out.println("Transactional Input error");
				e.printStackTrace();
				
			}
 		}
		return p;
 		
 	}
 
 
 }
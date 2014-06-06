package dshomework.network;

import java.lang.reflect.Constructor;



import com.sun.xml.internal.fastinfoset.util.StringArray;

import dshomework.processes.MigratableProcess;



public class TIOtest {
	public static void main(String atgs[]) throws Exception{
		String[] arrv = {"of","C:/test.txt","C:/javastuff/output.txt"};
//		GrepProcess gp = new GrepProcess(arrv);
//		new Thread(gp).start();	
		
		Class<?> userClass = Class.forName("dshomework.processes.GrepProcess");
		Constructor<?> constructorNew = userClass.getConstructor(String[].class);
		MigratableProcess instance = (MigratableProcess)constructorNew.newInstance((Object)arrv);
		new Thread(instance).start();
		
	}
}

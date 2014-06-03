package HW1;

import java.lang.reflect.Constructor;

import processmanager.MigratableProcess;

import com.sun.xml.internal.fastinfoset.util.StringArray;

import grepprocess.GrepProcess;

public class TIOtest {
	public static void main(String atgs[]) throws Exception{
		String[] arrv = {"of","E:/test.txt","E:/javastuff/output.txt"};
		GrepProcess gp = new GrepProcess(arrv);
		new Thread(gp).start();
		
		//Class<?> userClass = Class.forName("edu.cmu.ds.lab1.grepprocess.GrepProcess");
		//Constructor<?> constructorNew = userClass.getConstructor(StringArray.class);
		//Object instance = (Thread)constructorNew.newInstance(arrv);
		//((Thread) instance).start();
		
	}
}

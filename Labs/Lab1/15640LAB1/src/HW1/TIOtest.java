package HW1;

import java.lang.reflect.Constructor;


import com.sun.xml.internal.fastinfoset.util.StringArray;


public class TIOtest {
	public static void main(String atgs[]) throws Exception{
		String[] arrv = {"of","E:/test.txt","E:/javastuff/output.txt"};
		//GrepProcess gp = new GrepProcess(arrv);
		//new Thread(gp).start();
		
		Class<?> userClass = Class.forName("HW1.GrepProcess");
		Constructor<?> constructorNew = userClass.getConstructor(String[].class);
		Object instance = (Thread)constructorNew.newInstance(arrv);
		((Thread) instance).start();
		
	}
}

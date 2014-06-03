package HW1;


public class DecodeMessage {
	
	/* @params - args[] which is an array of strings
	 * @params - a which is the streing to be searched in the input array
	 * @return - int which gives position of 'a' inside args
	 * 			 int>=0 signifies that element exists
	 *			 int=-1 suggests item was not found
	 */			 	
	public static int getParams(String[] args, String a){
		for(int i=0;i<args.length;i++){
			if(args[i].equals(a))
				return i;
		}
		return -1;
	}
}

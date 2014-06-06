package dshomework.processes;

public class test {
	public static void main(String[] args) throws InterruptedException{
		String processInform = "grepprocess.GrepProcess of C:/input.txt C:/output/output3.txt";
		System.out.println(processInform.substring(0,processInform.indexOf(' '))); // "72"
		System.out.println( processInform.substring(processInform.indexOf(' ')+1)); // "tocirah sneab"
		System.out.println("New ");
		String[] yo =processInform.split(" ");
		String result = "";
		String[] finalS = new String[yo.length-1];
		for(int i = 1; i<yo.length;i++){
			finalS[i-1]=yo[i];
		}
		System.out.println(yo[0]+"!");
		for(int i = 0; i<finalS.length;i++){
			System.out.println(finalS[i]);
		}
		
		
		/*Thread tnew= new Tthread();
		tnew.start();
		Thread.sleep(10000);
		tnew.suspend();
		try{tnew.start();}
		catch(Exception e){System.out.println("Suspended process cannot start again!");e.printStackTrace();}
		*/
	}

}

class Tthread extends Thread{
	int count = 0;
	public Tthread(){
		
	}
	public Tthread(int t){
		this.count=t;
	}
	@Override 
	public void run(){
		while(true){
			System.out.println(count++);	
			if(count==3){
				new Tthread(60).start();
			}
			//if(count==10)
			//	this.interrupt();
			//if(count==70)
			//	this.interrupt();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	

}
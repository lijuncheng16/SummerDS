package HW1;

public class test {
	public static void main(String[] args) throws InterruptedException{
		Thread tnew= new Tthread();
		tnew.start();
		Thread.sleep(10000);
		tnew.suspend();
		try{tnew.start();}
		catch(Exception e){System.out.println("Suspended process cannot start again!");e.printStackTrace();}
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
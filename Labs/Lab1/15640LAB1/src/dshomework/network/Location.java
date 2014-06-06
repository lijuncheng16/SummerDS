package dshomework.network;


public class Location {
	public String ipAddress;
	public int socketNumber;
	
	public Location(String newipAddress, int newSocketNumber){
		ipAddress = newipAddress;
		socketNumber = newSocketNumber;
	}
	
	public String getIP(){
		return this.ipAddress;
	}
	
	public int getSocketNumber(){
		return this.socketNumber;
	}
	
	public String toString(){
		return "Location: IP="+ipAddress+" Socket="+socketNumber;
	}
}

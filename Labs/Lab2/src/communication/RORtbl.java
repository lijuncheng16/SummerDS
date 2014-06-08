package communication;

import java.util.*;

import registry.RemoteObjectRef;

//This is simple. ROR needs a new object key for each remote object (or its skeleton). 
//This can be done easily, for example by using a counter.
//We also assume a remote object implements only one interface, which is a remote interface.

public class RORtbl
{
	private int counter = 0;
	// TODO Hashtable

	// make a new table. 
	public RORtbl()
	{
	}

	// add a remote object to the table. 
	// Given an object, you can get its class, hence its remote interface.
	// Using it, you can construct a ROR. 
	// The host and port are not used unless it is exported outside.
	// In any way, it is better to have it for uniformity.
	public void addObj(String host, int port, Object o)
	{
	}

	// given ror, find the corresponding object.
	public Object findObj(RemoteObjectRef ror)
	{
		// if you use a hash table this is easy.
		return null;
	}
}
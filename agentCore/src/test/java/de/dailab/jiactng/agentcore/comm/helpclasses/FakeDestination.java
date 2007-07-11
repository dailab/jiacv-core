package de.dailab.jiactng.agentcore.comm.helpclasses;

import javax.jms.Destination;

public class FakeDestination implements Destination {
	String _name = "";

	public FakeDestination(String name){
		_name = name;
	}

	public String getName(){
		return _name;
	}
	
	public String toString(){
		return _name;
	}
}

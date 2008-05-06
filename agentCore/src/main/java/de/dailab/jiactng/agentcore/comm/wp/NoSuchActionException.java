package de.dailab.jiactng.agentcore.comm.wp;

import de.dailab.jiactng.agentcore.knowledge.IFact;

@SuppressWarnings("serial")
public class NoSuchActionException extends RuntimeException implements IFact{
	
	public NoSuchActionException(String s){
		super(s);
	}
	
}
package de.dailab.jiactng.agentcore.comm.wp.helpclasses;

import de.dailab.jiactng.agentcore.knowledge.IFact;

@SuppressWarnings("serial")
public class DirectoryAccessException extends RuntimeException implements IFact{
	
	public DirectoryAccessException(String s){
		super(s);
	}
	
}
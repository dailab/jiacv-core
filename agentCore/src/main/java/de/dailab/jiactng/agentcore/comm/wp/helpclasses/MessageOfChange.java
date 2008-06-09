package de.dailab.jiactng.agentcore.comm.wp.helpclasses;

import de.dailab.jiactng.agentcore.knowledge.IFact;

@SuppressWarnings("serial")
public class MessageOfChange implements  IFact{
	private FactSet _additions;
	private FactSet _removals;
	
	public MessageOfChange(FactSet additions, FactSet removals){
		_additions = additions;
		_removals = removals;
	}
	
	public FactSet getAdditions(){
		return _additions;
	}
	
	public FactSet getRemovals(){
		return _removals;
	}
}

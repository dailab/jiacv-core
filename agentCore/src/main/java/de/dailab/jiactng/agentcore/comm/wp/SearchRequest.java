package de.dailab.jiactng.agentcore.comm.wp;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public class SearchRequest implements IFact{

	private IFact _searchTemplate = null;
	private long _creationTime = -1;
	
	public SearchRequest(IFact template) {
		_searchTemplate = null;
		_creationTime = System.currentTimeMillis();
	}

	public IFact getSearchTemplate(){
		return _searchTemplate;
	}
	
	public long getCreationTime(){
		return _creationTime;
	}
	
	synchronized public long getAge(){
		return (System.currentTimeMillis()-_creationTime);
	}
	
}

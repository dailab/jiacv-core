package de.dailab.jiactng.agentcore.comm.wp;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public class SearchRequest implements IFact{

	/**  The Template to search for */
	private IFact _searchTemplate = null;
	
	/** Time of Creation of this SearchRequest in millis */
	private long _creationTime = -1;
	
	/** optional ID used e.g. for tracking within the DirectoryAccessBean */
	private String _ID;
	
	public SearchRequest(IFact template) {
		_searchTemplate = template;
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
	
	public void setID(String id){
		_ID = id;
	}
	
	public String getID(){
		return _ID;
	}
}

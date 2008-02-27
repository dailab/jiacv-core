package de.dailab.jiactng.agentcore.comm.wp;

import java.util.Set;

import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;

public class SearchRequest implements IFact{

	private IFact _searchTemplate = null;
	private Set<AgentDescription> _result = null;
	private ICommunicationAddress _requestingAgent = null;
	
	public SearchRequest(IFact template, ICommunicationAddress requestingAgent) {
		_searchTemplate = null;
		_requestingAgent = requestingAgent;
	}

	public ICommunicationAddress getRequestingAgent(){
		return _requestingAgent;
	}
	
	public IFact getSearchTemplate(){
		return _searchTemplate;
	}
	
	public Set<AgentDescription> getResult(){
		return _result;
	}
	
	public void setResult(Set<AgentDescription> agentDescriptions){
		_result = agentDescriptions;
	}
	
	public void addResult(AgentDescription agentDescription){
		_result.add(agentDescription);
	}
	
	
	
}

package de.dailab.jiactng.agentcore.comm.wp;

import java.util.Set;

import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class SearchRequest implements IFact{

	private IFact _searchTemplate = null;
	private Set<IFact> _result = null;
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
	
	public Set<IFact> getResult(){
		return _result;
	}
	
	public void setResult(Set<IFact> agentDescriptions){
		_result = agentDescriptions;
	}
	
	public void addResult(AgentDescription agentDescription){
		_result.add(agentDescription);
	}
	
	
	
}

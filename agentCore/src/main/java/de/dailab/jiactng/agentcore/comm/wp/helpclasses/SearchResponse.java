package de.dailab.jiactng.agentcore.comm.wp.helpclasses;

import java.util.Set;

import de.dailab.jiactng.agentcore.knowledge.IFact;

@SuppressWarnings("serial")
public class SearchResponse implements IFact {
	
	private SearchRequest _request = null;
	private Set<IFact> _result = null;

	public SearchResponse(SearchRequest request, Set<IFact> result) {
		_result = result;
		_request = request;
	}

	public Set<IFact> getResult(){
		return _result;
	}
	
	public void setResult(Set<IFact> result){
		_result = result;
	}
	
	public SearchRequest getSearchRequest(){
		return _request;
	}
}

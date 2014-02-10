package de.dailab.jiactng.agentcore.comm.helpclasses;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public class TestContent implements IFact {
    
    /** Serial UID */
    private static final long serialVersionUID = 8196690729210036592L;
    
    String _content;

	public TestContent() {
		_content = "This is a T E S T!";
	}
	
	public TestContent(String content){
		_content = content;
	}
	
	public String getContent(){
		return _content;
	}
	

}

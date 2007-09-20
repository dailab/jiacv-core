package de.dailab.jiactng.examples.chat;

import de.dailab.jiactng.agentcore.comm.message.IJiacContent;

public class TestContent implements IJiacContent {
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

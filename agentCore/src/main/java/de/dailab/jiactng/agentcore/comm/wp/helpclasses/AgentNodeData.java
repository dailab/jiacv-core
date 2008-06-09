package de.dailab.jiactng.agentcore.comm.wp.helpclasses;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;

public class AgentNodeData implements IFact {
	
	private ICommunicationAddress _address = null;
	private Long _creationTime = null;
	private String _UUID = null;
	
	public ICommunicationAddress getAddress() {
		return _address;
	}
	public void setAddress(ICommunicationAddress _address) {
		this._address = _address;
	}
	public Long getCreationTime() {
		return _creationTime;
	}
	public void setCreationTime(Long time) {
		_creationTime = time;
	}
	public String getUUID() {
		return _UUID;
	}
	public void setUUID(String uuid) {
		_UUID = uuid;
	}
	
	
}

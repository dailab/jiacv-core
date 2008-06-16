package de.dailab.jiactng.agentcore.comm.wp.helpclasses;


@SuppressWarnings("serial")
public class AgentNodeData implements Comparable<AgentNodeData>{

	private Long _timeoutTime = null;
	private String _UUID = null;

	public Long getTimeoutTime() {
		return _timeoutTime;
	}
	public void setTimeoutTime(Long time) {
		_timeoutTime = time;
	}
	public String getUUID() {
		return _UUID;
	}
	public void setUUID(String uuid) {
		_UUID = uuid;
	}

	@Override
	public int compareTo(AgentNodeData otherNode) {
		return _timeoutTime.compareTo(otherNode.getTimeoutTime());	
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AgentNodeData){
			AgentNodeData agentNode = (AgentNodeData) obj;
			boolean isEqual =  _UUID.equals(agentNode.getUUID());
			if ((agentNode.getTimeoutTime() != null) && (_timeoutTime != null)){
				isEqual = isEqual && agentNode.getTimeoutTime().equals(_timeoutTime);
			}
			return isEqual;
		} else {
			return super.equals(obj);
		}
	}

}

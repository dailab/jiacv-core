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
		// avoid two entries for the same agent node
		if (_UUID.equals(otherNode.getUUID())){
			return 0;
		}

		Long time1 = (_timeoutTime == null)? new Long(Long.MAX_VALUE):_timeoutTime;
		Long time2 = (otherNode.getTimeoutTime() == null)? new Long(Long.MAX_VALUE):otherNode.getTimeoutTime();

		// ordering by UUID in case of same timeout of different agent nodes
		if (time1.equals(time2)) {
			return _UUID.compareTo(otherNode.getUUID());
		}
		// ordering by timeout for different agent nodes
		else {
			return time1.compareTo(time2);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AgentNodeData){
			AgentNodeData agentNode = (AgentNodeData) obj;
			boolean isEqual =  _UUID.equals(agentNode.getUUID());
			return isEqual;
		} else {
			return super.equals(obj);
		}
	}
	
	@Override
	public String toString() {
		return new String("AgentNode UUID: " + _UUID + "; has timeout=" + _timeoutTime);
	}

}

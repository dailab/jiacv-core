package de.dailab.jiactng.agentcore.comm.wp.helpclasses;


/**
 * Meant to store information about known AgentNodes necessary to tell 
 * if they are already known and if they are still alive.
 * Used by the DirectoryAgentNodeBean
 * 
 * @author Martin Loeffelholz
 *
 */
@SuppressWarnings("serial")
public class AgentNodeData implements Comparable<AgentNodeData>{

	/**
	 * A systemtime when this AgentNodeData has to be refreshed
	 * or it is supposed that the given AgentNode has crashed or
	 * communication problems and is therefore not avaible anymore
	 */
	private Long _timeoutTime = null;
	
	/**
	 * The unique identifier for the AgentNode given
	 */
	private String _UUID = null;

	/**
	 * @return the time at which this agentnodedata is timed out
	 */
	public Long getTimeoutTime() {
		return _timeoutTime;
	}
	
	/**
	 * @param time the time at which this data might assumed to be obsolete
	 */
	public void setTimeoutTime(Long time) {
		_timeoutTime = time;
	}
	
	/**
	 * @return The UUID of the AgentNode for which informations are stored within this instance
	 */
	public String getUUID() {
		return _UUID;
	}
	
	/**
	 * @param uuid The UUID of the AgentNode for which informations are stored within this instance
	 */
	public void setUUID(String uuid) {
		_UUID = uuid;
	}

	/**
	 * This method is used by the AgentNodeDataBase to decide where within the database to store this data
	 */
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

	/**
	 * This method is used to decide if two instances of AgentNode store information about the same AgentNode
	 * @param obj the other AgentNodeData to compare with
	 * @return true if both instances of AgentNodeData store information about the same AgentNode
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AgentNodeData){
			AgentNodeData agentNode = (AgentNodeData) obj;
			boolean isEqual =  _UUID.equals(agentNode.getUUID());
			return isEqual;
		} else {
			System.err.println("AgentNodeData-ERROR: AgentNodeData.equals: Other Object was NOT instanceof AgentNodeData!");
			return false;
		}
	}
	
	/**
	 * Returns a String describing all stored informations within this instance
	 */
	@Override
	public String toString() {
		return new String("AgentNode UUID: " + _UUID + "; has timeout=" + _timeoutTime);
	}

}

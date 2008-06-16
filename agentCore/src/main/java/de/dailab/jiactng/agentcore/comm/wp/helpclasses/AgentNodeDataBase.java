package de.dailab.jiactng.agentcore.comm.wp.helpclasses;

import java.util.TreeMap;

public class AgentNodeDataBase {
	private TreeMap<String, AgentNodeData> _addressToAgentNodeDataMap;
	private TreeMap<Long, AgentNodeData> _timeoutToAgentNodeDataMap;
	
	public AgentNodeDataBase() {
		_addressToAgentNodeDataMap = new TreeMap<String, AgentNodeData>();
		_timeoutToAgentNodeDataMap = new TreeMap<Long, AgentNodeData>();
	}
	
	public synchronized void clear(){
		_addressToAgentNodeDataMap.clear();
		_timeoutToAgentNodeDataMap.clear();
	}
	
	public synchronized void put(AgentNodeData otherNode){
		if ((otherNode.getUUID() == null) || (otherNode.getTimeoutTime() == null)){
			System.err.println("AgentNodeDataBase ERROR: AgentNodeData had eith no UUID or TimeoutTime - storage denied!");
			return;
		}
		_addressToAgentNodeDataMap.put(otherNode.getUUID(), otherNode);
		_timeoutToAgentNodeDataMap.put(otherNode.getTimeoutTime(), otherNode);
	}
	
	public synchronized AgentNodeData get(String UUID){
		if (UUID == null){
			return null;
		} else {
			return _addressToAgentNodeDataMap.get(UUID);
		}
	}
	
	public synchronized AgentNodeData get(Long timeout){
		if (timeout == null){
			return null;
		} else {
			return _timeoutToAgentNodeDataMap.get(timeout);
		}
	}
	
	public synchronized Long getFirstTimeout(){
		if (_timeoutToAgentNodeDataMap.isEmpty()){
			return null;
		} else {
			return _timeoutToAgentNodeDataMap.firstKey();
		}
	}
	
	public synchronized AgentNodeData removeFirstTimeoutNode(){
		if (_timeoutToAgentNodeDataMap.isEmpty()) {
			return null;
		} else {
			AgentNodeData otherNode = _timeoutToAgentNodeDataMap.remove(_timeoutToAgentNodeDataMap.firstKey());
			if (otherNode != null) {
				_addressToAgentNodeDataMap.remove(otherNode.getUUID());
			}
			return otherNode;
		}
	}
	
	public synchronized AgentNodeData remove(String UUID){
		if (UUID == null){
			return null;
		} else {
			AgentNodeData otherNode = _addressToAgentNodeDataMap.remove(UUID);
			if (otherNode != null){
				_timeoutToAgentNodeDataMap.remove(otherNode.getTimeoutTime());
			}
			return otherNode;
		}
	}
	
	public boolean isEmpty(){
		return (_addressToAgentNodeDataMap.isEmpty() && _timeoutToAgentNodeDataMap.isEmpty());
	}

}

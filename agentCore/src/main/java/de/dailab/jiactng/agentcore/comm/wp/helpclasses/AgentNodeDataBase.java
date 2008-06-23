package de.dailab.jiactng.agentcore.comm.wp.helpclasses;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

/**
 * 
 * This Class is meant to store AgentNodeData within a Database that's capable of sorting them
 * both by timeouts and UUIDs. It is used within the <code>DirectoryAgentNodeBean</code> to keep track of
 * other AgentNodes that have committed entries to it.
 * 
 * @author Martin Loeffelholz
 *
 */
public class AgentNodeDataBase {
	private TreeMap<AgentNodeData, AgentNodeData> _dataBase;

	public AgentNodeDataBase() {
		_dataBase = new TreeMap<AgentNodeData, AgentNodeData>();
	}
	
	public synchronized void clear(){
		_dataBase.clear();
	}
	
	public synchronized void put(AgentNodeData otherNode){
		if (_dataBase.containsKey(otherNode)){
			_dataBase.remove(otherNode);
			_dataBase.put(otherNode, otherNode);
		} else {
			_dataBase.put(otherNode, otherNode);
		}
	}
	
	public synchronized AgentNodeData get(String UUID){
		AgentNodeData element = new AgentNodeData();
		element.setUUID(UUID);
		
		return _dataBase.get(element);
	}
	
	public synchronized Long getFirstTimeout(){
		if (_dataBase.isEmpty()){
			return null;
		} else {
			return _dataBase.firstKey().getTimeoutTime();
		}
	}
	
	public synchronized AgentNodeData removeFirstTimeoutNode(){
		if (_dataBase.isEmpty()){
			return null;
		} else {
			return _dataBase.remove(_dataBase.firstKey());
		}
	}
	
	public synchronized AgentNodeData remove(String UUID){
		if (UUID == null){
			return null;
		} else {
			AgentNodeData otherNode = new AgentNodeData();
			otherNode.setUUID(UUID);
			return _dataBase.remove(otherNode);
		}
	}
	
	public synchronized Set<String> getUUIDs(){
		Set<String> ids = new HashSet<String>();
		for (AgentNodeData node : _dataBase.keySet()) {
			ids.add(node.getUUID());
		}
		return ids;
	}
	
	public boolean isEmpty(){
		return _dataBase.isEmpty();
	}
	
	public void printDataBase(){
		System.out.println("AgentNode-DataBase is having the following Entries: ");
		int n = 1;
		for (AgentNodeData and : _dataBase.keySet()){
			System.out.println("Entry " + n++ + " reads: " + and);
		}
	}

}

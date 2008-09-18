package de.dailab.jiactng.agentcore.comm.wp.helpclasses;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

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
	private TreeSet<AgentNodeData> _dataBase;

	public AgentNodeDataBase() {
		_dataBase = new TreeSet<AgentNodeData>();
	}
	
	public synchronized void clear(){
		_dataBase.clear();
	}
	
	public synchronized AgentNodeData put(AgentNodeData otherNode){
		AgentNodeData oldNodeData = remove(otherNode.getUUID());
		_dataBase.add(otherNode);
		return oldNodeData;
	}
	
	public synchronized AgentNodeData get(String UUID){
		for (AgentNodeData elem : _dataBase) {
			if (elem.getUUID().equals(UUID)) {
				return elem;
			}
		}
		return null;
	}
	
	public synchronized Long getFirstTimeout(){
		if (_dataBase.isEmpty()){
			return null;
		} else {
			return _dataBase.first().getTimeoutTime();
		}
	}
	
	public synchronized AgentNodeData removeFirstTimeoutNode(){
		if (_dataBase.isEmpty()){
			return null;
		} else {
			AgentNodeData first = _dataBase.first();
			if (_dataBase.remove(first)) {
				return first;
			} else {
				return null;
			}
		}
	}
	
	public synchronized AgentNodeData remove(String UUID){
		if (UUID == null){
			return null;
		} else {
			AgentNodeData oldNodeData = null;
			for (AgentNodeData elem : _dataBase) {
				if (elem.getUUID().equals(UUID)) {
					oldNodeData = elem;
					break;
				}
			}
			if (oldNodeData != null){
				if (_dataBase.remove(oldNodeData)) {
					return oldNodeData;
				}
			}
			return null;
		}
	}
	
	public synchronized Set<String> getUUIDs(){
		Set<String> ids = new HashSet<String>();
		for (AgentNodeData node : _dataBase) {
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
		for (AgentNodeData and : _dataBase){
			System.out.println("Entry " + n++ + " reads: " + and);
		}
	}

}

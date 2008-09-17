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
		printDataBase();
		System.out.println("Putting following element to database " + otherNode);

		// remove old element if exists
		AgentNodeData oldNodeData = null;
		for (AgentNodeData key : _dataBase.keySet()) {
			if (key.getUUID().equals(otherNode.getUUID())) {
				oldNodeData = key;
				break;
			}
		}
		if (oldNodeData != null){
			_dataBase.remove(oldNodeData);
		}

		// add new element
		_dataBase.put(otherNode, otherNode);

		System.out.println("Putted following element to database " + otherNode);
		printDataBase();
	}
	
	public synchronized AgentNodeData get(String UUID){
		for (AgentNodeData key : _dataBase.keySet()) {
			if (key.getUUID().equals(UUID)) {
				return key;
			}
		}
		return null;
	}
	
	public synchronized Long getFirstTimeout(){
		if (_dataBase.isEmpty()){
			return null;
		} else {
			return _dataBase.firstKey().getTimeoutTime();
		}
	}
	
	public synchronized AgentNodeData removeFirstTimeoutNode(){
		printDataBase();
		System.out.println("Removing first element from database");
		if (_dataBase.isEmpty()){
			System.out.println("Database is empty");
			return null;
		} else {
			AgentNodeData result = _dataBase.remove(_dataBase.firstKey());
			System.out.println("Removed first element from database " + result);
			printDataBase();
			return result;
		}
	}
	
	public synchronized AgentNodeData remove(String UUID){
		printDataBase();
		System.out.println("Removing element from database with UUID "+UUID);
		if (UUID == null){
			System.out.println("UUID is unknown");
			return null;
		} else {
			AgentNodeData oldNodeData = null;
			for (AgentNodeData key : _dataBase.keySet()) {
				if (key.getUUID().equals(UUID)) {
					oldNodeData = key;
					break;
				}
			}
			AgentNodeData result = null;
			if (oldNodeData != null){
				result = _dataBase.remove(oldNodeData);
				System.out.println("Removed element from database with UUID "+UUID);
			}
			else {
				System.out.println("No element found in database with UUID "+UUID);
			}

			printDataBase();
			return result;
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

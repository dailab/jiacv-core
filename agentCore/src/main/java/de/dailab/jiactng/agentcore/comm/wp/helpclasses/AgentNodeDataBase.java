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

	/**
	 * Creates a fresh new Database
	 */
	public AgentNodeDataBase() {
		_dataBase = new TreeSet<AgentNodeData>();
	}
	
	/**
	 * removes all entries from the database, so it's as good as new
	 */
	public synchronized void clear(){
		_dataBase.clear();
	}
	
	/**
	 * Puts data about an AgentNode into the Database
	 * 
	 * @param otherNode The Data of the other Node consisting of an UUID and a timeout
	 * @return null if the data was new, or the AgentNodeData stored for that 
	 * 				agentnode so far before it was overwritten with otherNode
	 */
	public synchronized AgentNodeData put(AgentNodeData otherNode){
		AgentNodeData oldNodeData = remove(otherNode.getUUID());
		_dataBase.add(otherNode);
		return oldNodeData;
	}
	
	/**
	 * returns stored data about an AgentNode with the given UUID
	 * or null if there is no such AgentNode within the database
	 * 
	 * @param UUID the UUID of the AgentNode in question
	 * @return 	the AgentNodeData about the AgentNode with the given UUID
	 * 			or null if there is nothing stored about an AgentNode with such an UUID
	 */
	public synchronized AgentNodeData get(String UUID){
		for (AgentNodeData elem : _dataBase) {
			if (elem.getUUID().equals(UUID)) {
				return elem;
			}
		}
		return null;
	}
	
	/**
	 * @return 	the timeout-value of the AgentNode stored within the database
	 * 			that has the lowest timeout-value and so is the first entry
	 * 			where a timeout has to occure
	 */
	public synchronized Long getFirstTimeout(){
		if (_dataBase.isEmpty()){
			return null;
		} else {
			return _dataBase.first().getTimeoutTime();
		}
	}
	
	/**
	 * removes the Data about the AgentNode that's entry has to timeout first
	 * and returns it
	 * @return the AgentNodeData of the AgentNode that will timeout first
	 */
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
	
	/**
	 * Removes the AgentNodeData about the AgentNode with the given UUID
	 * @param UUID the UUID of the AgentNode whichs Data has to be removed
	 * @return 	the AgentNodeData of the AgentNode with the given UUID, 
	 * 			or null if no Data about this AgentNode is stored
	 */
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
	
	/**
	 * @return a Set with the UUIDs of all AgentNodes about which data is stored
	 */
	public synchronized Set<String> getUUIDs(){
		Set<String> ids = new HashSet<String>();
		for (AgentNodeData node : _dataBase) {
			ids.add(node.getUUID());
		}
		return ids;
	}
	
	/**
	 * @return true if no entries are within the database, false otherwise
	 */
	public boolean isEmpty(){
		return _dataBase.isEmpty();
	}
	
	/**
	 * prints the entries from this database using System.out to do so
	 */
	public void printDataBase(){
		 System.out.println("AgentNode-DataBase is having the following Entries: ");
		if (_dataBase.isEmpty()){
			 System.out.println("The Database is Empty.");
		} else {
			int n = 1;
			for (AgentNodeData and : _dataBase){
				 System.out.println("Entry " + n++ + " reads: " + and);
			}
		}
	}

}

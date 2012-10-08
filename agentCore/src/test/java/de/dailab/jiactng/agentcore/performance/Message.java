package de.dailab.jiactng.agentcore.performance;

import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * This class represents a message which the agents ca send
 * 
 * @author Hilmi Yildirim
 *
 */
public class Message implements IFact{
	
	/**
	 * The time of sending this message
	 */
	private long timeStamp;
	
	/**
	 * empty constructor
	 */
	public Message(){
		timeStamp = 0;
	}
	
	/**
	 * constructor of this class
	 * @param message
	 */
	public Message(long timeStamp){
		this.timeStamp = timeStamp;
	}
	
	/**
	 * @return time stamp
	 */
	public long getTimeStamp(){
		return this.timeStamp;
	}
}

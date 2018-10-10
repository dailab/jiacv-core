package de.dailab.jiactng.agentcore.performance;

import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * This class represents a message which the agents ca send
 * 
 * @author Hilmi Yildirim
 *
 */
public class Message implements IFact{
	private static final long serialVersionUID = 5737278004372816216L;
	
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

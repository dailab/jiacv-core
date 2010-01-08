package de.dailab.jiactng.agentcore.directory;

/**
 * Interface for controlling directory via JMX.
 * @author axle
 *
 */
public interface DirectoryAgentNodeBeanMBean {
	//getter, setter
	
	/**
	 * Get the interval the node sends an alive message in milliseconds.
	 * 
	 * @return the time between two alive messages in milliseconds
	 */
	long getAliveInterval();
	
	/**
	 * Set the interval the node sends an alive message in milliseconds.
	 * 
	 * @param interval the time between two alive messages in milliseconds
	 */
	void setAliveInterval(long interval);
	
	/**
	 * Get the interval the node sends an advertisement in milliseconds.
	 * 
	 * @return the time between two advertisements in milliseconds
	 */
	long getAdvertiseInterval();
	
	/**
	 * Set the interval the node sends an advertisement in milliseconds.
	 * 
	 * @param advertiseInterval the time between two advertisements in milliseconds
	 */
	void setAdvertiseInterval(long advertiseInterval);
	
	/**
	 * Whether or not a dump is printed to the console
	 * 
	 * @return if true, the dump is printed to the console
	 */
	boolean isDump();
	
	/**
	 * Set whether the dump should be printed to console.
	 * 
	 * @param dump if true, the dump will be printed to console
	 */
	void setDump(boolean dump);
	
	//sonstige operations auf dem directory
}

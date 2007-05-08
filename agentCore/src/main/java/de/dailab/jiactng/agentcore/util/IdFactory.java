package de.dailab.jiactng.agentcore.util;

/**
 * Collection of ID creation methods.
 * TODO concept for id and naming.
 * 
 * @author axle
 */
public final class IdFactory {

	/**
	 * Constructor hidden to avoid instantiation.
	 */
	private IdFactory() {}

	/**
	 * Creates a id for an agent platform. Remember, platform in TNG is a virtual concept
	 * which acquaints agents with each other regardless of the node they are running on.
	 * 
	 * @param hashcode the hashcode of the agent platform object
	 * @return the id of the agent platform
	 */
	public static String createPlatformId(int hashcode) {
		return new StringBuffer("p:")
			.append(Long.toHexString(System.currentTimeMillis() + hashcode))
			.toString();
	}
	
	/**
	 * Creates a id for an agent node. Remember, agent node coresponds to the Java VM
	 * the agent is living in.
	 * 
	 * @param hashcode the hashcode of the agent node object
	 * @return the id of the agent node
	 */
	public static String createAgentNodeId(int hashcode) {
		return new StringBuffer("n:")
			.append(Long.toHexString(System.currentTimeMillis() + hashcode))
			.toString();
	}
	
	/**
	 * Creates a id for an agent.
	 * 
	 * @param hashcode the hashcode of the agent object
	 * @return the id of the agent
	 */
	public static String createAgentId(int hashcode) {
		return new StringBuffer("a:")
			.append(Long.toHexString(System.currentTimeMillis() + hashcode))
			.toString();
	}
	
	/**
	 * Creates a id for a session.
	 * 
	 * @param hashcode the hashcode of the action or service object.
	 * @return the session id
	 */
	public static String createSessionId(int hashcode) {
		return new StringBuffer("s:")
			.append(Long.toHexString(System.currentTimeMillis() + hashcode))
			.toString();
	}

}

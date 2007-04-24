package de.dailab.jiactng.agentcore.util;

public final class IdFactory {

	/**
	 * Constructor hidden to avoid instantiation.
	 */
	private IdFactory() {}

	/**
	 * Creates a id for an agent node.
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
	 * @param hashcode the hashcode of the agent object
	 * @return the id of the agent
	 */
	public static String createAgentId(int hashcode) {
		return new StringBuffer("a:")
			.append(Long.toHexString(System.currentTimeMillis() + hashcode))
			.toString();
	}
}

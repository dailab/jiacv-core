package de.dailab.jiactng.examples;

import de.dailab.jiactng.agentcore.SimpleAgentNode;

/**
 * This is a starter class to start the JIAC V application node in your IDE for
 * debugging use. Therefore it is <code>final</code> and has just one
 * <code>main</code> method.
 *
 * @author mib
 *
 */
public final class SimpleChatExample {

	/**
	 * The plain main method. Parameters will not be used.
	 *
	 * @param args
	 *          ignored parameters
	 */
	public static void main(final String[] args) {
		/*
		 * This starter is just for debugging or education use. There is no need to
		 * create a whole class with a single main method to startup a JIAC V agent
		 * node. For productive use, please use a (Maven plugin) appassembler
		 * configuration in the pom.xml.
		 */
		SimpleAgentNode.main(new String[] { "classpath:simplechatnode.xml" });
	}
}

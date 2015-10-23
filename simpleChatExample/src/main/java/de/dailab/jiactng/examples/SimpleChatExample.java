package de.dailab.jiactng.examples;

import de.dailab.jiactng.agentcore.SimpleAgentNode;

public final class SimpleChatExample {

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

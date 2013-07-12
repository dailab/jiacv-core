package de.dailab.jiactng.agentcore.directory;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DirectoryAgentNodeBrokerTest {
	private final static String PATH_TO_NODE_CONFIG = "de/dailab/jiactng/agentcore/directory/";

	public static void main(String[] args) {
//		new ClassPathXmlApplicationContext(PATH_TO_NODE_CONFIG
//				+ "DirectoryNodeBroker1.xml").getBean("DirectoryNode1");

		new ClassPathXmlApplicationContext(PATH_TO_NODE_CONFIG
				+ "DirectoryNodeBroker2.xml").getBean("DirectoryNode2");

//		new ClassPathXmlApplicationContext(PATH_TO_NODE_CONFIG
//				+ "DirectoryNodeBroker3.xml").getBean("DirectoryNode3");
	}
}

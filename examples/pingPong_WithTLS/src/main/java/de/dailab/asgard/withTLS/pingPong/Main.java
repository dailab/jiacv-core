package de.dailab.asgard.withTLS.pingPong;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * With this application it is possible to start two agent nodes.
 * <p>
 * AgentNode1 with Ping-Agent is started with this programm arguments:
 * "/testPingPong02/testPingPong02_node1.xml PingPong_TLS_Node1"
 * <p>
 * AgentNode2 with Pong-Agent is started with this programm arguments:
 * "/testPingPong02/testPingPong02_node2.xml PingPong_TLS_Node2"
 * 
 * @author sse
 */
public class Main {

	/**
	 * First argument: Path to agent node config file <br>
	 * second argument: Node name
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.setProperty("log4j.configuration", "jiactng_log4j.properties");
		startNode(args[0], args[1]);
	}

	private static void startNode(String configfile, String nodename) {
		new ClassPathXmlApplicationContext(configfile).getBean(nodename);
	}
}

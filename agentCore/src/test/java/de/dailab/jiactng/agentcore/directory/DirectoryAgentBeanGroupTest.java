package de.dailab.jiactng.agentcore.directory;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.JIACTestForJUnit4;
import de.dailab.jiactng.agentcore.SimpleAgentNode;

public class DirectoryAgentBeanGroupTest extends JIACTestForJUnit4 {

	private SimpleAgentNode node1 = null;
	private SimpleAgentNode node2 = null;
	private SimpleAgentNode node3 = null;
	private DirectoryAgentNodeBeanStub directoryAgentNodeBeanStub1 = null;
	private DirectoryAgentNodeBeanStub directoryAgentNodeBeanStub2 = null;
	private DirectoryAgentNodeBeanStub directoryAgentNodeBeanStub3 = null;
	
	private final static String PATH_TO_NODE_CONFIG = "de/dailab/jiactng/agentcore/directory/";

	public void init() {
		node1 = (SimpleAgentNode) new ClassPathXmlApplicationContext(
				PATH_TO_NODE_CONFIG + "DirectoryNodeBean1.xml")
				.getBean("DirectoryNodeBean1");
		
		directoryAgentNodeBeanStub1 = node1.findAgentNodeBean(DirectoryAgentNodeBeanStub.class);
		
		node2 = (SimpleAgentNode) new ClassPathXmlApplicationContext(
				PATH_TO_NODE_CONFIG + "DirectoryNodeBean2.xml")
				.getBean("DirectoryNodeBean2");
		
		directoryAgentNodeBeanStub2 = node2.findAgentNodeBean(DirectoryAgentNodeBeanStub.class);
		
		node3 = (SimpleAgentNode) new ClassPathXmlApplicationContext(
				PATH_TO_NODE_CONFIG + "DirectoryNodeBean3.xml")
				.getBean("DirectoryNodeBean3");
		
		directoryAgentNodeBeanStub3 = node3.findAgentNodeBean(DirectoryAgentNodeBeanStub.class);
	}
	
	@Test
	public void notLostTestAndSendingMessagesCorrect(){
		init();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean node1KnowsNode2 = false;
		boolean node1KnowsNode3 = false;
		for(String address : directoryAgentNodeBeanStub1.getAllKnownAgentNodes()){
			if(address.contains(directoryAgentNodeBeanStub2.getAddress())){
				node1KnowsNode2 = true;
			}else if(address.contains(directoryAgentNodeBeanStub3.getAddress())){
				node1KnowsNode3 = true;
			}
		}
		
		assertTrue(node1KnowsNode2);
		assertTrue(node1KnowsNode3);
		
		boolean node2KnowsNode1 = false;
		for(String address : directoryAgentNodeBeanStub2.getAllKnownAgentNodes()){
			if(address.contains(directoryAgentNodeBeanStub1.getAddress())){
				node2KnowsNode1 = true;
			}
		}
		
		assertTrue(node2KnowsNode1);
		
		boolean node3KnowsNode1 = false;
		for(String address : directoryAgentNodeBeanStub3.getAllKnownAgentNodes()){
			if(address.contains(directoryAgentNodeBeanStub1.getAddress())){
				node3KnowsNode1 = true;
			}
		}
		
		assertTrue(node3KnowsNode1);
		
		
		//tests whether messages was sent to correct group
		boolean node1SendsCorrect = true;
		for(String address : directoryAgentNodeBeanStub1.getSendMessagesToGroup()){
			node1SendsCorrect = false;
			for(String groupAddress : directoryAgentNodeBeanStub1.getGroupNames()){
				if(address.contains(groupAddress)){
					node1SendsCorrect = true;
					continue;
				}
			}
			if(!node1SendsCorrect){
				break;
			}
		}
		
		assertTrue(node1SendsCorrect);
		
		boolean node2SendsCorrect = true;
		for(String address : directoryAgentNodeBeanStub2.getSendMessagesToGroup()){
			node2SendsCorrect = false;
			for(String groupAddress : directoryAgentNodeBeanStub2.getGroupNames()){
				if(address.contains(groupAddress)){
					node2SendsCorrect = true;
					continue;
				}
			}
			if(!node2SendsCorrect){
				break;
			}
		}
		
		assertTrue(node2SendsCorrect);
		
		boolean node3SendsCorrect = true;
		for(String address : directoryAgentNodeBeanStub3.getSendMessagesToGroup()){
			node3SendsCorrect = false;
			for(String groupAddress : directoryAgentNodeBeanStub3.getGroupNames()){
				if(address.contains(groupAddress)){
					node3SendsCorrect = true;
					continue;
				}
			}
			if(!node3SendsCorrect){
				break;
			}
		}
		
		assertTrue(node3SendsCorrect);
		
		//tests whether messages was received from correct group
		
		boolean node1ReceivesCorrect = true;
		for(String address : directoryAgentNodeBeanStub1.getReceivedMessagesFromGroup()){
			node1ReceivesCorrect = false;
			for(String groupAddress : directoryAgentNodeBeanStub1.getGroupNames()){
				if(address.contains(groupAddress)){
					node1ReceivesCorrect = true;
					continue;
				}
			}
			if(!node1ReceivesCorrect){
				break;
			}
		}
		
		assertTrue(node1ReceivesCorrect);
		
		boolean node2ReceivesCorrect = true;
		for(String address : directoryAgentNodeBeanStub2.getSendMessagesToGroup()){
			node2ReceivesCorrect = false;
			for(String groupAddress : directoryAgentNodeBeanStub2.getGroupNames()){
				if(address.contains(groupAddress)){
					node2ReceivesCorrect = true;
					continue;
				}
			}
			if(!node2ReceivesCorrect){
				break;
			}
		}
		
		assertTrue(node2ReceivesCorrect);
		
		boolean node3ReceivesCorrect = true;
		for(String address : directoryAgentNodeBeanStub3.getSendMessagesToGroup()){
			node3ReceivesCorrect = false;
			for(String groupAddress : directoryAgentNodeBeanStub3.getGroupNames()){
				if(address.contains(groupAddress)){
					node3ReceivesCorrect = true;
					continue;
				}
			}
			if(!node3ReceivesCorrect){
				break;
			}
		}
		
		assertTrue(node3ReceivesCorrect);
	}

	
	@After
	public void clean(){
		try {

			node1.doStop();
			node2.doStop();
			node3.doStop();
			Thread.sleep(1000);
			node1.cleanup();
			node2.cleanup();
			node3.cleanup();
		} catch (Exception e){
			//do nothing
		}

	}
}

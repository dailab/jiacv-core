package de.dailab.jiactng.agentcore.comm;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.IAgentNode;
import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.comm.helpclasses.DummyTransport;
import de.dailab.jiactng.agentcore.comm.helpclasses.MemoryExposingBean;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.message.ObjectContent;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.knowledge.IMemory;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;
import junit.framework.TestCase;

@SuppressWarnings("serial")
public class CommunicationBeanMessagingTest extends TestCase implements SpaceObserver<IFact>{

	private static final JiacMessage MESSAGE_TEMPLATE;
	private static final String MESSAGE_HEADER = "CommunicationBeanMessagingTest";
	
	public static final String ACTION_NAME= "de.dailab.jiactng.agentcore.comm.CommunicationBean#send";
	private static IAgentNode _communicationPlatform;
	private static IAgent _communicator;
    private static int testCount= -1;
    
	private static List<IAgentBean> _beans;
	private static CommunicationBean _cBean;
	private static MemoryExposingBean _meb;
	private static IMemory _memory;
	private static CommunicationBeanMessagingTest _testInstance = new CommunicationBeanMessagingTest();
	private static Lock _lock = _testInstance.new Lock();
	
	private static CommunicationAddress _receiverAddress = (CommunicationAddress) CommunicationAddressFactory.createMessageBoxAddress("MyLittleShopOfTests");
	
	private static DummyTransport _transport = new DummyTransport(); 
	
	private static Log _log = null;
	
	static {
		MESSAGE_TEMPLATE = new JiacMessage();
		MESSAGE_TEMPLATE.setHeader(IJiacMessage.Header.PROTOCOL, MESSAGE_HEADER);
	}
	
	protected void setUp() throws Exception {
		if (testCount < 0){
			// if this is before the first test...
			testCount= 0;

			// init log
			_log = LogFactory.getLog("CommunicationBeanTestLog");
			super.setUp();
			
			// agentplatformcreation
			ClassPathXmlApplicationContext xmlContext = new ClassPathXmlApplicationContext("de/dailab/jiactng/agentcore/comm/communicationTestContext.xml");
			_communicationPlatform = (IAgentNode) xmlContext.getBean("CommunicationPlatform");
			
			// get a list of all agents on the platform. There should be exactly one...
			List<IAgent> beanlist = _communicationPlatform.findAgents(); 
			
			// get this one and only agent from the platform
			_communicator =  beanlist.get(0);
			
			// get the beans of the agent
			_beans = _communicator.getAgentBeans(); 

			// find the communicationBean within the beans of that agent
			Iterator<IAgentBean> it = _beans.iterator();
			while (it.hasNext()){
				ILifecycle lc = it.next();
				if (lc instanceof CommunicationBean){
					_cBean = (CommunicationBean) lc;
				} else if (lc instanceof MemoryExposingBean){
					_meb = (MemoryExposingBean) lc;
				}
				
			}
			
			_memory = _meb.getMemory();
			
			// init DummyTransport for testing purposes and add it to the Communicationbean
			_transport.doInit();
			_cBean.addTransport(_transport);
			
			// now clear orderbuffer of Dummytransport, to clear it from the
			// initialisation orders given by default from the communicationBean
			_transport.orders.clear();
			
		}
	}
	
	
	/**
	 * This small tests checks if a given message is moved correctly to a transport
	 * 
	 * @throws Exception
	 */
	public void testMessageSending() throws Exception {
		JiacMessage messageToSend = null;
		
		JiacMessage messageSent = null;
		CommunicationAddress sentToAddress = null;
		
		String messageContent = "MyWonderfulLittleTestMessage";
		ObjectContent content = new ObjectContent(messageContent);
		
		messageToSend = new JiacMessage(content);
		
		_cBean.send(messageToSend, _receiverAddress);
		messageSent = (JiacMessage) _transport.messages.remove(0);
		sentToAddress = (CommunicationAddress) _transport.sentTo.remove(0);
		
		ObjectContent sentContent = (ObjectContent) messageSent.getPayload();
		String messageContentSent = (String) sentContent.getObject();
		
		assertEquals("Message was sent to proper address", _receiverAddress, sentToAddress);
		assertEquals("Message was sent properly", messageToSend, messageSent);
		assertEquals("MessageContent was intact", messageContent, messageContentSent);
	}
	
	
	
	
	public void testMessageReceiving() throws Exception {
		_memory.attach(this, MESSAGE_TEMPLATE);
		_cBean.register(_receiverAddress);
		
		String messageContent = "MyWonderfulLittleTestMessage";
		ObjectContent content = new ObjectContent(messageContent);
		
		JiacMessage message = new JiacMessage(content);
		message.setHeader(IJiacMessage.Header.PROTOCOL, MESSAGE_HEADER);
		
		_transport.delegateMessage(message, _receiverAddress);
		synchronized(_lock){
			_lock.wait();
		}
		JiacMessage messageReceived = (JiacMessage) _lock.message;
		ObjectContent contentReceived = (ObjectContent) messageReceived.getPayload();
		
		assertEquals("Got the same Message", message, messageReceived);
		assertEquals("Content was sent correctly", content, contentReceived);
	}
	
	
	
	public void notify(SpaceEvent<? extends IFact> event) {
		IJiacMessage message = null;
		ICommunicationAddress at = null;
		ICommunicationAddress from = null;
		
		if(event instanceof WriteCallEvent) {
			WriteCallEvent<IJiacMessage> wce= (WriteCallEvent<IJiacMessage>) event;
			message= _memory.remove(wce.getObject());
			synchronized(_lock){
				_lock.message = message;
				_lock.notify();
			}
			
		}
	}

	protected void tearDown() throws Exception {
		if(testCount >= 2){
			_log.info("tearing down testing environment");
			super.tearDown();
			((SimpleAgentNode)_communicationPlatform).shutdown();
			_log.info("CommunicationBeanTest closed. All Tests done. Good Luck!");
		}
	}

	private class Lock {
		public IJiacMessage message = null;
	}
}

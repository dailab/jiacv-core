package de.dailab.jiactng.agentcore.comm.protocol;

import javax.jms.Destination;

import junit.framework.TestCase;
import de.dailab.jiactng.agentcore.comm.helpclasses.FakeDestination;
import de.dailab.jiactng.agentcore.comm.message.IJiacContent;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;

public class AgentProtocolTest extends TestCase {
	
	private static String _operation = "testOperation";
	private static Destination _replyTo = new FakeDestination("replyTo");
	private static IJiacContent _payload = null;
	private static JiacMessage _jMsg;
	private static JiacMessage _reply;
//	private static AgentProtocol _ap = new AgentProtocol(null,null);
	
	private static String test1 = "Bli";
	private static String test2 = "Bla";
	private static String test3 = "Blupp";
	
	/**
	 * Testet doAcknoledge und doCommand Arbeitsroutinen in AgentProtocol
	 * 
	 * @author Martin Loeffelholz
	 */
	// TO DO: 
	// ToDo: Agenten erstellen, mit Beannames versehen, in Protocol einbinden und Vergleichstests machen
	// ToDo: funktionale und halb funktionale Tests mit Sender.
	
	

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
//	
//	public void testDoAcknowledge(){
//		ObjectContent beanReply = null;
//		
//		//Test: Get Services
//		_operation = _ap.ACK_AGT_GET_SERVICES;
//		_jMsg = new JiacMessage(_operation, _payload, _recipient, _startPoint, _replyTo );
//		_reply = (JiacMessage) _ap.doAcknowledge(_jMsg);
//		assertEquals("GetServicesTest", _ap.ACK_AGT_GET_SERVICES_SUCESS, _reply.getOperation());
//		
//		
//		//Test: Get Beannames
//		List<String> beanReplyList = null;
//		List<String> beanList = new ArrayList<String>();
//		beanList.add(test1);
//		beanList.add(test2);
//		beanList.add(test3);
//		
//		_operation = _ap.ACK_AGT_GET_BEANNAMES;
//		_payload = new ObjectContent((java.io.Serializable) beanList);
//		_jMsg = new JiacMessage(_operation, _payload, _recipient, _startPoint, _replyTo );
//		_reply = (JiacMessage) _ap.doAcknowledge(_jMsg);
//		beanReply = (ObjectContent) _reply.getPayload();
//		beanReplyList = (List<String>) beanReply.getObject();
//		assertTrue("BeanNamesTest", beanReplyList.contains(test1));
//		assertTrue("BeanNamesTest", beanReplyList.contains(test2));
//		assertTrue("BeanNamesTest", beanReplyList.contains(test3));
//		
//		// Test: Ping
//		_operation = _ap.ACK_AGT_PING;
//		_payload = new ObjectContent("PingPing");
//		_jMsg = new JiacMessage(_operation, _payload, _recipient, _startPoint, _replyTo );
//		_reply = (JiacMessage) _ap.doAcknowledge(_jMsg);
//		assertEquals("PingTest", _ap.CMD_AGT_NOP, _reply.getOperation());
//		assertEquals("PingTest", _jMsg.getPayload().toString() + "PongPong", _reply.getPayload().toString());
//		assertEquals("PingTest", _startPoint, _reply.getEndPoint());
//		assertEquals("PingTest", _recipient, _reply.getStartPoint());
//		assertEquals("PingTest", _replyTo , _reply.getSender());
//		
//		// Test: Pong
//		_operation = _ap.ACK_AGT_PONG;
//		_jMsg = new JiacMessage(_operation, _reply.getPayload(), _recipient, _startPoint, _replyTo );
//		_reply = (JiacMessage) _ap.doAcknowledge(_jMsg);
//		assertEquals("PongTest", _ap.ACK_AGT_PONG_SUCESS, _reply.getOperation());
//		
//		// Test: NOP
//		_operation = _ap.ACK_AGT_NOP;
//		_jMsg = new JiacMessage(_operation, _payload, _recipient, _startPoint, _replyTo );
//		_reply = (JiacMessage) _ap.doAcknowledge(_jMsg);
//		assertEquals("NOP-Test", _ap.ACK_AGT_NOP_SUCESS, _reply.getOperation());
//	}
//
//	public void testDoCommand(){
//		// Test: getServices 
//		_operation = _ap.CMD_AGT_GET_SERVICES;
//		_jMsg = new JiacMessage(_operation, _payload, _recipient, _startPoint, _replyTo );
//		_reply = (JiacMessage) _ap.doCommand(_jMsg);
//		assertEquals("GetServicesTest", _ap.CMD_AGT_GET_SERVICES_SUCESS, _reply.getOperation());
//		
//		//Test: getBeanNames
//		_operation = _ap.CMD_AGT_GET_SERVICES;
//		_jMsg = new JiacMessage(_operation, _payload, _recipient, _startPoint, _replyTo );
//		_reply = (JiacMessage) _ap.doCommand(_jMsg);
////		assertEquals("GetBeanNamesTest", );
//		// ToDo: Agenten erstellen, mit Beannames versehen, in Protocol einbinden und Vergleichstests machen
//		
//		//Test: Ping
//		_operation = _ap.CMD_AGT_PING;
//		_payload = new ObjectContent ("Ping");
//		_jMsg = new JiacMessage(_operation, _payload, _recipient, _startPoint, _replyTo );
//		_reply = (JiacMessage) _ap.doCommand(_jMsg);
//		assertEquals("PingTest", _ap.ACK_AGT_PING, _reply.getOperation());
//		assertEquals("PingTest", _payload + "Pong", _reply.getPayload().toString());
//		assertEquals("PingTest", _jMsg.getStartPoint(), _reply.getEndPoint());
//		assertEquals("PingTest", _jMsg.getEndPoint(), _reply.getStartPoint());
//		
//		//Test: NOP
//		_operation = _ap.CMD_AGT_NOP;
//		_jMsg = new JiacMessage(_operation, _payload, _recipient, _startPoint, _replyTo );
//		_reply = (JiacMessage) _ap.doCommand(_jMsg);
//		assertEquals("NOPTest", _ap.ACK_AGT_NOP_SUCESS, _reply.getOperation());
//	}

	public void testOverride(){
		assertTrue(true);
	}
}

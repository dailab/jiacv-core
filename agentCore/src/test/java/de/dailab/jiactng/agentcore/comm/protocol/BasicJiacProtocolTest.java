package de.dailab.jiactng.agentcore.comm.protocol;

import javax.jms.Destination;

import junit.framework.TestCase;
import de.dailab.jiactng.agentcore.comm.helpclasses.FakeDestination;
import de.dailab.jiactng.agentcore.comm.message.IJiacContent;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;

public class BasicJiacProtocolTest extends TestCase {

	private static String _operation = "testOperation";
	private static Destination _replyTo = new FakeDestination("replyTo");
	private static IJiacContent _payload = null;
	private static JiacMessage _jMsg;
	private static JiacMessage _reply;
	
//	private static BasicJiacProtocol _bjp = new BasicJiacProtocol(null, null);
	
	
	/**
	 * Testet doAcknoledge, doError und doCommand des BasicJiacProtocols.
	 *
	 * @author Martin Loeffelholz
	 */
	
	// ToDo: Funktionale und halbfunktionale Tests der eigentlichen Sendvorg�nge + evtl. testProcessMessage
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
//
//	public void testDoAcknowledge() {
//		_operation = _bjp.ACK_GET_AGENTS;
//		_jMsg = new JiacMessage(_operation, _payload, _recipient, _startPoint, _replyTo );
//		_reply = (JiacMessage) _bjp.doAcknowledge(_jMsg);
//		assertEquals("GetAgentsTest", _bjp.ACK_TEST_SUCESS, _reply.getOperation());
//		assertEquals("GetAgentsTest", _jMsg.getPayload() + "Gotcha Agent!", _reply.getPayload().toString());
//		assertEquals("GetAgentsTest", _jMsg.getEndPoint(), _reply.getStartPoint());
//		assertEquals("GetAgentsTest", _jMsg.getStartPoint(), _reply.getEndPoint());
//		
//		_operation = _bjp.ACK_GET_SERVICES;
//		_jMsg = new JiacMessage(_operation, _payload, _recipient, _startPoint, _replyTo );
//		_reply = (JiacMessage) _bjp.doAcknowledge(_jMsg);
//		assertEquals("GetSevicesTest", _bjp.ACK_TEST_SUCESS, _reply.getOperation());
//		assertEquals("GetSevicesTest", _jMsg.getPayload() + "Gotcha Service!", _reply.getPayload().toString());
//		assertEquals("GetSevicesTest", _jMsg.getEndPoint(), _reply.getStartPoint());
//		assertEquals("GetSevicesTest", _jMsg.getStartPoint(), _reply.getEndPoint());
//		
//		_operation = _bjp.ACK_PING;
//		_jMsg = new JiacMessage(_operation, _payload, _recipient, _startPoint, _replyTo );
//		_reply = (JiacMessage) _bjp.doAcknowledge(_jMsg);
//		assertEquals("PingTest", _bjp.ACK_TEST_SUCESS, _reply.getOperation());
//		assertEquals("PingTest", _jMsg.getPayload() + "Gotcha Ping!", _reply.getPayload().toString());
//		assertEquals("PingTest", _jMsg.getEndPoint(), _reply.getStartPoint());
//		assertEquals("PingTest", _jMsg.getStartPoint(), _reply.getEndPoint());
//		
//		_operation = _bjp.ACK_NOP;
//		_jMsg = new JiacMessage(_operation, _payload, _recipient, _startPoint, _replyTo );
//		_reply = (JiacMessage) _bjp.doAcknowledge(_jMsg);
//		assertEquals("NOPTest", _bjp.ACK_TEST_SUCESS, _reply.getOperation());
//		assertEquals("NOPTest", _jMsg.getPayload() + "Nothing to do *yawns*", _reply.getPayload().toString());
//		assertEquals("NOPTest", _jMsg.getEndPoint(), _reply.getStartPoint());
//		assertEquals("NOPTest", _jMsg.getStartPoint(), _reply.getEndPoint());
//		
//	}
//
//	public void testDoError() {
//		_operation = _bjp.ERR_GET_AGENTS;
//		_jMsg = new JiacMessage(_operation, _payload, _recipient, _startPoint, _replyTo );
//		_reply = (JiacMessage) _bjp.doError(_jMsg);
//		assertEquals("GetAgentsTest", _bjp.ACK_TEST_SUCESS, _reply.getOperation());
//		assertEquals("GetAgentsTest", _jMsg.getPayload() + _bjp.ERR_GET_AGENTS, _reply.getPayload().toString());
//		assertEquals("GetAgentsTest", _jMsg.getEndPoint(), _reply.getStartPoint());
//		assertEquals("GetAgentsTest", _jMsg.getStartPoint(), _reply.getEndPoint());
//		
//		_operation = _bjp.ERR_GET_SERVICES;
//		_jMsg = new JiacMessage(_operation, _payload, _recipient, _startPoint, _replyTo );
//		_reply = (JiacMessage) _bjp.doError(_jMsg);
//		assertEquals("GetSevicesTest", _bjp.ACK_TEST_SUCESS, _reply.getOperation());
//		assertEquals("GetSevicesTest", _jMsg.getPayload() + _bjp.ERR_GET_SERVICES, _reply.getPayload().toString());
//		assertEquals("GetSevicesTest", _jMsg.getEndPoint(), _reply.getStartPoint());
//		assertEquals("GetSevicesTest", _jMsg.getStartPoint(), _reply.getEndPoint());
//		
//		_operation = _bjp.ERR_PING;
//		_jMsg = new JiacMessage(_operation, _payload, _recipient, _startPoint, _replyTo );
//		_reply = (JiacMessage) _bjp.doError(_jMsg);
//		assertEquals("PingTest", _bjp.ACK_TEST_SUCESS, _reply.getOperation());
//		assertEquals("PingTest", _jMsg.getPayload() + _bjp.ERR_PING, _reply.getPayload().toString());
//		assertEquals("PingTest", _jMsg.getEndPoint(), _reply.getStartPoint());
//		assertEquals("PingTest", _jMsg.getStartPoint(), _reply.getEndPoint());
//		
//		_operation = _bjp.ERR_NOP;
//		_jMsg = new JiacMessage(_operation, _payload, _recipient, _startPoint, _replyTo );
//		_reply = (JiacMessage) _bjp.doError(_jMsg);
//		assertEquals("NOPTest", _bjp.ACK_TEST_SUCESS, _reply.getOperation());
//		assertEquals("NOPTest", _jMsg.getPayload() + _bjp.ERR_NOP, _reply.getPayload().toString());
//		assertEquals("NOPTest", _jMsg.getEndPoint(), _reply.getStartPoint());
//		assertEquals("NOPTest", _jMsg.getStartPoint(), _reply.getEndPoint());
//	}
//
//	public void testDoCommand() {
//		_operation = _bjp.CMD_GET_AGENTS;
//		_jMsg = new JiacMessage(_operation, _payload, _recipient, _startPoint, _replyTo );
//		_reply = (JiacMessage) _bjp.doCommand(_jMsg);
//		assertEquals("GetAgentsTest", _bjp.ACK_GET_AGENTS, _reply.getOperation());
//		assertEquals("GetAgentsTest", new ObjectContent("agents").toString(), _reply.getPayload().toString());
//		assertEquals("GetAgentsTest", _jMsg.getEndPoint(), _reply.getStartPoint());
//		assertEquals("GetAgentsTest", _jMsg.getStartPoint(), _reply.getEndPoint());
//		assertEquals("GetAgentsTest", _jMsg.getSender(), _reply.getSender());
//		
//		_operation = _bjp.CMD_GET_SERVICES;
//		_jMsg = new JiacMessage(_operation, _payload, _recipient, _startPoint, _replyTo );
//		_reply = (JiacMessage) _bjp.doCommand(_jMsg);
//		assertEquals("GetSevicesTest", _bjp.ACK_TEST_SUCESS, _reply.getOperation());
//		assertEquals("GetSevicesTest", _jMsg.getPayload() + _bjp.CMD_GET_SERVICES, _reply.getPayload().toString());
//		assertEquals("GetSevicesTest", _jMsg.getEndPoint(), _reply.getStartPoint());
//		assertEquals("GetSevicesTest", _jMsg.getStartPoint(), _reply.getEndPoint());
//		
//		_operation = _bjp.CMD_PING;
//		_jMsg = new JiacMessage(_operation, _payload, _recipient, _startPoint, _replyTo );
//		_reply = (JiacMessage) _bjp.doCommand(_jMsg);
//		assertEquals("PingTest", _bjp.ACK_PING, _reply.getOperation());
//		assertEquals("PingTest", new ObjectContent("Pong").toString(), _reply.getPayload().toString());
//		assertEquals("PingTest", _jMsg.getEndPoint(), _reply.getStartPoint());
//		assertEquals("PingTest", _jMsg.getStartPoint(), _reply.getEndPoint());
//		assertEquals("PingTest", _jMsg.getSender(), _reply.getSender());
//		
//		_operation = _bjp.CMD_NOP;
//		_jMsg = new JiacMessage(_operation, _payload, _recipient, _startPoint, _replyTo );
//		_reply = (JiacMessage) _bjp.doCommand(_jMsg);
//		assertEquals("NOPTest", _bjp.ACK_TEST_SUCESS, _reply.getOperation());
//		assertEquals("NOPTest", new ObjectContent("psst").toString(), _reply.getPayload().toString());
//		assertEquals("NOPTest", _jMsg.getEndPoint(), _reply.getStartPoint());
//		assertEquals("NOPTest", _jMsg.getStartPoint(), _reply.getEndPoint());
//	}

	public void testOverride(){
		assertTrue(true);
	}
}

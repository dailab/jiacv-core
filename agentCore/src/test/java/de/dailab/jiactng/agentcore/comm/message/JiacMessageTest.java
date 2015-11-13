package de.dailab.jiactng.agentcore.comm.message;

import junit.framework.TestCase;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage.Header;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;

/**
 * Tests for different constructions of JIAC message.
 * 
 * @author Jan Keiser
 */
public class JiacMessageTest extends TestCase {

	private static ICommunicationAddress sender = CommunicationAddressFactory.createMessageBoxAddress("myAddress");
	private static ICommunicationAddress replyTo = CommunicationAddressFactory.createGroupAddress("replyToAddress");
	private static IFact payload = new AgentDescription();
	private static String group = "myGroup";
	private static String protocol = "myProtocol";
	private static String headerKey = "myKey";
	private static String headerValue = "myValue";
	
	public void testJiacMessage() {
		// create JIAC message and set all attributes
		JiacMessage jMsg = new JiacMessage(payload, replyTo);
		jMsg.setGroup(group);
		jMsg.setProtocol(protocol);
		jMsg.setSender(sender);
		jMsg.setHeader(headerKey, headerValue);

		// test attribute values
		assertEquals(payload, jMsg.getPayload());
		assertEquals(replyTo, jMsg.getReplyToAddress());
		assertEquals(group, jMsg.getGroup());
		assertEquals(protocol, jMsg.getProtocol());
		assertEquals(sender, jMsg.getSender());
		assertTrue(jMsg.getHeaderKeys().contains(headerKey));
		assertEquals(headerValue, jMsg.getHeader(headerKey));

		// test message headers
		assertNull(jMsg.getHeader(Header.SEND_TO));
		assertEquals(replyTo.toString(), jMsg.getHeader(Header.REPLY_TO));
		assertEquals(group, jMsg.getHeader(Header.GROUP));
		assertEquals(protocol, jMsg.getHeader(Header.PROTOCOL));
		assertEquals(sender.toString(), jMsg.getHeader(Header.SENDER));
	}

	public void testDefaultValues() {
		// create empty JIAC message
		JiacMessage jMsg = new JiacMessage();

		// test attribute values and message header
		assertNull(jMsg.getPayload());
		assertNull(jMsg.getReplyToAddress());
		assertNull(jMsg.getGroup());
		assertNull(jMsg.getProtocol());
		assertNull(jMsg.getSender());
		assertTrue(jMsg.getHeaderKeys().isEmpty());
		assertNull(jMsg.getHeader(headerKey));
	}

	public void testNullParameters() {
		// create JIAC message with null values
		JiacMessage jMsg = new JiacMessage(null, null);
		jMsg.setGroup(null);
		jMsg.setProtocol(null);
		jMsg.setSender(null);
		jMsg.setHeader(headerKey, headerValue);
		jMsg.setHeader(headerKey, null);

		// test NullPointerException
		try {
			jMsg.setHeader(null, headerValue);
			TestCase.fail();
		} catch (NullPointerException e) {}

		// test attribute values
		assertNull(jMsg.getPayload());
		assertNull(jMsg.getReplyToAddress());
		assertNull(jMsg.getGroup());
		assertNull(jMsg.getProtocol());
		assertNull(jMsg.getSender());
		assertTrue(jMsg.getHeaderKeys().isEmpty());
		assertNull(jMsg.getHeader(headerKey));

		// test NullPointerException
		try {
			jMsg.getHeader(null);
			TestCase.fail();
		} catch (NullPointerException e) {}
	}
}

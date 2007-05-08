package de.dailab.jiactng.agentcore.comm.message;

import javax.jms.Destination;

import de.dailab.jiactng.agentcore.comm.helpclasses.FakeDestination;
import junit.framework.TestCase;

/**
 * tests for different construction and getting the jiacDestination
 * 
 * @author Martin Loeffelholz
 *
 */

public class JiacMessageTest extends TestCase {

	
	private static String operation = "testOperation";
	private static EndPoint recipient = (EndPoint) EndPointFactory.createEndPoint("testPlatform");
	private static EndPoint startPoint = (EndPoint) EndPointFactory.createEndPoint("testPlatform");
	private static Destination replyTo = new FakeDestination("replyTo");
	private static IJiacContent payload = null;
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testJiacMessageStringIJiacContentIEndPoint() {
		JiacMessage jMsg = new JiacMessage(operation, payload, recipient );
		assertEquals(operation, jMsg.getOperation());
		assertEquals(payload, jMsg.getPayload());
		assertEquals(recipient, jMsg.getEndPoint());
	}
	

	public void testJiacMessageStringIJiacContentIEndPointIEndPointDestination() {
		JiacMessage jMsg = new JiacMessage(operation, payload, recipient, startPoint, replyTo );
		assertEquals(operation, jMsg.getOperation());
		assertEquals(payload, jMsg.getPayload());
		assertEquals(recipient, jMsg.getEndPoint());
		assertEquals(startPoint, jMsg.getStartPoint());
		assertEquals(replyTo, jMsg.getSender());
	}

	public void testGetJiacDestination(){
		JiacMessage jMsg = new JiacMessage(operation, payload, recipient, startPoint, replyTo );
		EndPoint platformRecipient = (EndPoint) EndPointFactory.createEndPoint("testPlatform");
		
		assertEquals(recipient.getUniversalId() , jMsg.getJiacDestination());
		
		platformRecipient.setLocalId(platformRecipient.getLocalId() + JiacMessage.PLATFORM_ENDPOINT_EXTENSION);
		jMsg.setEndPoint(platformRecipient);
		
		assertEquals(platformRecipient.toString(), jMsg.getJiacDestination());
		
		platformRecipient.setLocalId("local");
		platformRecipient.setUniversalId("universal");
		jMsg.setEndPoint(platformRecipient);
		System.out.println(jMsg.getJiacDestination());
		
	}
	
}

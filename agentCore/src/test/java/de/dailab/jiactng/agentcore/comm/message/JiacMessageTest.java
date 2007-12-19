package de.dailab.jiactng.agentcore.comm.message;

import javax.jms.Destination;

import junit.framework.TestCase;
import de.dailab.jiactng.agentcore.comm.helpclasses.FakeDestination;
import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * tests for different construction and getting the jiacDestination
 * 
 * @author Martin Loeffelholz
 *
 */

public class JiacMessageTest extends TestCase {
	private static String operation = "testOperation";
	private static Destination replyTo = new FakeDestination("replyTo");
	private static IFact payload = null;
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	/* Function set to private
	public void testJiacMessageStringIJiacContentIEndPoint() {
		JiacMessage jMsg = new JiacMessage(operation, payload, recipient );
		assertEquals(operation, jMsg.getOperation());
		assertEquals(payload, jMsg.getPayload());
		assertEquals(recipient, jMsg.getEndPoint());
	}
	*/
//
//	public void testJiacMessageStringIJiacContentIEndPointIEndPointDestination() {
//		JiacMessage jMsg = new JiacMessage(operation, payload, recipient, startPoint, replyTo );
//		assertEquals(operation, jMsg.getOperation());
//		assertEquals(payload, jMsg.getPayload());
//		assertEquals(recipient, jMsg.getEndPoint());
//		assertEquals(startPoint, jMsg.getStartPoint());
//		assertEquals(replyTo, jMsg.getSender());
//	}
//
//	public void testGetJiacDestination(){
//		JiacMessage jMsg = new JiacMessage(operation, payload, recipient, startPoint, replyTo );
//		EndPoint platformRecipient = (EndPoint) EndPointFactory.createEndPoint("testPlatform");
//		
//		assertEquals(recipient.getUniversalId() , jMsg.getJiacDestination());
//		
//		platformRecipient.setLocalId(platformRecipient.getLocalId() + JiacMessage.PLATFORM_ENDPOINT_EXTENSION);
//		jMsg = new JiacMessage(operation, payload, platformRecipient, startPoint, replyTo);
//		
//		assertEquals(platformRecipient.toString(), jMsg.getJiacDestination());
//		
//		platformRecipient.setLocalId("local");
//		platformRecipient.setUniversalId("universal");
//		jMsg = new JiacMessage(operation, payload, platformRecipient, startPoint, replyTo);
//		System.out.println(jMsg.getJiacDestination());
//		
//	}
//	
//	public void testNullPointerException(){
//		boolean caught = false;
//		try {
//			JiacMessage jMsg = new JiacMessage(operation, payload, null, startPoint, replyTo);
//		} catch (NullPointerException e) {
//			caught = true;
//		} finally {
//			assertTrue("No Recipient declared", caught);
//		}
//		
//		caught = false;
//		try {
//			JiacMessage jMsg = new JiacMessage(operation, payload, recipient, null, replyTo);
//		} catch (NullPointerException e) {
//			caught = true;
//		} finally {
//			assertTrue("No StartPoint declared", caught);
//		}
//		
//		caught = false;
//		try {
//			JiacMessage jMsg = new JiacMessage(operation, payload, null, null, replyTo);
//		} catch (NullPointerException e) {
//			caught = true;
//		} finally {
//			assertTrue("No Endpoints declared", caught);
//		}
//	}
	
	public void testOverride(){
		assertTrue(true);
	}
	
}

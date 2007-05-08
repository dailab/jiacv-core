package de.dailab.jiactng.agentcore.comm.message;

import junit.framework.TestCase;
import de.dailab.jiactng.agentcore.comm.message.EndPoint;

/**
 * 
 * testing endpointcreation, conversion from and to strings and 
 * if endpointequalsfunction is working
 * 
 * @author Martin Loeffelholz
 *
 */

public class EndPointTest extends TestCase {

	private static EndPoint _firstEndPoint = new EndPoint("uid","lid");
	private static EndPoint _secondEndPoint;
	
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	
	public void testEndPointconstruction() {
		
		assertEquals(_firstEndPoint.getLocalId(), "lid");
		assertEquals(_firstEndPoint.getUniversalId(), "uid");
	}

	
	public void testEndPointConstToAndFromString() {
		
		_secondEndPoint = new EndPoint(_firstEndPoint.toString());
		assertEquals(_firstEndPoint, _secondEndPoint);
	}
	
	public void testEndpointDotEqualsFunction(){
		assertTrue(_secondEndPoint.equals(_firstEndPoint));
	}

}

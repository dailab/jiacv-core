package de.dailab.jiactng.agentcore.comm.message;

import junit.framework.TestCase;
import de.dailab.jiactng.agentcore.comm.message.EndPoint;
import de.dailab.jiactng.agentcore.comm.message.EndPointFactory;

/**
 * testing if localIDs are unique and globalIDs are the same 
 * for endpoints created at the same platform 
 * 
 * @author Martin Loeffelholz
 *
 */

public class EndPointFactoryTest extends TestCase {

	private static EndPoint _firstEndpoint;
	private static EndPoint _secondEndpoint;
	private static EndPoint _twelvethEndpoint;
	
	protected void setUp() throws Exception {
		super.setUp();
		_firstEndpoint = (EndPoint) EndPointFactory.createEndPoint("testPlatform");
		_secondEndpoint = (EndPoint) EndPointFactory.createEndPoint("testPlatform");		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testEndpointLocalId(){
		for (int i = 1; i <= 10; i++ ){
			_twelvethEndpoint = (EndPoint) EndPointFactory.createEndPoint("testPlatform");
		}
		
		assertFalse(_firstEndpoint.getLocalId().equals(_secondEndpoint.getLocalId())  );
		assertFalse(_twelvethEndpoint.getLocalId().equals( _firstEndpoint.getLocalId()));
		assertFalse(_twelvethEndpoint.getLocalId().equals( _secondEndpoint.getLocalId()));
	}
	
	public void testEndpointUniversalID(){
		assertTrue(_firstEndpoint.getUniversalId().equals(_secondEndpoint.getUniversalId()) );
	}

}

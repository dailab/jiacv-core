package de.dailab.jiactng.agentcore.comm.message;

import de.dailab.jiactng.agentcore.comm.Util;


/**
 * Erzeugt eindeutige Adressen - Endpoints
 * @author janko
 *
 */
public class EndPointFactory {

	private static long _counterLocal = 0L;	
	
	private static final char HOST_PLATFORM_SEPARATOR = '@';
	public static IEndPoint createEndPoint(String platformId) {
		IEndPoint endpoint = new EndPoint(platformId+HOST_PLATFORM_SEPARATOR+Util.convertToBase62(null), ""+(_counterLocal++));
		System.out.println("Endpoint created:"+endpoint.toString());
		return endpoint;
	}
}

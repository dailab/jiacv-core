package de.dailab.jiactng.agentcore.comm;

import javax.jms.Destination;

/**
 * Erzeugt eindeutige Adressen - Endpoints
 * @author janko
 *
 */
public class EndPointFactory {

	private static long _counterLocal = 0L;
	private static long _counterGlobal = 0L;	
	
	public static IEndPoint createEndPoint() {
		IEndPoint endpoint = new EndPoint(""+_counterGlobal, ""+(_counterLocal++));
		System.out.println("Endpoint created:"+endpoint.toString());
		return endpoint;
	}
}

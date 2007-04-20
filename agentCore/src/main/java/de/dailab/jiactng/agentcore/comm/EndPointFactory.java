package de.dailab.jiactng.agentcore.comm;


/**
 * Erzeugt eindeutige Adressen - Endpoints
 * @author janko
 *
 */
public class EndPointFactory {

	private static long _counterLocal = 0L;	
	
	public static IEndPoint createEndPoint(String platformId) {
		IEndPoint endpoint = new EndPoint(""+platformId, ""+(_counterLocal++));
		System.out.println("Endpoint created:"+endpoint.toString());
		return endpoint;
	}
}

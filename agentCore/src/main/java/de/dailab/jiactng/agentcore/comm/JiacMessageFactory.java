package de.dailab.jiactng.agentcore.comm;

import javax.jms.Destination;

/**
 * FactoryKlasse zum Erzeugen von JiacMessages. Soll nur Abeit sparen, ist nicht zwingend nötig.
 * @author janko
 *
 */
public class JiacMessageFactory {

//	public static IJiacMessage createJiacMessage(String op, IJiacContent payload) {
//		IJiacMessage message = null;
//		message = new JiacMessage(op, payload, EndPointFactory.createEndPoint());
//		return message;
//	}
//
//	public static IJiacMessage createJiacMessage(String op, IJiacContent payload, IEndPoint origin, Destination replyToDestination) {
//		IJiacMessage message = null;
//		message = new JiacMessage(op, payload, EndPointFactory.createEndPoint(), origin, replyToDestination);
//		return message;
//	}

	public static IJiacMessage createJiacMessage(String op, IJiacContent payload, IEndPoint origin, IEndPoint destEndPoint, Destination replyToDestination) {
		IJiacMessage message = null;
		message = new JiacMessage(op, payload, destEndPoint, origin, replyToDestination);
		return message;
	}
	
}

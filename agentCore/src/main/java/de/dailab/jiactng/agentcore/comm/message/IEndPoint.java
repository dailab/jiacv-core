package de.dailab.jiactng.agentcore.comm.message;

import java.io.Serializable;
/**
 * ein Endpunkt einer Kommunikation - stellt also eine Adresse auf dem JIAC-JMS-Bus dar
 * @author janko
 *
 * 
 * FIXME: move this to de.dailab.jiactng.agentcore.comm
 */
public interface IEndPoint extends Serializable {

	/**
	 * Gibt die globale, universal eindeutige Id des KommunikationsEndpunkts an
	 * @return
	 */
	public String getUniversalId();
	
	/**
	 * Gibt eine Platformweit eindeutige Id zur�ck.
	 * @return
	 */
	public String getLocalId();
}

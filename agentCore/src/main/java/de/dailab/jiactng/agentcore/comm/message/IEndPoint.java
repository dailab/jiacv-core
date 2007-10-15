package de.dailab.jiactng.agentcore.comm.message;

import java.io.Serializable;

import de.dailab.jiactng.agentcore.comm.IMessageBoxAddress;
/**
 * ein Endpunkt einer Kommunikation - stellt also eine Adresse auf dem JIAC-JMS-Bus dar
 * @author janko
 * @deprecated replaced by {@link IMessageBoxAddress}
 */
@Deprecated
public interface IEndPoint extends Serializable {

	/**
	 * Gibt die globale, universal eindeutige Id des KommunikationsEndpunkts an
	 * @return
	 */
	public String getUniversalId();
	
	/**
	 * Gibt eine Platformweit eindeutige Id zurueck.
	 * @return
	 */
	public String getLocalId();
}

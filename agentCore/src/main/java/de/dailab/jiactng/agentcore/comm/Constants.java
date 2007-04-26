package de.dailab.jiactng.agentcore.comm;

public class Constants {
	/**
	 * This is a Propertyname of a Property which must be set on Messaging-Messages to inform Receivers about the address
	 * Do a setProperty(ADDRESS_PROPERTY, "<SomeRecipientAddress>") on a Messageobject that is to be sent over the
	 * messagebus!
	 */
	public static final String ADDRESS_PROPERTY = "RECIPIENT";

	/**
	 * Dieses Property dient zur Erkennung von Ping Nachrichten von Plattformen. Der Wert ist die Id der sendenden
	 * Platform. Der Empfänger sieht also wer sich als lebend propagiert.
	 */
	public static final String PING_PLATFORM_ID_PROPERTY = "PING_PLATFORM_SENDER";

	public static final String[] LifeCycleStateNames = { "UNDEFINED", "INITIALIZING", "INITIALIZED", "STARTING",
																					"STARTED", "STOPPING", "STOPPED", "CLEANING_UP", "CLEANED_UP" };
}

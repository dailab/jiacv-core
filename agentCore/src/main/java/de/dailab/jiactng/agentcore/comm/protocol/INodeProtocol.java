package de.dailab.jiactng.agentcore.comm.protocol;

/**
 * Interface für das NodeProtocol. Es hält die Befehle als konstanten, so wird NodeProtocol übersichtlicher.
 * 
 * @author janko
 */
public interface INodeProtocol extends IProtocolHandler {
	// Diese Befehle versteht das Protokoll
	
	/* Ping -lebensnachricht mit infos über platform */
	public static final String CMD_PING = "PING";
	public static final String CMD_GET_AGENTS = "GET_AGENTS";
	public static final String CMD_GET_SERVICES = "GET_SERVICES";
	public static final String CMD_NOP = "NOP";

	// Dies sind die positiven Antworten auf die Kommandos
	public static final String ACK_PING = "ACK_PING";
	public static final String ACK_GET_AGENTS = "ACK_AGENTS";
	public static final String ACK_GET_SERVICES = "ACK_SERVICES";
	public static final String ACK_NOP = "ACK_NOP";

	// Dies sind die negativen Antworten auf die Kommandos
	public static final String ERR_PING = "ERR_PING";
	public static final String ERR_GET_AGENTS = "ERR_AGENTS";
	public static final String ERR_GET_SERVICES = "ERR_SERVICES";
	public static final String ERR_NOP = "ERR_NOP";

	public static final int PROCESSING_FAILED = -1;
	public static final int PROCESSING_SUCCESS = 0;

	public static final long DEFAULT_TTL = 10000L;
}

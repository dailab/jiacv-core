package de.dailab.jiactng.agentcore.comm.protocol;

public interface IAgentProtocol extends IProtocolHandler {
	public static final int PROCESSING_FAILED = -1;
	public static final int PROCESSING_SUCCESS = 0;

	public static final long DEFAULT_TTL = 10000L;

	// Diese Befehle versteht das Protokoll
	public static final String CMD_AGT_PING = "AGT_PING";
	public static final String CMD_AGT_PONG = "AGT_PONG";
	public static final String CMD_AGT_GET_SERVICES = "AGT_GET_SERVICES";
	public static final String CMD_AGT_GET_BEANNAMES = "AGT_GET_BEANNAMES";
	public static final String CMD_AGT_NOP = "AGT_NOP";

	// Dies sind die positiven Antworten auf die Kommandos
	public static final String ACK_AGT_PING = "ACK_AGT_PING";
	public static final String ACK_AGT_PONG = "ACK_AGT_PONG";
	public static final String ACK_AGT_GET_SERVICES = "ACK_AGT_SERVICES";
	public static final String ACK_AGT_GET_BEANNAMES = "ACK_GET_BEANNAMES";
	public static final String ACK_AGT_NOP = "ACK_AGT_NOP";
	
}

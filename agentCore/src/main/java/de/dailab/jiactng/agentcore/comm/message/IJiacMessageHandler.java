/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.message;

import de.dailab.jiactng.agentcore.knowledge.IKnowledgeHandler;

/**
 * Typed interface for a knowledge handler that works only on {@link IJiacMessage}s.
 * 
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public interface IJiacMessageHandler extends IKnowledgeHandler<IJiacMessage> {}

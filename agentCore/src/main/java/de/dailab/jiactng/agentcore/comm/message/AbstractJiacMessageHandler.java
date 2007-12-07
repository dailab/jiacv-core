/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.message;

import de.dailab.jiactng.agentcore.knowledge.ConsumingKnowledgeProcessor;

/**
 * Combines the JIAC message processor with the JIAC message handler.
 * 
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public abstract class AbstractJiacMessageHandler extends ConsumingKnowledgeProcessor<IJiacMessage> implements IJiacMessageHandler {
    public AbstractJiacMessageHandler() {
        super();
    }

    @Override
    public abstract void handle(IJiacMessage knowledge);
}

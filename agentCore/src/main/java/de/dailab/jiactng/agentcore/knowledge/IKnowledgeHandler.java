/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.knowledge;

import java.util.EventListener;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public interface IKnowledgeHandler<K extends IFact> extends EventListener {
    void handle(K knowledge);
}

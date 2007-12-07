/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.knowledge;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public interface IKnowledgeHandler<K extends IFact> {
    void handle(K knowledge);
}

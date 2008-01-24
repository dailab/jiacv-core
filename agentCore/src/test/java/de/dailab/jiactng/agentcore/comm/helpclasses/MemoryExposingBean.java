/**
 * 
 */
package de.dailab.jiactng.agentcore.comm.helpclasses;

import de.dailab.jiactng.agentcore.AbstractAgentBean;

/**
 * @author Martin Loeffelholz
 *
 */
public class MemoryExposingBean extends AbstractAgentBean {
	public de.dailab.jiactng.agentcore.knowledge.IMemory getMemory(){
		return memory;
	}
}

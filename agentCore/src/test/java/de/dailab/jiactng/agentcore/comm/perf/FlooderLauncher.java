/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.perf;

import de.dailab.jiactng.agentcore.IAgentNode;
import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class FlooderLauncher {
    private static boolean SHUTTED_DOWN= false;
    
    public static void shutdownNode(final IAgentNode node) {
        synchronized (FlooderLauncher.class) {
            if(SHUTTED_DOWN) {
                return;
            }
            
            SHUTTED_DOWN= true;
            
            new Thread() {
                public void run() {
                    try {
                        ((SimpleAgentNode) node).shutdown();
                    } catch (LifecycleException e) {
                        e.printStackTrace();
                    }
                }
                
            }.start();
        }
    }
    
    public static void main(String[] args) {
        SimpleAgentNode.main(new String[]{"de/dailab/jiactng/agentcore/comm/perf/node.xml"});
    }
}

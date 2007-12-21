/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.util;

import java.util.HashSet;
import java.util.Set;

import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class EqualityChecker {
    private EqualityChecker() {}
    
    public static boolean equals(Object a, Object b) {
        return a != null && b != null ? a.equals(b) : a == b;
    }
    
    public static void main(String[] args) {
        Set<IFact> facts= new HashSet<IFact>();
        
        JiacMessage message= new JiacMessage();
        facts.add(message);
        System.err.println(facts.toString());
        
        message.setSender(CommunicationAddressFactory.createGroupAddress("alle"));
        facts.add(message);
        System.err.println(facts.toString());
        
        message= new JiacMessage();
        facts.add(message);
        System.err.println(facts.toString());
    }
}

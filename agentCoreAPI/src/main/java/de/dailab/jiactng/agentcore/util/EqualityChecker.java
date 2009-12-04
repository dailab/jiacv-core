/*
 * $Id: EqualityChecker.java 18492 2008-06-13 07:56:12Z moekon $ 
 */
package de.dailab.jiactng.agentcore.util;

/**
 * @author Marcel Patzlaff
 * @version $Revision: 18492 $
 */
public class EqualityChecker {
    private EqualityChecker() {}
    
    public static boolean equals(Object a, Object b) {
        return a != null && b != null ? a.equals(b) : a == b;
    }
    
    public static boolean equalsOrNull(Object a, Object b) {
    	if(a == b) {
    		return true;
    	}
    	
    	if(a == null) {
    		return true;
    	}
    	
    	if(b == null) {
    		return true;
    	}
    	
    	return a.equals(b);
    }    
}

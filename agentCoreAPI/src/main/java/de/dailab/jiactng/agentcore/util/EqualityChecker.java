/*
 * $Id: EqualityChecker.java 18492 2008-06-13 07:56:12Z moekon $ 
 */
package de.dailab.jiactng.agentcore.util;

/**
 * Provides methods for checking equality of objects with different considerations of <code>null</code> values.
 * @author Marcel Patzlaff
 * @version $Revision: 18492 $
 */
public final class EqualityChecker {
    private EqualityChecker() {}

    /**
     * Checks the equality of two objects by consideration of null parameters.
     * It returns <code>false</code> if only one of the parameters is <code>null</code>.
     * @param a the first object
     * @param b the second object
     * @return the equality of the two objects
     */
    public static boolean equals(Object a, Object b) {
        return a != null && b != null ? a.equals(b) : a == b;
    }

    /**
     * Checks the equality of two objects by consideration of null parameters.
     * It returns also <code>true</code> if one of the parameters is <code>null</code>.
     * @param a the first object
     * @param b the second object
     * @return the equality of the two objects
     */
    public static boolean equalsOrNull(Object a, Object b) {
    	if (a == b) {
    		return true;
    	}
    	
    	if (a == null) {
    		return true;
    	}
    	
    	if (b == null) {
    		return true;
    	}
    	
    	return a.equals(b);
    }    

    /**
     * Check whether the first and second object are equal, or the other (second)
     * object is null. This can be used for template matching, where the second
     * argument is the template. The first object matches the template iff the
     * template is null or both are equal.
     * 
     * @param obj			the object to be matched
     * @param template		the template to match against
     * @return				whether the object matches the template
     */
    public static boolean equalsOrOtherNull(Object obj, Object template) {
    	return template == null || template.equals(obj);
    }

}

/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.action.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be used to support type informations
 * of an {@link Expose}d method where the return type is an array.
 *
 * @see Expose
 * 
 * @author Marcel Patzlaff
 * @version $Revision:$
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReturnTypes {
    Class[] value();
}

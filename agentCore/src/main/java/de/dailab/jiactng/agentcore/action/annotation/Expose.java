/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.action.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;

/**
 * This annotation can be used to mark methods in a bean inherited from {@link AbstractMethodExposingBean}
 * as actions. So marked methods will be exposed automatically.
 * 
 * Make sure that the visibility of these methods is <code>public</code>
 * because otherwise this annotation will be ignored.
 * 
 * If the annotated method returns an array of objects, you can provide
 * additional type informations.
 *
 * @author Marcel Patzlaff
 * @version $Revision$
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Expose {
    String name() default "";
    Class[] returnTypes() default {};
}

/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.action.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.dailab.jiactng.agentcore.environment.IEffector;

/**
 * This annotation can be used to mark methods in a bean inherited from {@link IEffector}
 * as actions. So marked methods will be exposed automatically.
 * 
 * Make sure that the visibility of these methods is at least <code>protected</code>
 * because otherwise invocation exceptions might occure.
 *
 * @see ReturnTypes
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Expose {
    String name() default "";
}

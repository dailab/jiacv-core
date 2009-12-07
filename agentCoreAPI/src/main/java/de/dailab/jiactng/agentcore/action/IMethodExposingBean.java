/*
 * $Id: $ 
 */
package de.dailab.jiactng.agentcore.action;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.dailab.jiactng.agentcore.action.scope.ActionScope;

/**
 * An interface of beans which exposes accessible methods which are marked with the
 * {@link Expose} annotation.
 * 
 * @see Expose
 * 
 * @author Marcel Patzlaff
 * @version $Revision: $
 */
public interface IMethodExposingBean {
    /**
     * This annotation can be used to mark methods in a bean implementing {@link IMethodExposingBean}
     * as actions. So marked methods will be exposed automatically.
     * 
     * Make sure that the visibility of these methods is <code>public</code>
     * because otherwise this annotation will be ignored.
     * 
     * If the annotated method returns an array of objects, you can provide
     * additional type informations.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface Expose {
        String name() default "";
        Class<?>[] returnTypes() default {};
        ActionScope scope() default ActionScope.AGENT;
    }
    
}

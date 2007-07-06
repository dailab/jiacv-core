/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.action;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.annotation.Expose;
import de.dailab.jiactng.agentcore.action.annotation.ReturnTypes;
import de.dailab.jiactng.agentcore.environment.IEffector;

/**
 * An abstract Bean which exposes accessible methods which are marked with the
 * {@link Expose} annotation.
 * 
 * @see Expose
 * @see ReturnTypes
 * 
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public abstract class AbstractMethodExposingBean extends AbstractAgentBean implements IEffector {
    private static final Class[] EMPTY_CLASSES= new Class[0];
    protected static final char METHOD_SEPARATING_CHAR= '#';
    
    public final void doAction(DoAction doAction) {
        Action action= doAction.getAction();
        String name= doAction.getAction().getName();
        // check for hashmark
        int sep= name.indexOf(METHOD_SEPARATING_CHAR);
        
        if(sep > 0 && sep < name.length() - 1) {
            try {
                Method method= getClass().getMethod(name.substring(sep + 1), action.getParameters());
                Object result= method.invoke(this, doAction.getParams());
                // TODO: where to go with the result?
                
                return;
            } catch (NoSuchMethodException nsme) {
                // simply fall through
                getLog().debug("action name '" + name + "' contains method separation but is a method of mine"); 
            } catch (IllegalAccessException iae) {
                // should not happen
                throw new IllegalArgumentException("doAction references an non-accessible method", iae);
            } catch (InvocationTargetException ite) {
                // should not happen -> type checking have to be implemented in the DoAction constructor!
                
                Throwable cause= ite.getCause();
                
                // delegate runtime exceptions
                if(cause != null && cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                }
                
                throw new RuntimeException("action invocation failed", ite);
            }
        }
        
        // if we are here, then we have no method with 'name'
        overrideDoAction(doAction);
    }

    public final ArrayList<? extends Action> getActions() {
        ArrayList<Action> actions= new ArrayList<Action>();
        
        for(Method method : getClass().getMethods()) {
            int modifiers= method.getModifiers();
            
            // jump over static or non-accessible methods
            if(Modifier.isStatic(modifiers) || !(Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers))) {
                continue;
            }
            
            // check for Expose annotation
            Expose expAnno= method.getAnnotation(Expose.class);
            if(expAnno != null) {
                Class returnType= method.getReturnType();
                Class[] returnTypes;
                if(returnType.isArray()) {
                    ReturnTypes rtAnno= method.getAnnotation(ReturnTypes.class);
                    returnTypes= rtAnno != null ? rtAnno.value() : new Class[]{returnType};
                } else {
                    returnTypes= returnType == void.class ? EMPTY_CLASSES : new Class[]{returnType};
                }
                
                String name= expAnno.name();
                
                if(name.length() == 0) {
                    name= method.getDeclaringClass().getName() + METHOD_SEPARATING_CHAR + method.getName();
                }
                
                // build the action object
                actions.add(
                    new Action(
                        name,
                        this,
                        method.getParameterTypes(),
                        returnTypes
                    )
                );
            }
        }
        
        // add further actions
        actions.addAll(overrideGetActions());
        return actions;
    }
    
    protected void overrideDoAction(DoAction doAction) {}
    protected List<? extends Action> overrideGetActions() {return Collections.emptyList();}
    protected abstract Log getLog();
}

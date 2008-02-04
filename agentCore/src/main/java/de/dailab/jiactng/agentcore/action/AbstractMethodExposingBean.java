/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.action;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.environment.IEffector;

/**
 * An abstract Bean which exposes accessible methods which are marked with the
 * {@link Expose} annotation.
 * 
 * @see Expose
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public abstract class AbstractMethodExposingBean extends AbstractAgentBean implements IEffector, AbstractMethodExposingBeanMBean {
    /**
     * This annotation can be used to mark methods in a bean inherited from {@link AbstractMethodExposingBean}
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
        Class[] returnTypes() default {};
    }
    
    static String getName(Method method) {
        String name= method.getAnnotation(Expose.class).name();
        
        if(name.length() == 0) {
            name= method.getDeclaringClass().getName() + METHOD_SEPARATING_CHAR + method.getName();
        }
        
        return name;
    }
    
    static Class[] getReturnTypes(Method method) {
        Expose expAnno= method.getAnnotation(Expose.class);
        Class returnType= method.getReturnType();
        Class[] returnTypes;
        if(returnType.isArray()) {
            returnTypes= expAnno.returnTypes().length > 0 ? expAnno.returnTypes() : new Class[]{returnType};
        } else {
            returnTypes= returnType == void.class ? EMPTY_CLASSES : new Class[]{returnType};
        }
        
        return returnTypes;
    }
    
    /**
     * Utility method which collects all exposed methods along the given hierarchy.
     * Methods which are annotated in more specialised classes will have precedence. 
     * 
     * @param clazz
     * @return
     */
    static ArrayList<Method> getExposedPublicMethods(Class<? extends AbstractMethodExposingBean> clazz) {
        Set<Class> processed= new HashSet<Class>();
        Queue<Class> nextStep= new LinkedList<Class>();
        ArrayList<Method> methods= new ArrayList<Method>();
        nextStep.offer(clazz);
        Class current;
        
        do {
            current= nextStep.poll();
            if(processed.add(current)) {
                for(Method method : current.getDeclaredMethods()) {
                    int modifiers= method.getModifiers();
                    
                    if(Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers)) {
                        continue;
                    }
                    
                    if(method.isAnnotationPresent(Expose.class)) {
                        boolean insert= true;
                        
                        /*
                         * Last method in hierarchy wins.
                         * If we already collected the same method discard the current one. 
                         */
                        for(Method collected : methods) {
                            if(equalsPublicMethod(collected, method)) {
                                insert= false;
                                break;
                            }
                        }
                        
                        if(insert) {
                            methods.add(method);
                        }
                    }
                    
                    // ascend hierarchy
                    if(current.getSuperclass() != null && current.getSuperclass() != AbstractMethodExposingBean.class) {
                        nextStep.offer(current.getSuperclass());
                    }
                    
                    for(Class superIntfc : current.getInterfaces()) {
                        nextStep.offer(superIntfc);
                    }
                }
            } else {
                continue;
            }
        } while(nextStep.size() > 0);
        return methods;
    }
    
    /**
     * Utility method for checking equality of methods without regard of the
     * declaring class.
     * 
     * @param a
     * @param b
     * @return
     */
    static boolean equalsPublicMethod(Method a, Method b) {
        if(!a.getName().equals(b.getName()))
            return false;
        
        Class[] aParams;
        Class[] bParams;
        
        if((aParams= a.getParameterTypes()).length != (bParams= b.getParameterTypes()).length)
            return false;
        
        for(int i= 0; i < aParams.length; ++i) {
            if(!aParams[i].equals(bParams[i]))
                return false;
        }
        
        return true;
    }
    
    private static final Class[] EMPTY_CLASSES= new Class[0];
    protected static final char METHOD_SEPARATING_CHAR= '#';
    
    public final void doAction(DoAction doAction) {
log.debug("typechecking is '" + doAction.typeCheck() + "'");
        
        Action action= doAction.getAction();
        Method method= searchMethod(action.getName(), action.getParameters());
        if(method != null) {
            try {
                Object result= method.invoke(this, doAction.getParams());
    			memory.write(doAction.getAction().createActionResult(
    					doAction, new Object[] { result }));
                log.debug("action processed and result written...");
                return;
            }  catch (IllegalAccessException iae) {
                // should not happen
                log.debug("cannot access action", iae);
                throw new IllegalArgumentException("doAction references a non-accessible method", iae);
            } catch (InvocationTargetException ite) {
                // should not happen -> type checking have to be implemented in the DoAction constructor!
                log.debug("error while invoking action", ite);
                Throwable cause= ite.getCause();
                memory.write(action.createActionResult(doAction, new Object[]{cause != null ? cause : ite}));
                return;
//                // delegate runtime exceptions
//                if(cause != null && cause instanceof RuntimeException) {
//                    throw (RuntimeException) cause;
//                }
//                
//                throw new RuntimeException("action invocation failed", ite);
            } catch (Exception re) {
                log.debug("something went wrong with this action '" + action.getName() + "' '" + method + "'", re);
            }
        }
        
        // if we are here, then we have no method with 'name'
        log.debug("did not found exposed method -> call overrideDoAction");
        overrideDoAction(doAction);
    }

    public final ArrayList<? extends Action> getActions() {
        ArrayList<Action> actions= new ArrayList<Action>();
        
        for(Method method : getExposedPublicMethods(getClass())) {
            // check for Expose annotation
            Class[] returnTypes= getReturnTypes(method);
            String name= getName(method);
            
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
        
        // add further actions
        actions.addAll(overrideGetActions());
        return actions;
    }
    
    protected void overrideDoAction(DoAction doAction) {}
    protected List<? extends Action> overrideGetActions() {return Collections.emptyList();}
    
    private Method searchMethod(String name, Class[] parameters) {
        String originalName= name;
        String assumedMethodName= null;
        // check for hashmark
        int sep= name.indexOf(METHOD_SEPARATING_CHAR);
        
        if(sep > 0 && sep < name.length() - 1) {
            assumedMethodName= name.substring(sep + 1);
        }
        
        for(Method method : getExposedPublicMethods(getClass())) {
            Expose exposeAnno= method.getAnnotation(Expose.class);
            Class[] mpar= method.getParameterTypes();
            if(mpar.length == parameters.length) {
                if(exposeAnno.name().equals(originalName) || (assumedMethodName != null && method.getName().equals(assumedMethodName))) {
                    boolean found= true;
                    for(int i= 0; i < mpar.length; ++i) {
                        if(!mpar[i].equals(parameters[i])) {
                            found= false;
                            break;
                        }
                    }
                    
                    if(found) {
                        return method;
                    }
                }
            }
        }
        
        return null;
    }

	/**
	 * Creates management information about the provided actions.
	 * @return list of action descriptions
	 */
	public TabularData getActionList() {
        ArrayList<? extends Action> actions = getActions();
        if (actions.isEmpty()) {
        	return null;
        }

        try {
        	// create empty table
        	String[] itemNames = new String[] {"Name", "Parameters", "Results", "ProviderBean"};
        	OpenType[] itemTypes = new OpenType[] {SimpleType.STRING, new ArrayType(1, SimpleType.STRING), new ArrayType(1, SimpleType.STRING), SimpleType.STRING};
        	CompositeType rowType = new CompositeType(actions.get(0).getClass().getName(), "provided action", itemNames, itemNames, itemTypes);
        	TabularType tabularType = new TabularType(actions.getClass().getName(), "list of provided actions", rowType, new String[] {"Name"});
        	TabularData actionList = new TabularDataSupport(tabularType);

        	// fill table
        	for (Action action : actions) {
        		// get parameter classes
        		Class[] parameters = action.getParameters();
        		int size = parameters.length;
        		String[] parameterList = new String[size];
        		for (int i=0; i<size; i++) {
        			parameterList[i] = parameters[i].getName();
        		}
        		// get result classes
        		Class[] results = action.getResults();
        		size = results.length;
        		String[] resultList = new String[size];
        		for (int i=0; i<size; i++) {
        			resultList[i] = results[i].getName();
        		}
        		// create and add action description
        		Object[] itemValues = new Object[] {action.getName(), parameterList, resultList, action.getProviderBean().getBeanName()};
        		CompositeData value = new CompositeDataSupport(rowType, itemNames, itemValues);
        		actionList.put(value);
        	}
        	return actionList;
        }
        catch (OpenDataException e) {
        	e.printStackTrace();
        	return null;
        }
	}
}

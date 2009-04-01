/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.action;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
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

import de.dailab.jiactng.agentcore.action.scope.ActionScope;
import de.dailab.jiactng.agentcore.comm.wp.DirectoryAccessBean;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

/**
 * An abstract Bean which exposes accessible methods which are marked with the
 * {@link Expose} annotation.
 * 
 * @see Expose
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public abstract class AbstractMethodExposingBean extends AbstractActionAuthorizationBean implements AbstractMethodExposingBeanMBean {
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
        Class<?>[] returnTypes() default {};
        ActionScope scope() default ActionScope.AGENT;
    }
    
    static String getName(Method method) {
        String name= method.getAnnotation(Expose.class).name();
        
        if(name.length() == 0) {
            name= method.getDeclaringClass().getName() + METHOD_SEPARATING_CHAR + method.getName();
        }
        
        return name;
    }
    
    static ActionScope getScope(Method method) {
      ActionScope scope = method.getAnnotation(Expose.class).scope();
            
      return scope;
    }
    
    static Class<?>[] getReturnTypes(Method method) {
        Expose expAnno= method.getAnnotation(Expose.class);
        Class<?> returnType= method.getReturnType();
        Class<?>[] returnTypes;
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
        Set<Class<?>> processed= new HashSet<Class<?>>();
        Queue<Class<?>> nextStep= new LinkedList<Class<?>>();
        ArrayList<Method> methods= new ArrayList<Method>();
        nextStep.offer(clazz);
        Class<?> current;
        
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
                    
                    for(Class<?> superIntfc : current.getInterfaces()) {
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
        
        Class<?>[] aParams;
        Class<?>[] bParams;
        
        if((aParams= a.getParameterTypes()).length != (bParams= b.getParameterTypes()).length)
            return false;
        
        for(int i= 0; i < aParams.length; ++i) {
            if(!aParams[i].equals(bParams[i]))
                return false;
        }
        
        return true;
    }
    
    private static final Class<?>[] EMPTY_CLASSES= new Class[0];
    protected static final char METHOD_SEPARATING_CHAR= '#';

    private Set<String> _registeredActions = Collections.emptySet();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doStart() throws Exception {
		super.doStart();

		// register actions in directory
		Action registerActionAction = memory.read(new Action(DirectoryAccessBean.ACTION_ADD_ACTION_TO_DIRECTORY));
		if (registerActionAction != null) {
			IAgentDescription agentDescription= thisAgent.getAgentDescription();
			for (Action act : getActions()) {
				if (_registeredActions.contains(act.getName())) {
					act.setProviderDescription(agentDescription);
					Serializable[] params = {act};
					DoAction action = registerActionAction.createDoAction(params, null);
					memory.write(action);
				}
			}
		}
	}

	/**
	 * @deprecated
	 */
	public void setRegisteredActions(Set<String> actions) {
		_registeredActions = actions;
	}

    public final void doAction(DoAction doAction) throws Exception {
log.debug("typechecking is '" + doAction.typeCheck() + "'");
        
        Action action= (Action)doAction.getAction();
        Method method= searchMethod(action.getName(), action.getInputTypes());
        if(method != null) {
            Serializable result= (Serializable)method.invoke(this, (Object[]) doAction.getParams());
            log.debug("action processed and about to write result...");
            if(method.getReturnType() != void.class) {
                memory.write(action.createActionResult(doAction, new Serializable[] { result }));
            } else {
                memory.write(action.createActionResult(doAction, new Serializable[0]));
            }
            return;
        }
        
        // if we are here, then we have no method with 'name'
        log.debug("did not found exposed method -> call overrideDoAction");
        overrideDoAction(doAction);
    }

    public final List<? extends Action> getActions() {
        ArrayList<Action> actions= new ArrayList<Action>();
        
        for(Method method : getExposedPublicMethods(getClass())) {
            // check for Expose annotation
            Class<?>[] returnTypes= getReturnTypes(method);
            String name= getName(method);
            ActionScope scope = getScope(method);
            // build the action object
            Action act =                 new Action(
                name,
                this,
                method.getParameterTypes(),
                returnTypes
            );
            act.setScope(scope);

            actions.add(act);
        }
        
        // add further actions
        actions.addAll(overrideGetActions());
        return actions;
    }
    
    protected void overrideDoAction(@SuppressWarnings("unused") DoAction doAction) throws Exception {
    	throw new Exception("No implementation of overrideDoAction in " + this.getClass().getName());
    }

    protected List<? extends Action> overrideGetActions() {return Collections.emptyList();}
    
    private Method searchMethod(String name, List<Class<?>> parameters) {
        String originalName= name;
        String assumedMethodName= null;
        // check for hashmark
        int sep= name.indexOf(METHOD_SEPARATING_CHAR);
        
        if(sep > 0 && sep < name.length() - 1) {
            assumedMethodName= name.substring(sep + 1);
        }
        
        for(Method method : getExposedPublicMethods(getClass())) {
            Expose exposeAnno= method.getAnnotation(Expose.class);
            List<Class<?>> mpar= Arrays.asList(method.getParameterTypes());
            
            if(mpar.size() == parameters.size()) {
                if(exposeAnno.name().equals(originalName) || (assumedMethodName != null && method.getName().equals(assumedMethodName))) {
                    if(mpar.equals(parameters)) {
                        return method;
                    }
                }
            }
//            
//            if(mpar.length == parameters.length) {
//                if(exposeAnno.name().equals(originalName) || (assumedMethodName != null && method.getName().equals(assumedMethodName))) {
//                    boolean found= true;
//                    for(int i= 0; i < mpar.length; ++i) {
//                        if(!mpar[i].equals(parameters[i])) {
//                            found= false;
//                            break;
//                        }
//                    }
//                    
//                    if(found) {
//                        return method;
//                    }
//                }
//            }
        }
        
        return null;
    }

	/**
	 * Creates management information about the provided actions.
	 * @return list of action descriptions
	 */
	@SuppressWarnings("unchecked")
    public TabularData getActionList() {
        List<? extends Action> actions = getActions();
        if (actions.isEmpty()) {
        	return null;
        }

        try {
        	// create empty table
        	String[] itemNames = new String[] {"Name", "InputTypes", "ResultTypes", "ProviderBean"};
        	OpenType[] itemTypes = new OpenType[] {SimpleType.STRING, new ArrayType(1, SimpleType.STRING), new ArrayType(1, SimpleType.STRING), SimpleType.STRING};
        	CompositeType rowType = new CompositeType(actions.get(0).getClass().getName(), "provided action", itemNames, itemNames, itemTypes);
        	TabularType tabularType = new TabularType(actions.getClass().getName(), "list of provided actions", rowType, new String[] {"Name", "InputTypes"});
        	TabularData actionList = new TabularDataSupport(tabularType);

        	// fill table
        	for (Action action : actions) {
        		// get names of parameter and result classes
        	    String[] inputTypeList = (action.getInputTypeNames()==null)? null:action.getInputTypeNames().toArray(new String[0]);
        		String[] resultTypeList = (action.getResultTypeNames()==null)? null:action.getResultTypeNames().toArray(new String[0]);

        		// create and add action description
        		Object[] itemValues = new Object[] {action.getName(), inputTypeList, resultTypeList, action.getProviderBean().getBeanName()};
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

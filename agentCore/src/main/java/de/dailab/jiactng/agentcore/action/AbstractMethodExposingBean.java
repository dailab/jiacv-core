/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.action;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
import de.dailab.jiactng.agentcore.action.scope.ActionScope;
import de.dailab.jiactng.agentcore.directory.IOntologyStorage;

/**
 * An abstract Bean which exposes accessible methods which are marked with the
 * {@link IMethodExposingBean.Expose} annotation.
 * 
 * @see IMethodExposingBean.Expose
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public abstract class AbstractMethodExposingBean extends AbstractActionAuthorizationBean implements IMethodExposingBean, AbstractMethodExposingBeanMBean {

	private HashMap<Long,Session> sessions = new HashMap<Long,Session>();
	
	private IOntologyStorage ontologyStorage = null;
	
	private int counter = 0;
	
	public static String getServicename(Method method){
		String servicename= method.getAnnotation(Expose.class).servicename();
		
		if (servicename.isEmpty()){
			servicename = null;
		}
		
		return servicename;
	}
	
	public static String getOperationname(Method method){
		String operationname= method.getAnnotation(Expose.class).operationname();
		
		if (operationname.isEmpty()){
			operationname = null;
		}
		
		return operationname;
	}
	
	
    public static String getName(Method method) {
        String name= method.getAnnotation(Expose.class).name();
        
        if(name.length() == 0) {
            name= method.getDeclaringClass().getName() + METHOD_SEPARATING_CHAR + method.getName();
        }
        
        return name;
    }
    
    /**
     * Checks if a semantic analysis for the method is necessary. 
     * Reads the attribute semantify.
     * 
     * @see IMethodExposingBean.Expose
     * 
     * @param method Method to be checked
     * @return True if tag semantify is set to true, false otherwise.
     */
    public static boolean isSemanticRequested(Method method) {
    	return method.getAnnotation(Expose.class).semantify();
    }
    
    /**
     * Checks if an URI for the semantic description of the service is set
     * 
     * @param method Method to be checked.
     * @return The URI to the semantic description as a string, if available.
     */
    public static String getSemanticURI(Method method) {
    	return method.getAnnotation(Expose.class).semanticURI();
    }
    
    public static ActionScope getScope(Method method) {
      return method.getAnnotation(Expose.class).scope();
    }
    
    public static Class<?>[] getReturnTypes(Method method) {
        final Expose expAnno= method.getAnnotation(Expose.class);
        final Class<?> returnType= method.getReturnType();
        Class<?>[] returnTypes;
        if(returnType.isArray()) {
            returnTypes= expAnno.returnTypes().length > 0 ? expAnno.returnTypes() : new Class[]{returnType};
        } else {
            returnTypes= returnType == void.class ? EMPTY_CLASSES : new Class[]{returnType};
        }
        
        return returnTypes;
    }
    
    public static List<String> getTags(Method method) {
    	Expose expose = method.getAnnotation(Expose.class);
    	if (expose != null && expose.tags() != null && expose.tags().length > 0) {
    		return Arrays.asList(expose.tags());
    	}
    	return null;
    }
    
    public static String getDescription(Method method) {
    	Expose expose = method.getAnnotation(Expose.class);
    	return expose != null ? expose.documentation() : null;
    }
    
    /**
     * Try to get the input parameter names from the method. The respective methods
     * are only available in Java 8, and only if compiled with '-parameters' flag.
     *
     * @param method	some method
     * @return			parameters, if set, or null
     */
    public static List<String> getInputNames(Method method) {
    	// if specified, use annotated parameter names from @Expose
    	Expose expAnno = method.getAnnotation(Expose.class);
    	String[] annotatedParamNames = expAnno.paramNames();
    	if (annotatedParamNames != null && annotatedParamNames.length > 0) {
    		return Arrays.asList(annotatedParamNames);
    	}
    	// otherwise, try to use reflection to get parameter names
    	/*
    	 * Method.getParameters is only available in Java 8, thus using reflection to
    	 * use reflection... this should later be replaced with regular method calls.
    	 * Also, sources have to be compiled with "javac -parameters".
    	 */
    	try {
			Method getParameters = Method.class.getMethod("getParameters");
			Object[] parameters = (Object[]) getParameters.invoke(method);
			List<String> names = new ArrayList<>();
			for (Object param : parameters) {
				Method hasName = param.getClass().getMethod("isNamePresent");
				if (hasName.invoke(param).equals(Boolean.TRUE)) {
					Method getName = param.getClass().getMethod("getName");
					String name = (String) getName.invoke(param);
					names.add(name);
				} else {
					// no action names available without '-parameters' compiler flag
					// return null to indicate that no names are available, and not no parameters given
					return null;
				}
			}
			return Collections.unmodifiableList(names);
    	} catch (NoSuchMethodError | NoSuchMethodException e) {
    		// getParameters not available in Java 7
		} catch (Exception e) {
			// something else went wrong
			e.printStackTrace();
		}
    	return null;
    }
    
    /**
     * Utility method which collects all exposed methods along the given hierarchy.
     * Methods which are annotated in more specialised classes will have precedence. 
     * 
     * Modified this method so it can be used by AbstractAgentBean to check whether
     * methods are using the @Expose tag, which in this case could not be used.
     * 
     * @param clazz the class of the method exposing bean
     * @return the found methods
     */
    public static ArrayList<Method> getExposedPublicMethods(Class<? extends AbstractAgentBean> clazz) {
        final Set<Class<?>> processed= new HashSet<Class<?>>();
        final Queue<Class<?>> nextStep= new LinkedList<Class<?>>();
        final ArrayList<Method> methods= new ArrayList<Method>();
        nextStep.offer(clazz);
        Class<?> current;
        
        do {
            current= nextStep.poll();
            if(processed.add(current)) {
                for(Method method : current.getDeclaredMethods()) {
                    final int modifiers= method.getModifiers();
                    
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
                } // end method loop
                
                // ascend hierarchy
                if(current.getSuperclass() != null && current.getSuperclass() != AbstractMethodExposingBean.class) {
                	nextStep.offer(current.getSuperclass());
                }
                
                for(Class<?> superIntfc : current.getInterfaces()) {
                	nextStep.offer(superIntfc);
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
     * @param a the first method
     * @param b the second method
     * @return the result of equality check
     */
    public static boolean equalsPublicMethod(Method a, Method b) {
        if(!a.getName().equals(b.getName())) {
            return false;
        }
        
        final Class<?>[] aParams = a.getParameterTypes();
        final Class<?>[] bParams = b.getParameterTypes();
        
        if(aParams.length != bParams.length) {
            return false;
        }
        
        for(int i= 0; i < aParams.length; ++i) {
            if(!aParams[i].equals(bParams[i])) {
                return false;
            }
        }
        
        return true;
    }
    
    private static final Class<?>[] EMPTY_CLASSES= new Class[0];
    protected static final char METHOD_SEPARATING_CHAR= '#';

	/**
	 * {@inheritDoc}
	 */
	public void doAction(DoAction doAction) throws Exception {
    	if (log.isDebugEnabled()) {
    		log.debug("typechecking is '" + doAction.typeCheck() + "'");
    	}
        
        final Action action= (Action)doAction.getAction();
        final Method method= searchMethod(action.getName(), action.getInputTypes());
        if(method != null) {
        	final long threadId = Thread.currentThread().getId();
        	sessions.put(threadId, doAction.getSession());
            final Serializable result= (Serializable)method.invoke(this, (Object[]) doAction.getParams());
            sessions.remove(threadId);
        	if (log.isDebugEnabled()) {
        		log.debug("action processed and about to write result...");
        	}
            if(method.getReturnType() != void.class) {
            	if (action.getResultTypes().size() == 1) {
            		memory.write(action.createActionResult(doAction, new Serializable[] { result }));
            	} else {
            		if (result instanceof Serializable[]) {
            			Serializable[] results = (Serializable[]) result;
            			List<Class<?>> expected = action.getResultTypes();
            			// check whether number of results match
            			if (results.length != expected.size()) {
            				log.warn(String.format("Number of Results does not match number given in ActionDescription. "
            						+ "Expected %d, but got %d", expected.size(), results.length));
            			}
            			// check whether result types match
            			for (int i = 0; i < Math.min(results.length, expected.size()); i++) {
            				if (results[i] != null && ! action.getResultTypes().get(i).isInstance(results[i])) {
            					log.warn(String.format("Action result does not match expected type. Expected %s, but got %s", 
            							expected.get(i).getName(), results[i].getClass().getName()));
            				}
            			}
            			// return unpacked results
            			memory.write(action.createActionResult(doAction, results));
            		} else {
            			// just return the single result
            			log.warn("Action @Exposed with multiple return type should return Serializable[], but got " + result.getClass().getName());
            			memory.write(action.createActionResult(doAction, new Serializable[] { result }));
            		}
            	}
            } else {
                memory.write(action.createActionResult(doAction, new Serializable[0]));
            }
            return;
        }
        
        // if we are here, then we have no method with 'name'
        log.debug("did not found exposed method -> call overrideDoAction");
        overrideDoAction(doAction);
    }

	/**
	 * {@inheritDoc}
	 */
    public List<? extends Action> getActions() {
        final ArrayList<Action> actions= new ArrayList<Action>();
        this.ontologyStorage = thisAgent.getAgentNode().findAgentNodeBean(IOntologyStorage.class);
        
        boolean warnAgain = true;
        for(Method method : getExposedPublicMethods(getClass())) {
            // check for Expose annotation
            final Class<?>[] returnTypes= getReturnTypes(method);
            final String name= getName(method);
            final ActionScope scope = getScope(method);
            final String semanticURI = getSemanticURI(method);
            
            // build the action object
            final Action act = new Action(
                name,
                this,
                method.getParameterTypes(),
                returnTypes
            );
            
            act.setTags(getTags(method));
            act.setDocumentation(getDescription(method));
            
        	if (semanticURI != null && !semanticURI.equals("")) {
        		try {
        			Class.forName("de.dailab.jiactng.owlsdescription.ServiceDescription");
        		} catch (ClassNotFoundException e) {
        			if (warnAgain) {
        				log.warn("Exposing Action with semantic service IRI without having access to "
        						+ "ServiceDescription class to handle invocations of the service.");
        				warnAgain = false;
        			}
        		}
        		act.setSemanticServiceDescriptionIRI(semanticURI);
        	}
            
            // add input parameter names, if those are set, or 'arg0', 'arg1', etc.
        	act.setInputNames(getInputNames(method));
            
            act.setScope(scope);
            
            String servicename = getServicename(method);
            String operationname = getOperationname(method);
            
            WebserviceAction wsaction = null;
            
            if ((servicename != null) ||
            		(operationname != null) ){
            	wsaction = new WebserviceAction(act);
            	wsaction.setServiceName(servicename);
            	wsaction.setOperationName(operationname);
            }
            
            if (wsaction != null){
            	actions.add(wsaction);
            } else {
            	actions.add(act);
            }
        }
        
        // add further actions
        actions.addAll(overrideGetActions());
        return actions;
    }
    
    protected void overrideDoAction(@SuppressWarnings("unused") DoAction doAction) throws Exception {
    	throw new Exception("No implementation of overrideDoAction in " + this.getClass().getName());
    }

    protected List<? extends Action> overrideGetActions() {return Collections.emptyList();}
    
    protected Method searchMethod(String name, List<Class<?>> parameters) {
        final String originalName= name;
        String assumedMethodName= null;
        // check for hashmark
        final int sep= name.indexOf(METHOD_SEPARATING_CHAR);
        
        if(sep > 0 && sep < name.length() - 1) {
            assumedMethodName= name.substring(sep + 1);
        }
        
        for(Method method : getExposedPublicMethods(getClass())) {
            final Expose exposeAnno= method.getAnnotation(Expose.class);
            final List<Class<?>> mpar= Arrays.asList(method.getParameterTypes());
            
            if(mpar.size() == parameters.size()) {
                if(exposeAnno.name().equals(originalName) || method.getName().equals(assumedMethodName)) {
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
        final List<? extends Action> actions = getActions();
        if (actions.isEmpty()) {
        	return null;
        }

        try {
        	// create empty table
        	final String[] itemNames = new String[] {"Name", "InputTypes", "ResultTypes", "ProviderBean"};
        	final OpenType[] itemTypes = new OpenType[] {SimpleType.STRING, new ArrayType(1, SimpleType.STRING), new ArrayType(1, SimpleType.STRING), SimpleType.STRING};
        	final CompositeType rowType = new CompositeType(actions.get(0).getClass().getName(), "provided action", itemNames, itemNames, itemTypes);
        	final TabularType tabularType = new TabularType(actions.getClass().getName(), "list of provided actions", rowType, new String[] {"Name", "InputTypes"});
        	final TabularData actionList = new TabularDataSupport(tabularType);

        	// fill table
        	for (Action action : actions) {
        		// get names of parameter and result classes
        	    final String[] inputTypeList = (action.getInputTypeNames()==null)? null:action.getInputTypeNames().toArray(new String[action.getInputTypeNames().size()]);
        		final String[] resultTypeList = (action.getResultTypeNames()==null)? null:action.getResultTypeNames().toArray(new String[action.getResultTypeNames().size()]);

        		// create and add action description
        		final Object[] itemValues = new Object[] {action.getName(), inputTypeList, resultTypeList, action.getProviderBean().getBeanName()};
        		final CompositeData value = new CompositeDataSupport(rowType, itemNames, itemValues);
        		actionList.put(value);
        	}
        	return actionList;
        }
        catch (OpenDataException e) {
        	e.printStackTrace();
        	return null;
        }
	}

	/**
	 * Gets session information about the action, which is actual in execution by the current thread.
	 * @return the session object or <code>null</code> if the current thread is not in action execution
	 */
	protected Session getSession() {
		return sessions.get(Thread.currentThread().getId());
	}
}

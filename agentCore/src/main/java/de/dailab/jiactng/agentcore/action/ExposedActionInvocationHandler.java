/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.action;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean.Expose;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.knowledge.IMemory;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public abstract class ExposedActionInvocationHandler implements IActionInvocationHandler {
    static final Class[] EMPTY_CLASSES= new Class[0];
    
    static final ResultReceiver DUMMY_RECEIVER= new ResultReceiver() {
        public void receiveResult(ActionResult result) {}
    };
    
    class Invocator implements InvocationHandler, IActionInvocationPreparer {
        private ResultReceiver _resultReceiver;
        
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if(IActionInvocationPreparer.class == method.getDeclaringClass()) {
                // it's a method from the preparer interface -> I can handle it
                return method.invoke(this, args);
            }
            
            if(!method.isAnnotationPresent(Expose.class)) {
                throw new UnsupportedOperationException("this method is not exposed as an action");
            }
            
            String name= AbstractMethodExposingBean.getName(method);
            Class[] returnTypes= AbstractMethodExposingBean.getReturnTypes(method);

            Set<Action> actions= getMemory().readAll(new Action(name, null, method.getParameterTypes(), returnTypes));
            Action action= null;
            
            /*
             * Tuple space matcher does not check the equality of array dimensions and elements.
             * So I have to check the action signature by myself...
             */
            Class[] methodParams= method.getParameterTypes();
            outerloop: for(Action a : actions) {
                Class[] actionParams= a.getParameters();
                
                if(actionParams.length != methodParams.length) {
                    continue;
                }
                
                for(int i= 0; i < actionParams.length; ++i) {
                    if(actionParams[i] != methodParams[i]) {
                        continue outerloop;
                    }
                }
                
                action= a;
                break;
            }
            
            if(action == null) {
                throw new UnsupportedOperationException("the associated action could not be found");
            }
            
            try {
                getMemory().write(action.createDoAction(args, _resultReceiver == null ? DUMMY_RECEIVER : _resultReceiver));
                return null;
            } finally {
                _resultReceiver= null;
            }
        }

        public void setResultReceiver(ResultReceiver receiver) {
            _resultReceiver= receiver;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getInvocatorInstance(Class<T> actionProviderInterface) {
        if (!actionProviderInterface.isInterface()) {
            throw new IllegalArgumentException("action provider must be an interface");
        }

        return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {IActionInvocationPreparer.class,
                actionProviderInterface }, new Invocator());
    }

    protected abstract IMemory getMemory();
}

/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.action;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;

import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean.Expose;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.knowledge.IMemory;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycleListener;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import de.dailab.jiactng.agentcore.management.Manager;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public abstract class ExposedActionInvocator implements IActionInvocationHandler {
    static final Class[] EMPTY_CLASSES= new Class[0];
    
    static final ResultReceiver DUMMY_RECEIVER= new ResultReceiver() {
        public void receiveResult(ActionResult result) {}
        public void execute() {}
        public String getBeanName() {return "dummy";}
        public int getExecuteInterval() {return 0;}
        public void handleLifecycleException(LifecycleException e, LifecycleStates state) {}
        public void setBeanName(String name) {}
        public void setExecuteInterval(int executeInterval) {}
        public void setMemory(IMemory mem) {}
        public void setThisAgent(IAgent agent) {}
        public void disableManagement() {}
        public void enableManagement(Manager manager) {}
        public boolean isManagementEnabled() {return false;}
        public void addLifecycleListener(ILifecycleListener listener) {}
        public void cleanup() {}
        public LifecycleStates getState() {return LifecycleStates.STARTED;}
        public void init(){}
        public void removeLifecycleListener(ILifecycleListener listener) {}
        public void start(){}
        public void stateChanged(LifecycleStates oldState, LifecycleStates newState) {}
        public void stop(){}
    };
    
    class Invocator implements InvocationHandler, IActionInvocationPreparer {
        private ResultReceiver _resultReceiver;
        
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if(IActionInvocationPreparer.class.isAssignableFrom(method.getDeclaringClass())) {
                return method.invoke(this, args);
            }
            
            Expose expAnno= method.getAnnotation(Expose.class);

            if(expAnno == null) {
                throw new UnsupportedOperationException("this method is not exposed as an action");
            }
            
            String name= AbstractMethodExposingBean.getName(method);
            Class[] returnTypes= AbstractMethodExposingBean.getReturnTypes(method);

            Set<Action> actions= getMemory().readAll(new Action(name, null, method.getParameterTypes(), returnTypes));
            Action action= null;
            
            
            // check the parameters by myself
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

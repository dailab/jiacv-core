/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.action;

/**
 * An action invocation handler can be used whenever methods
 * were exposed as actions.
 * <p>
 * This handler ensures type safety and wraps the tedious work of
 * action lookup, doAction creation and communication.
 * 
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public interface IActionInvocationHandler {
    /**
     * This method returns an instance of the invocator that is responsible
     * for the given action provider.
     * <p>
     * To prepare the action invocation, you can cast the result to
     * {@link IActionInvocationPreparer} and use the methods defined there.
     * 
     * @param <T>                       the interface of the action provider
     * @param actionProviderInterface   the class of the action provider interface
     * @return                          the responsible action invocator instance
     */
    <T> T getInvocatorInstance(Class<T> actionProviderInterface);
}

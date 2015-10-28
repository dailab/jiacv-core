package de.dailab.jiactng.examples.pingpong.services;

import de.dailab.jiactng.agentcore.action.IMethodExposingBean;
import de.dailab.jiactng.agentcore.action.scope.ActionScope;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.examples.pingpong.ontology.Ping;
import de.dailab.jiactng.examples.pingpong.ontology.Pong;

/**
 * This is the interface of a <strong>Ping Pong</strong> with services. This
 * interface is used to separate declaration and implementation. In this
 * interface we define the JIAC V agent names with {@link String} constants.
 * This interface extends the interface definition from
 * {@link IMethodExposingBean}, therefore we can add the {@link Expose}
 * annotation to our methods.
 *
 * <p>
 * <strong>Advantage</strong>: you can use Javadoc methods to document your
 * services. Second you can separate the interfaces and {@link IFact} in one
 * (Maven) module and put the implementation in a different module. Other
 * developers don't need your implementation.
 * </p>
 *
 * @author mib
 */
public interface PongServices extends IMethodExposingBean {

	/**
	 * A <strong>pingpong</strong> action expects a {@link Ping} as parameter and
	 * returns a {@link Pong}.
	 */
	String ACTION_PINGPONG = "PongAgentBean#PingPong";

	/**
	 * A <strong>echo</strong> action returns the parameter {@link IFact}. It
	 * depends on the implementation if the result will be modified or not.
	 */
	String ACTION_ECHO = "PongAgentBean#Echo";

	/**
	 * To every {@link Ping} will be returned one {@link Pong}.
	 *
	 * @param ping
	 *          a none <code>null</code> {@link Ping}
	 * @return a {@link Pong} depends on the {@link Ping}
	 * @throws Exception
	 *           on any unexpected parameter
	 */
	@Expose(name = ACTION_PINGPONG, scope = ActionScope.GLOBAL)
	Pong pingpong(final Ping ping) throws Exception;

	/**
	 * Returns the parameter {@link IFact}.
	 *
	 * @param arg0
	 *          any serialiseable {@link IFact}
	 * @return the parameter {@link IFact}
	 */
	@Expose(name = ACTION_ECHO, scope = ActionScope.GLOBAL)
	IFact echo(final IFact arg0);
}

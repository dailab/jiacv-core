package de.dailab.jiactng.examples.pingpong.services;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.examples.pingpong.ontology.Ping;

/**
 * A {@link Ping} service agent bean searches for all
 * {@link PongServices#ACTION_PINGPONG} actions and invokes them. If the invoked
 * actions reply, this {@link ResultReceiver} prints the &quot;answers&quot;.
 *
 * This example bases on the {@link AbstractAgentBean} and
 * {@link ResultReceiver} to show the separation of invoking services and
 * receive results from the invoked services. Therefore the
 * {@link ResultReceiver} forces you to implement a separate
 * {@link ResultReceiver#receiveResult(ActionResult)} method.
 *
 * <p>
 * <strong>Note</strong>, you are in a distributed environment. Messages have a
 * delivery delay. The seperation into two methods supports you to design your
 * program unblocking.
 * </p>
 *
 * @author mib
 */
public class PingServiceAgentBean extends AbstractAgentBean implements ResultReceiver {

	private int counter = 0;

	@Override
	public void execute() {
		List<IActionDescription> pingpongs = this.thisAgent.searchAllActions(new Action(PongServices.ACTION_PINGPONG));
		if (pingpongs.size() == 0) {
			this.log.info("found not '" + PongServices.ACTION_PINGPONG + "' actions");
		}
		for (IActionDescription action : pingpongs) {
			this.log.info("invoking " + action.getProviderDescription().getAgentNodeUUID() + "/" + action.getProviderDescription().getAid());
			this.invoke(action, new Serializable[] { new Ping(++this.counter) }, this);
		}
	}

	@Override
	public void receiveResult(final ActionResult result) {

		this.log.info(result.getAction().getName() + " from " + result.getAction().getProviderDescription().getAid() + " with results: "
				+ Arrays.toString(result.getResults()));

	}

}

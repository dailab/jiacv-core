package de.dailab.jiactng.agentcore.environment;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.DoAction;

public abstract class AbstractMethodEffector extends AbstractAgentBean
		implements IEffector {

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface ExposeAction {
	}

	public void doAction(DoAction doAction) {
		String aName = doAction.getAction().getName();
		Class[] params = doAction.getAction().getParameters();
		try {
			Method m = this.getClass().getMethod(aName, params);
			Object result = m.invoke(this, doAction.getParams());
			memory.write(doAction.getAction().createActionResult(
					doAction.getSession(), new Object[] { result }, doAction));
		} catch (Exception ex) {
			thisAgent.getLog(this).error(ex);
		}
	}

	public ArrayList<? extends Action> getActions() {
		ArrayList<Action> ret = new ArrayList<Action>();
		for (Method m : this.getClass().getMethods()) {
			if (m.isAnnotationPresent(ExposeAction.class)) {
				Class[] inputs = m.getParameterTypes();
				Class[] results = new Class[] { m.getReturnType() };

				Action a = new Action(m.getName(), this, inputs, results);
				ret.add(a);
			}
		}
		return ret;
	}

}
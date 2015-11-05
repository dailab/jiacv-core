package de.dailab.jiactng.agentcore.action;

import java.io.Serializable;

import de.dailab.jiactng.agentcore.ontology.IActionDescription;

/**
 * Test bean for testing @Expose with multiple returnTypes.
 *
 * @see TestExposeMultiReturn
 *
 * @author kuester
 */
public class ExposeMultiReturnBean extends AbstractMethodExposingBean {

	/*
	 * Here, the Serializable[] is un-packad into different return values,
	 * i.e. {"foo", 42, 3.14}
	 * (actually, it is just not wrapped in another Serializable[])
	 */
	@Expose(name="multi", returnTypes={String.class, Integer.class, Double.class})
	public Serializable[] testMulti() {
		return new Serializable[] {"foo", 42, 3.14};
	}
	
	/*
	 * This returns just a single string, as usual, i.e. {"foo"}
	 */
	@Expose(name="single")
	public String testSingle() {
		return "foo";
	}
	
	/*
	 * This returns the String[] as single return value, wrapped inside the
	 * usual Serializable[], i.e. {{"foo", "bar"}}
	 */
	@Expose(name="array")
	public String[] testArray() {
		return new String[] {"foo", "bar"};
	}

	
	public ActionResult invokeAndWait(String name) {
		IActionDescription action = thisAgent.searchAction(new Action(name));
		return invokeAndWaitForResult(action, new Serializable[0], 5000L);
	}
	
}

package de.dailab.jiactng.agentcore;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;

/**
 * Unit test for testing {@link Action#matches(de.dailab.jiactng.agentcore.ontology.IActionDescription)}
 * and {@link AgentDescription#matches(de.dailab.jiactng.agentcore.ontology.IAgentDescription)}
 * 
 * @author kuester
 */
public class TestMatching {

	static final Action action = new Action("foo", null, new Class<?>[] {String.class}, new Class<?>[] {Integer.class});
	static final Action otherAc = new Action("bar", null, new Class<?>[] {Double.class}, new Class<?>[] {Boolean.class});
	static final Action templateAc = new Action("foo");
	
	static final AgentDescription agent = new AgentDescription("12345", "foo", "dai", "whatever", null, UUID.randomUUID().toString());
	static final AgentDescription otherAg = new AgentDescription("54321", "bar", "dai", "whatever", null, UUID.randomUUID().toString());
	static final AgentDescription templateAg = new AgentDescription("12345", "foo", null, null, null, null);
	
	@Test
	public void testActionMatches() {
		Assert.assertTrue(action.matches(templateAc));
		Assert.assertFalse(templateAc.matches(action));
		Assert.assertFalse(action.matches(otherAc));
		Assert.assertFalse(otherAc.matches(action));
		Assert.assertFalse(otherAc.matches(templateAc));
		Assert.assertFalse(templateAc.matches(otherAc));
	}
	
	@Test
	public void testAgentMatches() {
		Assert.assertTrue(agent.matches(templateAg));
		Assert.assertFalse(templateAg.matches(agent));
		Assert.assertFalse(agent.matches(otherAg));
		Assert.assertFalse(otherAg.matches(agent));
		Assert.assertFalse(otherAg.matches(templateAg));
		Assert.assertFalse(templateAg.matches(otherAg));
	}
	
}

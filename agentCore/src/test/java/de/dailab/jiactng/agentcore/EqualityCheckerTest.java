package de.dailab.jiactng.agentcore;

import org.junit.Assert;
import org.junit.Test;

import de.dailab.jiactng.agentcore.util.EqualityChecker;

/**
 * Unit tests for {@link EqualityChecker}
 *
 * @author kuester
 */
public class EqualityCheckerTest {

	@Test
	public void testEquals() {
		Assert.assertFalse(EqualityChecker.equals(new String("foo"), new String("bar")));
		Assert.assertTrue (EqualityChecker.equals(new String("foo"), new String("foo")));
		Assert.assertFalse(EqualityChecker.equals(new String("foo"), null));
		Assert.assertFalse(EqualityChecker.equals(null,              new String("foo")));
		Assert.assertTrue (EqualityChecker.equals(null,              null));
	}
	
	@Test
	public void testEqualsOrNull() {
		Assert.assertFalse(EqualityChecker.equalsOrNull(new String("foo"), new String("bar")));
		Assert.assertTrue (EqualityChecker.equalsOrNull(new String("foo"), new String("foo")));
		Assert.assertTrue (EqualityChecker.equalsOrNull(new String("foo"), null));
		Assert.assertTrue (EqualityChecker.equalsOrNull(null,              new String("foo")));
		Assert.assertTrue (EqualityChecker.equalsOrNull(null,              null));
	}

	@Test
	public void testEqualsOrOtherNull() {
		Assert.assertFalse(EqualityChecker.equalsOrOtherNull(new String("foo"), new String("bar")));
		Assert.assertTrue (EqualityChecker.equalsOrOtherNull(new String("foo"), new String("foo")));
		Assert.assertTrue (EqualityChecker.equalsOrOtherNull(new String("foo"), null));
		Assert.assertFalse(EqualityChecker.equalsOrOtherNull(null,              new String("foo")));
		Assert.assertTrue (EqualityChecker.equalsOrOtherNull(null,              null));
	}
	
}

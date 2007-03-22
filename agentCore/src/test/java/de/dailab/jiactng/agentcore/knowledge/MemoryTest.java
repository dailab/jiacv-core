package de.dailab.jiactng.agentcore.knowledge;

import java.util.Iterator;
import java.util.Set;

import org.sercho.masp.space.SimpleObjectSpace;
import org.sercho.masp.space.TupleSpace;
import org.springframework.context.Lifecycle;

import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleEvent;

import junit.framework.TestCase;

public class MemoryTest extends TestCase {

	protected IMemory memory;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		memory = new Memory();
		memory.init();
		memory.start();
	}

	/**
	 * Tests whether the TupleSpace has been
	 * initiated.
	 */
	public void testInitiated() {
		assertNotNull(memory.getTupleSpace());
	}

	/**
	 * Tests the status of the memory after it's been started.
	 */
	public void testStatus() {
		assertEquals(ILifecycle.LifecycleStates.STARTED, memory.getState());
	}
	
	/**
	 * Tests whether the written fact can be read from the space.
	 */
	public void testReadWrite() {
		memory.write(new Fact(1, "test", true));
		Fact fact = memory.read(new Fact(null, null, null));
		assertNotNull(fact);
		assertEquals(fact.integer.intValue(), 1);
		assertEquals(fact.string, "test");
		assertEquals(fact.bool.booleanValue(), true);

		Set<IFact> ifacts = memory.readAllOfType(IFact.class);
		assertNotNull(ifacts);
		assertEquals(ifacts.size(), 1);

		Set<Fact> facts = memory.readAllOfType(Fact.class);
		assertNotNull(facts);
		assertEquals(facts.size(), 1);
		memory.removeAll(new Fact(null, null, null));
	}
	
	/**
	 * Test whether the written fact will be removed.
	 */
	public void testRemove() {
		memory.write(new Fact(2, "test", true));
		Fact fact = memory.remove(new Fact(null, null, null));
		assertNotNull(fact);

		Set<IFact> facts = memory.readAllOfType(IFact.class);
		assertNotNull(facts);
		assertEquals(facts.size(), 0);
	}
	
	/**
	 * Test whether blocking read will work when no fact will be written to the space.
	 */
	public void testBlockingReadEmpty() {
		long starttime = System.currentTimeMillis();
		Fact fact = memory.read(new Fact(null, null, null), 1000);
		long stoptime = System.currentTimeMillis();
		assertNull("time="+(stoptime-starttime), fact);
		System.out.println("time="+(stoptime-starttime));
	}
	
	/**
	 * Test whether blocking read will work.
	 */
	public void testBlockingRead() {
		long starttime = System.currentTimeMillis();
		new Thread() {
			public void run() {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				memory.write(new Fact(3, "test", true));
			}
		}.start();
		Fact fact = memory.read(new Fact(null, null, null), 1000);
		long stoptime = System.currentTimeMillis();
		assertNotNull("time="+(stoptime-starttime), fact);
		System.out.println("time="+(stoptime-starttime));
		memory.removeAll(new Fact(null, null, null));
	}
	
	/**
	 * Test whether blocking remove will work when no fact will be written to the space.
	 */
	public void testBlockingRemoveEmpty() {
		long starttime = System.currentTimeMillis();
		Fact fact = memory.remove(new Fact(null, null, null), 1000);
		long stoptime = System.currentTimeMillis();
		assertNull("time="+(stoptime-starttime), fact);
		System.out.println("time="+(stoptime-starttime));
	}
	
	/**
	 * Test whether blocking read will work.
	 */
	public void testBlockingRemove() {
		long starttime = System.currentTimeMillis();
		new Thread() {
			public void run() {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				memory.write(new Fact(3, "test", true));
			}
		}.start();
		Fact fact = memory.remove(new Fact(null, null, null), 1000);
		long stoptime = System.currentTimeMillis();
		assertNotNull("time="+(stoptime-starttime), fact);
		System.out.println("time="+(stoptime-starttime));
		memory.removeAll(new Fact(null, null, null));
	}

	/**
	 * Tests whether an id has been set.
	 */
	public void testId() {
		assertNotNull(memory.getID());
		assertEquals(memory.getID(), "FactBase");
	}
	
	/**
	 * Tests whether the matcher is available.
	 */
	public void testMatcher() {
		assertNotNull(memory.getMatcher());
	}
	
	/**
	 * Tests whether the updater has been set.
	 */
	public void testUpdater() {
		assertNotNull(memory.getUpdater());
	}
	
	/**
	 * Test whether a fact can be updated.
	 */
	public void testUpdateEmpty() {
		boolean result = memory.update(new Fact(null, null, null), new Fact(5, "update", false));
		assertEquals(result, false);
	}
	
	/**
	 * Test whether a fact can be updated.
	 */
	public void testUpdate() {
		memory.write(new Fact(6, "test", true));
		boolean result = memory.update(new Fact(null, null, null), new Fact(5, "update", false));
		assertEquals(result, true);
		Fact fact = memory.remove(new Fact(null, null, null));
		assertEquals(fact.integer.intValue(), 5);
		assertEquals(fact.string, "update");
		assertEquals(fact.bool.booleanValue(), false);
	}
	
	/**
	 * Test whether a fact can be partially updated.
	 */
	public void testUpdatePartially() {
		memory.write(new Fact(6, "test", true));
		boolean result = memory.update(new Fact(null, null, null), new Fact(5, null, false));
		assertEquals(result, true);
		Fact fact = memory.remove(new Fact(null, null, null));
		assertEquals(fact.integer.intValue(), 5);
		assertEquals(fact.string, "test");
		assertEquals(fact.bool.booleanValue(), false);
	}
	
	/**
	 * Tests whether all facts can be obtained.
	 */
	public void testReadAll() {
		memory.write(new Fact(1, "test1", true));
		memory.write(new Fact(2, "test2", true));
		memory.write(new Fact(3, "test3", true));
		Set<Fact> facts = memory.readAll(new Fact(null, null, null));
		assertNotNull(facts);
		assertEquals(facts.size(), 3);
		memory.removeAll(new Fact(null, null, null));
	}
	
	/**
	 * Tests whether all facts can be removed.
	 */
	public void testRemoveAll() {
		memory.write(new Fact(1, "test1", true));
		memory.write(new Fact(2, "test2", true));
		memory.write(new Fact(3, "test3", true));
		Set<Fact> facts = memory.removeAll(new Fact(null, null, null));
		assertNotNull(facts);
		assertEquals(facts.size(), 3);
		facts = memory.removeAll(new Fact(null, null, null));
		assertNotNull(facts);
		assertEquals(facts.size(), 0);
	}
	
	/**
	 * Tests whether all facts can be removed.
	 */
	public void testReadAllOfType() {
		memory.write(new Fact(1, "test1", true));
		memory.write(new Fact(2, "test2", true));
		memory.write(new Fact(3, "test3", true));
		
		Set<IFact> ifacts = memory.readAllOfType(IFact.class);
		assertNotNull(ifacts);
		assertEquals(ifacts.size(), 3);
		
		Set<Fact> facts = memory.readAllOfType(Fact.class);
		assertNotNull(facts);
		assertEquals(facts.size(), 3);
		
		memory.removeAll(new Fact(null, null, null));
	}
	
	/**
	 * Tests whether all facts can be removed.
	 */
	public void testUpdateAll() {
		memory.write(new Fact(1, "test1", true));
		memory.write(new Fact(2, "test2", true));
		memory.write(new Fact(3, "test3", true));
		boolean result = memory.update(new Fact(null, null, null), new Fact(null, "updated", null));
		assertEquals(result, true);
		Set<Fact> facts = memory.removeAll(new Fact(null, null, null));
		assertEquals(facts.size(), 3);
		for (Fact fact:facts) {
			assertEquals(fact.string, "updated");
		}
	}
	
	/**
	 * Tests the iterator.
	 */
	public void testIterator() {
		memory.write(new Fact(1, "test1", true));
		memory.write(new Fact(2, "test2", true));
		memory.write(new Fact(3, "test3", true));
		Iterator<IFact> it = memory.iterator();
		assertNotNull(it);

		int count=0;
		while (it.hasNext()) {
			it.next();
			count++;
		}
		assertEquals(count, 3);
		memory.removeAll(new Fact(null, null, null));
	}
	
	/**
	 * Tests whether a tuple space can be exchanged.
	 */
	public void testSpaceExchange() {
		TupleSpace<IFact> space = memory.getTupleSpace();
		String name = "TestFactBase";
		memory.setTupleSpace(new SimpleObjectSpace<IFact>(name));
		assertNotNull(memory.getTupleSpace());
		assertEquals(memory.getID(), name);
		memory.setTupleSpace(space);
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		memory.stop();
		memory.cleanup();
		memory = null;
	}
}

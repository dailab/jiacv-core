package de.dailab.jiactng.agentcore.execution;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.Future;

import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;

/**
 * A non-blocking ExecutionCycle implementation. This class executes active agentbeans
 * (those agentbeans where the <code>executionInterval</code> is set to a
 * value greater than 0) and takes care of action requests (<code>DoAction</code>)
 * and their results (<code>ActionResult</code>) in a parallel manner by using the
 * thread pool of the agent node.
 * 
 * @author Jan Keiser
 * 
 * @see IExecutionCycle
 * @see DoAction
 * @see ActionResult
 */
public class NonBlockingExecutionCycle extends BasicExecutionCycle 
	implements NonBlockingExecutionCycleMBean {

	protected TreeMap<Long,List<Future<?>>> futures = new TreeMap<Long,List<Future<?>>>();

	/**
	 * Run-method for the execution cycle. The method iterates over the list of
	 * agentbeans and calls the execute method of each <i>active</i> agentbean.
	 * 
	 * This method also takes care of new <code>DoAction</code>s and
	 * <code>ActionResult</code>s.
	 * 
	 * The <code>NonBlockingExecutionCycle</code> only executes agentbeans and
	 * handles DoActions and ActionResults when it has reached
	 * <code>LifecycleStates.STARTED</code>.
	 * 
	 * @see de.dailab.jiactng.agentcore.execution.IExecutionCycle#run()
	 * @see de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates
	 */
	public void run() {
		processFutureTimeouts();

		// check if lifecycle has been started --> execute if STARTED
		if (getState() == LifecycleStates.STARTED) {
			processAutoExecutionServices();

			// execute the ripest bean
			processBeanExecution();

			// process one doAction
			// TODO: check if read can be used
			final DoAction act = memory.remove(DOACTION_TEMPLATE);
			processDoAction(act);

			// process one actionResult
			// TODO: check if read can be used
			ActionResult result = memory.remove(ACTIONRESULT_TEMPLATE);
			processActionResult(result);			
			
			// Session-Cleanup, if Session has a timeout
			processSessionTimeouts();
		}

		// reject execution if SimpleExecutionCycle hasn't been started
		else {
			log.error("Trying to run NonBlockingExecutionCycle in state "
					+ getState());
		}
	}

	/** 
	 * all execution cycle operations are performed in parallel using the agent's thread pool.
	 */
	@Override
	protected void performExecutionCycleProcess(Runnable process) {
		final Future<?> executionFuture = thisAgent.getThreadPool().submit(process);
		addFuture(executionFuture);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public int getRunningHandlers() {
		synchronized (futures) {
			int result = 0;
			for (List<Future<?>> futureList : futures.values()) {
				result += futureList.size();
			}
			return result;
		}
	}


	/* Adds a runnable thread to the internal data structure with the agent's bean execution timeout. */
	private void addFuture(Future<?> executionFuture) {
		synchronized (futures) {
			long timeout = System.currentTimeMillis() + thisAgent.getBeanExecutionTimeout();
			List<Future<?>> futureList = futures.get(timeout);
			if (futureList == null) {
				futureList = new ArrayList<Future<?>>();
				futures.put(timeout, futureList);
			}
			futureList.add(executionFuture);
		}		
	}

	/**
	 * Cancels all threads, which timeout was reached, 
	 * and updates the internal data structure.
	 * @see Future#cancel(boolean)
	 */
	protected void processFutureTimeouts() {
		synchronized(futures) {
			final long now = System.currentTimeMillis();

			// cancel and remove futures which has reached timeout
			while (!futures.isEmpty() && (futures.firstKey().longValue() < now)) {
				final List<Future<?>> futureList = futures.pollFirstEntry().getValue();
				for (Future<?> future : futureList) {
					if (future.cancel(true)) {
						log.warn("Handler was interrupted by the execution cycle due to timeout constraints");
					} else if (!future.isCancelled() && !future.isDone()) {
						log.warn("Handler can not be canceled by the execution cycle");
					}
				}
			}

			// remove futures which are already done or canceled
			final Long[] keys = futures.keySet().toArray(new Long[futures.keySet().size()]);
			for (int i=0; i<keys.length; i++) {
				final Iterator<Future<?>> iterator = futures.get(keys[i]).iterator();
				while (iterator.hasNext()) {
					final Future<?> future = iterator.next();
					if (future.isCancelled() || future.isDone()) {
						iterator.remove();
					}
				}
				if (futures.get(keys[i]).isEmpty()) {
					futures.remove(keys[i]);
				}
			}
		}		
	}

}

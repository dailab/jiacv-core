package de.dailab.jiactng.agentcore;

import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;

/**
 * A simple implementation of a service library.
 * 
 * @author Jan Keiser
 */
public class SimpleServiceLibrary extends AbstractLifecycle implements
		IServiceLibrary {

	/**
	 * The DFL code of the deployed services.
	 */
	private String  services    = null;

	@Override
	public void doCleanup() throws LifecycleException {
		// TODO Auto-generated method stub

	}

	@Override
	public void doInit() throws LifecycleException {
		// TODO Auto-generated method stub

	}

	@Override
	public void doStart() throws LifecycleException {
		// TODO Auto-generated method stub

	}

	@Override
	public void doStop() throws LifecycleException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IServiceLibrary#getServices()
	 */
	public String getServices() {
	    return services;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IServiceLibrary#setServices(String)
	 */
	public void setServices(String code) {
	    this.services = code;
	}

}

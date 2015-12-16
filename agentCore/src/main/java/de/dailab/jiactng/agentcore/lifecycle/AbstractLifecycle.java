package de.dailab.jiactng.agentcore.lifecycle;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.dailab.jiactng.agentcore.management.Manager;

/**
 * Abstract base class for <code>ILifecycle</code> implementations.
 * <b>Not threadsafe</b>
 *
 * @author Joachim Fuchs
 */
public abstract class AbstractLifecycle extends NotificationBroadcasterSupport implements ILifecycle, AbstractLifecycleMBean {
    
    /**
     * The lifecycle handler that is used internally.
     */
    protected DefaultLifecycleHandler lifecycle = null;

	/** 
	 * The logger to be used. 
	 */
	protected Logger log = null;

	/** 
	 * The configured log level. 
	 */
	private Level intendedLogLevel = null;

	/** 
	 * The manager of the agent node 
	 */
	protected Manager _manager = null;

    /**
     * Default constructor that creates an internally used lifecycle handler for the default mode.
     */
    public AbstractLifecycle() {
        
        lifecycle = new DefaultLifecycleHandler(this);
        
    }

//    /**
//     * Constructor that creates an internally used lifecycle handler for the given mode.
//     * @param strict <code>true</code> for strict mode and <code>false</code> for loose mode
//     */
//    public AbstractLifecycle(boolean strict) {
//        
//        lifecycle = new DefaultLifecycleHandler(this, strict);
//        
//    }
    
    /**
     * The number of the next JMX compliant notification.
     */
    protected long sequenceNumber = 1;
    
//    /**
//     * Reference to the spring application context
//     */
//    protected ApplicationContext applicationContext = null;
    
    /**
     * You may override this method to change the lifecycle event propagation behavior.
     * @throws LifecycleException if the object is not in one of the expected
     * previous states (depending on mode) or an error occurs during change of 
     * the state.
     */
    public final void init() throws LifecycleException {
        try {
        	lifecycle.beforeInit();
        }
        catch (IllegalStateException e) {
        	switch (getState()) {
        		case INITIALIZED:
        		case STOPPED:
        		case STARTED:
        			// already initialized => do nothing
            		return;
            	default:
                	throw new LifecycleException("Initialization not allowed in state " + getState(), e);
        	}
        }
        
        try {
            
            doInit();
            
        } catch (Throwable t) {
        	// keep in state UNDEFINED in case of exception
        	lifecycle.afterInit(false);

        	if (t instanceof LifecycleException) {
                throw (LifecycleException)t;
            }
            
            throw new LifecycleException("Failed to initialize", t);
        }
        
        lifecycle.afterInit(true);
        
    }
    
    /**
     * You may override this method to change the lifecycle event propagation behavior.
     * @throws LifecycleException if the object is not in one of the expected
     * previous states (depending on mode) or an error occurs during change of 
     * the state.
     */
    public final void start() throws LifecycleException {
        try {
        	lifecycle.beforeStart();
        }
        catch (IllegalStateException e) {
        	switch (getState()) {
        		case STARTED:
        			// already started => do nothing
        			return;
        		default:
        			throw new LifecycleException("Starting not allowed in state " + getState(), e);
        	}
        }
        
        try {
            
            doStart();
            
        } catch (Throwable t) {
        	// keep in state INITIALIZED in case of exception
        	lifecycle.afterStart(false);

        	if (t instanceof LifecycleException) {
                throw (LifecycleException)t;
            }
            
            throw new LifecycleException("Failed to start", t);
        }
        
        lifecycle.afterStart(true);
        
    }
    
    /**
     * You may override this method to change the lifecycle event propagation behavior.
     * @throws LifecycleException if the object is not in one of the expected
     * previous states (depending on mode) or an error occurs during change of 
     * the state.
     */
    public final void stop() throws LifecycleException {
        try {
        	lifecycle.beforeStop();
        }
        catch (IllegalStateException e) {
        	switch (getState()) {
        		case INITIALIZED:
        		case STOPPED:
        		case UNDEFINED:
        		case CLEANED_UP:
        			// already stopped => do nothing
            		return;
            	default:
                	throw new LifecycleException("Stopping not allowed in state " + getState(), e);
        	}
        }
        
        try {
            
            doStop();
            
        } catch (Throwable t) {
        	// switch to state STOPPED also in case of exception for completion of a shutdown 
            lifecycle.afterStop();

            if (t instanceof LifecycleException) {
                throw (LifecycleException)t;
            }

            throw new LifecycleException("Failed to stop", t);
        }
        
        lifecycle.afterStop();
        
    }
    
    /**
     * You may override this method to change the lifecycle event propagation behavior.
     * @throws LifecycleException if the object is not in one of the expected
     * previous states (depending on mode) or an error occurs during change of 
     * the state.
     */
    public final void cleanup() throws LifecycleException {
        try {
        	lifecycle.beforeCleanup();
        }
        catch (IllegalStateException e) {
        	switch (getState()) {
        		case UNDEFINED:
        		case CLEANED_UP:
        			// already cleaned up => do nothing
            		return;
            	default:
                    throw new LifecycleException("Cleaning up not allowed in state " + getState(), e);
        	}
        }
        
        try {
            
            doCleanup();
            
        } catch (Throwable t) {
        	// switch to state CLEANED_UP also in case of exception for completion of a shutdown 
            lifecycle.afterCleanup();

            if (t instanceof LifecycleException) {
                throw (LifecycleException)t;
            }
            
            throw new LifecycleException("Failed to clean up", t);
        }
        
        lifecycle.afterCleanup();
        
    }
    
    /**
     * Registers the supplied <code>ILifecycleListener</code>.
     * @param listener the lifecycle listener.
     */
    public final void addLifecycleListener(ILifecycleListener listener) {
        
        lifecycle.addLifecycleListener(listener);
        
    }
    
    /**
     * Unregisters the supplied <code>ILifecycleListener</code>.
     * @param listener the lifecycle listener.
     */
    public final void removeLifecycleListener(ILifecycleListener listener) {
        
        lifecycle.removeLifecycleListener(listener);
        
    }
    
    /**
     * Returns the current lifecycle state of this <code>ILifecycle</code>.
     *
     * @return the current lifecycle state.
     */
    public final LifecycleStates getState() {
        
        return lifecycle.getState();
        
    }
    
    /**
	 * {@inheritDoc}
	 */
    public final String getLifecycleState() {
        return getState().toString();
    }
    
//	/**
//	 * Getter for attribute "Strict" of the managed resource.
//	 * @return the lifecycle mode of this resource
//	 */
//	public boolean isStrict() {
//		return lifecycle.isStrict();
//	}
	
    /**
     * Uses JMX to send notifications that the attribute "LifecycleState"
     * of the managed lifecycle (e.g. agent) has been changed.
     *
     * @param oldState the old state of the lifecycle
     * @param newState the new state of the lifecycle
     */
    public final void stateChanged(LifecycleStates oldState, LifecycleStates newState) {
        final Notification n =
                new AttributeChangeNotification(this,
                sequenceNumber++,
                System.currentTimeMillis(),
                "LifecycleState changed",
                "LifecycleState",
                "java.lang.String",
                oldState.toString(),
                newState.toString());
        
        sendNotification(n);
    }

    /**
     * Returns information about the <code>AttributeChangeNotification</code> 
     * this lifecycle instance may send to notify about the changed state.
     * @return list with only one notification information.
     */
    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        final String[] types = new String[] {
            AttributeChangeNotification.ATTRIBUTE_CHANGE
        };
        final String name = AttributeChangeNotification.class.getName();
        final String description = "An attribute of this MBean has changed";
        final MBeanNotificationInfo info =
                new MBeanNotificationInfo(types, name, description);
        return new MBeanNotificationInfo[] {info};
    }
    
	/**
	 * {@inheritDoc}
	 */
	public final String getLogLevel() {
		if (log != null) {
			return log.getEffectiveLevel().toString();
/*
			if (log.isTraceEnabled()) {
				return Level.TRACE.toString();
			}
			else if (log.isDebugEnabled()) {
				return Level.DEBUG.toString();
			}
			else if (log.isInfoEnabled()) {
				return Level.INFO.toString();
			}
			else if (log.isWarnEnabled()) {
				return Level.WARN.toString();
			}
			else if (log.isErrorEnabled()) {
				return Level.ERROR.toString();
			}
			else if (log.isFatalEnabled()) {
				return Level.FATAL.toString();
			}
			else {
				return Level.OFF.toString();
			}
*/
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setLogLevel(String level) {
		intendedLogLevel = Level.toLevel(level, intendedLogLevel);
		if (log != null && (intendedLogLevel != null)) {
			log.setLevel(intendedLogLevel);
		}
	}

	/**
	 * Sets a logger to be used for logging purposes. It also sets the log level 
	 * of this logger if it was preassigned with method <code>setLogLevel</code>.
	 * @param newLog The logger instance.
	 */
	protected final void setLog(Logger newLog) {
		log = newLog;
		if ((log != null) && (intendedLogLevel != null)) {
			log.setLevel(intendedLogLevel);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final CompositeData getLogger() {
		if (log == null) {
			return null;
		}
		final String[] itemNames = new String[] { "class", "fatal", "error", "warn", "info", "debug", "trace" };
		try {
			final CompositeType type = new CompositeType("javax.management.openmbean.CompositeDataSupport", "Logger information", itemNames, new String[] { "Implementation of the logger instance",
					"fatal", "error", "warn", "info", "debug", "trace" }, new OpenType[] { SimpleType.STRING, SimpleType.BOOLEAN, SimpleType.BOOLEAN, SimpleType.BOOLEAN, SimpleType.BOOLEAN,
					SimpleType.BOOLEAN, SimpleType.BOOLEAN });
			return new CompositeDataSupport(type, itemNames, new Object[] {
			        log.getClass().getName(), Boolean.valueOf(log.isEnabledFor(Level.FATAL)),
			        Boolean.valueOf(log.isEnabledFor(Level.ERROR)), Boolean.valueOf(log.isEnabledFor(Level.WARN)),
			        Boolean.valueOf(log.isInfoEnabled()), Boolean.valueOf(log.isDebugEnabled()),
			        Boolean.valueOf(log.isTraceEnabled()) });
		} catch (OpenDataException e) {
			e.printStackTrace();
			return null;
		}
	}

	  /**
	   * Marks the resource as managed.
	   * 
	   * @param manager The manager responsible for this resource.
	   */
	  public void enableManagement(Manager manager) {
	    _manager = manager;
	  }

	  /**
	   * Marks the resource as unmanaged.
	   */
	  public void disableManagement() {
	    _manager = null;
	  }

	/**
	 * Checks whether the management of this object is enabled or not.
	 * 
	 * @return <code>true</code> if the management is enabled, otherwise <code>false</code>
	 */
	public final boolean isManagementEnabled() {
		return _manager != null;
	}

	/**
     * Put your initialization code here.
     * @throws Exception if this lifecycle instance can not be initialized.
     */
    public abstract void doInit() throws Exception ;
    
    /**
     * Put your start code here.
     * @throws Exception if this lifecycle instance can not be started.
     */
    public abstract void doStart() throws Exception ;
    
    /**
     * Put your stop code here.
     * @throws Exception if this lifecycle instance can not be stopped.
     */
    public abstract void doStop() throws Exception ;
    
    /**
     * Put your clean up code here.
     * @throws Exception if this lifecycle instance can not be cleaned up.
     */
    public abstract void doCleanup() throws Exception ;
    
}

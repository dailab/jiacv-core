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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Level;

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
	protected Log log = null;

	/** 
	 * The configured log level. 
	 */
	private String logLevel = null;

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
    public void init() throws LifecycleException {
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
    public void start() throws LifecycleException {
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
    public void stop() throws LifecycleException {
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
    public void cleanup() throws LifecycleException {
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
    public void addLifecycleListener(ILifecycleListener listener) {
        
        lifecycle.addLifecycleListener(listener);
        
    }
    
    /**
     * Unregisters the supplied <code>ILifecycleListener</code>.
     * @param listener the lifecycle listener.
     */
    public void removeLifecycleListener(ILifecycleListener listener) {
        
        lifecycle.removeLifecycleListener(listener);
        
    }
    
    /**
     * Returns the current lifecycle state of this <code>ILifecycle</code>.
     *
     * @return the current lifecycle state.
     */
    public LifecycleStates getState() {
        
        return lifecycle.getState();
        
    }
    
    /**
	 * {@inheritDoc}
	 */
    public String getLifecycleState() {
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
    public void stateChanged(LifecycleStates oldState, LifecycleStates newState) {
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
	public String getLogLevel() {
		if (log != null && (log instanceof Log4JLogger)) {
			logLevel = ((Log4JLogger)log).getLogger().getEffectiveLevel().toString();
		}
		return logLevel;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setLogLevel(String level) {
		logLevel = level;
		if (log != null && (log instanceof Log4JLogger)) {
			((Log4JLogger)log).getLogger().setLevel(Level.toLevel(logLevel));
		}
	}

	/**
	 * Sets a logger to be used for logging purposes. It also sets the log level 
	 * of this logger if it was preassigned with method <code>setLogLevel</code>.
	 * @param newLog The logger instance.
	 */
	protected void setLog(Log newLog) {
		log = newLog;
		if ((log != null) && (logLevel != null) && (log instanceof Log4JLogger)) {
			((Log4JLogger)log).getLogger().setLevel(Level.toLevel(logLevel));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public CompositeData getLogger() {
		if (log == null) {
			return null;
		}
		final String[] itemNames = new String[] { "class", "debug", "error", "fatal", "info", "trace", "warn" };
		try {
			final CompositeType type = new CompositeType("javax.management.openmbean.CompositeDataSupport", "Logger information", itemNames, new String[] { "Implementation of the logger instance", "debug",
					"error", "fatal", "info", "trace", "warn" }, new OpenType[] { SimpleType.STRING, SimpleType.BOOLEAN, SimpleType.BOOLEAN, SimpleType.BOOLEAN, SimpleType.BOOLEAN,
					SimpleType.BOOLEAN, SimpleType.BOOLEAN });
			return new CompositeDataSupport(type, itemNames, new Object[] {
			        log.getClass().getName(), Boolean.valueOf(log.isDebugEnabled()),
			        Boolean.valueOf(log.isErrorEnabled()), Boolean.valueOf(log.isFatalEnabled()),
			        Boolean.valueOf(log.isInfoEnabled()), Boolean.valueOf(log.isTraceEnabled()),
			        Boolean.valueOf(log.isWarnEnabled()) });
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
	public boolean isManagementEnabled() {
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

package de.dailab.jiactng.agentcore.lifecycle;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.NotificationBroadcasterSupport;

/**
 * Abstract base class for <code>ILifecycle</code> implementations.
 * <b>Not threadsafe</b>
 *
 * @author Joachim Fuchs
 */
public abstract class AbstractLifecycle extends NotificationBroadcasterSupport implements ILifecycle {

    /**
     * The lifecycle handler that is used internally
     */
    protected DefaultLifecycleHandler lifecycle = new DefaultLifecycleHandler(this);

    /**
     * You may override this method to change the lifecycle event propagation behavior.
     */
    public void init() throws LifecycleException {

        lifecycle.beforeInit();

        try {

            doInit();

        } catch (Throwable t) {

            throw new LifecycleException("Failed to initialize", t);

        }

        lifecycle.afterInit();

    }

    /**
     * You may override this method to change the lifecycle event propagation behavior.
     */
    public void start() throws LifecycleException {

        lifecycle.beforeStart();

        try {

            doStart();

        } catch (Throwable t) {

            throw new LifecycleException("Failed to start", t);

        }

        lifecycle.afterStart();

    }

    /**
     * You may override this method to change the lifecycle event propagation behavior.
     */
    public void stop() throws LifecycleException {

        lifecycle.beforeStop();

        try {

            doStop();

        } catch (Throwable t) {

            throw new LifecycleException("Failed to stop", t);

        }

        lifecycle.afterStop();

    }

    /**
     * You may override this method to change the lifecycle event propagation behavior.
     */
    public void cleanup() throws LifecycleException {

        lifecycle.beforeCleanup();

        try {

            doCleanup();

        } catch (Throwable t) {

            throw new LifecycleException("Failed to clean up", t);

        }

        lifecycle.afterCleanup();

    }

    /**
     * Registers the supplied <code>ILifecycleListener</code>.
     */
    public void addLifecycleListener(ILifecycleListener listener) {

        lifecycle.addLifecycleListener(listener);

    }

    /**
     * Unregisters the supplied <code>ILifecycleListener</code>.
     */
    public void removeLifecycleListener(ILifecycleListener listener) {

        lifecycle.removeLifecycleListener(listener);

    }

    /**
     * Returns the current lifecycle state of this <code>ILifecycle</code>.
     *
     * @return the current lifecycle state
     */
    public LifecycleStates getState() {

        return lifecycle.getState();

    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        String[] types = new String[] {
            AttributeChangeNotification.ATTRIBUTE_CHANGE
        };
        String name = AttributeChangeNotification.class.getName();
        String description = "An attribute of this MBean has changed";
        MBeanNotificationInfo info =
            new MBeanNotificationInfo(types, name, description);
        return new MBeanNotificationInfo[] {info};
    }

    /**
     * Put your initialization code here.
     */
    public abstract void doInit() throws LifecycleException ;

    /**
     * Put your start code here.
     */
    public abstract void doStart() throws LifecycleException ;

    /**
     * Put your stop code here.
     */
    public abstract void doStop() throws LifecycleException ;

    /**
     * Put your clean up code here.
     */
    public abstract void doCleanup() throws LifecycleException ;

}

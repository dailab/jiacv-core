package de.dailab.jiactng.agentcore.management.jmx;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.NotificationFilter;

/**
 * This NotificationFilter inherited class disables the lifecyclestate attribute, but
 * lets every other AttributeChangeNotification pass.
 * @author jakob
 */
public class DisableLifeCycleAttributeFilter implements NotificationFilter {
	private static final long serialVersionUID = 1L;
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isNotificationEnabled(Notification notification) {
		if (notification instanceof AttributeChangeNotification) {
			AttributeChangeNotification acn = (AttributeChangeNotification)notification;
			if (acn.getAttributeName().equals("LifecycleState")) {
				return false;
			}
			return true;
		}
		return false;
	}
}
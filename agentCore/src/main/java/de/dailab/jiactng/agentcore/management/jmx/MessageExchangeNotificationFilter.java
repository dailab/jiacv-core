package de.dailab.jiactng.agentcore.management.jmx;

import javax.management.Notification;
import javax.management.NotificationFilter;

/**
 * This class implements the <code>NotificationFilter</code> interface for the 
 * <code>MessageExchangeNotification</code>. The filtering is performed on the 
 * type of the notification instance.
 * @author Jan Keiser
 */
public class MessageExchangeNotificationFilter implements NotificationFilter {

	private static final long serialVersionUID = 1L;

	/**
	 * Invoked before sending the specified notification to the listener.
	 * This filter compares the type of the specified notification with the type
	 * of an message exchange notification. If the type equals the type of an 
	 * message exchange notification, the notification must be sent to the listener 
	 * and this method returns <code>true</code>.
	 * @param notification The notification to be sent.
	 * @return <code>true</code> if the notification has to be sent to the listener, 
	 * <code>false</code> otherwise.
	 */
	@Override
	public boolean isNotificationEnabled(Notification notification) {
		if (notification.getType().equals(MessageExchangeNotification.MESSAGE_EXCHANGE) &&
				(notification instanceof MessageExchangeNotification)) {
			return true;
		}
		return false;
	}

}

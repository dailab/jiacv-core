package de.dailab.jiactng.agentcore.management.jmx;

import javax.management.Notification;
import javax.management.NotificationFilter;

public class MessageExchangeNotificationFilter implements NotificationFilter {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean isNotificationEnabled(Notification notification) {
		if (notification.getType().equals(MessageExchangeNotification.MESSAGE_EXCHANGE)) {
			return true;
		}
		return false;
	}

}

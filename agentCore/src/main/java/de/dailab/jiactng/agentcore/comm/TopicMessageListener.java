package de.dailab.jiactng.agentcore.comm;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.comm.protocol.IProtocolHandler;

/**
 * Eine MessageListener, der den Consumern übergeben wird.
 * Es soll nur auf Nachrichten aus Topics reagieren.
 * @author janko
 */
public class TopicMessageListener implements MessageListener {
	Log log = LogFactory.getLog(getClass());
	IProtocolHandler _protocol;
	CommBean _commBean; 
	
	public TopicMessageListener(IProtocolHandler protocol, CommBean commBean) {
		_protocol = protocol;
		_commBean = commBean;
	}

	/**
	 * Empfängt die JMS Nachrichten, leitet an die CommBean und an das Protocol weiter.
	 * momentan wird jede ObjektNachricht bestätigt.
	 */
	public void onMessage(Message msg) {
		log.debug(" JiacMessageListener msg received");
		if (msg != null) {
			try {
				ObjectMessage oMsg = (ObjectMessage) msg;
				String msgRecipient = oMsg.getStringProperty(Constants.ADDRESS_PROPERTY);
				debugMsg(oMsg, msgRecipient);
				// System.out
				// .println("An '" + msgReceipient + "' gerichtete Nachricht erhalten:\n
				// " + oMsg.getObject().toString());
				handleMessage(msg);
				msg.acknowledge();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void handleMessage(Message msg) {
		IJiacMessage jiacMessage = Util.extractJiacMessage(msg);
		if (!jiacMessage.getStartPoint().equals(_commBean.getAddress())) {
			_commBean.messageReceivedFromTopic(msg);
			_protocol.processMessage(msg);
			
		}
	}

	private void debugMsg(ObjectMessage msg, String addressProperty) throws JMSException {
		JiacMessage jMsg = (JiacMessage) msg.getObject();
		log.debug("Von:" + jMsg.getStartPoint() + " An:" + jMsg.getEndPoint() + " Content:"
				+ jMsg.getPayload().toString());
		log.debug("AddressProperty: " + addressProperty);
	}
}

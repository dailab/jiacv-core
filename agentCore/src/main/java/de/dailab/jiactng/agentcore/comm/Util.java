package de.dailab.jiactng.agentcore.comm;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.net.InetAddress;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

/**
 * UtilityKlasse, die statische Methoden kapselt.
 * 
 * @author janko
 */
public class Util {
	static Logger log4j = Logger.getLogger("de.dailab.jiactng.agentcore.comm.Util");

	public static final char SEPARATOR = '#';

	/**
	 * Liefert die LocalHost-Inetaddress
	 * 
	 * @return die locahlhost-inetaddress, oder null bei Fehler/exception
	 */
	public static InetAddress getLocalHost() {
		try {
			return InetAddress.getLocalHost();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static int getLocalHostIpAsInt() {
		byte[] ipaddress = getLocalHost().getAddress();
		int retVal = 0;
		for (int i = 0; i < ipaddress.length; i++) {
			retVal = (retVal | ipaddress[i] << (8 * i));
		}
		return retVal;
	}

	/**
	 * Erzeugt einen InitialenKontext f�r den ActiveMQ zugriff.
	 * 
	 * @param factoryClassname der Name der ConnectionFactoryKlasse, die zum instanziieren von Connections verwendet
	 *          werden soll
	 * @param urlString die ProviderUrl, d.h. die Url des Brokers
	 * @param platformId die Id der Platform - es wird eine Queue mit diesem Namen erzeugt,und unter dem gleichen Namen
	 *          ins JNDI eingetragen
	 * @return den erzeugten Kontext, oder null bei fehler
	 */
	public static Context createInitialContext(String factoryClassname, String urlString, String platformId) {
		Properties props = new Properties();
		// props.setProperty(Context.INITIAL_CONTEXT_FACTORY,
		// "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
		// props.setProperty(Context.PROVIDER_URL, "tcp://hostname:61616");
		props.setProperty(Context.INITIAL_CONTEXT_FACTORY, factoryClassname);
		props.setProperty(Context.PROVIDER_URL, urlString);
		props.setProperty("queue.JiacQueue", "AgentQueue");
		props.setProperty("queue.TestQueue", "TestQueue");
		props.setProperty("queue." + platformId, platformId);

		props.setProperty("topic.JiacTopic", "JiacTngPlatform");
		Context ctx = null;
		try {
			ctx = new InitialContext(props);
			// printContextEnvProps(ctx);
		} catch (Exception e) {
			log4j.error(e.getMessage());
		}
		return ctx;
	}

	/**
	 * Zum Debuggen
	 * 
	 * @param ctx
	 */
	public static void printContextEnvProps(Context ctx) {
		try {
			Hashtable ht = ctx.getEnvironment();
			for (Iterator iter = ht.keySet().iterator(); iter.hasNext();) {
				Object element = (Object) iter.next();
				System.out.println(element.toString() + ": " + ht.get(element).toString());
			}
		} catch (Exception e) {
			log4j.error(e.getMessage());
		}
	}

	/**
	 * Sucht im Context nach einer Connectionfactory mit gegebenen Namen
	 * 
	 * @param ctx
	 * @param factoryName
	 * @return
	 * @throws NamingException
	 */
	public static ConnectionFactory lookupConnectionFactory(Context ctx, String factoryName) throws NamingException {
		ConnectionFactory connectionFactory = (ConnectionFactory) ctx.lookup(factoryName);
		return connectionFactory;
	}

	/**
	 * Sucht im Context nach einer Destination mit gegebenen Namen
	 * 
	 * @param ctx
	 * @param destName
	 * @return
	 * @throws NamingException
	 */
	public static Destination lookupDestination(Context ctx, String destName) throws NamingException {
		Destination destination = (Destination) ctx.lookup(destName);
		return destination;
	}

	/**
	 * Sucht im Context nach einem Topic mit gegebenen Namen
	 * 
	 * @param ctx
	 * @param topicName
	 * @return
	 * @throws NamingException
	 */
	public static Topic lookupTopic(Context ctx, String topicName) throws NamingException {
		Topic destination = (Topic) ctx.lookup(topicName);
		return destination;
	}

	/**
	 * Sucht im Context nach einer Queue mit gegebenen Namen
	 * 
	 * @param ctx
	 * @param queueName
	 * @return
	 * @throws NamingException
	 */
	public static Queue lookupQueue(Context ctx, String queueName) throws NamingException {
		Queue destination = (Queue) ctx.lookup(queueName);
		return destination;
	}

	/**
	 * Sucht im Context nach einer dynamischen Queue mit gegebenen Namen
	 * 
	 * @param ctx der Context
	 * @param queueName der Queuename ohne prefix - dieser wird in der Methoed erzeugt und verwendet
	 * @return
	 * @throws NamingException
	 */
	public static Queue lookupDynamicQueue(Context ctx, String queueName) throws NamingException {
		String prefix = "dynamicQueue/";
		Queue destination = (Queue) ctx.lookup(prefix + queueName);
		return destination;
	}

	/**
	 * Hier wird der Selektor zum Filtern von Platform-Ping-Messages erzeugt
	 * 
	 * @param platformId die Id des senders
	 * @return der String, der einen Selektor angibt, in dem das PING_PLATFORM_ID_PROPERTY ungleich dem �bergebenen ist
	 */
	public static String createPlatformPingSelector(String platformId) {
		String messageSelector = Constants.PING_PLATFORM_ID_PROPERTY + "<>'" + platformId + "'";
		System.out.println("msgSelector set to '" + messageSelector + "'");
		return messageSelector;
	}

	/**
	 * Eine eindeutige Platform-Id, die IPadresse+":"+zeit
	 * 
	 * @id eine id um die erzeugte ID zus�tzlich eindeutiger machen zu k�nnen
	 * @return
	 */
	public static String createPlatformId(long id) {
		long time = GregorianCalendar.getInstance().getTime().getTime();
		return new String(Util.getLocalHost().toString() + SEPARATOR + id + SEPARATOR + time);
	}

	public static void close(Connection con) {
		closeAll(con, null, null, null);
	}

	public static void closeAll(Connection con, Session session) {
		closeAll(con, session, null, null);
	}

	/**
	 * Hilfsmethode zum Schliessen von ressourcen der send-Methode
	 * 
	 * @param con
	 * @param session
	 * @param producer
	 */
	public static void closeAll(Connection con, Session session, MessageProducer producer, TopicPublisher publisher) {
		if (producer != null) {
			try {
				producer.close();
			} catch (JMSException e) {
				log4j.warn(e.getMessage());
			}
		}
		if (publisher != null) {
			try {
				publisher.close();
			} catch (JMSException e) {
				log4j.warn(e.getMessage());
			}
		}
		if (session != null) {
			try {
				session.close();
			} catch (JMSException e) {
				log4j.warn(e.getMessage());
			}
		}
		if (con != null) {
			try {
				con.close();
			} catch (JMSException e) {
				log4j.warn(e.getMessage());
			}
		}
		publisher = null;
		producer = null;
		session = null;
		con = null;
		System.gc();
	}

	public static String createPlatformId() {
		return Util.createPlatformId(Math.abs(Util.getLocalHostIpAsInt()));
	}

	/**
	 * Erzeugt mit Programmparametern ein initialContext
	 * 
	 * @param args, ein Stringarray mit den parameter <ConnectionFactoryName>, <URL zum ActiveMQ-Broker>
	 * @return den Context, oder null wenn keine Parameter angegeben wurden
	 * @see com.jmstest.infrastructure.Util.createInitialContext(conFactory, url)
	 */
	public static Context createContext(String[] args, String platformId) {
		if (args != null && args.length > 1) {
			String conFactory = args[0];
			String url = args[1];
			if (conFactory != null && url != null) {
				return Util.createInitialContext(conFactory, url, platformId);
			}
		}
		return null;
	}

	public static char getRandomChar() {
		int rnd = (int) (Math.random() * 26);
		char[] field = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'n', 'o', 'p', 'q', 'r', 's',
																						't', 'u', 'v', 'w', 'x', 'y', 'z' };
		return field[rnd];
	}

	public static String getRandomName() {
		String[] rndNames = { "Gandalf", "Sonne", "Bilbo", "Titan", "Zeus", "CuteKnut", "Merkur", "Gaia", "Mond" };
		int rnd = (int) (Math.random() * rndNames.length);
		return rndNames[rnd];
	}

	/**
	 * Holt aus eine JMSMessage die JiacMessage raus, bei Fehler wird null zur�ckgegeben
	 * 
	 * @param msg
	 * @return die in der JMSMessage enthaltene JiacMessage, oder null bei fehler
	 */
	public static IJiacMessage extractJiacMessage(Message msg) {
		try {
			if (msg instanceof ObjectMessage) {
				Object jiacMsg = ((ObjectMessage) msg).getObject();
				return (IJiacMessage) jiacMsg;
			}
		} catch (JMSException ex) {}
		return null;
	}

	/** Swing HelperMethode */
	public static Container getRootContainer(Component comp) {
		Container parent = comp.getParent();
		while (parent != null && !(parent instanceof Frame)) {
			parent = parent.getParent();
		}
		return parent;
	}

	/**
	 * Erzeugt mit einer Session eine neue Queue
	 * 
	 * @param session die Session f�r die die Queue erzeugt wird
	 * @param destName
	 * @return die erzeugte Queue, oder null wenn eingabewerte null waren
	 */
	public static Destination createQueueFromName(Session session, String destName) throws JMSException {
		if (session != null && destName != null) {
			Destination destination = session.createQueue(destName);
			return destination;
		}
		return null;
	}

	/**
	 * Erzeugt mit einer Session eine neue Topic
	 * 
	 * @param session die Session f�r die die Topic erzeugt wird
	 * @param destName
	 * @return die erzeugte Topic, oder null wenn eingabewerte null waren
	 */
	public static Destination createTopicFromName(Session session, String destName) throws JMSException {
		if (session != null && destName != null) {
			Destination destination = session.createTopic(destName);
			return destination;
		}
		return null;
	}

	/**
	 * setzt properties am Messageobject mit passenden methoden
	 */
	public static void setProperties(Message msg, Properties props) {
		if (msg != null && props != null) {
			for (Iterator iter = props.keySet().iterator(); iter.hasNext();) {
				String key = (String) iter.next();
				Object value = props.getProperty(key);
				try {
					if (value instanceof String) {
						msg.setStringProperty(key, (String) value);
					} else if (value instanceof Byte) {
						msg.setByteProperty(key, ((Byte) value).byteValue());
					} else if (value instanceof Boolean) {
						msg.setBooleanProperty(key, ((Boolean) value).booleanValue());
					} else if (value instanceof Double) {
						msg.setDoubleProperty(key, ((Double) value).doubleValue());
					} else if (value instanceof Float) {
						msg.setFloatProperty(key, ((Float) value).floatValue());
					} else if (value instanceof Integer) {
						msg.setIntProperty(key, ((Integer) value).intValue());
					} else if (value instanceof Long) {
						msg.setLongProperty(key, ((Long) value).longValue());
					} else if (value instanceof Short) {
						msg.setShortProperty(key, ((Short) value).shortValue());
					} else {
						// evtl. sicherstellen, dass es serialisierbar ist
						msg.setObjectProperty(key, value);
					}
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		}
	}
}

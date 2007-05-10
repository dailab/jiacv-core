package de.dailab.jiactng.agentcore.comm;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.servicediscovery.IService;
import de.dailab.jiactng.agentcore.servicediscovery.IServiceDescription;
import de.dailab.jiactng.agentcore.servicediscovery.ServiceDescription;
import de.dailab.jiactng.agentcore.servicediscovery.ServiceParameter;

/**
 * Einfach noch n Service zum testen. es sind die gleichen methoden wie in exampleservice nur die operationenNamen sind anders
 * 
 * @author janko
 */
public class ExampleService2 extends AbstractAgentBean implements IService {

	public static final String SERVICE_GETTIME = "getTime2";
	public static final String SERVICE_GETSYSINFO = "getSysInfo2";
	public static final String SERVICE_TIME_DIFFERENCE = "getTimeDifference2";

	public static final char SEPARATOR = ':';

	// erstmal nur lokal aufrufen und ergbins nach system.out schreiben
	public void doAction(DoAction actionName) {
		if (SERVICE_GETTIME.equals(actionName.getAction().getName())) {
			System.out.println(getTime());
		} else if (SERVICE_GETSYSINFO.equals(actionName.getAction().getName())) {
			System.out.println(getSysInfo());
		} else if (SERVICE_TIME_DIFFERENCE.equals(actionName.getAction().getName())) {
			System.out.println(getTimeDifference(new Date()).toString());
		}
	}

	/**
	 * Wird bei der AgentenInitalisierung aufgerufen, das Ergebnis gespeichert
	 */
	public ArrayList<Action> getActions() {
		ArrayList<Action> actionList = new ArrayList<Action>();
		actionList.add(new Action(SERVICE_GETTIME, this, new Class[0], new Class[0]));
		actionList.add(new Action(SERVICE_GETSYSINFO, this, new Class[0], new Class[0]));
		Class inputClasses[] = { Date.class };
		Class outputClasses[] = { Date.class };
		actionList.add(new Action(SERVICE_TIME_DIFFERENCE, this, inputClasses, outputClasses));
		return actionList;
	}

	// SERVICE_GETTIME
	// die WOrkermethode für diesen Service
	private Date getTime() {
		return (new GregorianCalendar()).getTime();
	}

	// SERVICE_GETSYSINFO
	// die WOrkermethode für diesen Service
	private String getSysInfo() {
		String s = System.getProperties().getProperty("os.name") + ':' + System.getProperties().getProperty("os.arch")
																						+ ':' + System.getProperties().getProperty("os.version");
		return s;
	}

	// n Service der eingabe und ausgabeparameter hat
	// die WOrkermethode für diesen Service
	private Date getTimeDifference(Date srcTime) {
		// mock
		return new Date();
	}

	/**
	 * Entsprechend der getActions-Methode wird eine Liste mit ServiceBeschreibungen erzeugt.
	 * 
	 * @return
	 */
	public List<IServiceDescription> getServiceDescriptions() {
		List<IServiceDescription> list = new ArrayList<IServiceDescription>();
		String keywords[] = { "Time" };
		ServiceParameter spIn[] = {};
		ServiceParameter spOut[] = {};
		ServiceDescription sd = new ServiceDescription(new Date(), createServiceId(SERVICE_GETTIME), SERVICE_GETTIME,
																						keywords, spIn, spOut, "", "", createServiceId(SERVICE_GETTIME), "", "");
		list.add(sd);
		
		keywords = new String[2];
		keywords[0] = "System";
		keywords[1] = "Info";
		spIn = new ServiceParameter[0];
		spOut = new ServiceParameter[0];
		sd = new ServiceDescription(new Date(), createServiceId(SERVICE_GETSYSINFO), SERVICE_GETSYSINFO, keywords, spIn,
																						spOut, "", "", createServiceId(SERVICE_GETSYSINFO), "", "");
		list.add(sd);
		
		keywords = new String[1];
		keywords[0] = "Time";
		spIn = new ServiceParameter[1];
		spIn[0] = new ServiceParameter("java.util.Date", "srcDate");
		spOut = new ServiceParameter[1];
		spOut[0] = new ServiceParameter("java.util.Date", "timeDiff");
		sd = new ServiceDescription(new Date(), createServiceId(SERVICE_TIME_DIFFERENCE), SERVICE_TIME_DIFFERENCE, keywords, spIn,
																						spOut, "", "", createServiceId(SERVICE_TIME_DIFFERENCE), "", "");

		list.add(sd);
		return list;
	}

	/**
	 * Erzeugt n eindeutigen String für die Operation des Services
	 * 
	 * @param operationName der Operationsname
	 * @return eindeutiger String für die ServiceOperation
	 */
	private String createServiceId(String operationName) {
		return operationName + SEPARATOR + this.getClass().getSimpleName() + beanName + SEPARATOR
																						+ thisAgent.getAgentName() + SEPARATOR + thisAgent.getAgentNode().getName();
	}

	/**
	 * Service-Methode, ruft eine Service-Operation auf.
	 */
	public ServiceParameter[] invoke(String serviceName) {
		if (SERVICE_GETTIME.equals(serviceName)) {
			System.out.println(getTime());
		} else if (SERVICE_GETSYSINFO.equals(serviceName)) {
			System.out.println(getSysInfo());
		} else if (SERVICE_TIME_DIFFERENCE.equals(serviceName)) {
			System.out.println(getTimeDifference(new Date()).toString());
		}
		return null;
	}
}

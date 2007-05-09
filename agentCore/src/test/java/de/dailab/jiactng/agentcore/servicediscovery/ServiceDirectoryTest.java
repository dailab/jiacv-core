package de.dailab.jiactng.agentcore.servicediscovery;

import java.util.Set;

import junit.framework.TestCase;

public class ServiceDirectoryTest extends TestCase {

	ServiceDirectory sDir;

	public void setUp() {
		sDir = new ServiceDirectory();
		try {
			sDir.doInit();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	public void tearDown() {
		try {
			sDir.doCleanup();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	/**
	 * 
	 *
	 */
	public void testRegisterService() {
		String serviceName = "TestService";
		IServiceDescription desc1 = new ServiceDescription(null, "Service1 ID", serviceName, null, null, null, null, null,
																						null, null, null);
		sDir.registerService(desc1);
		Set<ServiceDescription> sd = sDir.findServiceByName(serviceName);
		assertEquals(desc1, sd.toArray()[0]);
		assertSame(desc1, sd.toArray()[0]);

		IServiceDescription desc1a = new ServiceDescription(null, "Service1a ID", "TestService", null, null, null, null,
																						null, null, null, null);
		assertFalse(desc1a.equals(sd.toArray()[0]));

	}

	public void testDeRegisterService() {
		String serviceName = "TestService";
		IServiceDescription desc1 = new ServiceDescription(null, "Service1 ID", serviceName, null, null, null, null, null,
																						null, null, null);
		IServiceDescription desc2 = new ServiceDescription(null, "Service2 ID", "TestService2", null, null, null, null,
																						null, null, null, null);
		IServiceDescription desc3 = new ServiceDescription(null, "Service3 ID", "TestService3", null, null, null, null,
																						null, null, null, null);
		sDir.registerService(desc1);
		sDir.registerService(desc2);
		sDir.registerService(desc3);

		Set<ServiceDescription> sd = sDir.findServiceByName(serviceName);
		assertEquals(desc1, (ServiceDescription) sd.toArray()[0]);
		sDir.deRegisterService((ServiceDescription) sd.toArray()[0]);
		Set<ServiceDescription> sd2 = sDir.findServiceByName(serviceName);

		assertFalse(sd2.contains(desc1));
	}

	public void testFindServiceByName() {
		String serviceName1 = "TestService1";
		String serviceName2 = "TestService2";
		String serviceName3 = "TestService3";
		IServiceDescription desc1 = new ServiceDescription(null, "Service1 ID", serviceName1, null, null, null, null, null,
																						null, null, null);
		IServiceDescription desc2 = new ServiceDescription(null, "Service2 ID", serviceName2, null, null, null, null, null,
																						null, null, null);
		IServiceDescription desc3 = new ServiceDescription(null, "Service3 ID", serviceName3, null, null, null, null, null,
																						null, null, null);
		sDir.registerService(desc1);
		sDir.registerService(desc2);
		sDir.registerService(desc3);

		Set<ServiceDescription> sd = sDir.findServiceByName(serviceName1);
		assertEquals(1, sd.size());
		assertEquals(desc1, sd.toArray()[0]);

		sd = sDir.findServiceByName(serviceName2);
		assertEquals(1, sd.size());
		assertEquals(desc2, sd.toArray()[0]);

		sd = sDir.findServiceByName(serviceName3);
		assertEquals(1, sd.size());
		assertEquals(desc3, sd.toArray()[0]);
	}

	public void testGetServiceNumber() {
		String serviceName = "TestService";
		IServiceDescription desc1 = new ServiceDescription(null, "Service1 ID", serviceName, null, null, null, null, null,
																						null, null, null);
		IServiceDescription desc2 = new ServiceDescription(null, "Service2 ID", "TestService2", null, null, null, null,
																						null, null, null, null);
		IServiceDescription desc3 = new ServiceDescription(null, "Service3 ID", "TestService3", null, null, null, null,
																						null, null, null, null);
		sDir.registerService(desc1);
		sDir.registerService(desc2);
		sDir.registerService(desc3);

		int number = sDir.getServiceNumber();
		assertEquals(3, number);
	}

}

package de.dailab.jiactng.agentcore.performance;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * This class measures the duration of the setup procedure of a
 * spring-based application (e.g. agent node). Additionally the number of threads,
 * the used heap and non-heap memory and the number of loaded classes are measured
 * for the running application.
 * 
 * @author Jan Keiser
 */
public class SetupMeasurement {

	/**
	 * Starts and measures an spring-based application.
	 * @param args the filename of the spring configuration
	 */
	public static void main(String[] args) {
		// start application and measure duration
		long startTime = System.nanoTime();		
		if (args.length > 0) {
			new ClassPathXmlApplicationContext(args[0]);
		}
		long duration = System.nanoTime() - startTime;

		// get heap and non-heap memory usage
		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
		long heap = memoryBean.getHeapMemoryUsage().getUsed();
		long nonHeap = memoryBean.getNonHeapMemoryUsage().getUsed();
		
		// get number of loaded classes
		int classes = ManagementFactory.getClassLoadingMXBean().getLoadedClassCount();
		
		// get number of live threads
		int threads = ManagementFactory.getThreadMXBean().getThreadCount();
		
		// print all data to console
		System.out.println("Duration (nanos): " + duration);
		System.out.println("Heap size(bytes): " + heap);
		System.out.println("Non-heap (bytes): " + nonHeap);
		System.out.println("Threads (number): " + threads);
		System.out.println("Classes (number): " + classes);
		
		System.exit(0);
	}
		
}

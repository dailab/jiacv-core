/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.util;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.Map;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;

/**
 * @author Some Guy in the internet
 * @version $Revision:$
 */
public class MemoryDebugger {
    static class NotificationListenerImpl implements NotificationListener {
        public void handleNotification(Notification notification, Object handback) {
            if (notification.getType().equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED)) {
                Map<Thread,StackTraceElement[]> map= Thread.getAllStackTraces();
                
                for(Thread thread : map.keySet()) {
                    printStackTrace(thread.getName(), map.get(thread));
                }
            }
        }
        
        private void printStackTrace(String name, StackTraceElement[] elements) {
            System.err.println("Thread " + name + " computes: ");
            
            for(int i= 0; i < elements.length; ++i) {
                System.err.println("\tat " + elements[i]);
            }
            
        }
    }

    private static final MemoryPoolMXBean tenuredGenPool= findTenuredGenPool();
    private static boolean initialised= false;
    
    private static MemoryPoolMXBean findTenuredGenPool() {
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            if (pool.getType() == MemoryType.HEAP && pool.isUsageThresholdSupported()) {
                return pool;
            }
        }
        throw new AssertionError("Could not find tenured space");
    }

    public static void setup() {
        setup(0.75);
    }
    
    public static void setup(double fraction) {
        synchronized (MemoryDebugger.class) {
            if(!initialised) {
                long maxMemory= tenuredGenPool.getUsage().getMax();
                tenuredGenPool.setUsageThreshold((long) (fraction * maxMemory));
                MemoryMXBean mxBean = ManagementFactory.getMemoryMXBean();
                ((NotificationEmitter) mxBean).addNotificationListener(new NotificationListenerImpl(), null, null);
            }
        }
    }
}

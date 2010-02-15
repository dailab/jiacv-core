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

public class MemoryDebugger {
    static class NotificationListenerImpl implements NotificationListener {
        public void handleNotification(Notification notification, Object handback) {
            if (notification.getType().equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED)) {
                final Map<Thread,StackTraceElement[]> map= Thread.getAllStackTraces();
                
                for(Map.Entry<Thread,StackTraceElement[]> entry : map.entrySet()) {
                    printStackTrace(entry.getKey().getName(), entry.getValue());
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

    private static final MemoryPoolMXBean TENURED_GEN_POOL = findTenuredGenPool();
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
                final long maxMemory= TENURED_GEN_POOL.getUsage().getMax();
                TENURED_GEN_POOL.setUsageThreshold((long) (fraction * maxMemory));
                final MemoryMXBean mxBean = ManagementFactory.getMemoryMXBean();
                ((NotificationEmitter) mxBean).addNotificationListener(new NotificationListenerImpl(), null, null);
                initialised= true;
            }
        }
    }
}

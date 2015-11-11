/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.management.jmx;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import javax.management.remote.JMXServiceURL;

import junit.framework.TestCase;
import de.dailab.jiactng.aamm.ApplicationContext;
import de.dailab.jiactng.agentcore.IAgentNode;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class JmxRemoteTest extends TestCase {

	private class StreamPumper extends Thread {
        private final BufferedReader _reader;
        private final String _prefix;

        StreamPumper(InputStream in, String prefix) {
            _reader= new BufferedReader(new InputStreamReader(in), 1024);
            _prefix= prefix;
        }

        @Override
        public void run() {
            try {
                String s;
                while((s= _reader.readLine()) != null) {
                	// ignore warnings from URL tester
                	if (!s.startsWith("Unable to connect to ")) {
                		System.out.println(_prefix + ": " + s);
                	}
                }
            } catch (Throwable t) {
                // ignore
            } finally {
                try {_reader.close();} catch (IOException e) {}
            }
        }
    }


    public void testJmxRemote() throws Exception {
        // launch node
        ApplicationContext ac= new ApplicationContext("de.dailab.jiactng.agentcore.management.jmx.jmxremote");
        final IAgentNode node= (IAgentNode) ac.getBean("de.dailab.jiactng.agentcore.management.jmx.jmxremote#NodeWithRemoteJMX");

        // wait 5 seconds to ensure creation and registration of the JMX connector server
        Thread.sleep(5000);

        // ensure that node is started within 2 seconds (not needed anymore)
        StartListener listener= new StartListener(node);
        node.addLifecycleListener(listener);
        assertTrue("node did not start properly", listener.ensureStarted(2000L));

        // get one of the service URLs, but ignore 127.0.0.1 if other URL exists
        Set<JMXServiceURL> jmxURLs = node.getJmxURLs();
        assertTrue("no connector found", !jmxURLs.isEmpty());
        Iterator<JMXServiceURL> i = jmxURLs.iterator();
        JMXServiceURL url = i.next();
        if (url.getHost().equals("127.0.0.1") && i.hasNext()) {
        	url = i.next();
        }
        String serviceUrl= url.toString();

        // test connecting to service URL without error
        Process clientProcess= createClientProcess(JmxClient.class, serviceUrl);
        new StreamPumper(clientProcess.getInputStream(), "JmxClient.STDOUT").start();
        new StreamPumper(clientProcess.getErrorStream(), "JmxClient.STDERR").start();
        assertEquals("client failed", 0, clientProcess.waitFor());
    }

    private Process createClientProcess(Class<?> mainClass, String... mainArgs) throws Exception {
        URL[] urls= null;

        // do we have a URL class loader?
        try {
            ClassLoader loader= mainClass.getClassLoader();

            if(loader instanceof URLClassLoader) {
                urls= ((URLClassLoader) loader).getURLs();
            }
        } catch (Exception e) {
            System.out.println("could not loader urls: " + e.getMessage());
        }

        if(urls == null) {
            // get code location for test class only
            try {
                URL url= mainClass.getProtectionDomain().getCodeSource().getLocation();
                if(url != null) {
                    urls= new URL[] {url};
                }
            } catch (Exception e) {
                System.out.println("could not obtain code location: " + e.getMessage());
            }
        }

        assertNotNull("could not resolve any code location", urls);

        // build class path for client process
        StringBuilder builder= new StringBuilder();
        for(int i= 0; i < urls.length; ++i) {
            URL url= urls[i];
            try {
                File file= new File(url.toURI());
                String path= file.getAbsolutePath();

                if(builder.length() > 0) {
                    builder.append(File.pathSeparatorChar);
                }

                path.replace(" ", "\\ ");
                builder.append(path);
                
//                if(path.indexOf(' ') > 0) {
//                    builder.append('"').append(path).append('"');
//                } else {
//                    builder.append(path);
//                }
            } catch (Exception e) {
                System.err.println("have to leave out: "+ Arrays.asList(urls));
            }
        }

        // build command line
        String[] cmdL= new String[4 + mainArgs.length];
        cmdL[0]= "java";
        cmdL[1]= "-classpath";
        cmdL[2]= builder.toString();
        cmdL[3]= mainClass.getName();
        System.arraycopy(mainArgs, 0, cmdL, 4, mainArgs.length);

        // debug
        StringBuilder msg = new StringBuilder("Starting client process: ");
        for (String s : cmdL) {
        	msg.append(s).append(" ");
        }
        System.out.println(msg.toString());

        // create client process
        return Runtime.getRuntime().exec(cmdL);
    }
}

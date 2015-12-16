package de.dailab.jiactng.agentcore;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Set;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.JIACTestForJUnit3;
import de.dailab.jiactng.agentcore.directory.DirectoryAgentNodeBean;
import de.dailab.jiactng.agentcore.util.IdFactory;

/**
 * Tests if the directories of agent nodes with the same discovery URI are 
 * interact and these of agent nodes with different discovery URIs are not.
 * @author Jan Keiser
 */
public class PlatformTest extends JIACTestForJUnit3 {

	private Process process = null;
	private String nodeId = null;

    private class StreamPumper extends Thread {
        private final BufferedReader _reader;

        StreamPumper(InputStream in) {
            _reader = new BufferedReader(new InputStreamReader(in), 1024);
        }

        @Override
        public void run() {
            try {
                String s;

                while((s = _reader.readLine()) != null) {
                    if(s.length() > 0) {
                    	fail("Other agent node failed with message: " + s);
                    }
                }
            } catch (Throwable t) {
                // ignore
            } finally {
                try {_reader.close();} catch (IOException e) {}
            }
        }
    }

    /**
	 * Creates the other agent node within an own process.
	 * @throws Exception if the process can not be started.
	 * @see Runtime#exec(String[])
	 */
	@Override
	protected void setUp() throws Exception {
		final Class<?> mainClass = SimpleAgentNode.class;
		final String[] mainArgs = new String[] {"de/dailab/jiactng/agentcore/defaultPlatformNode.xml"};
        URL[] urls = null;

        // do we have a URL class loader?
        try {
            ClassLoader loader = mainClass.getClassLoader();

            if(loader instanceof URLClassLoader) {
                urls = ((URLClassLoader) loader).getURLs();
            }
        } catch (Exception e) {
            System.out.println("could not loader urls: " + e.getMessage());
        }

        if(urls == null) {
            // get code location for test class only
            try {
                URL url = mainClass.getProtectionDomain().getCodeSource().getLocation();
                if(url != null) {
                    urls = new URL[] {url};
                }
            } catch (Exception e) {
                System.out.println("could not obtain code location: " + e.getMessage());
            }
        }

        assertNotNull("could not resolve any code location", urls);

        // build class path for client process
        StringBuilder builder = new StringBuilder();
        for(int i= 0; i < urls.length; ++i) {
            URL url = urls[i];
            try {
                File file = new File(url.toURI());
                String path = file.getAbsolutePath();

                if(builder.length() > 0) {
                    builder.append(File.pathSeparatorChar);
                }

                if(path.indexOf(' ') > 0) {
                    builder.append('"').append(path).append('"');
                } else {
                    builder.append(path);
                }
            } catch (Exception e) {
                System.err.println("have to leave out: " + Arrays.asList(urls));
            }
        }

        // build command line
        String[] cmdL= new String[4 + mainArgs.length];
        cmdL[0]= "java";
        cmdL[1]= "-classpath";
        cmdL[2]= builder.toString();
        cmdL[3]= mainClass.getName();
        System.arraycopy(mainArgs, 0, cmdL, 4, mainArgs.length);

        // create client process
        process = Runtime.getRuntime().exec(cmdL);

        // read node ID from stdout
        InputStream err = process.getErrorStream();
        new StreamPumper(err).start();
        InputStream in = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in), 1024);
        
        // just a workaround to avoid fixing some problems with the input reader right now
        // TODO Do we need these tests anymore? If so, please find a solution to timing and inputstream issue
        
        for (String log = reader.readLine(); (log != null) && (nodeId == null); log = reader.readLine()){
        	int startIndex = log.indexOf(IdFactory.IdPrefix.Node.toString());
        	int stopIndex = log.indexOf(" ", startIndex);
        	try {
        		nodeId = log.substring(startIndex, stopIndex);
        	}
        	catch (Exception e) {}
        }
        reader.close();
        in.close();

		super.setUp();
	}

	/**
	 * Stops the process of the other agent node.
	 * @throws Exception if an unexpected exception occurs.
	 */
	@Override
	protected void tearDown() throws Exception {
		process.destroy();
		process = null;
		nodeId = null;

		super.tearDown();
	}

	/**
	 * Test if the other agent node was found by the new created agent node
	 * also not overwriting the discovery URI.
	 * @throws Exception if the new agent node can not be created or destroyed.
	 */
	public void testNonOverwriteDiscoveryURI() throws Exception {
        // launch node
		ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("de/dailab/jiactng/agentcore/defaultPlatformNode.xml");
        final SimpleAgentNode node = (SimpleAgentNode) ac.getBean("myNode");

        // wait 15 seconds to ensure synchronization of directories
        try {
        	Thread.sleep(15000);
        }
        catch(InterruptedException e) {
        	e.printStackTrace();
        }

        // search for other node
        for (IAgentNodeBean bean : node.getAgentNodeBeans()) {
        	if (bean instanceof DirectoryAgentNodeBean) {
        		Set<String> nodes = ((DirectoryAgentNodeBean)bean).getAllKnownAgentNodes();
        		assertTrue("other agent node not found", nodes.contains(DirectoryAgentNodeBean.ADDRESS_NAME+"@"+nodeId));
        	}
        }

        // shutdown node
        node.shutdown();
        ac.close();
    }

	/**
	 * Test if the other agent node was not found by the new created agent node
	 * overwriting the discovery URI.
	 * @throws Exception if the new agent node can not be created or destroyed.
	 */
    public void testOverwriteDiscoveryURI() throws Exception {
        // launch node
		ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("de/dailab/jiactng/agentcore/otherPlatformNode.xml");
        final SimpleAgentNode node = (SimpleAgentNode) ac.getBean("myNode");

        // wait 15 seconds to ensure synchronization of directories
        try {
        	Thread.sleep(15000);
        }
        catch(InterruptedException e) {
        	e.printStackTrace();
        }

        // search for other node
        for (IAgentNodeBean bean : node.getAgentNodeBeans()) {
        	if (bean instanceof DirectoryAgentNodeBean) {
        		Set<String> nodes = ((DirectoryAgentNodeBean)bean).getAllKnownAgentNodes();
        		assertFalse("other agent node found", nodes.contains(DirectoryAgentNodeBean.ADDRESS_NAME+"@"+nodeId));
        	}
        }

        // shutdown node
        node.shutdown();
        ac.close();
    }

}

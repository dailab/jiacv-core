/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.management.jmx;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.TestCase;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class JmxRemoteTest extends TestCase {
    private Process _clientProcess= null;
    
    @Override
    protected void setUp() throws Exception {
        URL[] urls= null;
        
        // do we have a URL class loader?
        try {
            ClassLoader loader= JmxClient.class.getClassLoader();
            
            if(loader instanceof URLClassLoader) {
                urls= ((URLClassLoader) loader).getURLs();
            }
        } catch (Exception e) {
            System.out.println("could not loader urls: " + e.getMessage());
        }
        
        if(urls == null) {
            // get code location for test class only
            try {
                URL url= JmxClient.class.getProtectionDomain().getCodeSource().getLocation();
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
                
                if(path.indexOf(' ') > 0) {
                    builder.append('"').append(path).append('"');
                } else {
                    builder.append(path);
                }
            } catch (Exception e) {
                System.err.println("have to leave out: "+ urls);
            }
        }
        
        // build command line
        String[] cmdL= new String[] {
            "java",
            "-classpath",
            builder.toString(),
            JmxClient.class.getName()
        };
        
        // create client process
        _clientProcess= Runtime.getRuntime().exec(cmdL);
    }
    
    public void testJmxRemote() throws Exception {
        InputStream in= _clientProcess.getInputStream();
        for(int ch; (ch= in.read()) > 0;) {
            System.out.print((char) ch);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        if(_clientProcess != null) {
            _clientProcess.destroy();
        }
        
        _clientProcess= null;
    }
}

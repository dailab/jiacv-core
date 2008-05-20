package de.dailab.jiactng.agentcore.util.jar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A utility class to collect JAR objects.
 * @author Marcel Patzlaff
 */
class JARClassPath {
    private final URLStreamHandler STREAM_HANDLER= new URLStreamHandler() {
        @Override
        protected URLConnection openConnection(final URL u) {
            return new JARConnection(u);
        }
    };
    
    private final class JARConnection extends URLConnection {
        private boolean _connected= false;
        private int _contentLength;
        private long _contentTime;
        private String _contentType;
        private InputStream _sourceStream;
        
        JARConnection(final URL url) {
            super(url);
        }

        @Override
        public void connect() throws IOException {
            if(!_connected) {
                String resource= url.getFile();
                
                if(resource == null || resource.length() <= 0)
                    throw new IOException("Unable to find resource: " + url.toString());
                
                int index= resource.indexOf('!');
                
                if(index <= 0 || index >= resource.length() - 1)
                    throw new IOException("The URL is no valid resource location: " + url.toString());
                
                String containerName= resource.substring(0, index);
                String fileName= resource.substring(index + 1);
                
                JAR container= getJAR(containerName);
                if(container == null)
                    throw new IOException("Unable to find container: " + containerName);
                
                _sourceStream= container.getInputStream(fileName);
                
                if(_sourceStream == null)
                    throw new IOException("Unable to find content for: " + fileName);
                
                _contentLength= _sourceStream.available();
                _contentTime= System.currentTimeMillis();
                _contentType= guessContentTypeFromName(resource);
                _connected= true;
            }
        }

        @Override
        public int getContentLength() {
            if(!_connected) {
                try {
                    connect();
                } catch (IOException e) {
                    return -1;
                }
            }
            
            return _contentLength;
        }

        @Override
        public String getContentType() {
            if(!_connected) {
                try {
                    connect();
                } catch (IOException e) {
                    return null;
                }
            }
            
            return _contentType;
        }

        @Override
        public long getDate() {
            if(!_connected) {
                try {
                    connect();
                } catch (IOException e) {
                    return 0;
                }
            }
            
            return _contentTime;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            connect();
            return _sourceStream;
        }

        @Override
        public long getLastModified() {
            if(!_connected) {
                try {
                    connect();
                } catch (IOException e) {
                    return 0;
                }
            }
            
            return _contentTime;
        }
    }
    
    /** Finds file by name. * */
    private static File fileFromName(String resource) {
        File file = new File(resource);

        if (file.isFile()) {
            return file;
        }

        URL url = JARClassLoader.getJVMClassLoader().getResource(resource);

        if ((url == null) || !"file".equals(url.getProtocol())) {
            return null;
        }

        file = new File(url.getFile());

        if (file.isFile()) {
            return file;
        }

        return null;
    }

    private final Set<JAR> _jars= new HashSet<JAR>();
    private final Map<String,URL> _urlCache= new HashMap<String, URL>();

    /**
     * Adds a JAR to the class path.
     * @param jar The JAR to add.
     */
    synchronized void addJAR(final JAR jar) {
        _jars.add(jar);
    }

    /**
     * Adds a JAR resource to the class path.
     * @param resource The URL or name of the JAR file.
     * @return The added JAR.
     * @throws IOException if the JAR is not readable.
     */
    synchronized JAR addJAR(final String resource) throws IOException {
        JAR jar;
        File file = fileFromName(resource);

        if (file != null) {
            jar = new JARFile(file);
        } else {
            try {
                // test for url
                URL url= new URL(resource);
                if(!url.getProtocol().equals("file"))
                    jar= new JARMemory(resource, url.openStream());
                else
                    jar= new JARMemory(new File(url.getFile()));
            } catch (MalformedURLException ioe) {
                jar= new JARMemory(new File(resource));
            }
        }

        addJAR(jar);

        return jar;
    }
    
    /**
     * Creates an array of <code>JAR</code>s currently hold within
     * this class path.
     * @return The list of JARs.
     */
    synchronized JAR[] getJARs() {
        return _jars.toArray(new JAR[_jars.size()]);
    }
    
    /**
     * Returns the container associated with the entry.
     * Name clashes are resolved as the entry can only
     * be associated with <strong>one</strong> <code>JAR</code>.
     * @param entry The name of the JAR entry.
     * @return The JAR which contains the entry or <code>null</code>.
     */
    synchronized JAR getContainer(final String entry) {
        for(JAR jar : _jars) {
            if(jar.constainsResource(entry)) {
                return jar;
            }
        }
        return null;
    }
    
    /**
     * Returns the <code>URL</code> associated with the entry.
     * @param entry The name of the JAR entry.
     * @return The URL of the entry or <code>null</code>.
     * @see #getContainer(String)
     */
    synchronized URL getURL(final String entry) {
        return getURL(getContainer(entry), entry);
    }
    
    /**
     * Returns the <code>URL</code> associated with the entry.
     * @param container
     *          Is used only as a hint, if no association was found. If
     *          the container does not control the entry, this method will
     *          return <code>null</code>.
     * @param entry The name of the JAR entry.
     * @return The URL of the entry or <code>null</code>.
     * @see #getContainer(String)
     */
    synchronized URL getURL(final JAR container, final String entry) {
        URL result= _urlCache.get(entry);
        
        if(result != null)
            return result;
        
        if(container == null || !container.constainsResource(entry))
            return null;
        
        try {
            String location= container.getJarName() + "!" + entry;
            result= new URL("jarcl", null, -1, location, STREAM_HANDLER);
            _urlCache.put(entry, result);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            result= null;
        }
        
        return result;
    }
    
    /**
     * Gets the JAR with the given name.
     * @param name The name of the JAR.
     * @return The JAR or <code>null</code>.
     */
    synchronized JAR getJAR(final String name) {
        for(JAR jar : _jars) {
            if(jar.getJarName().equals(name))
                return jar;
        }        
        return null;
    }
}

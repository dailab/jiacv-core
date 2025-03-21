package de.dailab.jiactng.agentcore.util.jar;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

/**
 * Class loader for JIAC TNG. Loads classes from URLs and JARs.
 * @author Ralf Sesseler
 */
public class JARClassLoader extends URLClassLoader {

	/** The JVM class loader. * */
    private static final JARClassLoader CLASSLOADER = new JARClassLoader();

    /** 
     * Get JVM class loader.
     * @return The class loader of the JVM.
     */
    public static JARClassLoader getJVMClassLoader() {
        return CLASSLOADER;
    }

    private final JARClassPath jarCP= new JARClassPath();

    /** The already loaded classes by this class loader instance. */
    private HashMap<String,Class<?>> loadedClasses = new HashMap<String,Class<?>>();

    /** 
     * Creates an empty class loader with the current class loader as parent.
     */
    public JARClassLoader() {
        // We initialize the URLClassloader with a parent ClassLoader
        // since java 2 uses delegation. When a marketplace is started
        // within a servlet e.g. the catalina-webabb classloader is
        // used.
        super(new URL[0], JARClassLoader.class.getClassLoader());
    }

    // -- add urls / jars

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }

    /** 
     * Adds an URL to the class loader.
     * @param url The URL to add in String syntax.
     * @return The added URL.
     * @throws MalformedURLException if the given URL is syntactically incorrect.
     */
    public synchronized URL addURL(String url) throws MalformedURLException {
        // System.out.println("[jarcl] addURL " + url);
        final URL u = new URL(url);
        addURL(u);

        return u;
    }

    /** 
     * Adds a JAR to the class loader. 
     * @param jar The JAR to add.
     */
    public synchronized void addJAR(JAR jar) {
        jarCP.addJAR(jar);
    }

    /** 
     * Creates a JAR and adds it to the class loader.
     * @param resource The URL or name of the JAR file.
     * @return The added JAR.
     * @throws FileNotFoundException if the file does not exist.
     * @throws IOException if the resource is not readable.
     */
    public synchronized JAR addJAR(String resource) throws IOException, FileNotFoundException {
        return jarCP.addJAR(resource);
    }

	/**
	 * {@inheritDoc}
	 */
    @Override
    public synchronized URL findResource(String name) {
    	final URL result = jarCP.getURL(name);
        return result == null ? super.findResource(name) : result;
    }

	/**
	 * {@inheritDoc}
	 */
    @Override
    public synchronized Enumeration<URL> findResources(String name) throws IOException {
        final List<URL> result = Collections.list(super.findResources(name));

        final URL localURL= jarCP.getURL(name);
        
        if(localURL != null) {
            result.add(localURL);
        }

        return Collections.enumeration(result);
    }

    /**
     * Gets the JARs of this class loader.
     * @return The list of JARs.
     */
    public JAR[] getJARs() {
        return jarCP.getJARs();
    }
    
    // FIXME: Was das? Wird das gebraucht? Ohne Klassenname ist das sowieso etwas
    // unschoen. -- marcel
    public synchronized boolean addClass(byte[] code) {
        resolveClass(defineClass(null, code, 0, code.length));
        return true;
    }

	/**
	 * Returns a multiline text which contains the JARs and URLs 
	 * of the class loader.
	 * @return a string representation of the class loader
	 */
    @Override
    public synchronized String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("JARClassLoader: ");

        final JAR[] jars= getJARs();
        sb.append("\njars [").append(jars.length).append("]\n");
        for (Object jar : jars) {
            sb.append('\t').append(jar).append('\n');
        }

        final URL[] urls= getURLs();
        sb.append("\nurls [").append(urls.length).append("]\n");
        for (URL url : urls) {
            sb.append('\t').append(url).append('\n');
        }

        return sb.toString();
    }

	/**
	 * {@inheritDoc}
	 */
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
    	// check if class is already loaded
    	Class<?> c = loadedClasses.get(name);
    	if (c != null) {
    		return c;
    	}

    	// load class
    	try {
    		c = findClass(name);
    		loadedClasses.put(name, c);
    		return c;
    	} catch (ClassNotFoundException e) {
    		return super.loadClass(name);
    	}
    }

    @Override
    protected synchronized Class<?> findClass(String name) throws ClassNotFoundException {
        // get class from the class path of this child class loader
        final String fileName = new StringBuffer().append(name.replace('.', '/')).append(".class").toString();
        final JAR jar= jarCP.getContainer(fileName);        
        if(jar != null) {
            try {
                final InputStream is = jar.getInputStream(fileName);
                final byte[] classCode = getBytes(is);
                is.close();
                final URL url = jarCP.getURL(jar, fileName);
                final CodeSource cs = url == null ? null : new CodeSource(url, (Certificate[]) null);
                return defineClass(name, classCode, 0, classCode.length, cs);
            } catch (IOException ioe) {}
        }

        throw new ClassNotFoundException(name);
    }
    
    /**
     * Since neither InputStream.available() nor JarEntry.getSize() works, the
     * length to read is unknown. *
     */
    private byte[] getBytes(InputStream is) throws IOException {
        final byte[] buffer = new byte[8192];
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
        int n;

        while ((n = is.read(buffer, 0, buffer.length)) != -1) {
            baos.write(buffer, 0, n);
        }

        return baos.toByteArray();
    }
}
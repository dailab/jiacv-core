package de.dailab.jiactng.agentcore.util.jar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Stores jar file in memory.
 * @author Ralf Sesseler
 */
public class JARMemory implements JAR, Serializable {

	private static final long serialVersionUID = 1L;

	/** Name when loading (maybe relative to class path). * */
    private final String name;

    /** Data of jar. * */
    private byte[] jar= new byte[0];

    /**
     * Creates a JAR memory from a JAR file.
     * @param file The JAR file to read from.
     * @throws FileNotFoundException if the JAR file does not exist.
     * @throws IOException if the JAR file is not readable.
     */
    public JARMemory(JARFile file) throws FileNotFoundException, IOException {
        this(new File(file.getJarName()));
    }

    /**
     * Creates a JAR memory from a JAR file of the classpath.
     * @param resource The classpath-relative name of the JAR file to read from.
     * @throws FileNotFoundException if the JAR file does not exist.
     * @throws IOException if the JAR file is not readable.
     */
    public JARMemory(String resource) throws FileNotFoundException, IOException {
        this(resource, JARClassLoader.getJVMClassLoader().getResourceAsStream(resource));
    }

    /**
     * Creates a JAR memory from a JAR file.
     * @param file The JAR file to read from.
     * @throws FileNotFoundException if the JAR file does not exist.
     * @throws IOException if the JAR file is not readable.
     */
    public JARMemory(File file) throws FileNotFoundException, IOException {
        this(file.toString(), new FileInputStream(file));
    }

    /**
     * Creates a JAR memory from a JAR input stream.
     * @param name The name of the JAR.
     * @param in The JAR input stream to read from.
     * @throws IOException if the JAR stream is not readable.
     */
    public JARMemory(String name, InputStream in) throws IOException {
        this.name= name;
        readJAR(in);
    }

    /** Read jar from stream. * */
    private void readJAR(InputStream is) throws IOException {
        ByteArrayOutputStream temp= new ByteArrayOutputStream(4096);
        byte[] readBuffer= new byte[4096];
        
        int numBytes;

        while ((numBytes= is.read(readBuffer)) != -1) {
            temp.write(readBuffer, 0, numBytes);
        }
        
        jar= temp.toByteArray();
    }

    /**
     * Gets input stream for entry in JAR.
     * <P>
     * Remark: available() is not correct!
     * 
     * @param entryName The name of JAR entry
     * @return The input stream to read the JAR entry.
     */
    public InputStream getInputStream(String entryName) {
        try {
            JarInputStream jis = new JarInputStream(new ByteArrayInputStream(jar));

            while (jis.available() != 0) {
                JarEntry je = jis.getNextJarEntry();

                if ((je != null) && je.getName().equals(entryName)) {
                    return jis;
                }
            }
        } catch (Exception x) {
            x.printStackTrace();
        }

        return null;
    }

    /**
     * The name of the JAR.
     * @return The file name of the JAR (absolute or class path relative).
     */
    public String getJarName() {
        return name;
    }

    /**
     * Checks if the JAR contains a given entry.
     * @param resource The name of the JAR entry.
     * @return <code>true</code> if the entry exists.
     */
    public boolean constainsResource(String resource) {
        try {
            JarInputStream jis = new JarInputStream(new ByteArrayInputStream(jar));
            
            while (jis.available() != 0) {
                JarEntry je = jis.getNextJarEntry();

                if ((je != null) && je.getName().equals(resource)) {
                    return true;
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof JAR)) {
            return false;
        }

        return name.equals(((JAR) o).getJarName());
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Returns complete content of JAR as byte array.
     * @return The complete content of JAR.
     */
    public byte[] getByteArray() {
        byte[] result= new byte[jar.length];
        System.arraycopy(jar, 0, result, 0, result.length);
        return result;
    }

    @Override
    public String toString() {
        return "JARMemory :: " + getJarName();
    }
}
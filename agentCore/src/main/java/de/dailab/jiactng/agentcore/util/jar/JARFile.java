package de.dailab.jiactng.agentcore.util.jar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Jar file. Extends JarFile by class path relative loading and implementing
 * interface JAR.
 * @author Ralf Sesseler
 */
public class JARFile extends JarFile implements JAR {

    /** Name when loading (maybe relative to class path). */
    private String name;

    /**
     * Constructor.
     * @param file File name or resource of jar.
     * @throws IOException if the path of the file can not be resolved.
     */
	public JARFile(File file) throws IOException {
        super(file);
        name = file.getPath();
	}

    /**
     * Gets input stream for entry in jar.
     * @param entryName file name of jar entry
     * @return input stream to read jar entry
     */
    public InputStream getInputStream(String entryName) {
        JarEntry entry = getJarEntry(entryName);

        try {
            if (entry != null) {
                return getInputStream(entry);
            }
        } catch (IOException x) {
            // do nothing
        }

        return null;
    }

    /**
     * The name of the jar.
     * @return the file name of the jar (absolute or class path relative)
     */
    public String getJarName() {
        return name;
    }

    /**
     * Checks if the JAR file contains a given entry.
     * @param resource The name of the JAR entry.
     * @return <code>true</code> if the entry exists.
     */
    public boolean constainsResource(String resource) {
        return getJarEntry(resource) != null;
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

    @Override
    public String toString() {
        return "JARFile :: " + getJarName();
    }
}

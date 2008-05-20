package de.dailab.jiactng.agentcore.util.jar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

/**
 * A class reading and extracting content of jar-files.
 * For example:
 * <pre>
 *      JARFileReader jfr = new JARFileReader("c:\jarTest\Test.jar");
 *      jfr.extractAll();
 * </pre>
 *
 * @author  Nicolas Braun
**/

public class JARFileReader extends ClassLoader {

  private JarFile jarFile;        /** The regarded jar-file. **/
  private String jarFileName;     /** The name of the regarded jar-file. **/


  /**
   * The constructor for viewing content of specified jar-file.
   * @param jarFileName The specified jar-file.
   * @exception IOException An error can occur when creating JarFile-object.
  **/
  public JARFileReader(String jarFileName) throws IOException {
    this.jarFileName = jarFileName;
    this.jarFile = new JarFile(jarFileName);
  }



  /**
   * A method getting all entries of jar-file.
   * @return A collection of all jar-file entries.
  **/
  public Vector<JarEntry> getAllEntries() {
    Vector<JarEntry> jarFileEntries = new Vector<JarEntry>();
    Enumeration<JarEntry> enumList = jarFile.entries();

    while (enumList.hasMoreElements()) {  // get all file-entries
      JarEntry entry = enumList.nextElement();
      jarFileEntries.add(entry);
    }

    return jarFileEntries;
  }

  /**
   * A faster method to get all entries of jar-file.
   * @return An enumeration of all jar-file entries.
  **/
  public Enumeration<JarEntry> getEntries() {
    return jarFile.entries();
  }


  /** A method extracting whole content of jar-file. **/
  public void extractAll() {

    try {
      byte[] buf = new byte[4096];
      JarInputStream in = new JarInputStream(
                          new FileInputStream(jarFileName));

      // get operating system file separator
      String fileSeparator = System.getProperty("file.separator");
      // create temporarily file with string tokenizer
      StringTokenizer st = new StringTokenizer(jarFileName,fileSeparator);

      File tempFile = null;
      while (st.hasMoreTokens()) {
        String token = st.nextToken();

        if (token.endsWith(".jar")) {
          String dirName = token.substring(0,token.length()-4);
          tempFile = new File(dirName);
          tempFile.mkdir();
          break;
        }
      }

      String pathToTmpFile = tempFile.getAbsolutePath();
      FileOutputStream out = null;

      while (true) {

        JarEntry entry = (JarEntry) in.getNextEntry();

        if (entry == null) {    // no more entries
          break;
        }

        if (entry.isDirectory()) {    // entry is directory
          String dirName = entry.getName();
          File jarDir = new File(
              new StringBuffer()
              .append(pathToTmpFile)
              .append(fileSeparator)
              .append(dirName)
              .toString());
          jarDir.mkdir();
        }

        if (! entry.isDirectory()) {    // entry is a file
          out = new FileOutputStream(
              new StringBuffer()
              .append(pathToTmpFile)
              .append(fileSeparator)
              .append(entry.getName())
              .toString());
          int len;

          while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
          }

          out.close();
          in.closeEntry();
        }
      }
      in.close();
    }
    catch (IOException ioe) {
      System.err.println(ioe.toString());
    }
  }
}
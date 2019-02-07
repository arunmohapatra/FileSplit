/*
 * Class:    loadpemcerts
 *
 *
 */

package com.progress.ubroker.util;

// Java packages.
import java.lang.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.awt.TextArea;

// Progress Packages
import com.progress.ubroker.util.Base64;

public abstract class CertLoader extends Object
{
    public  static final int        CERT_TYPE_NULL = 0;
    public  static final int        CERT_TYPE_PEM = 1;
    public  static final int        CERT_TYPE_DER = 2;
    public  static final int        CERT_TYPE_ZIP = 3;
    public  static final int        CERT_TYPE_JAR = 4;
    public  static final int        CERT_TYPE_DCL = 5;
    public  static final int        CERT_TYPE_DIRECTORY = 6;

    public  static final int        LOAD_SOURCE_FILE = 0;
    public  static final int        LOAD_SOURCE_CLASSPATH = 1;

    /*
     * Class CertData, holds one or more X509certificate objects
     */
    public class CertData extends Object
    {
        protected   String              m_certName = null;
        protected   Vector              m_certs = new Vector();
        protected   Enumeration         m_certEnum = null;

        CertData(String certName)
        {
            m_certName = certName;
        }

        protected void finalize() throws Throwable
        {
            m_certName = null;
            m_certs.removeAllElements();
        }

        public  String  getCertName() { return(m_certName); }

        public  int     getCertCount() { return(m_certs.size()); }

        public  void    addCert(Object  certObject) { m_certs.addElement(certObject); }

        /* returns enumeration of X509Certificate objects */
        public  Enumeration enumCerts() { m_certEnum = m_certs.elements(); return(m_certEnum); }

        public  boolean hasMoreElements() { return (m_certEnum.hasMoreElements()); }

        public  Object  nextElement() { return(m_certEnum.nextElement()); }
    }   /* end of class CertData */

    /*
     * class CertFile, holds one or more CertData class objects.
     */
    public class CertFile extends Object
    {


        protected   String              m_loadPath = null;
        protected   int                 m_certType = CertLoader.CERT_TYPE_NULL;
        protected   int                 m_loadSource = CertLoader.LOAD_SOURCE_FILE;
        protected   Vector              m_certData = new Vector();
        protected   Enumeration         m_certEnum = null;

        CertFile(String path, int certType, int loadSource)
        {
            m_loadPath = path;
            m_certType = certType;
            m_loadSource = loadSource;
        }

        protected void finalize() throws Throwable
        {
            m_loadPath = null;
            m_certData.removeAllElements();
        }

        public  void    addCertData(CertData certData)
        {
            m_certData.addElement(certData);
        }

        public  boolean removeCertData(CertData certData)
        {
            m_certEnum = null;
            return m_certData.removeElement(certData);
        }

        public  String  getLoadPath() { return(m_loadPath); }

        public  int     getCertFileType() { return(m_certType); }

        /* returns enumeration of CertData objects */
        public  Enumeration enumCertData() { m_certEnum = m_certData.elements(); return(m_certEnum); }

        public  boolean hasMoreElements() { return (m_certEnum.hasMoreElements()); }

        public  Object  nextElement() { return(m_certEnum.nextElement()); }
    }   /* End of class CertFile */

    /*
     * CertLoader class data.
     */
    protected   boolean             m_flagDebug = false;
    protected   boolean             m_flagMicrosoftJVM = false;
    protected   boolean             m_flagIsApplet = false;
    protected   boolean             m_flagRunningInApplet = false;
    protected   boolean             m_flagIgnoreLoadErrors = false;
    protected   boolean             m_flagSearchDiskFilesOnly = false;
    protected   boolean             m_flagSearchClasspathOnly = false;
    protected   Vector              m_x509Certificates = new Vector();
    protected   PrintWriter         m_printStream = new PrintWriter(System.out, true);
    protected   TextArea            m_textArea = null;
    protected   CertFile            m_certFile = null;
    protected   CertData            m_certData = null;
    protected   File                m_rootPath = new File(".");

    /**
     * <!-- CertLoader() -->
     * <p>Default Constructor for the class.
     * </p>
     */
    public CertLoader()
    {
    }

    /**
     * <!-- load() -->
     * <p>load certificates from a certificate store.
     * </p>
     * <br>
     * @param args is an array of String objects that are used like a command
     * line.  Option switches must preceed the single path argument
     * <br>
     * <p>usage: load [-d] [-a] [-f] [-p] [-i] [-r root_path] path
     * </p>
     * <ls>
     * <le> -d               print debug information
     * <le> -a               running in an Applet context
     * <le> -f               search for certificate stores as only a disk file
     * <le> -p               search for certificate stores in only the Java CLASSPATH
     * <le> -i               ignore individual certificate load errors and
     *                       continue processing.  Normally stops loading certificates
     *                       on the first error.
     * <le> -r roo_path      load disk files relative to this root path
     * </ls>
     * <p>path must be a semicolon separated String containing one or more of
     * these types of certificate stores ( entry[;entry...] ):
     * </p>
     * <ls>
     * <le>    xxxx.jar    load pem files from a single jar file.
     * <le>    xxxx.zip    load pem files from a single zip file.
     * <le>    xxxx.pem    load single pem certificate file.
     * <le>    xxxx.0      load single pem certificate file.
     * <le>    xxxx.cer    load single binary certificate file.
     * <le>    xxxx.crt    load single binary certificate file.
     * <le>    xxxx.dcl    load all of the single .pem, .0, .cer, & .crt files
     *                     in the digital-certificate-list formatted file.
     * <le>    (directory) load all of the single .pem, .0, .cer, & .crt files
     *                     from a disk directory (implies -f for this entry only)
     * </ls>
     * <p>The loader normally searches for each type of certificate store first
     * as a disk file and then as a resource.  The exceptions are:
     * </p>
     * <ls>
     * <le>The -a switch is set indicating the laoder is running in the context
     * of an applet.  In that case, only the Java CLASSPATH is searched
     * <le>The -f switch is set indicating to search for disk files only
     * <le>The -p switch is set indicating to search only the Java CLASSPATH
     * </ls>
     * <p>Special notes:
     * </p>
     * <ls>
     * <le>A manifest file is searched for entries in the format ""
     * <le>If searching for a .jar or .zip file in the Java CLASSPATH, both the
     * location of the file and the file itself must be in the CLASSPATH.  Example:
     * ...;somepathtojar;certjar.jar;....
     * </ls>
     * <br>
     * @return  void
     * <br>
     * @exception   Exception
     * @exception   IllegalArgumentException (thrown when the args array is
     * empty or contains an illegal switch.
     * @exception   FileNotFoundException (thrown when a load path entry
     * cannot be found)
     */
    public void load(String[] args) throws Exception, IllegalArgumentException,
            FileNotFoundException
    {
        // Load a set of PEM encoded certs from either a Jar file, a disk
        // file or from the CLASSPATH
        if (0 == args.length)
        {
            throw new IllegalArgumentException("Insufficient arguments");
        }
        else
        {
            int     argPtr = 0;
            boolean processSwitches = true;

            // Evaluate any option switches...
            //
            while (processSwitches && argPtr < args.length)
            {
                if ('-' == args[argPtr].charAt(0))
                {
                    if (args[argPtr].equalsIgnoreCase("-d"))
                        m_flagDebug = true;
                    else if (args[argPtr].equalsIgnoreCase("-a"))
                    {
                        m_flagRunningInApplet = true;
                        m_flagSearchClasspathOnly = true;
                        m_flagSearchDiskFilesOnly = false;
                    }
                    else if (args[argPtr].equalsIgnoreCase("-i"))
                        m_flagIgnoreLoadErrors = true;
                    else if (args[argPtr].equalsIgnoreCase("-f"))
                    {
                        m_flagSearchDiskFilesOnly = true;
                        m_flagSearchClasspathOnly = false;
                    }
                    else if (args[argPtr].equalsIgnoreCase("-p"))
                    {
                        m_flagSearchClasspathOnly = true;
                        m_flagSearchDiskFilesOnly = false;
                    }
                    else if (args[argPtr].equalsIgnoreCase("-r"))
                    {
                        argPtr++;
                        if (argPtr < args.length)
                        {
                            if ('-' == args[argPtr].charAt(0))
                            {
                                throw new IllegalArgumentException("No value supplied to -r option");
                            }

                            m_rootPath = new File(args[argPtr]);
                        }
                        else
                        {
                            throw new IllegalArgumentException("No value supplied to -r option");
                        }
                    }
                    else
                    {
                        throw new IllegalArgumentException("Unknown argument: " + args[argPtr]);
                    }

                    argPtr++;
                }
                else
                {
                    // no more switches, exit
                    processSwitches = false;
                }
            }

            // There must be one argument left, the thing to load.
            //
            if (argPtr >= args.length)
            {
                throw new IllegalArgumentException("Missing certificate store name to load");
            }

            // Evaluate the environment.
            //
            if (m_flagDebug)
                dumpEnvironment();

            if (-1 != System.getProperty("java.vendor").indexOf("Microsoft"))
            {
                m_flagMicrosoftJVM = true;
            }

            // parse the load path into individual entries.
            StringTokenizer path = new StringTokenizer(args[argPtr], ";");

            while (path.hasMoreElements())
            {
                String  loadEntry = path.nextToken();
                File    fPath = new File(loadEntry);
                File    fullPath = null;
                boolean isFile = false;
                boolean isResource = false;
                boolean isDirectory = false;
                int     loadSource = CertLoader.LOAD_SOURCE_FILE;

                if (fPath.isAbsolute())
                {
                    fullPath = fPath;
                }
                else
                {
                    fullPath = new File(m_rootPath, loadEntry);
                }

                // Can't search for directories in applet mode or if we are
                // restricted to searching for a classpath resource.
                if (!m_flagRunningInApplet && !m_flagSearchClasspathOnly)
                    isDirectory = (fPath.exists() & fPath.isDirectory());

                if (!isDirectory)
                {
                    // If allowed to search for files
                    if (!m_flagSearchClasspathOnly)
                    {
                        if (m_flagDebug)
                        {
                            if (m_flagDebug )
                            {
                                println("Testing file access: " + loadEntry);
                            }
                        }
                        isFile = (fullPath.exists() & fullPath.isFile());
                    }

                    if (!isFile)
                    {
                        // If allowed to search for resources
                        if (!m_flagSearchDiskFilesOnly)
                            isResource = (isResource(loadEntry));
                    }
                }


                // Set the load type, file/directory or classpath resource.
                if (isResource)
                {
                    loadSource = CertLoader.LOAD_SOURCE_CLASSPATH;
                }
                else
                {
                    loadSource = CertLoader.LOAD_SOURCE_FILE;
                }

                // do a sanity check, it must be a file, resource, or directory
                // or it's useless to try and process the entry.
                if (!isFile && !isResource && !isDirectory)
                {
                    if (m_flagIgnoreLoadErrors)
                    {
                        if (m_flagDebug)
                            println("The load path entry can't be found : " + loadEntry);
                        continue;
                    }
                    else
                    {
                        throw new FileNotFoundException("The load path entry can't be found : " + loadEntry);
                    }
                }

                // Decide how to load...
                try
                {
                    // For each loadEntry create a new cert storage object
                    // and add it to the list of ones we have found.  Then
                    // call the appropriate loader type.
                    //
                    // NOTE: it doesn't have to contain any certificates.
                    //
                    if (loadEntry.endsWith(".jar"))
                    {
                        m_certFile = new CertFile(loadEntry ,
                                                  CertLoader.CERT_TYPE_JAR,
                                                  loadSource);
                        m_x509Certificates.addElement(m_certFile);

                        if (m_flagMicrosoftJVM)
                            // Load the Jar as a ZIP file.  MS doesn't support JAR
                            // classes, but it works the same.
                            if (isFile)
                                // loadFromZipFile(loadEntry);
                                loadFromZipFile(fullPath.getPath());
                            else
                                loadFromZipResource(loadEntry);
                        else
                            if (isFile)
                                // loadFromZipFile(loadEntry);
                                loadFromZipFile(fullPath.getPath());
                            else
                                loadFromZipResource(loadEntry);
                    }
                    else if (loadEntry.endsWith(".zip"))
                    {
                        m_certFile = new CertFile(loadEntry,
                                                  CertLoader.CERT_TYPE_ZIP,
                                                  loadSource);
                        m_x509Certificates.addElement(m_certFile);

                        if (isFile)
                            // loadFromZipFile(loadEntry);
                            loadFromZipFile(fullPath.getPath());
                        else
                            loadFromZipResource(loadEntry);
                    }
                    else if (loadEntry.endsWith(".pem") ||
                             loadEntry.endsWith(".txt") ||
                             loadEntry.endsWith(".0"))
                    {
                        m_certFile = new CertFile(loadEntry,
                                                  CertLoader.CERT_TYPE_PEM,
                                                  loadSource);
                        m_x509Certificates.addElement(m_certFile);

                        // Record context for later use.
                        m_certData = new CertData(loadEntry);
                        m_certFile.addCertData(m_certData);

                        if (isFile)
                            // loadPemFile(loadEntry);
                            loadPemFile(fullPath.getPath());
                        else
                            loadCertResource(loadEntry);
                    }
                    else if (loadEntry.endsWith(".cer") ||
                             loadEntry.endsWith(".crt"))
                    {
                        m_certFile = new CertFile(loadEntry,
                                                  CertLoader.CERT_TYPE_DER,
                                                  loadSource);
                        m_x509Certificates.addElement(m_certFile);

                        // Record context for later use.
                        m_certData = new CertData(loadEntry);
                        m_certFile.addCertData(m_certData);

                        if (isFile)
                            // loadBinaryFile(loadEntry);
                            loadBinaryFile(fullPath.getPath());
                        else
                            loadCertResource(loadEntry);
                    }
                    else if (loadEntry.endsWith(".dcl"))
                    {
                        m_certFile = new CertFile(loadEntry,
                                                  CertLoader.CERT_TYPE_DCL,
                                                  loadSource);
                        m_x509Certificates.addElement(m_certFile);

                        if (isFile)
                            // loadFromManifestFile(loadEntry);
                            loadFromManifestFile(fullPath.getPath());
                        else
                            loadFromManifestResource(loadEntry);
                    }
                    else if (isDirectory)
                    {
                        m_certFile = new CertFile(loadEntry,
                                                  CertLoader.CERT_TYPE_DIRECTORY,
                                                  loadSource);
                        m_x509Certificates.addElement(m_certFile);

                        loadFromDirectory(loadEntry);
                    }
                    else
                    {
                        if (m_flagIgnoreLoadErrors)
                            println("Not a supported certificate store : " + loadEntry);
                        else
                        {
                            throw new Exception("Not a certificate store : " + loadEntry);
                        }
                    }
                }
                catch (Exception e)
                {
                    if (!m_flagIgnoreLoadErrors)
                        throw e;
                }
            }
        }
    }

    /**
     * <!-- setLogStream() -->
     * <p>Set the debug logging to the specified PrintStream.  The default is
     * to use stdout.
     * </p>
     * <br>
     * @param logStream is the PrintStream object to set.  If non-null it will
     * be set to the specified stream.  If null, System.out will be set.
     * <br>
     * @return  void
     * <br>
     * @exception   Exception
     */
    public void setLogStream(PrintWriter logStream) throws Exception
    {
        if (null == logStream)
        {
            m_printStream = new PrintWriter(System.out, true);
        }
        else
        {
            m_printStream = logStream;
        }
    }

    /**
     * <!-- setLogStream() -->
     * <p>Set the debug logging to the specified TextArea applet object for
     * debugging java applets.
     * </p>
     * <br>
     * @param logStream is the TextArea (applet) object to set.  If non-null it will
     * be set to the specified stream.
     * <br>
     * @return  void
     * <br>
     * @exception   Exception
     */
    public void setLogStream(TextArea logStream) throws Exception
    {
        m_textArea = logStream;
    }


    public static void usage()
    {
        System.out.println("");
        System.out.println("usage: loadpermcerts <options> <path>");
        System.out.println("   <options> load options");
        System.out.println("   -d               print debug information");
        System.out.println("   -a               running in an Applet context");
        System.out.println("   -i               ignore load errors");
        System.out.println("   -f               load from disk files only");
        System.out.println("   -p               load from Java CLASSPATH only");
        System.out.println("   <path> certificate store load path ( entry[;entry[...]] ) ");
        System.out.println("        xxxx.jar    load pem files from a single jar file.");
        System.out.println("        xxxx.zip    load pem files from a single zip file.");
        System.out.println("        xxxx.pem    load single pem certificate file.");
        System.out.println("        xxxx.0      load single pem certificate file.");
        System.out.println("        xxxx.cer    load single binary certificate file.");
        System.out.println("        xxxx.crt    load single binary certificate file.");
        System.out.println("        xxxx.dcl    load single certificate files from a");
        System.out.println("                    digital-certificate-list formatted file");
        System.out.println("        (directory) load single certificate files from a");
        System.out.println("                    disk directory");
        System.out.println("");
    }

    /*
     * <!-- println() -->
     * <p>Control printing to stdout based on running in an applet context.
     * </p>
     * <br>
     * @param
     * <br>
     * @return  void
     */
    protected void    println(String out)
    {
        if (!m_flagRunningInApplet)
        {
            try
            {
                m_printStream.println("[CertLoader] " + out);
            }
            catch (Exception e)
            {
            }
        }
        else
        {
            if (null != m_textArea)
            {
                m_textArea.append("[CertLoader] ");
                m_textArea.append(out);
                m_textArea.append("\n");
                m_textArea.repaint();
            }
        }
    }

    /**
     * <!-- createAndStoreBinaryCertificate() -->
     * <p>Create a X509Certificate object and store it in the m_X509Certificates
     * vector.
     * </p>
     * <br>
     * @param certBytes is a byte[] array that holds the raw certificate data
     * <br>
     * @exception   Exception
     */
    protected abstract void createAndStoreBinaryCertificate(byte[] certBytes)
        throws Exception;

    /**
     * <!-- numberOfCertificates() -->
     * <p>Return the number of certificates available for use.
     * </p>
     * <br>
     * @return  int
     */
    public  int numberOfCertificates()
    {
        if (null != m_x509Certificates)
        {
            int             certCount = 0;

            // Run the list of cert files and data entries to get the total
            // count.
            //
            Enumeration files = m_x509Certificates.elements();
            while (files.hasMoreElements())
            {
                CertLoader.CertFile file = (CertLoader.CertFile) files.nextElement();
                Enumeration certEntries = file.enumCertData();
                while (certEntries.hasMoreElements())
                {
                    CertLoader.CertData certData = (CertLoader.CertData) certEntries.nextElement();
                    certCount += certData.getCertCount();
                }
            }
            return(certCount);
        }
        else
        {
            return(0);
        }
    }

    /**
     * <!-- enumCertFiles() -->
     * <p>Get an enumeration of all the certificate file that was loaded.
     * </p>
     * <br>
     * @return  Enumeration
     */
    public  Enumeration enumCertFiles()
    {
        return(m_x509Certificates.elements());
    }

    /*
     * <!-- readBinaryCert() -->
     * <p>Read a binar certificate from an input stream, only one certificate
     * is allowed per stream.
     * </p>
     * <br>
     * @param inStream is the InputStream object the binary certificate data
     * is read from.
     * <br>
     * @return  boolean
     * <br>
     * @exception   Exception
     * @exception   IOException (error reading file contents)
     * @exception   InstantiationException (Cannot create required class object)
     */
    protected boolean readBinaryCert(java.io.BufferedInputStream inStream) throws Exception,
            IOException, InstantiationException
    {
        boolean         loaded = false;
        int             bytesRead = 0;
        byte[]          certData = new byte[4096];
        int             available = 0;

        /* Load the certificate data. */
        while (0 != (available = inStream.available()))
        {
            int tmp = inStream.read (certData, bytesRead, certData.length - bytesRead);
            if (tmp > 0)
                bytesRead += tmp;
            else
                throw new IOException("Error reading 0 bytes from file before EOL");
            // System.out.println("** read " + tmp + " bytes of " + available);
        }
        if (m_flagDebug)
            println ("  Loaded " + bytesRead + " bytes of certificate data.");

        byte[] certificate = new byte[bytesRead];
        System.arraycopy (certData, 0, certificate, 0, bytesRead);

        try
        {
            createAndStoreBinaryCertificate(certificate);
            loaded = true;
        }
        catch (Exception e)
        {
            throw e;
        }

        return(loaded);
    }

    /*
     * <!-- readPemCert() -->
     * <p>Read a PEM certificate from an input reader. mutliple certificate
     * are allowed per reader.
     * </p>
     * <br>
     * @param inReader is the BufferedReader object the PEM certificate data
     * is read from.
     * <br>
     * @return  boolean true if all (1 or more) pem certicates are loaded and
     * stored.
     * <br>
     * @exception   Exception
     * @exception   EOFException (found end of file)
     */
    protected boolean readPemCert(BufferedReader inReader) throws Exception,
            EOFException
    {
        int             storeCount = 0;
        boolean         found = false;
        boolean         search = true;
        boolean         continueRead = true;
        String          inLine = null;

        while (continueRead)
        {
            found = false;
            search = true;

            // Search for the beginning of the certificate data...
            while (search)
            {
                inLine = inReader.readLine();
                if (null == inLine)
                {
                    // EOF, return
                    continueRead = false;
                    break;
                }
                if ( (-1 != inLine.indexOf("-BEGIN CERTIFICATE-")) ||
                     (-1 != inLine.indexOf("-BEGIN X509 CERTIFICATE-")) )
                {
                    // found the beginning.
                    if (m_flagDebug)
                        println(inLine);
                    found = true;
                    break;
                }
            }
            if (found)
            {
                // Build a string buffer for a max 4096 byte cert
                //
                StringBuffer    sb = new StringBuffer(4096);
                found = false;
                while (search)
                {
                    inLine = inReader.readLine();
                    if (null == inLine)
                    {
                        // Reached end of file
                        throw new EOFException("Unexpected end of file");
                    }

                    if (m_flagDebug)
                        println(inLine);


                    // Look for the end of the certifiate data.
                    if ( (-1 != inLine.indexOf("-END CERTIFICATE-")) ||
                         (-1 != inLine.indexOf("-END X509 CERTIFICATE-")) )
                    {
                        // found the end.
                        found = true;
                        break;
                    }

                    // Add it to the buffer (minus \n[\r] characters)
                    //
                    sb.append(inLine);
                }
                if (found)
                {
                    // Now convert to a binary byte stream and read the certificate
                    // again as a binary stream.
                    Base64  b64 = new Base64();
                    try
                    {
                        if (m_flagDebug)
                            println("Converting PEM to binary...");
                        ByteArrayInputStream certBytes = new ByteArrayInputStream(b64.decode(sb.toString()));
                        java.io.BufferedInputStream bufferedBytes = new java.io.BufferedInputStream(certBytes);
                        readBinaryCert(bufferedBytes);
                        storeCount++;
                    }
                    catch (Exception e)
                    {
                        if (m_flagIgnoreLoadErrors)
                            println("Cannot convert PEM certificate to binary : " + e.getMessage());
                        else
                            throw new Exception("Cannot convert PEM certificate to binary : " + e.getMessage());
                    }
                }
            }
        }
        return((0 < storeCount) ? true : false);
    }


    /*
     * <!-- loadBinaryFile() -->
     * <p>Load a single binary certificate from a disk file.
     * </p>
     * <br>
     * @param filePath is the file path to the binary certificate file.
     * <br>
     * @return  void
     * <br>
     * @exception   Exception
     */
    protected void loadBinaryFile(String filePath) throws Exception
    {
        File    inFile = new File(filePath);
        if (!inFile.exists())
        {
            throw new Exception("Error: Could not find file: " + filePath);
        }
        FileInputStream inFileStream = null;
        java.io.BufferedInputStream inStream = null;
        try
        {
            inFileStream = new FileInputStream(filePath);
            inStream = new java.io.BufferedInputStream(inFileStream);
        }
        catch (Exception e)
        {
            throw new Exception("Error accessing " + filePath + " : " + e.getMessage());
        }

        if (m_flagDebug)
            println("Reading from file: " + filePath);

        boolean fReadStatus = readBinaryCert(inStream);
        inFileStream.close();
        if (!fReadStatus)
        {
            throw new Exception("No certificate found");
        }
    }

    /*
     * <!-- loadPemFile() -->
     * <p>Load a single PEM certificate from a disk file.
     * </p>
     * <br>
     * @param filePath is the file path to the PEM certificate file.
     * <br>
     * @return  void
     * <br>
     * @exception   Exception
     */
    protected void loadPemFile(String filePath) throws Exception
    {
        boolean fReadStatus = false;

        File    inFile = new File(filePath);
        if (!inFile.exists())
        {
            throw new Exception("Error: Could not find file: " + filePath);
        }

        FileInputStream inStream = null;
        InputStreamReader inStreamReader = null;
        BufferedReader inReader = null;
        try
        {
            inStream = new FileInputStream(filePath);
            inStreamReader = new InputStreamReader(inStream);
            inReader = new BufferedReader(inStreamReader);
        }
        catch (Exception e)
        {
            throw new Exception("Error accessing " + filePath + " : " + e.getMessage());
        }

        if (m_flagDebug)
            println("Reading from file: " + filePath);

        try
        {
            fReadStatus = readPemCert(inReader);
            inStream.close();
            if (!fReadStatus)
            {
                if (m_flagIgnoreLoadErrors)
                    println("No certificate found in PEM file");
                else
                    throw new Exception("No certificate found in PEM file");
            }
        }
        catch (Exception e)
        {
            inStream.close();
            if (!m_flagIgnoreLoadErrors)
                throw e;
        }
    }

    /**
     * <!-- loadCertResource() -->
     * <p>Load a certificate from a resource stream.
     * </p>
     * <br>
     * @param entryName is a String object holding the name of the certificate
     * resource to load.
     * <br>
     * @return  boolean returns true if 1, or more, certificates were loaded.
     * <br>
     * @exception   Exception
     */
    protected boolean loadCertResource(String entryName) throws Exception
    {
        boolean         fReadStatus = false;
        InputStream     inStream = null;

        try
        {
            inStream = getResourceStream(entryName);
            if (null == inStream)
            {
                throw new Exception("Could not find certificate resource entry: " + entryName);
            }
        }
        catch (Exception e)
        {
            throw new Exception("Cannot get certificate resource stream : " + e.toString());
        }

        try
        {
            fReadStatus = loadCertStream(entryName, inStream);
        }
        catch (Exception e)
        {
            if (m_flagIgnoreLoadErrors)
                println(e.getMessage());
            else
                throw e;
        }

        return(fReadStatus);
    }

    /**
     * <!-- loadCertStream() -->
     * <p>Decide which type of certificate to load (PEM or binary) and then
     * dispatch to the correct input stream cert reader.
     * </p>
     * <br>
     * @param entryName is the String name of the certificate stream.
     * @param inStream is the InputStream for the entryName certificate.
     * <br>
     * @return  boolean
     * <br>
     * @exception   Exception
     */
    protected boolean loadCertStream(String entryName, InputStream inStream) throws Exception
    {
        boolean fReadStatus = true;

        if (entryName.endsWith(".pem") ||
            entryName.endsWith(".0") ||
            entryName.endsWith(".txt"))
        {
            if (m_flagDebug)
                println("Loading certificate stream for " + entryName);

            InputStreamReader inStreamReader = null;
            BufferedReader inReader = null;
            try
            {
                inStreamReader = new InputStreamReader(inStream);
            }
            catch (Exception e)
            {
                throw new Exception("Cannot get input stream reader...");
            }
            try
            {
                inReader = new BufferedReader(inStreamReader);
            }
            catch (Exception e)
            {
                throw new Exception("Cannot get buffered reader...");
            }

            // Record context for later use.
            m_certData = new CertData(entryName);
            m_certFile.addCertData(m_certData);

            fReadStatus = readPemCert(inReader);
        }
        else if (entryName.endsWith(".cer") ||
                 entryName.endsWith(".crt"))
        {
            if (m_flagDebug)
                println("Loading certificate stream for " + entryName);

            java.io.BufferedInputStream inBufferedStream = null;
            try
            {
                inBufferedStream = new java.io.BufferedInputStream(inStream);
            }
            catch (Exception e)
            {
                throw new Exception("Couldn't access " + entryName + " : " + e.getMessage());
            }

            // Record context for later use.
            m_certData = new CertData(entryName);
            m_certFile.addCertData(m_certData);

            fReadStatus = readBinaryCert(inBufferedStream);
        }
        else
        {
            if (m_flagDebug)
                println("Skipping certificate stream " + entryName);
        }

        return(fReadStatus);
    }


    /*
     * <!-- loadFromZipResource() -->
     * <p>Load an array of binary or PEM certificates from a ZIP file that is
     * a resource in the CLASSPATH.  The file extensions supported in the ZIP
     * file are:
     * </p>
     * <ls>
     * <le>.pem  A single PEM encoded certificate
     * <le>.0  A single PEM encoded certificate (RSA naming convention)
     * <le>.cer A single binary certificate
     * <le>.crt A single binary certificate
     * </ls>
     * <p>The ZIP file's location is obtained through the CLASSPATH.  So add the
     * directory used to store the ZIP file into the JVM's CLASSPATH.  If the
     * Zip file contains PEM encoded certificates, also place the ZIP file
     * in the class path.
     * </p>
     * <br>
     * @param srcZip is the [relative] path to the Zip file resource.
     * <br>
     * @return  void
     * <br>
     * @exception   Exception
     */
    protected void loadFromZipResource(String srcZip) throws Exception
    {
        Class           loader = this.getClass();
        try
        {
            int         count = 0;
            if (m_flagDebug)
                println("Searching for Zip file resource " + srcZip + "...");

            if (null == loader)
            {
                throw new Exception("Cannot obtain class loader");
            }

            URL         zipUrl = null;
            try
            {
                // Use VM sensitive getResource()
                zipUrl = getResourceURL(srcZip);
            }
            catch (Exception e)
            {
                throw new Exception("Could not get the Zip file resource: " + srcZip +
                    " : " + e.toString());
            }
            if (null != zipUrl)
            {
                String  zipPath = zipUrl.getFile();
                // Compensate for differing implementations of URL.getFile();
                if (zipPath.startsWith("/FILE"))
                {
                    zipPath = zipUrl.getFile().substring("/FILE".length());
                }
                else if (zipPath.startsWith("/"))
                {
                    zipPath = zipUrl.getFile().substring(1);
                }
                if (-1 != zipPath.indexOf('+'))
                {
                    zipPath = zipPath.replace('+', '.');
                }

                loadFromZipFile(zipPath);

            }
            else
            {
                throw new Exception("Could not find " + srcZip + " in the classpath");
            }
        }
        catch (Exception e)
        {
            throw new Exception("loadFromZipResource(): " + e.getMessage());
        }
    }

    /*
     * <!-- loadFromJarResource() -->
     * <p>Load an array of binary or PEM certificates from a Jar file accessed
     * as a resource in the CLASSPATH.  The file extensions supported in the
     * Jar file are:
     * </p>
     * <ls>
     * <le>.pem  A single PEM encoded certificate
     * <le>.0  A single PEM encoded certificate (RSA naming convention)
     * <le>.cer A single binary certificate
     * <le>.crt A single binary certificate
     * </ls>
     * <p>The Jar file's location is obtained through the CLASSPATH.  So add the
     * directory used to store the Jar file into the JVM's CLASSPATH.  If the
     * Jar file contains PEM encoded certificates, also place the Jar file
     * in the class path.
     * </p>
     * <br>
     * @param srcJar is the [relative] path to the Jar file resource.
     * <br>
     * @return  void
     * <br>
     * @exception   Exception
     */
//     protected void loadFromJarResource(String srcJar) throws Exception
//     {
//         try
//         {
//             ClassLoader     loader = this.getClass().getClassLoader();
//             URL             jarUrl = null;
//             int             count = 0;
//
//             if (m_flagDebug)
//                 println("Searching for jar file resource " + srcJar + "...");
//
//             if (null == loader)
//             {
//                 throw new Exception("Cannot obtain class loader");
//             }
//
//             try
//             {
//                 // Use VM sensitive getResource instead...
//                 // jarUrl = loader.getResource(srcJar);
//                 jarUrl = getResourceURL(srcJar);
//             }
//             catch (Exception e)
//             {
//                 throw new Exception("Could not get the jar file resource: " + srcJar +
//                     " : " + e.getMessage());
//             }
//             if (null != jarUrl)
//             {
//                 String  jarPath = jarUrl.getFile();
//                 // Compensate for differing implementations of URL.getFile();
//                 if (jarPath.startsWith("/FILE"))
//                 {
//                     jarPath = jarUrl.getFile().substring("/FILE".length());
//                 }
//                 else if (jarPath.startsWith("/"))
//                 {
//                     jarPath = jarUrl.getFile().substring(1);
//                 }
//                 if (-1 != jarPath.indexOf('+'))
//                 {
//                     jarPath = jarPath.replace('+', '.');
//                 }
//
//                 loadFromJarFile(jarPath);
//
//             }
//             else
//             {
//                 throw new Exception("Could not find " + srcJar + " in the classpath");
//             }
//         }
//         catch (Exception e)
//         {
//             throw new Exception("loadFromJarResource(): " + e.getMessage());
//         }
//     }

    /**
     * <!-- loadFromZipFile() -->
     * <p>Load a set of certificates from a ZIP formatted disk file.
     * </p>
     * <br>
     * @param zipPath is a String that points to the zip file to load certificates
     * from.
     * <br>
     * @return  void
     * <br>
     * @exception   Exception
     */
    protected void loadFromZipFile(String zipPath) throws Exception
    {
        ZipFile     zipFile = null;
        boolean     fReadStatus = false;
        int         entriesRead = 0;
        Enumeration enum1 = null;
        int         count = 0;
        String      oldCp = System.getProperty("java.class.path");
        String      newCp = new String(oldCp + ";" + zipPath);

        if (m_flagDebug)
            println("Loading from Zip file: " + zipPath);

        try
        {
            zipFile = new ZipFile(new File(zipPath));
        }
        catch (Exception e)
        {
            throw new Exception("Can't new ZipFile : " + e.getMessage());
        }

        try
        {

            enum1 = zipFile.entries();
            count = 0;
            while (enum1.hasMoreElements())
            {
                ZipEntry        entry = (ZipEntry)enum1.nextElement();
                String          entryName = entry.getName();
                InputStream     inStream = null;

                if (-1 != entryName.indexOf("META-INF"))
                {
                    // Skip meta information like the manifest
                    continue;
                }

                if (m_flagDebug)
                    println("**Loading cert: " + entryName);

                try
                {
                    inStream = zipFile.getInputStream(entry);
                    fReadStatus = loadCertStream(entryName, inStream);
                    inStream.close();
                }
                catch (Exception e)
                {
                    // Make sure the input stream is closed.
                    if (null != inStream)
                    {
                        inStream.close();
                    }
                    throw e;
                }

                if (fReadStatus)
                {
                    entriesRead++;
                }
                else
                {
                    throw new Exception("Couldn't read zip entry: " + entry.getName());
                }
                count++;
            }
            if (m_flagDebug)
            {
                println("The Zip file had " + count + " entries");
                println("Successfully read " + entriesRead + " entries");
            }

            // Restore the original classpath...
            // System.setProperty("java.class.path", oldCp);
        }
        catch (Exception e)
        {
            // Restore the original classpath...
            // System.setProperty("java.class.path", oldCp);
            throw e;
        }
    }

    /**
     * <!-- loadFromJarFile() -->
     * <p>Load a set of certificates from a JAR formatted disk file.
     * </p>
     * <br>
     * @param zipPath is a String that points to the jar file to load certificates
     * from.
     * <br>
     * @return  void
     * <br>
     * @exception   Exception
     */
//     protected void loadFromJarFile(String jarPath) throws Exception
//     {
//         JarFile     jarFile = null;
//         boolean     fReadStatus = false;
//         int         entriesRead = 0;
//         Enumeration enum1 = null;
//         int         count = 0;
//         String      oldCp = System.getProperty("java.class.path");
//         String      newCp = new String(oldCp + ";" + jarPath);
//
//         if (m_flagDebug)
//             println("**Loading from jar file: " + jarPath);
//
//         try
//         {
//             jarFile = new JarFile(new File(jarPath));
//         }
//         catch (Exception e)
//         {
//             throw new Exception("Cannot new JarFile");
//         }
//
//         try
//         {
//             // temporarily add the zip file to the classpath
//             // System.setProperty("java.class.path", newCp);
//
//             enum1 = jarFile.entries();
//             count = 0;
//             while (enum1.hasMoreElements())
//             {
//                 ZipEntry        entry = (ZipEntry)enum1.nextElement();
//                 String          entryName = entry.getName();
//                 InputStream     inStream = null;
//                 URL             resURL = null;
//
//                 if (-1 != entryName.indexOf("META-INF"))
//                 {
//                     // Skip meta information like the manifest
//                     continue;
//                 }
//
//                 try
//                 {
//                     inStream = jarFile.getInputStream(entry);
//                     fReadStatus = loadCertStream(entryName, inStream);
//                     inStream.close();
//                 }
//                 catch (Exception e)
//                 {
//                     // Make sure the stream is closed.
//                     if (null != inStream)
//                         inStream.close();
//                     throw e;
//                 }
//
//                 if (fReadStatus)
//                 {
//                     entriesRead++;
//                 }
//                 else
//                 {
//                     throw new Exception("Couldn't read jar entry: " + entry.getName());
//                 }
//                 count++;
//             }
//             if (m_flagDebug)
//             {
//                 println("The jar file had " + count + " entries");
//                 println("Successfully read " + entriesRead + " entries");
//             }
//
//             // Restore the original classpath...
//             // System.setProperty("java.class.path", oldCp);
//         }
//         catch (Exception e)
//         {
//             // Restore the original classpath...
//             // System.setProperty("java.class.path", oldCp);
//             throw e;
//         }
//
//     }
//
    /**
     * <!-- loadFromDirectory() -->
     * <p>Load a set of certificate files from a directory.  It is assuemed that
     * the checks have already been compeleted to indicate that it exists and
     * is a directory.  It will load files with file extensions: .pem, .cer, .0,
     * and .crt.
     * </p>
     * <br>
     * @param dirPath is a String that contains the directory path.
     * <br>
     * @return  void
     * <br>
     * @exception   Exception
     */
    protected void loadFromDirectory(String dirPath) throws Exception
    {
        File        path = new File(dirPath);
        String      loadEntry = null;
        String[]    entries = path.list();
        File        fileEntry = null;

        if (m_flagDebug)
            println("Loading from directory : " + dirPath);

        // get a list of all the entries.
        for (int i = 0; i < entries.length; i++)
        {
            loadEntry = entries[i];
            fileEntry = new File(path, loadEntry);
            // Weed out directories.
            try
            {
                // Record context for later use.
                m_certData = new CertData(fileEntry.getPath());
                m_certFile.addCertData(m_certData);

                if (isPemCertFile(fileEntry))
                    loadPemFile(fileEntry.getPath());
                if (isBinaryCertFile(fileEntry))
                    loadBinaryFile(fileEntry.getPath());
            }
            catch (Exception e)
            {
                if (m_flagIgnoreLoadErrors)
                    println(e.toString());
                else
                    throw e;
            }

        }

    }

    /*
     * <!-- loadFromManifestFile() -->
     * <p>Load the single certificate files listed in a Manifest formatted
     * disk file. Note: all manifest file entries are relative to the
     * current working directory.
     * </p>
     * <br>
     * @param file  is a String that names the disk Manifest file to use.
     * <br>
     * @return  void
     * <br>
     * @exception   Exception
     */
    protected void loadFromManifestFile(String file) throws Exception
    {
        File            inFile = new File("./", file);
        File            rootPath = new File(inFile.getParent());
        BufferedReader  inReader = new BufferedReader(new FileReader(inFile));

        if (m_flagDebug)
            println("Loading from digital-certificate-list (.dcl) file : " + file );

        try
        {
            loadManifest(inReader, rootPath);
            inReader.close();
        }
        catch (Exception e)
        {
            if (null != inReader)
                inReader.close();

            if (m_flagIgnoreLoadErrors)
                println(e.toString());
            else
                throw e;
        }
    }

    /*
     * <!-- loadFromManifestResource() -->
     * <p>Load the single certificate files listed in a Manifest formatted
     * Java CLASSPATH resource.
     * </p>
     * <br>
     * @param file  is a String that names the CLASSPATH Manifest file resource to use.
     * <br>
     * @return  void
     * <br>
     * @exception   Exception
     */
    protected void loadFromManifestResource(String resource) throws Exception
    {
        InputStream         inStream = null;
        InputStreamReader   inStreamReader = null;
        BufferedReader      inReader = null;

        if (m_flagDebug)
            println("Loading from Manifest resource : " + resource );

        try
        {
            inStream = getResourceStream(resource);
            inStreamReader = new InputStreamReader(inStream);
            inReader = new BufferedReader(inStreamReader);

            loadManifest(inReader, null);
            inStream.close();
        }
        catch (Exception e)
        {
            if (null != inStream)
                inStream.close();

            if (m_flagIgnoreLoadErrors)
                println(e.toString());
            else
                throw e;
        }
    }

    /*
     * <!-- loadManifest() -->
     * <p>Parse through the input stream that containes Manifest formatted lines.
     * for each certificate file entry, load the certificate.  Manifest entries
     * used in this method are <code>"<name>: <entry-name>"</code>.  All other
     * lines are ignored.
     * </p>
     * <br>
     * @param inReader is a BufferedReader that points to the Manifest formatted
     * line data.
     * @param rootFilePath is a File object that points to the root file path
     * to prefix to all disk file based entries.  This is required for disk
     * files and must be "null" for loading resources.
     * <br>
     * @return  void
     * <br>
     * @exception   Exception
     */
    protected void loadManifest(BufferedReader inReader, File rootFilePath) throws Exception
    {
        boolean     continueRead = true;
        boolean     search = true;
        boolean     found = false;
        boolean     loadAsFile = ((null != rootFilePath) ? true : false);
        String      inLine = null;
        int         prefixLength = "Name: ".length();
        int         inLineLength = 0;

        // loop reading lines to the end of file...
        //
        try
        {
            while (continueRead)
            {
                search = true;
                found = false;

                while (search)
                {
                    inLine = inReader.readLine();
                    if (null == inLine)
                    {
                        // EOF, return
                        continueRead = false;
                        break;
                    }

                    // See if this is line we are looking for...
                    //
                    inLineLength = inLine.length();
                    if (0 < inLineLength)
                    {
                        if (inLine.startsWith("Name: ") &&
                            (prefixLength < inLineLength))
                        {
                            found = true;
                            break;
                        }
                    }
                }
                if (found)
                {
                    // found an entry, parse the value (certificate file entry)
                    // and see if it is something we want to deal with...
                    String entry = inLine.substring(prefixLength);

                    if (entry.endsWith(".cer") ||
                        entry.endsWith(".crt"))
                    {
                        if (loadAsFile)
                        {
                            File entryPath = new File(rootFilePath, entry);

                            // Record context for later use.
                            m_certData = new CertData(entryPath.getPath());
                            m_certFile.addCertData(m_certData);

                            loadBinaryFile(entryPath.getPath());
                        }
                        else
                        {
                            // Record context for later use.
                            m_certData = new CertData(entry);
                            m_certFile.addCertData(m_certData);

                            loadCertResource(entry);
                        }
                    }
                    else if (entry.endsWith(".pem") ||
                             entry.endsWith(".0"))
                    {
                        if (loadAsFile)
                        {
                            File entryPath = new File(rootFilePath, entry);

                            // Record context for later use.
                            m_certData = new CertData(entryPath.getPath());
                            m_certFile.addCertData(m_certData);

                            loadPemFile(entryPath.getPath());
                        }
                        else
                        {
                            // Record context for later use.
                            m_certData = new CertData(entry);
                            m_certFile.addCertData(m_certData);

                            loadCertResource(entry);
                        }
                    }
                    else
                    {
                        if (m_flagDebug)
                        println("Skipping manifest entry : " + entry);
                    }
                }
            }
        }
        catch (Exception e)
        {
            if (m_flagIgnoreLoadErrors)
                println(e.toString());
            else
                throw e;
        }
    }

    // test for a pem certificate file
    protected boolean isPemCertFile(File target)
    {
        if (target.isFile() &&
            (target.getName().endsWith(".pem") ||
             target.getName().endsWith(".0")))
        {
            return(true);
        }
        else
        {
            return(false);
        }
    }

    // test for a binary certificate file
    protected boolean isBinaryCertFile(File target)
    {
        if (target.isFile() &&
            (target.getName().endsWith(".cer") ||
             target.getName().endsWith(".crt")))
        {
            return(true);
        }
        else
        {
            return(false);
        }
    }

    // Is the string pointing to an accessible resource.
    protected boolean isResource(String target)
    {
        boolean     retVal = false;
        try
        {
            InputStream is = getResourceStream(target);
            if (null != is)
            {
                is.close();
                retVal = true;
            }
            else
            {
                if (m_flagDebug)
                    println ("The file " + target + " is not a CLASSPATH resource");
            }
        }
        catch (Exception e)
        {
        }

        return(retVal);
    }

    // Get an input stream from a CLASSPATH resource.
    //
    protected InputStream   getResourceStream(String target)
    {
        InputStream returnValue = null;

        try
        {
            if (null != target &&
                0 < target.length())
            {
                // For some reason, Microsoft requires this???
                //
                if (m_flagMicrosoftJVM  &&
                    !target.startsWith("/"))
                {
                    StringBuffer resourceName = new StringBuffer();
                    resourceName.append("/");
                    resourceName.append(target);
                    if (m_flagDebug )
                    {
                        println("Using Microsoft resource loader for: " + resourceName.toString());
                    }
                    returnValue = this.getClass().getResourceAsStream(resourceName.toString());
                }
                else
                {
                    if (m_flagDebug )
                    {
                        println("Using Java resource loader for: " + target);
                    }
                    returnValue = this.getClass().getClassLoader().getResourceAsStream(target);
                }
            }
        }
        catch (Exception e)
        {
            if (m_flagDebug)
            {
                println("The resource stream " + target + " could not be obtained.");
            }
            //e.printStackTrace();
        }
        return(returnValue);
    }

    protected URL getResourceURL(String target)
    {
        URL     returnValue = null;
        try
        {
            if (null != target &&
                0 < target.length())
            {
                // For some reason, Microsoft requires this???
                //
                if (m_flagMicrosoftJVM  &&
                    !target.startsWith("/"))
                {
                    StringBuffer resourceName = new StringBuffer();
                    resourceName.append("/");
                    resourceName.append(target);
                    if (m_flagDebug )
                    {
                        println("Getting URL to Microsoft resource : " + resourceName.toString());
                    }
                    returnValue = this.getClass().getResource(resourceName.toString());
                }
                else
                {
                    if (m_flagDebug )
                    {
                        println("Getting Java URL resource: " + target);
                    }
                    returnValue = this.getClass().getClassLoader().getResource(target);
                }
            }
        }
        catch (Exception e)
        {
            if (m_flagDebug)
            {
                println("The resource URL for " + target + " could not be obtained.");
            }
            //e.printStackTrace();
        }
        return(returnValue);

    }

    /*
    ** An internal debug method to dump the environment.
    */
    protected void    dumpEnvironment()
    {
        if (m_flagDebug)
        {
            println("Using environment:");
            println("    OS arch   : " + System.getProperty("os.arch"));
            println("    OS name   : " + System.getProperty("os.name"));
            println("    OS version: " + System.getProperty("os.version"));
            println("    Vendor    : " + System.getProperty("java.vendor"));
            println("    IsApplet  : " + m_flagRunningInApplet);
            StringBuffer searchTypes = new StringBuffer();
            if (!m_flagSearchClasspathOnly && !m_flagSearchDiskFilesOnly)
            {
                searchTypes.append("Files & CLASSPATH");
            }
            else
            {
                if (m_flagSearchClasspathOnly)
                    searchTypes.append("CLASSPATH ");
                if (m_flagSearchDiskFilesOnly)
                    searchTypes.append("Files");
            }
            if (!m_flagRunningInApplet)
            {
                try
                {
                    println("    CLASSPATH : " + System.getProperty("java.class.path"));
                }
                catch (Exception e)
                {
                }
            }
            println("");
        }
    }

}

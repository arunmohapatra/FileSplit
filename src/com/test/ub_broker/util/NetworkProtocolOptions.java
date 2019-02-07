/*
/* <p>Copyright 2000-2001 Progress Software Corportation, All rights reserved.</p>
/* <br>
/* <p>Class:        NetworkProtocolOptions  </p>
/* <br>
/* All rights reserved.  No part of this program or document
/* may be  reproduced in  any form  or by  any means without
/* permission  in  writing  from  Data Language Corporation.
/*
*/

package com.progress.ubroker.util;

import  java.lang.*;
import  java.util.Properties;
import  java.util.Enumeration;

/**
 * <p>
 * This class functions as a static Properties object that acts as a central
 * global point to store and access option (String) values for the network
 * protocol handler objects such as TCPClientProtocol.
 * </p>
 *
 * <p>
 * It operates as a reduced functionality Properties object.  It only supports
 * the Properties methods:
 * </p>
 * <ul>
 * <li>setProperty()
 * <li>getProperty()
 * <li>propertyNames()
 * </ul>
 *
 * <p>
 * Normally this class tries to initialize its internal properties object with
 * the System's properties object as the default set.  That way, the locally
 * set options take precedence over the system properties.  The options can
 * be set programably or through system properties on the command line.  Makes
 * a nice hierarcy, especially when you don't have the permissions to programmably
 * set system properties.
 * </p>
 * <p>
 * If running in a VM with a SecurityManager, it may not allow this class to
 * automatically use the System's properties object (System.getProperties()) as
 * a set of defaults.
 * If it can't get access
 * to it, it will continue to run silently without any notification.
 * </p>
 *
 * <p>
 * The loose set of rules governing option name format is as follows:
 * <br>
 * <code>psc.protocol_name.option_name</code>
 * <br>
 * First start with psc to avoid namespace collision with system properties;
 * then add the protocol such as tcp, http, ssl, etc;
 * then add the option name such as sotimeout.  Keep all names all lowercase
 * for consistency.
 * </p>
 */
public class NetworkProtocolOptions
{
    /*
     * CLASS Properties.
     * public static        <type>  <name> = <value>;
     */
    /** Set the Socket Timeout option for direct communications to an AppServer
     * via the TCP protocol.  The number is in the range of 0 to n milliseconds,
     * with zero indicating to wait forever. */
    public static final     String  TCP_SOTIMEOUT = "psc.tcp.sotimeout";

    /** Set the AppServer protocol to use ("yes") or not ("no") the nameserver
     * to resolve the AppServer's ubroker IP address and port.  If set to no,
     * the IP port and address in the URL are the AppServer ubroker's.
     */
    public  static final    String  TCP_NAMESERVER = "psc.tcp.nameserver";

    /** The default [client] proxy host to use.  The String format is "host:port" */
    /* NOTE: Maybe someday, set specific proxies like: psc.http.proxy.<name>=host:port */
    public  static final    String  HTTP_PROXY_HOST_OPTION = "psc.http.proxy";

    /** The default [client] proxy authentication to use.  The String format
     * is "[realm:]uid:pwd".  The realm is optional.  So it may be specified as
     * realm:uid:pwd  or uid:pwd */
    /* NOTE: Maybe someday, set specific proxies authentication
     * like: psc.http.proxy.<name>.auth=[realm:]uid:pwd*/
    public  static final    String  HTTP_PROXY_AUTH_OPTION = "psc.http.proxy.auth";

    /** The timeout, in seconds, to wait for a response from the AIA.  This is
     * an String integer value of zero to +n.  Negative numbers are ignored.
     * Zero indicates wait forever*.  */
    public  static final    String  HTTP_TIMEOUT_OPTION = "psc.http.timeout";

    /** The http server authentication.  This is an alternative to putting the
     * authentication information into the URL.  You should use this if
     * possible.
     * The value format is "[realm:]uid:password".  The realm is optional.
     */
    public  static final    String  HTTP_AUTH_OPTION = "psc.http.auth";

    /** The path used to search for Root Digital Certificates used by the
     * HTTP/S client. */
    public  static final    String  HTTPS_CERT_PATH = "psc.https.certpath";

    /** The default path to use when the user doesn't specify their own. */
    public  static final    String  HTTPS_DEFAULT_JAVA_CERT_PATH = "psccerts.jar";
    public  static final    String  HTTPS_DEFAULT_WINDOWS_CERT_PATH = "psccerts.zip";
    public  static final    String  HTTPS_DEFAULT_APPLET_CERT_PATH = "psccerts.dcl";

    /** The HTTPS server authentication option bitmask of HTTPS_AUTH_XXXXX
     * values.
     */
    public  static final    String  HTTPS_SERVER_DOMAIN_AUTH = "psc.https.auth.serverdomain";
    /* Used for debugging and specifying a specific network domain name to match
     * in the server Digital Certificate's subject name field.
     */
    public  static final    String  HTTPS_SERVER_DOMAIN = "psc.https.auth.domain";
    /* Used for debugging the CertLoader class. The value must be "true" or "false".
     */
    public  static final    String  HTTPS_CERT_LOADER_DEBUG = "psc.certloader.debug";

    /*
     * Super Object Properties.
     *  protected       <type>          <name> = <value>;
     */

    /*
     * Object Instance Properties.
     *  private         <type>          <name> = <value>;
     */
    private static      Properties      m_protocolOptions = null;
    private static      boolean         m_initDone = false;

    /*
     * Constructors...
     */

    /**
    * <!-- NetworkProtocolOptions() -->
    * <p>The default class constructor.
    * </p>
    */
    public NetworkProtocolOptions()
    {
    }

    /*
     * ACCESSOR METHODS:
     */
    /**
     * <!-- getOptions() -->
     * <p>Get a Properties object that holds the global Network protocl options.
     * </p>
     * <br>
     * @return  Properties
     * <br>
     * @exception   Exception
     */
    public static Properties getOptions() throws Exception
    {
        if (!m_initDone)
            init();
        return(m_protocolOptions);
    }


    /**
     * <!-- setProperty() -->
     * <p>Emulate a static Properties class object's setProperty() method.
     * </p>
     * <br>
     * @param optionName is a String specifying an property network protocol
     * option name.
     * @param optionValue is a String specifying the valuefor the optionName
     * argument.
     * <br>
     * @return  Object
     */
    public static Object setProperty(String optionName, String optionValue)
    {
        if (!m_initDone)
            init();
        return(m_protocolOptions.put(optionName, optionValue));
    }

    /**
     * <!-- getProperty() -->
     * <p>Emulate a static Properties class object's getProperty() method.
     * </p>
     * <br>
     * @param optionName is a String specifying an property network protocol
     * option name.
     * <br>
     * @return  String
     */
    public static String getProperty(String optionName)
    {
        if (!m_initDone)
            init();
        return(m_protocolOptions.getProperty(optionName));
    }

    /**
     * <!-- propertyNames() -->
     * <p>Emulate the Properties object's propertyNames() method.
     * </p>
     * <br>
     * @return  Enumeration
     */
    public static Enumeration propertyNames()
    {
        if (!m_initDone)
            init();
        return(m_protocolOptions.propertyNames());
    }

    /**
     * <!-- getStringProperty() -->
     * <p>Get a String value from a property.
     * </p>
     * <br>
     * @param   properties is the Properties object to get the property from.
     * If null, it gets it from this property set.
     * @param   propName is the String property name to find.  A null or empty
     * value will return the default.
     * @param   defaultValue is the String to return if the property cannot be found.
     * <br>
     * @return  String
     */
    public  static String getStringProperty(Properties properties,
                                            String     propName,
                                            String     defaultValue)
    {
        String      returnValue = defaultValue;
        Properties  src = ((null == properties) ? m_protocolOptions : properties);

        if (null != propName &&
            0 < propName.length())
        {
            String tmp = src.getProperty(propName);
            returnValue = ((null != tmp) ? tmp : defaultValue);
        }

        return(returnValue);
    }

    /**
     * <!-- getLongProperty() -->
     * <p>Get a long property value.
     * </p>
     * <br>
     * @param   properties is the Properties object to get the property from.
     * If null, it gets it from this property set.
     * @param   propName is the String property name to find.  A null or empty
     * value will return the default.
     * @param   defaultValue is the long to return if the property cannot be found.
     * <br>
     * @return  long
     */
    public  static long getLongProperty(Properties properties,
                                        String     propName,
                                        long       defaultValue)
    {
        long        returnValue = defaultValue;
        Properties  src = ((null == properties) ? m_protocolOptions : properties);

        if (null != propName &&
            0 < propName.length())
        {
            returnValue = NetworkProtocolOptions.longFromString(src.getProperty(propName),
                                                                defaultValue);
        }

        return(returnValue);
    }

    /**
     * <!-- getIntProperty() -->
     * <p>Get a int property value.
     * </p>
     * <br>
     * @param   properties is the Properties object to get the property from.
     * If null, it gets it from this property set.
     * @param   propName is the String property name to find.  A null or empty
     * value will return the default.
     * @param   defaultValue is the int to return if the property cannot be found.
     * <br>
     * @return  int
     */
    public  static int getIntProperty(Properties properties,
                                      String     propName,
                                      int        defaultValue)
    {
        int     returnValue = defaultValue;
        Properties  src = ((null == properties) ? m_protocolOptions : properties);

        if (null != propName &&
            0 < propName.length())
        {
            returnValue = NetworkProtocolOptions.intFromString(src.getProperty(propName),
                                                               defaultValue);
        }

        return(returnValue);
    }

    /**
     * <!-- getBooleanProperty() -->
     * <p>Get a boolean property value.
     * </p>
     * <br>
     * @param   properties is the Properties object to get the property from.
     * If null, it gets it from this property set.
     * @param   propName is the String property name to find.  A null or empty
     * value will return the default.
     * @param   defaultValue is the boolean to return if the property cannot be found.
     * <br>
     * @return  boolean
     */
    public  static boolean getBooleanProperty(Properties properties,
                                              String     propName,
                                              boolean    defaultValue)
    {
        boolean     returnValue = defaultValue;
        Properties  src = ((null == properties) ? m_protocolOptions : properties);

        if (null != propName &&
            0 < propName.length())
        {
            returnValue = NetworkProtocolOptions.booleanFromString(src.getProperty(propName),
                                                                   defaultValue);
        }

        return(returnValue);
    }

    /**
     * <!-- longFromString() -->
     * <p>Get a long from a String with a default value.
     * </p>
     * <br>
     * @param in    is the String input value
     * @param defaultValue is the long default value if the input string is
     * null or empty or invalid
     * <br>
     * @return  long
     */
    public  static long longFromString(String in,
                                       long   defaultValue)
    {
        long        returnValue = defaultValue;

        try
        {
            if (null != in &&
                0 < in.length())
            {
                returnValue = Long.parseLong(in);
            }
        }
        catch (Throwable e)
        {
        }
        return(returnValue);
    }

    /**
     * <!-- intFromString() -->
     * <p>Get a int from a String with a default value.
     * </p>
     * <br>
     * @param in    is the String input value
     * @param defaultValue is the int default value if the input string is
     * null or empty
     * <br>
     * @return  int
     */
    public  static int intFromString(String in,
                                     int    defaultValue)
    {
        int     returnValue = defaultValue;

        try
        {
            if (null != in &&
                0 < in.length())
            {
                returnValue = Integer.parseInt(in);
            }
        }
        catch (Throwable e)
        {
        }
        return(returnValue);
    }

    /**
     * <!-- booleanFromString() -->
     * <p>Get a boolean from a String with a default value.
     * </p>
     * <br>
     * @param in    is the String input value
     * @param defaultValue is the boolean default value if the input string is
     * null or empty
     * <br>
     * @return  boolean
     */
    public  static boolean booleanFromString(String in,
                                            boolean defaultValue)
    {
        boolean     returnValue = defaultValue;

        try
        {
            if (null != in &&
                0 < in.length())
            {
                Boolean bool = Boolean.valueOf(in);
                returnValue = bool.booleanValue();
            }
        }
        catch (Throwable e)
        {
        }

        return(returnValue);
    }


    /*
     * PRIVATE METHODS:
     */
    /*
     * <!-- init() -->
     * <p>Init the internal properties object.  Provided just in case the
     * static initializer doesn't get called by the VM.  This REALLY needs to
     * run.
     * </p>
     * <br>
     * @return  void
     */
    private static void init()
    {
        if (!m_initDone)
        {
            if (null == m_protocolOptions)
            {
                try
                {
                    // Try creating with the system properties acting as the
                    // defaults
                    m_protocolOptions = new Properties(System.getProperties());
                }
                catch (SecurityException e)
                {
                    // Create without the System properties, we'll just have
                    // to live with it.
                    m_protocolOptions = new Properties();
                }
                finally
                {
                    if (null == m_protocolOptions)
                    {
                        m_protocolOptions = new Properties();
                    }
                }
            }

            m_initDone = true;
        }
    }

    /**
     * <!-- () -->
     * <p>Static null namespace intializer for this object.  Kind of like
     * a default destructor for a public static class ...., but not quite.
     * </p>
     * <br>
     */
    static
    {
        init();
    }
}
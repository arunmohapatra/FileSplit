/*
/*
/* <p>Copyright 2000-2004 Progress Software Corportation, All rights reserved.</p>
/* <br>
/* <p>Class:        PscURLParser    </p>
/* <br>
/* All rights reserved.  No part of this program or document
/* may be  reproduced in  any form  or by  any means without
/* permission  in  writing  from  Data Language Corporation.
/*
*/

package com.progress.common.util;

import java.net.MalformedURLException;
import java.util.StringTokenizer;

/**
 * The PscURLParser class fills the need to parse and validate URLs that
 * contain the Progress unique schemes such as AppServer and AppServerDC.
 * <p>
 * Since the URL parsing needs across multi-platforms and different vendor's
 * URL classes do not support all of these protocols, this class provides the
 * basic parsing abiltiy.  It does not replace the functionality of the real
 * URL class, so don't look for it here.
 */
public class PscURLParser
{
    private static final    String  m_defHost = "localhost";
    private static final    String  m_defPath = "/";
    private static final    String[]    m_validSchemes = {"AppServer",
                                                          "AppServerDC",
                                                          "http",
                                                          "https",
                                                          "AppServerS",
                                                          "AppServerDCS",
                                                          "internal"
    };

    /* matching indexes to the supported schemes above. */
    public  static final    String[]    m_defPorts = {"5162",
                                                      "3090",
                                                      "80",
                                                      "443",
                                                      "5162",
                                                      "3090",
                                                      "0"
    };

    /*
     * Instance Data.
     */
    private String                  m_scheme = m_validSchemes[0];
    private String                  m_authority = m_defHost;
    private String                  m_authorityUser = null;
    private String                  m_authorityPwd = "";
    private String                  m_authorityHost = m_defHost;
    private String                  m_authorityPort = null;
    private String                  m_path = m_defPath;
    private String                  m_query = null;
    private String                  m_fragment = null;

    /**
     * Default Constructor
     */
    public PscURLParser()
    {
    }

    /**
     * Constructor that takes a URL specification as an input argument.
     * <p>
     * @param   urlSpec is a String object that contains a minimum of 1 character
     * to trigger an automatic parsing operation.  Note: the parsing operation
     * may fail silently.
     * <br>
     * @exception   MalformedURLException
     */
    public PscURLParser(String urlSpec) throws MalformedURLException
    {
        if (null != urlSpec && 0 < urlSpec.length())
        {
            parseURL(urlSpec);
        }
    }


    /**
     * <!-- getScheme() -->
     * <p>Return the parsed URL scheme.
     * </p>
     * <br>
     * @return  String  May be null if a URL has not yet been parsed.
     */
    public String getScheme()
    {
        return((null != m_scheme) ? new String(m_scheme) : null);
    }

    /**
     * <!-- getAuthority() -->
     * <p>Return the ULR authority field which may conditionally contain the
     * user info user-id and password, and the network-address host and port
     * information.
     * </p>
     * <br>
     * @return  String
     */

    public String getAuthority()
    {
        StringBuffer    out = new StringBuffer(512);

        if (null != m_authorityUser)
        {
            out.append(m_authorityUser);
            if (null != m_authorityPwd)
            {
                out.append(":");
                out.append(m_authorityPwd);
            }
            out.append("@");
        }
        if (null != m_authorityHost)
        {
            out.append(m_authorityHost);
            if (null != m_authorityPort)
            {
                out.append(":");
                out.append(m_authorityPort);
            }
        }
        return((0 < out.length()) ? new String(out.toString()) : null);
    }

    /**
     * <!-- getPath() -->
     * <p>Return the URL path field
     * </p>
     * <br>
     * @return  String
     */
    public String getPath()
    {
        return(new String(m_path));
    }


    /**
     * <!-- getQuery() -->
     * <p>Return the URL's query field.  This field may contain multiple query
     * elements.
     * </p>
     * <br>
     * @return  String May return null if no query information is available.
     */
    public String getQuery()
    {
        return((null == m_query) ? null : new String(m_query));
    }

    /**
     * <!-- getFragment() -->
     * <p>Return the URL's Fragement field if one exists.
     * </p>
     * <br>
     * @return  String May return null if a Fragment has not been specified.
     */
    public String getFragment()
    {
        return((null == m_fragment) ? null : new String(m_fragment));
    }

    /**
     * <!-- getUserId() -->
     * <p>Return the user-id of the URL authority field.
     * </p>
     * <br>
     * @return  String May be null if user-info has not been specified.
     */
    public String getUserId()
    {
        return((null == m_authorityUser) ? null : new String(m_authorityUser));
    }

    /**
     * <!-- getUserPassword() -->
     * <p>Return the user-id' password from the URL authority field.
     * </p>
     * <br>
     * @return  String May be null if no password has has not been specified.
     */
    public String getUserPassword()
    {
        return((null == m_authorityPwd) ? null : new String(m_authorityPwd));
    }

    /**
     * <!-- getHost() -->
     * <p>Return the URL authority field's Host specification.
     * </p>
     * <br>
     * @return  String
     */
    public String getHost()
    {
        return(new String(m_authorityHost));
    }

    /**
     * <!-- getPort() -->
     * <p>Return the URL authority field's Port specification.
     * </p>
     * <br>
     * @return  int May return the default port for the protocol if a
     * specific port has not been specified in the URL
     */
    public int getPort()
    {
        if (null == m_authorityPort)
        {
            return(Integer.parseInt(getDefaultPort(m_scheme)));
        }
        else
        {
            return(Integer.parseInt(m_authorityPort));
        }
    }

    /**
     * <!-- setScheme() -->
     * <p>Set the URL scheme.
     * </p>
     * <br>
     * @param   inScheme is a String that represents a valid network scheme.  If
     * null or empty, the default scheme will be set (AppServer)
     */
    public void setScheme(String inScheme)
    {
        if (null != inScheme &&
            0 < inScheme.length())
        {
            m_scheme = inScheme;
        }
        else
        {
            m_scheme = m_validSchemes[0];
        }
    }

    /**
     * <!-- getService() -->
     * <p>Return the URL's AppService service name, if one exists.  Note, the
     * AIA does not support multiple query elements.
     * </p>
     * <br>
     * @return  String May be null if a service has not been set.
     */
    public  String getService()
    {
        String  returnValue = null;

        // AppServer & AppServerDC schemes
        if (m_scheme.equalsIgnoreCase(m_validSchemes[0]) ||
            m_scheme.equalsIgnoreCase(m_validSchemes[1]) ||
            m_scheme.equalsIgnoreCase(m_validSchemes[4]) ||
            m_scheme.equalsIgnoreCase(m_validSchemes[5]))
        {
            if (1 < m_path.length())
            {
                returnValue = m_path.substring(1);
            }
        }
        // http & https schemes
        else if (m_scheme.equalsIgnoreCase(m_validSchemes[2]) ||
                 m_scheme.equalsIgnoreCase(m_validSchemes[3]))
        {
            if (null != m_query)
            {
                StringTokenizer     parser = new StringTokenizer(m_query, "&");
                while (parser.hasMoreTokens())
                {
                    String  element = parser.nextToken();
                    if (element.startsWith("AppService="))
                    {
                        returnValue = element.substring(element.indexOf("=") + 1);
                        break;
                    }
                }
            }
        }

        return(returnValue);
    }

    /**
     * <!-- setPath() -->
     * <p>Set the URL path field
     * </p>
     * <br>
     * @param   inPath is a String that represents the new path.  If null or
     * empty, "/" will be set
     */
    public void setPath(String inPath)
    {
        if (null != inPath &&
            0 < inPath.length())
        {
            if (inPath.startsWith("/"))
            {
                m_path = inPath;
            }
            else
            {
                m_path = m_defPath + inPath;
            }
        }
        else
        {
            m_path = m_defPath;
        }
    }


    /**
     * <!-- setQuery() -->
     * <p>Set the URL's query field.  This field may contain multiple query
     * elements.
     * </p>
     * <br>
     * @param String inQuery is a String that may be null or blank to erase the
     * current value (if any). The query string should not include the leading
     * question-mark, but it must include the and-sign (&) delimiter between
     * values.
     */
    public void setQuery(String inQuery)
    {
        if (null != inQuery &&
            0 < inQuery.length())
        {
            if (inQuery.startsWith("?"))
            {
                if (1 < inQuery.length())
                {
                    m_query = inQuery.substring(1);
                }
                else
                {
                    m_query = null;
                }
            }
            else
            {
                m_query = inQuery;
            }
        }
        else
        {
            m_query = null;
        }
    }

    /**
     * <!-- setFragment() -->
     * <p>Set the URL's Fragement field.
     * </p>
     * <br>
     * @param inFragment is a String object that holds the Fragment portion of
     * the URL.  If the value is null or blank, any existing value is removed (
     * set to null).  Otherwise the string is recorded as is.  Do not specify
     * the pound-sign Fragment delimiter.  If supplied it will be removed.
     */
    public void setFragment(String inFragment)
    {
        if (null != inFragment &&
            0 < inFragment.length())
        {
            if (inFragment.startsWith("#"))
            {
                if (1 < inFragment.length())
                {
                    m_fragment = inFragment.substring(1);
                }
                else
                {
                    m_fragment = null;
                }
            }
            else
            {
                m_fragment = inFragment;
            }
        }
        else
        {
            m_fragment = null;
        }
    }

    /**
     * <!-- setUserId() -->
     * <p>Set the user-id of the URL authority field.
     * </p>
     * <br>
     * @param   inUser is a String that gets the new user id.  If the value is
     * null or blank, both the userid and any password value is removed.
     */
    public void setUserId(String inUser)
    {
        if (null != inUser &&
            0 < inUser.length())
        {
            m_authorityUser = inUser;
        }
        else
        {
            m_authorityUser = null;
            m_authorityPwd = null;
        }
    }

    /**
     * <!-- setUserPassword() -->
     * <p>Set the user-id' password for the URL authority field.
     * </p>
     * <br>
     * @param inPassword is a String object holding the new user password.
     * The value is ignored if a user-id does not exist.  If a user id exists
     * and the value is null or blank, a blank password is set.  Otherwise the
     * input argument value is recorded.
     */
    public void setUserPassword(String inPassword)
    {
        if (null != inPassword &&
            0 < inPassword.length())
        {
            if (null != m_authorityUser &&
                0 < m_authorityUser.length())
            {
                m_authorityPwd = inPassword;
            }
        }
        else
        {
            if (null != m_authorityUser &&
                0 < m_authorityUser.length())
            {
                m_authorityPwd = "";
            }
            else
            {
                m_authorityPwd = null;
            }
        }
    }

    /**
     * <!-- setHost() -->
     * <p>Set the URL authority field's Host specification.
     * </p>
     * <br>
     * @param   inHost is a String parameter specifying the new host name.  The
     * name may be in DNS or dot-format.  If a null or blank value is passed, the
     * host is reverted to the default "localhost".
     */
    public void setHost(String inHost)
    {
        if (null != inHost &&
            0 < inHost.length())
        {
            m_authorityHost = inHost;
        }
        else
        {
            m_authorityHost = m_defHost;
        }
    }

    /**
     * <!-- setPort() -->
     * <p>Set the URL authority field's Port specification.
     * </p>
     * <br>
     * @param   inPort is a int in the range 3 to 65536.  A value < 3 will be
     * set to 3 and a value larger than 65536 will be set to 65536.  Set the
     * value to -1 to reset the default value.
     */
    public void setPort(int inPort)
    {
        int     tmpPort = inPort;
        if (-1 == tmpPort)
        {
            m_authorityPort = null;
        }
        else
        {
            if (3 > tmpPort)
            {
                tmpPort = 3;
            }
            if (65536 < tmpPort)
            {
                tmpPort = 65536;
            }
            m_authorityPort = Integer.toString(inPort);
        }
    }

    /**
     * <!-- setPort() -->
     * <p>Return the URL authority field's Port specification.
     * </p>
     * <br>
     * @param   inPort is an String int that is in the range 3 to 65536.  A
     * null or empty value will reset the value to the default.
     */
    public void setPort(String inPort)
    {
        if (null == inPort ||
            0 == inPort.length())
        {
            m_authorityPort = null;
        }
        else
        {
            try
            {
                int tmp = Integer.parseInt(inPort);
                m_authorityPort = Integer.toString(tmp);
            }
            catch (Exception e)
            {
            }
        }
    }

    /**
     * <!-- setService() -->
     * <p>Sets the AppServer service name.
     * </p>
     * <br>
     * @param inService is a String value that is the new service name to set.
     * If the value is null or blank, any existing service name is removed.  NOTE:
     * all query elements are removed except for the "AppService" element
     * <br>
     * @return  void
     */
    public void setService(String inService)
    {
        StringBuffer    newValue = new StringBuffer();

        // AppServer & AppServerDC schemes
        if (m_scheme.equalsIgnoreCase(m_validSchemes[0]) ||
            m_scheme.equalsIgnoreCase(m_validSchemes[1]) ||
            m_scheme.equalsIgnoreCase(m_validSchemes[4]) ||
            m_scheme.equalsIgnoreCase(m_validSchemes[5]))
        {
            m_path = m_defPath;
            if (null != inService &&
                0 < inService.length())
            {
                newValue.append("/");
                newValue.append(inService);
                m_path = newValue.toString();
            }
        }
        // http & https schemes
        else if (m_scheme.equalsIgnoreCase(m_validSchemes[2]) ||
                 m_scheme.equalsIgnoreCase(m_validSchemes[3]))
        {
            if (null != m_query)
            {
                m_query = null;
            }
            if (null != inService &&
                0 < inService.length())
            {
                newValue.append("AppService=");
                newValue.append(inService);
                m_query = newValue.toString();
            }
        }
        // else nothing, not a appservice protocol.
        //
    }

    /**
     * <!-- parseURL() -->
     * <p>Parse the URL according the RFC (dont remember).
     * </p>
     * <br>
     * @param inURL is the URL specification to parse and apply defaults to.
     * <br>
     * @exception   MalformedURLException
     */
    public void parseURL(String inURL) throws MalformedURLException
    {
        int         fieldStart = 0;
        int         fieldEnd = 0;
        int         tmp = -1;
        int         inLen = inURL.length();
        int         baseIndex = 0;
        int         lastCharOffset = inLen - 1;

        init();

        if (0 < inLen)
        {
            // parse the URL looking for the scheme delimiter ":"
            fieldEnd = inURL.indexOf(":", fieldStart);
            tmp = inURL.indexOf("/", fieldStart);
            tmp = ((-1 == tmp) ? inLen : tmp);
            if (-1 != fieldEnd &&
                fieldEnd < tmp &&
                fieldEnd > fieldStart)
            {
                m_scheme = inURL.substring(fieldStart, fieldEnd);
                baseIndex = fieldEnd + 1;           // offset of new field delimiter
            }
            // If more??
            if (baseIndex <= lastCharOffset)
            {
                // parse the network authority with the delimiter "//"
                fieldStart = inURL.indexOf("//", baseIndex);
                if (-1 != fieldStart)
                {
                    fieldStart += 2;        // offset of new field data or delimiter
                    // Try the path delimiter
                    fieldEnd = inURL.indexOf("/", fieldStart);
                    if (-1 == fieldEnd)
                    {
                        // Try the query delimiter
                        fieldEnd = inURL.indexOf("?", fieldStart);
                    }
                    if (-1 == fieldEnd)
                    {
                        // Try the fragment delimiter
                        fieldEnd = inURL.indexOf("#", fieldStart);
                    }
                    // check the results.
                    if (-1 != fieldEnd)
                    {
                        if (fieldEnd > fieldStart)
                        {
                            m_authority = inURL.substring(fieldStart, fieldEnd);
                        }
                        baseIndex = fieldEnd;
                    }
                    else if (0 < (lastCharOffset - fieldStart))
                    {
                        // rest of line
                        m_authority = inURL.substring(fieldStart);
                        baseIndex = lastCharOffset + 1;
                    }
                    else
                    {
                        baseIndex = fieldStart;
                    }

                    // parse the authority fields.
                    if (null != m_authority && 0 < m_authority.length())
                    {
                        parseAuthority();
                    }
                }
            }
            // If more?? parse the path
            if (baseIndex <= lastCharOffset)
            {
                fieldStart = inURL.indexOf("/", baseIndex);
                if (-1 != fieldStart)
                {
                    // Try the query delimiter
                    fieldEnd = inURL.indexOf("?", fieldStart);
                    if (-1 == fieldEnd)
                    {
                        // Try the fragment delimiter.
                        fieldEnd = inURL.indexOf("#", fieldStart);
                    }
                    // check the results
                    if (-1 != fieldEnd)
                    {
                        if (fieldEnd > fieldStart)
                        {
                            m_path = inURL.substring(fieldStart, fieldEnd);
                        }
                        baseIndex = fieldEnd;
                    }
                    else if (0 < (lastCharOffset - fieldStart))
                    {
                        // rest of line
                        m_path = inURL.substring(fieldStart);
                        baseIndex = lastCharOffset + 1;
                    }
                    else
                    {
                        baseIndex = fieldStart + 1;
                    }
                }
            }
            // If more?? parse the query string(s)
            if (baseIndex <= lastCharOffset)
            {
                fieldStart = inURL.indexOf("?", baseIndex);
                if (-1 != fieldStart)
                {
                    fieldStart++;
                    // Try the fragment delimiter
                    fieldEnd = inURL.indexOf("#", fieldStart);
                    // check the results
                    if (-1 != fieldEnd)
                    {
                        if (fieldEnd > fieldStart)
                        {
                            m_query = inURL.substring(fieldStart, fieldEnd);
                            baseIndex = fieldEnd;
                        }
                        baseIndex = fieldEnd;
                    }
                    else if (0 < (lastCharOffset - fieldStart))
                    {
                        // rest of line
                        m_query = inURL.substring(fieldStart);
                        baseIndex = lastCharOffset + 1;
                    }
                    else
                    {
                        baseIndex = fieldStart + 1;
                    }
                }
            }
            // Any remainder is the fragment or path
            if (baseIndex < lastCharOffset)
            {
                if (0 == m_path.compareTo(m_defPath) &&
                    null == m_query)
                {
                    m_path += inURL.substring(baseIndex);
                }
                else
                {
                    m_fragment = inURL.substring(++baseIndex);
                }
            }
        }
    }

    /**
     * <!-- toString() -->
     * <p>Reconstruct the full URL to a String object, including the default
     * field information.  Note that this does not include any path or query
     * specific information like service names.
     * </p>
     * <br>
     * @return  String
     */
    public String toString()
    {
        StringBuffer    out = new StringBuffer(512);

        out.append(m_scheme);
        out.append("://");
        if (null != m_authorityUser)
        {
            out.append(m_authorityUser);
            if (null != m_authorityPwd)
            {
                out.append(":");
                out.append(m_authorityPwd);
            }
            out.append("@");
        }
        if (null != m_authorityHost)
        {
            out.append(m_authorityHost);
            if (null != m_authorityPort)
            {
                out.append(":");
                out.append(m_authorityPort);
            }
            else
            {
                // Since AppServer & AppServerDC aren't real protocols with
                // real defaults, force them into the URL spec.
                //
                if (m_scheme.equalsIgnoreCase(m_validSchemes[0]) ||
                    m_scheme.equalsIgnoreCase(m_validSchemes[1]) ||
                    m_scheme.equalsIgnoreCase(m_validSchemes[4]) ||
                    m_scheme.equalsIgnoreCase(m_validSchemes[5]))
                {
                    out.append(":");
                    out.append(getDefaultPort(m_scheme));
                }
            }
        }
        out.append(m_path);
        if (null != m_query)
        {
            out.append("?");
            out.append(m_query);
        }
        if (null != m_fragment)
        {
            out.append("#");
            out.append(m_fragment);
        }
        return(new String(out.toString()));
    }

    /**
     * <!-- getURL() -->
     * <p>Get a validated URL specification
     * </p>
     * <br>
     * @return  String
     * <br>
     * @exception   MalformedURLException
     */
    public  String getURL()
        throws MalformedURLException
    {
        String      returnValue = null;

        // Validate the scheme as one we support.
        //
        boolean     schemeFound = false;
        for (int i = 0; i < m_validSchemes.length; i++)
        {
            if (m_scheme.equalsIgnoreCase(m_validSchemes[i]))
            {
                schemeFound = true;
            }
        }
        if (!schemeFound)
        {
            throw new MalformedURLException("URL' scheme is not supported: " + m_authorityPort);
        }

        // Validate the port number...
        //
        int     intPort = -1;
        try
        {
            if (null != m_authorityPort)
            {
                intPort = Integer.parseInt(m_authorityPort);
            }
            else
            {
                intPort = Integer.parseInt(getDefaultPort(m_scheme));
            }
        }
        catch (Exception e)
        {
            throw new MalformedURLException("URL' port number is invalid: " + m_authorityPort);
        }

        if (2 > intPort || 65536 < intPort)
        {
            throw new MalformedURLException("URL' port number is invalid: " + m_authorityPort);
        }

        return(toString());
    }

    /**
     * <!-- getAppServerURL() -->
     * <p>Reconstruct a URL specification from the parsed URL that includes the
     * service name if one was not supplied in the original URL's path/query
     * field.
     * </p>
     * <br>
     * @param   defaultService is the service name to supply if one was not
     * in the originally parsed URL
     * <br>
     * @return  String
     * <br>
     * @exception   MalformedURLException
     */
    public String getAppServerURL(String defaultService) throws MalformedURLException
    {
        // Insert any default service.
        //
        // NOTE:  the AppServerDC scheme does not need a service name
        // so it does not need to insert any default service


        // AppServer scheme
        if (m_scheme.equalsIgnoreCase(m_validSchemes[0]) ||
            m_scheme.equalsIgnoreCase(m_validSchemes[4]))
        {
            if (0 == m_path.compareTo(m_defPath))
            {
                if (null != defaultService &&
                    0 < defaultService.length())
                {
                    m_path += defaultService;
                }
                else
                {
                    throw new MalformedURLException("Missing service name");
                }
            }

            // A path exists, but it cannot have sub paths. so validate
            // that it doesn't
            if (-1 != m_path.indexOf("/", 1))
            {
                throw new MalformedURLException("The URL's service name includes a sub-path");
            }
        }
        // http & https schemes
        else if (m_scheme.equalsIgnoreCase(m_validSchemes[2]) ||
                 m_scheme.equalsIgnoreCase(m_validSchemes[3]))
        {
            // check to see that the query holds a valid service spec.
            // An appserver URL can only have 1 (due to AIA coding) query
            // element.
            String  existing = getService();
            if (null == existing)
            {
                // No service, this will remove all non-service query elements.
                //
                setService(null);
            }
            else
            {
                // This will serve to remove all query elements except for
                // the AppService one.
                //
                setService(existing);
            }
            if (null == m_query)
            {
                if (null != defaultService &&
                    0 < defaultService.length())
                {
                    if (-1 != defaultService.indexOf("/", 1))
                    {
                        throw new MalformedURLException("The URL's service name includes a sub-path");
                    }
                    m_query = "AppService=" + defaultService;
                }
                else
                {
                	if (! m_path.endsWith("/apsv"))
                    throw new MalformedURLException("Missing service name");
                }
            }
            else
            {
            }

            // Validate the port number...
            //
            int     intPort = -1;
            try
            {
                if (null != m_authorityPort)
                {
                    intPort = Integer.parseInt(m_authorityPort);
                }
                else
                {
                    intPort = Integer.parseInt(getDefaultPort(m_scheme));
                }
            }
            catch (Exception e)
            {
                throw new MalformedURLException("URL' port number is invalid: " + m_authorityPort);
            }

            if (2 > intPort || 65536 < intPort)
            {
                throw new MalformedURLException("URL' port number is invalid: " + m_authorityPort);
            }

            // cannot have a default path.
            if (0 == m_path.compareTo(m_defPath))
            {
                throw new MalformedURLException("The URL is missing the AIA adapter path");
            }

        }

        // If we get a scheme that is different than AppServer, AppServerDC,
        // http, or https then it is an unsupported scheme

        // NOTE:  the AppServerDC scheme does not need a service name
        // so it does not need to insert any default service

        else if( !(m_scheme.equalsIgnoreCase(m_validSchemes[1]) ||
                   m_scheme.equalsIgnoreCase(m_validSchemes[5]) ||
                   m_scheme.equalsIgnoreCase(m_validSchemes[6])))
        {
            throw new MalformedURLException("Unsupported network scheme: " + m_scheme);
        }

        return(toString());
    }

    /**
     * <!-- getDefaultPort() -->
     * <p>Get the default port for the specified scheme.
     * </p>
     * <br>
     * @param scheme    is a String holding the scheme name.  Note that the
     * scheme delimiter character (colon) must not be included
     * <br>
     * @return  String May return empty if an invalid scheme is provided.
     */
    public String    getDefaultPort(String scheme)
    {
        String  defPort = "";
        if (null != scheme && 0 < scheme.length())
        {
            for (int i = 0; i < m_validSchemes.length; i++)
            {
                if (scheme.equalsIgnoreCase(m_validSchemes[i]))
                {
                    defPort = m_defPorts[i];
                    break;
                }
            }
        }
        return(defPort);
    }

    /*
     * Run method.
     */
    public void run(String[] args) throws Exception
    {
        if (1 > args.length)
        {
            System.out.println("usage: PscURLParser <url> [def_service]");
        }
        else
        {
            parseURL(args[0]);

            System.out.println("Results:");
            System.out.println("    scheme:    " + getScheme());
            System.out.println("    authority: " + getAuthority());
            if (null != m_authority)
            {
                if (null != m_authorityUser)
                {
                    System.out.println("        user info:     ");
                    System.out.println("            uid: " + getUserId());
                    System.out.println("            pwd: " + getUserPassword());
                }
                if (null != m_authorityHost)
                {
                    System.out.println("        net addr: ");
                    System.out.println("            host: " + getHost());
                    System.out.println("            port: " + Integer.toString(getPort()));
                }
            }
            System.out.println("    path:      " + getPath());
            System.out.println("    query:     " + getQuery());
            System.out.println("    fragment:  " + getFragment());

            System.out.println("toString(): " + toString());
            System.out.println("getURL(): " + getURL());
            System.out.println("Resulting AppServer URL: " + getAppServerURL((1 < args.length) ? args[1] : null));
            System.out.println("Service: " + getService());
            setService(null);
            System.out.println("Remove Service: " + getURL());
            setService("newService");
            System.out.println("set Service: " + getURL());
        }
    }

    public static void main(String[] args)
    {
        PscURLParser THIS = new PscURLParser();
        try
        {
            try
            {
                THIS.run(args);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        catch(Exception e)
        {
            System.out.println("Error: " + e.toString());
        }
    }

    /*
     * <!-- init() -->
     * <p>Initialize the class data.
     * </p>
     */
    protected void      init()
    {
        m_scheme = m_validSchemes[0];
        m_authority = m_defHost;
        m_authorityUser = null;
        m_authorityPwd = "";
        m_authorityHost = m_defHost;
        m_authorityPort = null;
        m_path = m_defPath;
        m_query = null;
        m_fragment = null;
    }

    /*
     * <!-- parseAuthority() -->
     * <p>Parse the authority field into user-info and network address.  Then
     * parse those sub-fields into user-id/password and host/port respectively.
     * </p>
     */
    protected void parseAuthority() throws MalformedURLException
    {
        int         delim = m_authority.indexOf("@");
        String      authorityUserPwd = null;
        String      authorityHostPort = null;

        if (-1 != delim)
        {
            authorityUserPwd = m_authority.substring(0, delim);
            authorityHostPort = m_authority.substring(++delim);
        }
        else
        {
            authorityHostPort = m_authority;
        }

        if (null != authorityUserPwd)
        {
            delim = authorityUserPwd.lastIndexOf(":");
            if (-1 != delim)
            {
                m_authorityUser = authorityUserPwd.substring(0, delim);
                m_authorityPwd = authorityUserPwd.substring(++delim);
            }
            else
            {
                m_authorityUser = authorityUserPwd;
            }
        }

        if (null != authorityHostPort)
        {
            /* must check for a literal IPv6 address first */
            if (authorityHostPort.charAt(0) == '[')
            {
                int endaddr;

                /* find  matching brace */
                endaddr = authorityHostPort.indexOf("]");
                if (endaddr == -1)
                {
                    throw new MalformedURLException("Invalid IPv6 literal address: " + authorityHostPort);
                }
                m_authorityHost = authorityHostPort.substring(1, endaddr++);
                if (authorityHostPort.length() > endaddr)
                {
                    if (authorityHostPort.charAt(endaddr) != ':')
                    {
                        throw new MalformedURLException("Invalid IPv6 literal address: " + authorityHostPort);
                    }
                    endaddr++;
                    m_authorityPort = parseAuthorityPort(
                                         authorityHostPort.substring(endaddr));
                }
            }
            else if ((delim = authorityHostPort.lastIndexOf(":")) != -1)
                {
                    m_authorityHost = authorityHostPort.substring(0, delim);
                    m_authorityPort = parseAuthorityPort(
                                         authorityHostPort.substring(++delim));
                }
            else
            {
                m_authorityHost = authorityHostPort;
            }
        }
    }

    private String parseAuthorityPort(String portString)
        throws MalformedURLException
    {
        String sPort = portString;

        // if the default port is supplied, null it to indicate we are
        // not to return this value in toString().
        //
        if (0 == sPort.compareTo(getDefaultPort(m_scheme)))
        {
            sPort = null;
        }
        else
        {
            int tmp = 0;
            try
            {
                tmp = Integer.parseInt(sPort);
            }
            catch (Exception e)
            {
                throw new MalformedURLException("Invalid numeric port number: " + m_authorityPort);
            }
            // Check the value range.  It must be > 2 (stderr) and < 65536
            if (2 >= tmp || 65536 < tmp)
            {
                throw new MalformedURLException("Invalid numeric port number: " + m_authorityPort);
            }
        }

        return sPort;
    }


    /**
     * Determine whether this is a protocol that uses the name
     * server or directly connects to the AppServer.
     *
     * @return true if direct connect, false otherwise
     */
    public boolean isDirectConnect()
    {
        if (m_scheme.equalsIgnoreCase(m_validSchemes[1]) ||
            m_scheme.equalsIgnoreCase(m_validSchemes[2]) ||
            m_scheme.equalsIgnoreCase(m_validSchemes[3]) ||
            m_scheme.equalsIgnoreCase(m_validSchemes[5]) ||
            m_scheme.equalsIgnoreCase(m_validSchemes[6]))
        {
            return true;
        }
        else
        {
            return false;
        }    
    }

    /**
     * Determine whether this is a protocol that uses SSL
     * to connect to the AppServer.
     *
     * @return true if SSL connect, false otherwise
     */
    public boolean isSSLConnect()
    {
        if (m_scheme.equalsIgnoreCase(m_validSchemes[4]) ||
            m_scheme.equalsIgnoreCase(m_validSchemes[5]))
        {
            return true;
        }
        else
        {
            return false;
        }    
    }
}

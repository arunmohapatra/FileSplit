/*
/*
/* <p>Copyright 2000-2004 Progress Software Corportation, All rights reserved.</p>
/* <br>
/* <p>Class:        SocketConnectionInfoEx  </p>
/* <br>
/* All rights reserved.  No part of this program or document
/* may be  reproduced in  any form  or by  any means without
/* permission  in  writing  from  Progress Software Corporation.
*/


package com.progress.ubroker.util;

import java.net.MalformedURLException;

import com.progress.common.util.PscURLParser;

/**
 * The SocketConnectionInfo class is used to validate and parse a Progress
 * URL specification into it's individual parts.  The URL is parsed
 * according to RFC 2396.
 *<br>
 *<code><protocol>://[<userid>[:<password>]@]<host>[:<port>][/<path>][?<query>=<value>[;...]][#<reference>]</code>
 *<br>
 */
public class SocketConnectionInfoEx extends PscURLParser
{

    /*********************************************************************/
    /* Constants                                                         */
    /*********************************************************************/

    /** The default host URL parameter. */
    public static final String DEF_HOST = "localhost";

    /** The String names of the protocols handled by this class. */
    public static final String[] PROTOCOL_NAMES =
    {
        "file",
        "appserver",
        "http",
        "https",
        "appserverDC",
        "AppServerS",
        "AppServerDCS",
    };

    /* The integer protocol type carried by this instance. They just so happen
     * to double as indexes into the PROTOCOL_NAMES String array. */
    /** The file:// protocol */
    public static final int FILE_PROTOCOL = 0;
    /** The appserver:// protocol */
    public static final int APPSERVER_PROTOCOL = 1;
    /** The http:// protocol */
    public static final int HTTP_PROTOCOL = 2;
    /** The https:// protocol */
    public static final int HTTPS_PROTOCOL = 3;
    /** The appserverDC:// protocol */
    public static final int APPSERVERDC_PROTOCOL = 4;
    /** The AppServerS:// protocol */
    public static final int APPSERVERS_PROTOCOL = 5;
    /** The AppServerDCS:// protocol */
    public static final int APPSERVERDCS_PROTOCOL = 6;
        

    /** If a protocol is not specified, it is "file". */
    public static final String DEF_PROTOCOL = "AppServer";
    /** The default TCP port number used for Name Server installations */
    public static final int DEF_PORT_NAMESERVER = 5162;
    /** The default TCP port number used for AppServer installations */
    public static final int DEF_PORT_APPSERVER = 3090;
    /** The default TCP port number used for WebSpeed server installations */
    public static final int DEF_PORT_WEBSPEED = 3090;
    /** The default HTTP port number */
    public static final int DEF_PORT_HTTP = 80;
    /** The default HTTP/S port number */
    public static final int DEF_PORT_HTTPS = 443;

    /*********************************************************************/
    /* Static Data                                                       */
    /*********************************************************************/

    /*********************************************************************/
    /* Instance Data                                                     */
    /*********************************************************************/

    // private String url;

    /*********************************************************************/
    /* Constructors                                                      */
    /*********************************************************************/

    public SocketConnectionInfoEx(String url)
    throws MalformedURLException
    {
        // Parse the user supplied URL to apply the defaults.
        //
        super.parseURL(url);

    }

    /*********************************************************************/
    /* Public Methods                                                    */
    /*********************************************************************/

    /*********************************************************************/
    /*                                                                   */
    /*********************************************************************/
    public void setProtocolType(int protocol)
    {
        if (protocol < PROTOCOL_NAMES.length)
        {
            super.setScheme(PROTOCOL_NAMES[protocol]);
        }
    }

    /*********************************************************************/
    /*                                                                   */
    /*********************************************************************/
    public void setPort(int port)
    {
        super.setPort(Integer.toString(port));
    }

    /*********************************************************************/
    /*                                                                   */
    /*********************************************************************/
    public void setProtocol(String protocol)
    {
        super.setScheme(protocol);
    }

    /*********************************************************************/
    /*                                                                   */
    /*********************************************************************/
    public String getProtocol()
    {
        return(super.getScheme());
    }

    /*********************************************************************/
    /*                                                                   */
    /*********************************************************************/
    public int    getProtocolType()
    {
        String      urlProtocol = super.getScheme();
        int         returnValue = -1;
        for (int i = 0; i < PROTOCOL_NAMES.length ; i++)
        {
            if (urlProtocol.equalsIgnoreCase(PROTOCOL_NAMES[i]))
            {
                returnValue = i;
                break;
            }
        }
        return returnValue;
    }

}   /* end SocketConnectionInfo */

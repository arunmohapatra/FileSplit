
/*************************************************************/
/* Copyright (c) 1984-1996 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation. */
/*************************************************************/
/*
 * $Id: SocketConnectionInfo.java,v 1.3 1998/03/31 16:35:12 davec Exp $
 */

/*********************************************************************/
/* Module : SocketConnectionInfo                                     */
/*                                                                   */
/*                                                                   */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.util;

import java.io.*;
import java.net.*;


/*********************************************************************/
/*                                                                   */
/* Class SocketConnectionInfo                                        */
/*                                                                   */
/*********************************************************************/

public class SocketConnectionInfo
{

/*********************************************************************/
/* Constants                                                         */
/*********************************************************************/

public static final String DEF_HOST = "localhost";
/*
public static final String DEF_PROTOCOL = "appserver";
*/
public static final String DEF_PROTOCOL = "file";
public static final int DEF_PORT_NAMESERVER = 5162;
public static final int DEF_PORT_APPSERVER = 3090;
public static final int DEF_PORT_WEBSPEED = 3090;

/*********************************************************************/
/* Static Data                                                       */
/*********************************************************************/

/*********************************************************************/
/* Instance Data                                                     */
/*********************************************************************/

private String url;
private String host;
private String service;
private String protocol;
private int    port;

/*********************************************************************/
/* Constructors                                                      */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public SocketConnectionInfo(String url)
    throws MalformedURLException
    {
    URL tmpUrl;
	String tmpService;

    tmpUrl = new URL(url);

    this.protocol = DEF_PROTOCOL;
    this.host = tmpUrl.getHost();
	if(this.host.equals(""))
		this.host = DEF_HOST;

	tmpService = tmpUrl.getFile();

    /* allow an empty service name */
/*
	if (tmpService.equals("/"))
		throw new MalformedURLException("Missing service name");
*/
	this.service = tmpService.substring(1);

    this.port = tmpUrl.getPort();
	if(this.port == -1)
		throw new MalformedURLException("Port number required");
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public SocketConnectionInfo(String url, int defPort)
    throws MalformedURLException
    {
    URL tmpUrl;
	String tmpService;

    tmpUrl = new URL(url);

    this.protocol = DEF_PROTOCOL;
    this.host = tmpUrl.getHost();
	if(this.host.equals(""))
		this.host = DEF_HOST;
    
	tmpService = tmpUrl.getFile();
	if (tmpService.length() == 0 || tmpService.equals("/"))
		throw new MalformedURLException("Missing service name");
	this.service = tmpService.substring(1);

    this.port = tmpUrl.getPort();
	if (this.port == -1)
		this.port = defPort;
    }


/*********************************************************************/
/* Public Methods                                                    */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public void setHost(String host)
    {
    this.host = host;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public void setService(String service)
    {
    this.service = service;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public void setProtocol(String protocol)
    {
    this.protocol = protocol;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public void setPort(int port)
    {
    this.port = port;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public String getHost()
    {
    return host;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public String getService()
    {
    return service;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public String getProtocol()
    {
    return protocol;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public int getPort()
    {
    return port;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public String getUrl()
    {
    return protocol + "://" + host + ":" + port + "/" + service;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/


}	/* end SocketConnectionInfo */

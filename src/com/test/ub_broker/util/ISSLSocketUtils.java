/*
** ISSLSocketUtils.java
** This interface is used to interface to a vendor specific SSL socket 
** managemetn utility.  It is used in conjunction with the ISSLParams interface
** to remove vendor specific SSL code in creating a SSL enabled Socket object 
*  and institute the opening SSL handshake operation.
**
** Copyright (C) 2002-2004,  Progress Software Corp.
**
** This library is free software; you can redistribute it and/or modify it 
** under the terms of the GNU Lesser Public License as published by the Free
** Software Foundation; either version 2 of the License, or (at your option)
** any later version.
**
** This library is distributed in the hope that it will be useful, but 
** WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
** or FITNESS FOR A PARTICULAR PURPOSE.  See the BNU Lesser General Public
** License for more details.  
**
** You should have received a copy of the GNU Lesser Public License along with
** this library; if not, write to the Free Software Foundation, Inc., 59
** Temple Place, Suite 330, Boston, MA 02111-1307 USA
**
*/

package com.progress.ubroker.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * This interface provides a collection of SSLSocket related operation.  It
 * allows all these SSL specific operations to be dynamically loaded and
 * executed by implementation classes without requiring the actuall SSL support
 * classes to be present in the base support component.
 */
public interface ISSLSocketUtils
{
    /* Embedded interfaces. */
    /**
     * The PeerCertificateInfo holds high level digital certificate info
     * for the peer SSL connection.  On a client this would be one of the
     * server's certificates, on a server it would be one of the client's
     * certificates.  It gives a base line of information about the certificates
     * serial number, subject DN, issuer DN, from date and to date.
     */
    public interface IPeerCertificateInfo
    {
        public String getSerialNumber();
        public String getSubjectName();
        public String getAltSubjectName();
        public String getIssuerName();
        public String getFromDate();
        public String getToDate();
        public byte[] getSignature();
    }



    /**
     * The SSLInfo class is a container that holds SSL related information
     * for the connection.
     */
    public interface ISSLInfo
    {
        public  String getCipherSuite();
        public  String getSessionId();
        public  int    getVersion();
        public  String getPeerName();
        public  ISSLSocketUtils.IPeerCertificateInfo[] getPeerCertificateInfo();
    }


    /*
     * CLASS Constants
     */
     /** <description> */
     // private static final <type>  <name> = <value>;

    /*
     * CLASS Properties.
     */
     /** <description> */
     // public static        <type>  <name> = <value>;

    /*
     * Super Object Properties.
     */
     /** <description> */
     //  protected       <type>          <name> = <value>;

    /*
     * Object Instance Properties.
     */
     // private         <type>          <name> = <value>;

    /*
     * ACCESSOR METHODS:
     */

    /*
     * PUBLIC METHODS:
     */
    public Socket createSSLSocket(InetAddress address,
                                  int         port,
                                  ISSLParams  sslParams) throws IOException, UnknownHostException;

    public Socket createSSLSocket(Socket      socket,
                                  ISSLParams  sslParams) throws IOException;

    public Socket createSSLSocket(Socket      socket,
                                  ISSLParams  sslParams,
                                  boolean     server) throws IOException;

    public Socket createSSLSocket(String      host,
                                  int         port,
                                  ISSLParams  sslParams) throws IOException;

    /**
     * <!-- getSocketSSLInfo() -->
     * <p>Get an high level block of informat regarding the SSL connection.
     * </p>
     * <br>
     * @return  SSLInfo (may contain null information fields if this is not
     * an SSL socket)
     * <br>
     * @exception   IOException
     */
    public ISSLSocketUtils.ISSLInfo getSocketSSLInfo(Socket sock) throws IOException;

    /*
     * PROTECTED (SUPER) METHODS:
     */

    /*
     * PRIVATE METHODS:
     */

}

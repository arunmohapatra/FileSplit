/*
/* <p>Copyright 2000-2004 Progress Software Corportation, All rights reserved.</p>
/* <br>
/* <p>Class:        SSLSocketUtilsStub    </p>
/* <br>
/* All rights reserved.  No part of this program or document
/* may be  reproduced in  any form  or by  any means without
/* permission  in  writing  from  Data Language Corporation.
/ *
*/

package com.progress.ubroker.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * This module provides the ISSLSocketUtils interface implementation for
 * doing nothing.  A basic stub class.
 *
 */
public class SSLSocketUtilsStub implements ISSLSocketUtils
{
    /* Embedded classes . */

    /*
     * The PeerCertificateInfo holds high level digital certificate info
     * for the peer SSL connection.  On a client this would be one of the
     * server's certificates, on a server it would be one of the client's
     * certificates.  It gives a base line of information about the certificates
     * serial number, subject DN, issuer DN, from date and to date.
     */
    public class PeerCertificateInfo implements ISSLSocketUtils.IPeerCertificateInfo
    {
        public PeerCertificateInfo() { }
        public String getSerialNumber() { return(""); }
        public String getSubjectName() { return(""); }
        public String getIssuerName() { return(""); }
        public String getFromDate() { return(""); }
        public String getToDate() { return(""); }
        public byte[] getSignature() { return null; }
		public String getAltSubjectName() { return null; }
    }



    /*
     * The SSLInfo class is a container that holds SSL related information
     * for the connection.
     */
    public class SSLInfo implements ISSLSocketUtils.ISSLInfo
    {
        public  SSLInfo() { }
        public  String getCipherSuite() { return(""); }
        public  String getSessionId() { return(""); }
        public  int    getVersion() { return(0); }
        public  String getPeerName() { return(""); }
        public  ISSLSocketUtils.IPeerCertificateInfo[] getPeerCertificateInfo() { return(null); }
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
     * Constructors...
     */

    /**
    * <!-- SSLSocketUtilsStub() -->
    * <p>The default class constructor.
    * </p>
    * <br>
    *return   void
    */
    public SSLSocketUtilsStub()
    {
    }

    /*
     * ACCESSOR METHODS:
     */

    /*
     * PUBLIC METHODS:
     */
    /*
     * ACCESSOR METHODS:
     */

    /*
     * PUBLIC METHODS:
     */
    public Socket createSSLSocket(InetAddress address,
                                  int         port,
                                  ISSLParams  sslParams) throws IOException, UnknownHostException
    {
        throw new UnsupportedOperationException("SSL operations are not supported in this component version");
    }

    public Socket createSSLSocket(Socket      socket,
                                  ISSLParams  sslParams) throws IOException
    {
        throw new UnsupportedOperationException("SSL operations are not supported in this component version");
    }

    public Socket createSSLSocket(Socket      socket,
                                  ISSLParams  sslParams,
                                  boolean     server)
    {
        throw new UnsupportedOperationException("SSL operations are not supported in this component version");
    }

    public Socket createSSLSocket(String      host,
                                  int         port,
                                  ISSLParams  sslParams) throws IOException
    {
        throw new UnsupportedOperationException("SSL operations are not supported in this component version");
    }

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
    public ISSLSocketUtils.ISSLInfo getSocketSSLInfo(Socket sock) throws IOException
    {
        throw new IOException("SSL operations are not supported in this component version");
    }

    /*
     * PROTECTED (SUPER) METHODS:
     */

    /*
     * PRIVATE METHODS:
     */

}
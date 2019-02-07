/*
/* <p>Copyright 2000-2005 Progress Software Corportation, All rights reserved.</p>
/* <br>
/* <p>Class:        HttpClientProtocol  </p>
/* <br>
/* All rights reserved.  No part of this program or document
/* may be  reproduced in  any form  or by  any means without
/* permission  in  writing  from  Data Language Corporation.
/*
*/

package com.progress.ubroker.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import HTTPClient.AuthorizationInfo;
import HTTPClient.Codecs;
import HTTPClient.HTTPConnection;
import HTTPClient.HTTPResponse;
import HTTPClient.NVPair;

import com.progress.common.ehnlog.IAppLogger;
import com.progress.open4gl.dynamicapi.IPoolProps;
import com.progress.ubroker.util.INetworkProtocol;
import com.progress.ubroker.util.IubMsgInputStream;
import com.progress.ubroker.util.IubMsgOutputStream;
import com.progress.ubroker.util.NetworkProtocolException;
import com.progress.ubroker.util.SocketConnectionInfoEx;
import com.progress.ubroker.util.ubMsg;

/**
 * <p>
 * The HttpClientProtocol class is a network protocol handler that enables the
 * AppServer network protocol to be tunneled through the HTTP protocol to an
 * AppServer via a AIA Java servlet running on a HTTP server.
 * <p>
 * It provides the standard INetworkProtocol interface through its parent class
 * NetworkClient Protocol.  It works in conjunction with the HttpClientMsgInputStream
 * and HttpClientMsgOutputStream classes to provide the Java like InputStream
 * and OutputStream IO interfaces.  This class extends specific public interfaces
 * to the stream classes that are for the HTTP tunneling operation only.
 * <p>
 * The class accepts input properties through the parent class's initialization
 * with the NetworkProtocolOptions class.  All options that are used through
 * that mechanism will use the prefix <code>psc.http.</code>.  The specific
 * property options used are:
 * <ul>
 * <li>psc.http.proxy=host:port Connect through a proxy HTTP server.  The
 * default is to not use a proxy server.
 * <li>psc.http.proxy.auth=[realm:]userid:password The realm is optional but
 * recommended to respond to proxy authentication required status returns.  This
 * option is used only if the psc.http.proxy option is set.  The default is
 * to not use proxy HTTP server authentication.
 * <li>psc.http.timeout=seconds The number of seconds to wait for a response
 * from an AIA servlet.  The default is 180 (3 minutes).
 * <li>psc.http.auth=[realm:]userid:password The HTTP Basic authentication user
 * id and password to send to the HTTP server hosting the AIA servlet.  The
 * default is to not use any authentication.  The realm portion is optional,
 * but recommended if used.
 * </ul>
 * <p>
 * The properties may be set through the system properties object or directly
 * through the NetworkProtocolOptions class.  Note that the options will be
 * polled only at the time the openConnection() method is called.
 * <p>
 * The protocol handler will support ONE HTTP proxy server and optionally its
 * Basic authentication.  If the Basic authentication realm is not known, the
 * class attempts to pre-emtively send the credentials to every URL based on
 * the server.  The same condition exists for sending the credentials to the
 * HTTP server.  If the realm is known, it will be used.  Otherwise it will
 * attempt to send the authentication credentials to every URL on the HTTP
 * server.
 * <p>
 * The class will ignore proxy server connections when the url is on of
 * localhost or 127.0.0.1 (localhost).
 * <p>
 * Cookie handling is explicitly disabled.  It may be re-enabled at a later
 * time by commenting out the line that removes the cookie handler.   NOTE: it
 * will probably take additional property options to handle cookie stores.
 *
 *
 */
public class HttpClientProtocol extends NetworkClientProtocol
{
    /*
     * CLASS Constants
     * private static final <type>  <name> = <value>;
     */
    /* The HTTP header that provides the AIA connection *handle* */
    protected static final    String  CONNECT_ID_IN_HEADER = "CONNHDL";

    /* Default response timeout: infinity */
    protected static final    String  HTTP_DEFAULT_TIMEOUT = "0";

    protected static final    String  AIA_MSG_ID = "AIAMSGID";

    protected static final    String  CCID_MSG_ID = "X-CLIENT-CONTEXT-ID";

    /*
     * CLASS Properties.
     * public static        <type>  <name> = <value>
     */
    public  static final    int     HTTP_STATE_IDLE = 0;
    public  static final    int     HTTP_STATE_GET = 1;
    public  static final    int     HTTP_STATE_POST = 2;
    public  static final    int     HTTP_STATE_GET_RESPONSE_HEADERS = 3;
    public  static final    int     HTTP_STATE_GET_RESPONSE_DATA = 4;

    /*
     * Super Object Properties.
     *  protected       <type>          <name> = <value>;
     */
    /* One time initialization of the HTTPClient for setting module lists, etc. */
    protected   static  boolean                 m_HTTPClientInitialized = false;

    /* Instance data... */
    protected           HTTPConnection          m_httpClient = null;
    protected           HttpClientMsgInputStream       m_msgInputStream = null;
    protected           HttpClientMsgOutputStream      m_msgOutputStream = null;
    protected           int                     m_msgInputStreamRefCount = 0;
    protected           int                     m_msgOutputStreamRefCount = 0;
    protected           SocketConnectionInfoEx  m_connectURL = null;
    protected           Vector                  m_ubMsgResponses = new Vector();
    protected           String                  m_aiaConnectionId = null;
    protected           AuthorizationInfo       m_serverAuthObject = null;
    protected           Vector                  m_proxyAuthObjects = new Vector();
    protected           long                    m_aiaMsgId = 0;
    protected           long                    m_aiaStopMsgId = -1;
    protected           String                  m_connectionIdQuery = null;
    protected           String                  m_userId = null;
    protected           String                  m_password = null;
    protected           boolean                 m_logicalConnectionOpen = false;
    protected           int                     m_httpState = HTTP_STATE_IDLE;
    protected           Thread                  m_httpThread = null;

    /*
     * Inner class to collect all of the information for a particular
     * ubMessage exchange.  Treat this a a simple public structure since
     * it can only be used by this class and its children.
     *
     * We will keep a stack of these things in the object's m_ubMsgResponses
     * variable.
     */
    protected class UBMessageResponse
    {
        public              HTTPResponse            m_ubMsgResponse = null;
        public              int                     m_sendMsgResponseCode = -1;
        public              int                     m_sendMsgExceptionMsgCode = -1;
        public              Object[]                m_sendMsgExceptionErrorDetail = null;
        public              String                  m_httpOperationName = null;
        public              long                    m_aiaMsgId = 0;
        public              boolean                 m_queResponse = true;
        public              int                     m_ubMsgRequestCode = 0;
        //public              byte[]                  m_ubResponseData = null;
        public              InputStream             m_httpInputStream = null;

        public UBMessageResponse()
        {
        };

        public UBMessageResponse(HTTPResponse msgResponse)
        {
           m_ubMsgResponse = msgResponse;
        }

        protected void finalize() throws Throwable
        {
            if (null != m_httpInputStream)
            {
                m_httpInputStream.close();
                m_httpInputStream = null;
            }
            if (null != m_ubMsgResponse)
            {
                m_ubMsgResponse = null;
            }
            // m_ubResponseData = null;
        }
    }

    /*
     * Object Instance Properties.
     *  private         <type>          <name> = <value>;
     */

    /*
     * An inner class who's purpose is to perform an out-of-band POST operation
     * to send SETSTOP to the AIA in the event of a LONG...... response data
     * fetch.
     *
     */
    public class SendStopThread implements Runnable
    {
        private     HTTPConnection              m_httpConnection = null;
        private     HttpClientMsgOutputStream   m_msgOutputStream = null;
        private     long                        m_aiaMsgId = -1;
        private     HttpClientProtocol          m_httpClient = null;

        public SendStopThread(HttpClientProtocol           httpClient,
                       HTTPConnection               stopHttpObject,
                       HttpClientMsgOutputStream    msgOutputStream,
                       long                         stopMsgId)
        {
            m_httpClient = httpClient;
            m_httpConnection = stopHttpObject;
            m_msgOutputStream = msgOutputStream;
            m_aiaMsgId = stopMsgId;
        }

        public void run()
        {
/*****
            m_httpClient.loggingObject().LogMsgln(m_httpClient.loggingDestination(),
                                                  Logger.LOGGING_DEBUG,
                                                  Logger.TIMESTAMP,
                                                  "Send Stop Thrad sending stop message.");
******/
            UBMessageResponse ubResponse = null;

            try
            {
                ubResponse = m_httpClient.processHttpRequest(m_httpConnection,
                                                             m_msgOutputStream,
                                                             "stop",
                                                             m_aiaMsgId);
            }
            catch (Exception e)
            {
                // Pass this type right on through.
                //
/*****
                m_httpClient.loggingObject().LogMsgln(m_httpClient.loggingDestination(),
                                                      Logger.LOGGING_DEBUG,
                                                      Logger.TIMESTAMP,
                                                      "Send Stop Thread detected an I/O error posting (write) message: " +
                                                      e.getMessage());
*******/
            }
            finally
            {
                if (null != ubResponse)
                {
                    try
                    {
                        // Close out the stream, we're not interested in the response.
                        ubResponse.m_httpInputStream.close();
                        ubResponse.m_httpInputStream = null;
                        ubResponse.m_ubMsgResponse = null;
                        ubResponse = null;
                    }
                    catch (Exception e)
                    {
/*******
                        m_httpClient.loggingObject().LogMsgln(m_httpClient.loggingDestination(),
                                                              Logger.LOGGING_DEBUG,
                                                              Logger.TIMESTAMP,
                                                              "Send Stop Thread detected an I/O error closing response: " +
                                                              e.getMessage());
********/
                    }
                }
            }
        }
    }



    /*
     * Constructors...
     */

    /**
    * <!-- HttpClientProtocol() -->
    * <p>The default class constructor.
    * </p>
    * <br>
    */
    public HttpClientProtocol()
    {
        // Set the protocol specific base class properties.
        //
        m_protocolType = INetworkProtocol.PROTOCOL_HTTP_TUNNEL;
        m_protocolTypeName = INetworkProtocol.m_protocolTypeNames[INetworkProtocol.PROTOCOL_HTTP_TUNNEL];

        // Create and init the basic HTTPClient package.
        //
    }

    /*
     * Final cleanup.
     */
    protected void finalize() throws Throwable
    {
        // auto release the object.
        try
        {
            release();
        }
        catch (Exception e)
        {
        }
    }

    /*
     * ACCESSOR METHODS:
     */

    /*
     * PUBLIC METHODS:
     */
    /**
     * <!-- init() -->
     * <p>Initialize [and connect] the network protocol handling class.
     * </p>
     * <br>
     * @param protocolOptions a Properties object with protocol specific
     * initialization options.  See the documentation for the specific protocol
     * class type for a list of property names and their value ranges.
     * @param loggingObject a AppLogger object to use in tracing operations, errors
     * and 3rd party exceptions.
     * @param loggingDestingation an int that holds a AppLogger.DEST_XXXXXX value
     * that sets the destination for logging.
     * <br>
     * @return  void
     * <br>
     * @exception   Exception
     */
    public synchronized void     init(Properties             protocolOptions,
                                      IAppLogger             loggingObject,
                                      int                    loggingDestination) throws Exception
    {
        super.init(protocolOptions, loggingObject, loggingDestination);

        // do the one-time HTTPClient intitialization.
        //
        if (!m_HTTPClientInitialized)
        {
            // Add/remove any basic modules.

            // Don't do this again.
            m_HTTPClientInitialized = true;
        }

    }

    /**
     * <!-- release() -->
     * <p>Release all of the dynamic resources created by the network protocol
     * handler class during its init() operation.  The class is no longer
     * usable uless init() is called again.
     * </p>
     * <br>
     * @return  void
     * <br>
     * @exception   Exception
     */
    public void release() throws Exception
    {
        if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
            m_loggingObj.logBasic(  m_debugLogIndex,
                                   "Releasing (closing) HTTPClientProtocol.");

        // Close the physical connection
        //
        try
        {
            closeHttpConnection(m_httpClient, this);
        }
        catch (Exception e)
        {
        }
        finally
        {
            m_httpClient = null;
        }

        // Close the logical connection
        //
        closeConnection(true);
    }

    /**
     * <!-- resolveConnectionInfo() -->
     * <p> Use the Name Server, or not, to resolve the AppServer's uBroker IP
     * address and port.
     * </p>
     * <br>
     * @param connectInfo a SocketConnectionInfoEx object that will contain the
     * resolved AppServer IP connection information.
     * <br>
     * @return  void
     * <br>
     * @exception   Exception
     */
    public void resolveConnectionInfo(SocketConnectionInfoEx connectInfo) throws Exception
    {
        // The HTTP url is the absolute address, no NameServer is ever involved.

        // verify that the protocol is "http".
        //
        if (!connectInfo.getProtocol().equalsIgnoreCase("http") &&
            !connectInfo.getProtocol().equalsIgnoreCase("http"))
        {
            throw new MalformedURLException("The URL's network protocol must be http");
        }
    }


    /**
     * <!-- openConnection() -->
     * <p> Establish a network layer communications link to an AppServer.
     * This may involve createing a physical network link, depending upon the
     * protocol being used. It may establish a socket for raw TCP/IP
     * connections, setup a HTTP proxy server, or do nothing depending upon
     * the protocol being used.
     * </p>
     * <br>
     * @param connectInfo a SocketConnectionInfoEx object that will contain the
     * AppServer's uBroker or applicatoin server process IP connection
     * information.
     * @param progressServerType an int that specifies the expected Progress
     * server type we will establish a connection to.  NOTE: this is not
     * used in HTTP tunneling.
     * @param connectionOptions a Properties object with connection specific
     * initialization options.  See the documentation for the specific protocol
     * class type for a list of property names and their value ranges.
     * @param userId a String object holding the user credentials that are
     * required by the network protocol to establish a communications channel.
     * This may be for a Web Server's basic authentication for HTTP tunneling,
     * or a private key file specification for SSL, or unused for raw TCP/IP
     * communications.
     * @param password a String object holding the password to use in
     * authenticating the client to the network communications channel.  This
     * may be the password for a Web Server's basic authentication, a private key
     * file password, or unused for raw TCP/IP communications.
     * <br>
     * @return  void
     * <br>
     * @exception   Exception
     */
    public synchronized void openConnection(SocketConnectionInfoEx   connectInfo,
                                            int                      progressServerType,
                                            Properties               connectionOptions,
                                            Object                   userId,
                                            String                   password) throws Exception
    {

        if (null != m_aiaConnectionId)
        {
            throw new NetworkProtocolException(NetworkProtocolException.BASE_MSG,
                                               connectInfo.getProtocol(),
                                               "An AppServer connection already exists.");
        }

        // validate the URL information and verify that the protocol is "http".
        //
        if (!connectInfo.getProtocol().equalsIgnoreCase("http") &&
            !connectInfo.getProtocol().equalsIgnoreCase("https"))
        {
            throw new MalformedURLException("The URL's network protocol must be http or https");
        }

        // Make a copy of the connection URL.
        //
        m_connectURL = connectInfo;

        // If local instance specific options are supplied, add them to the
        // local set.
        if (null != connectionOptions)
        {
            Enumeration enum1 = connectionOptions.propertyNames();
            while (enum1.hasMoreElements())
            {
                String  key = (String)enum1.nextElement();
                String  value = connectionOptions.getProperty(key);
                if (null != value)
                {
                    m_protocolProperties.put(key, value);
                }
            }
        }


        // save a copy of th euser id and password;
        // NOTE: we only support string user id's and passwords at this time.
        //
        if (null != userId)
        {
            m_userId = new String((String)userId);
            if (null != password)
            {
                m_password = new String(password);
            }
        }

        // Init the path and query string to just the AIA path element of the URL
        //
        m_connectionIdQuery = new String(m_connectURL.getPath());

        // Clear aia message count so that we can keep track of which message
        // was sent and in the AIA by looking at the header.
        //
        m_aiaMsgId = 0;

        // Create and init the new http connection object
        m_httpClient = newHttpConnection(this);

        // Test the connection to the AIA...  effectively simulate the socket
        // connect operation.  But do this only for this base class.  The
        // outer classes must implement their own.
        //
        if (INetworkProtocol.PROTOCOL_HTTP_TUNNEL == m_protocolType)
        {
            try
            {
                testProtocolConnection();
            }
            catch (Exception e)
            {
                // If the test fails for any reason, the AIA cannot reliably
                // be reached.  So close up any connection information now
                // and leave any physical connections behind.
                //
                try
                {
                    closeHttpConnection(m_httpClient, this);
                }
                catch (Exception e1)
                {
                }
                finally
                {
                    m_httpClient = null;
                }
                throw e;
            }
        }

    }


    /**
     * <!-- closeConnection() -->
     * <p> Dissolve the network connection to an AppServer's uBroker or
     * application server process.  Its operation is protocol dependent.  It
     * may close a raw TCP/IP socket, or do nothing.  See the comments for the
     * actual protocol class implementation.
     * </p>
     * <br>
     * @param forceClose is a boolean that indicates to force the message
     * stream references to null even if closing the stream throws an
     * exception.  You should normally pass false for this argument.
     * @return  void
     * <br>
     * @exception   Exception
     */
    public void closeConnection(boolean forceClose) throws Exception
    {
        if (null != m_httpClient)
        {
            // close all of any outstanding response intput streams.
            //
            if (0 < m_ubMsgResponses.size())
            {
                for (int i = 0; i < m_ubMsgResponses.size(); i++)
                {
                    UBMessageResponse ubMsgResp = (UBMessageResponse) m_ubMsgResponses.elementAt(i);
                    ubMsgResp.m_httpInputStream.close();
                    ubMsgResp.m_httpInputStream = null;
                    ubMsgResp.m_ubMsgResponse = null;
                }
            }

            try
            {

                if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                    m_loggingObj.logBasic(  m_debugLogIndex,
                                          "Closing the HTTP connection to: " +
                                          m_connectURL.getHost() + ":" +
                                          Integer.toString(m_connectURL.getPort()));

                // Cease any current operations.
                try
                {
                    closeHttpConnection(m_httpClient, this);
                }
                catch (Exception e)
                {
                }
                finally
                {
                    m_httpClient = null;
                }
            }
            catch (Exception e)
            {
                // forward the exception...
                throw e;
            }
            finally
            {
                m_connectURL = null;
                m_aiaConnectionId = null;
                m_connectionIdQuery = null;
                m_ubMsgResponses.removeAllElements();
                m_proxyAuthObjects.removeAllElements();
                m_serverAuthObject = null;
                m_password = null;
                m_userId = null;
                m_logicalConnectionOpen = false;
                m_msgInputStream = null;
                m_msgOutputStream = null;
                m_msgInputStreamRefCount = 0;
                m_msgOutputStreamRefCount = 0;

            }
        }
    }


    /**
     * <!-- rawSocket() -->
     * <p> Get a reference to the raw Socket object that supports the underlying
     * TCP/IP communications.
     * </p>
     * <br>
     * @return  Socket
     * <br>
     */
    public Socket           rawSocket()
    {
        // Raw sockets are not accessible with the HTTP protocol.
        return(null);
    }


    /**
     * <!-- getMsgInputStream() -->
     * <p> Get the IubMsgInputStream interface that represents the protocol's
     * intput stream.
     * </p>
     * @param streamType is an int that specifies one of the MSG_STREAM_XXXX
     * constants.  This indicates which type of XxxxxMsgInputStream class to
     * create and return
     * <br>
     * @return  IubMsgInputStream
     * <br>
     */
    public IubMsgInputStream  getMsgInputStream(int streamType) throws Exception
    {
        // Now create the IubMsgInputStream and IubMsgOutputStream interfaces
        //
        switch (streamType)
        {
            case INetworkProtocol.MSG_STREAM_UB_BINARY:
                if (null != m_httpClient &&
                    null == m_msgInputStream)
                {
                    m_msgInputStream = new HttpClientMsgInputStream(this);
                }
                m_msgInputStreamRefCount++;
                break;
            default:
                NetworkProtocolException npe = new NetworkProtocolException(NetworkProtocolException.BASE_MSG,
                                                   "TCP",
                                                   "Unsupported Input Stream type requested.");
                m_loggingObj.logStackTrace("",
                                           npe);
///////////     m_trace.print(npe, 1);
                throw npe;
        }

        return(m_msgInputStream);
    }


    /**
     * <!-- getMsgOutputStream() -->
     * <p> Get the IubMsgOutputStream interface that represents the protocol's
     * output stream.
     * </p>
     * @param streamType is an int that specifies one of the MSG_STREAM_XXXX
     * constants.  This indicates which type of XxxxxMsgOutputStream class to
     * create and return
     * <br>
     * @return  IubMsgOutputStream
     * <br>
     */
    public IubMsgOutputStream getMsgOutputStream(int streamType) throws Exception
    {
        switch (streamType)
        {
            case INetworkProtocol.MSG_STREAM_UB_BINARY:
                if (null != m_httpClient &&
                    null == m_msgOutputStream)
                {
                    m_msgOutputStream = new HttpClientMsgOutputStream(this);
                }
                m_msgOutputStreamRefCount++;
                break;
            default:
                NetworkProtocolException npe = new NetworkProtocolException(NetworkProtocolException.BASE_MSG,
                                                   "TCP",
                                                   "Unsupported Output Stream type requested.");
                m_loggingObj.logStackTrace("",
                                           npe);
//////////      m_trace.print(npe, 1);
                throw npe;
        }

        return(m_msgOutputStream);
    }


    /**
     * <!-- setDynamicProtocolProperty() -->
     * <p>Set a network protocol handler's runtime property.  It will immediately
     * take effect.
     * </p>
     * <br>
     * @param propertyName is a String name of the property to set (see the
     * list of supported property names in the class documentation for the
     * specific protocols)
     * @param propertyValue is a String holding the value of the property (see
     * the property name descriptions in the class documentation for the
     * specific protocols)
     * <br>
     * @return  void
     * <br>
     * @exception   Exception
     */
    public void setDynamicProtocolProperty(String propertyName,
                                           String propertyValue) throws Exception
    {
        // Let the base class handle this mundate stuff.
        //
        super.setDynamicProtocolProperty(propertyName, propertyValue);

        // If the lower level hasn't choked, see if it is a dynamic property.
        //
        if (propertyName.equalsIgnoreCase( IPoolProps.HTTP_TIMEOUT ))
        {
            // Set the dynamic property if we have a real value.
            if (null != propertyValue &&
                0 < propertyValue.length())
            {
                try
                {
                    int     newTimeout = Integer.parseInt(propertyValue);
                    // Accept only 0 to +n
                    if (0 <= newTimeout)
                    {
                        if (null != m_httpClient)
                        {
                            m_httpClient.setTimeout(newTimeout);
                        }
                    }
                    throw new Exception("Invalid timeout value: " + propertyValue);
                }
                catch (Exception e)
                {
                     if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                         m_loggingObj.logBasic(  m_debugLogIndex,
                                          "Error setting property " +
                                           IPoolProps.HTTP_TIMEOUT +
                                           ": " + e.getMessage());
                }
            }
        }
    }


    /**
     * <!-- sendUbMessage() -->
     * <p>Send the binary message to the AIA and wait for the response.  The
     * response will be saved for the accompanying read operation.
     * </p>
     * <br>
     * @param outMsg is the io stream to send the message to
     * <br>
     * @return  void
     * <br>
     * @exception   IOException when a HTTPClient POST operation has an IO
     * error
     * @exception   NetworkProtocolException when any error occurs that is
     * not an IO error
     */
    public void sendUbMessage(HttpClientMsgOutputStream outMsg)
        throws IOException, NetworkProtocolException
    {


        // If the connection has been closed, we cannot perform the operation.
        //
        if (m_logicalConnectionOpen && null == m_aiaConnectionId)
        {
            throw new NetworkProtocolException(NetworkProtocolException.NETWORK_PROTOCOL_ERROR,
                                          m_protocolTypeName,
                                          "No " + m_protocolTypeName + " connection exists");
        }

        // Create a new physical connection if necessary.
        //
        if (null == m_httpClient)
        {
            m_httpClient = newHttpConnection(this);
        }

        //Now send out the new ub message and wait for a new response.
        //
        try
        {
            boolean         queueResponse = true;
            boolean         inlineGetResponse = false;

            UBMessageResponse ubResponse = sendHttpRequest(m_httpClient,
                                                           outMsg,
                                                           "AppServer",
                                                           m_aiaMsgId++);

            // Test for special protocol handling characteristics.
            if (ubMsg.UBRQ_WRITEDATA == ubResponse.m_ubMsgRequestCode ||
                ubMsg.UBRQ_WRITEDATALAST == ubResponse.m_ubMsgRequestCode ||
                ubMsg.UBRQ_DISCONNECT == ubResponse.m_ubMsgRequestCode ||
                ubMsg.UBRQ_CONNECT == ubResponse.m_ubMsgRequestCode)
            {
                inlineGetResponse = true;
            }

            // See if we need to execute an in-line getResponse operation.
            //
            if (inlineGetResponse)
            {
                // This is a write data stream.  So get the AIA response code
                // here.  It should be zero unless there is a Write error out
                // to the appserver.  If so, we want to catch it ASAP.
                //
                getHttpResponse(m_httpClient, ubResponse);

                String  lengthHeader = ubResponse.m_ubMsgResponse.getHeader("Content-Length");
                if (null != lengthHeader &&
                    0 == lengthHeader.compareTo("0"))
                {
                    // Don't queue zero length data responses, ever...
                    //
                    queueResponse = false;
                }
            }


            // See if we need to queue the response for later handling.
            //
            if (queueResponse)
            {
                // Load this response into the top of the response silo.
                //
                synchronized(m_ubMsgResponses)
                {
                    m_ubMsgResponses.addElement(ubResponse);
                }
/*                if (!m_loggingObj.ignore(Logger.LOGGING_TRACE))
                {
                    m_loggingObj.LogMsgln(m_loggingDest,
                                          Logger.LOGGING_TRACE,
                                          Logger.TIMESTAMP,
                                          "Queing http response for message " +
                                          ubResponse.m_aiaMsgId);
                }
*/
            }
            else
            {
/*                if (!m_loggingObj.ignore(Logger.LOGGING_TRACE))
                {
                    m_loggingObj.LogMsgln(m_loggingDest,
                                          Logger.LOGGING_TRACE,
                                          Logger.TIMESTAMP,
                                          "Skipping http response for message " +
                                          ubResponse.m_aiaMsgId);
                }
*/
                // Close out the input stream if we're not queuing it.
                //
                ubResponse.m_httpInputStream.close();
                ubResponse.m_httpInputStream = null;
                ubResponse.m_ubMsgResponse = null;
                ubResponse= null;
            }
        }
        catch (EOFException eof)
        {
            // Convert the exception to a generic protocol handler exception
            // and then throw it back to the caller.
            //
            NetworkProtocolException npe = new NetworkProtocolException(NetworkProtocolException.NETWORK_CONNECTION_ABORTED,
                                               m_protocolTypeName,
                                               eof.getMessage());
            m_loggingObj.logStackTrace("",
                                       npe);
            throw npe;
        }
        catch (IOException e1)
        {
            // Pass this type right on through.
            //
            if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                m_loggingObj.logBasic(  m_debugLogIndex,
                                  "Detected I/O error posting (write) message: " +
                                  e1.getMessage());
            throw e1;
        }
        catch (Exception e2)
        {
            // Convert the exception to a generic protocol handler exception
            // and then throw it back to the caller.
            //
            NetworkProtocolException npe = new NetworkProtocolException(NetworkProtocolException.NETWORK_PROTOCOL_ERROR,
                                               m_protocolTypeName,
                                               e2.toString());
            m_loggingObj.logStackTrace("",
                                       npe);
            throw npe;
        }
    }

    /**
     * <!-- getUbResponseMessage() -->
     * <p>Get the returned HTTP response and extract the broker message from
     * it.  Then do whatever evaluation and cleanup is required before
     * returning the information.
     * <p>
     * NOTE: the caller is repsonsible for closing the input stream associated
     * with the HTTPResponse object contained in the returned UBMessageResponse
     * object.
     * </p>
     * <br>
     * @return  a UBMessageResponse object that holds the HTTP repsonse that
     * includes the AIA's return message.
     * <br>
     * @exception   IOException
     * @exception   NetworkProtocolException
     */
    public HttpClientProtocol.UBMessageResponse getUbResponseMessage()
        throws IOException, NetworkProtocolException
    {
        HttpClientProtocol.UBMessageResponse    returnValue = null;

        // If the connection has been closed, we cannot perform the operation.
        //
        if (null == m_httpClient)
        {
            throw new NetworkProtocolException(NetworkProtocolException.NETWORK_PROTOCOL_ERROR,
                                               m_protocolTypeName,
                                               "No " + m_protocolTypeName + " connection exists");
        }

        try
        {
            do
            {
                // pop the stack of pending responses (they must be read in
                // the order they were sent).
                //
                synchronized (m_ubMsgResponses)
                {
                    try
                    {
/*                        if (!m_loggingObj.ignore(Logger.LOGGING_TRACE))
                        {
                            m_loggingObj.LogMsgln(m_loggingDest,
                                                  Logger.LOGGING_TRACE,
                                                  Logger.TIMESTAMP,
                                                  "Obtaining response from the response silo...");

                        }
*/

                        // Remove the next message to be read from the silo (FIFO)
                        //
                        returnValue = (UBMessageResponse)m_ubMsgResponses.firstElement();
                        m_ubMsgResponses.removeElementAt(0);
                    }
                    catch (Exception e)
                    {
                        EOFException eofe =
                            new EOFException("A HTTP response is not available");

                        m_loggingObj.logStackTrace("",
                                                   eofe);
                        throw eofe;
                    }
                }

                // Now complete the message response operation.
                //
                getHttpResponse(m_httpClient, returnValue);

                // the AIA sends blank messages from time to time.  Ignore them.
                // They are like keep-alive (heart beat) messages to maintain a
                // persistent connnection.
                //
                String  lengthHeader = returnValue.m_ubMsgResponse.getHeader("Content-Length");
                if (null != lengthHeader &&
                    0 == lengthHeader.compareTo("0"))
                {
/*                    if (!m_loggingObj.ignore(Logger.LOGGING_TRACE))
                    {
                        m_loggingObj.LogMsgln(m_loggingDest,
                                              Logger.LOGGING_TRACE,
                                              Logger.TIMESTAMP,
                                              "Skipping zero length content response for message " +
                                              returnValue.m_aiaMsgId);
                    }
*/

                    // A zero length response.  Close out the input stream.
                    returnValue.m_httpInputStream.close();
                    returnValue.m_httpInputStream = null;
                    returnValue.m_ubMsgResponse = null;
                    returnValue = null;
                }
                else
                {
/*                    if (!m_loggingObj.ignore(Logger.LOGGING_TRACE))
                    {
                        m_loggingObj.LogMsgln(m_loggingDest,
                                              Logger.LOGGING_TRACE,
                                              Logger.TIMESTAMP,
                                              "Dequeue response for message " +
                                              returnValue.m_aiaMsgId);
                    }
*/
                }

            } while( null == returnValue);

            // Evaluate the message response and see if we need to do any
            // protocol level work like closing down etc.
            //

            // The first thing is to handle the AIA AppServer connection session
            // id.  If this is a connection operation, then an id hasn't been
            // registered yet and we must have a valid header (non-blank)
            // returned from the aia.  If we have a registered id, then the
            // one returned from the AIA must match.
            //
            //  When connectionId == null, we expect that a connection is being
            //  made.  If the aia sends us a "" connection id, it is an error
            //  or disconnect, indicating the end of the.  We'll keep the old
            //  id around until the BrokerSystem analyzes the return and
            //  tells us to close the connection or not.
            //

            // Inspect the message's returned data type.  It has to be one of
            // binary 'application/octet-stream'
            //
            String      contentType = returnValue.m_ubMsgResponse.getHeader("Content-Type");
            if (null != contentType)
            {
                if (0 != contentType.compareTo("application/octet-stream"))
                {
                     if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                         m_loggingObj.logBasic(  m_debugLogIndex,
                                               "Unexpected HTTP Content-Type header: " +
                                               contentType);

                }
            }
            else
            {
                // Until AIA is consistent in building message header contents, just
                // log and warn this situation.
                //
                // NetworkProtocolException npe = new NetworkProtocolException(NetworkProtocolException.NETWORK_PROTOCOL_ERROR,
                //                                    m_protocolTypeName,
                //                                    "Empty HTTP Content-Type header");
                // m_loggingObj.LogStackTrace(m_loggingDest,
                //                            Logger.LOGGING_ERRORS,
                //                            Logger.TIMESTAMP,
                //                            "",
                //                            npe);
                // throw npe;
                if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                    m_loggingObj.logBasic(  m_debugLogIndex,
                                          "Warning, missing Content-Type header.");
            }

            // Obtain the AIA's Connection session id and store it,  we have
            // to have one for sending the next message...
            //
            String      connectionId = returnValue.m_ubMsgResponse.getHeader(CONNECT_ID_IN_HEADER);
            if (null != connectionId &&
                0 < connectionId.length())
            {

                if (null == m_aiaConnectionId)
                {
                    // Record the aia's connection id for later use.
                    m_aiaConnectionId = new String(connectionId);
                    // Build the new path and connection handle query string.
                    //
                    StringBuffer uri = new StringBuffer(m_connectURL.getPath());
                    uri.append('?');
                    uri.append(CONNECT_ID_IN_HEADER);
                    uri.append('=');
                    uri.append(m_aiaConnectionId);
                    m_connectionIdQuery = uri.toString();

                    // Mark the logical Appserver Connection open for use.
                    m_logicalConnectionOpen = true;
                }
            }
            else
            {
                // Get a null connection id header, reset the connection.
                //
                // m_aiaConnectionId = null;
                // m_connectionIdQuery = new String(m_connectURL.getPath());

                // make a debug log entry, but let the AppServer protocol
                // handler (BrokerSystem) decide what to do.
                //
                if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))

                    m_loggingObj.logBasic(  m_debugLogIndex,
                                          "Detected aia resetting connection id to null.");
                if (!m_ubMsgResponses.isEmpty())
                {
                   if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                       m_loggingObj.logBasic(  m_debugLogIndex,
                                              "Detected null connection id with responses pending.");
                }
            }

        }
        catch (Exception e2)
        {
            NetworkProtocolException npe = new NetworkProtocolException(NetworkProtocolException.BASE_MSG,
                                               m_protocolTypeName,
                                               e2.toString());
            m_loggingObj.logStackTrace("",
                                       npe);
            throw npe;
        }

        return(returnValue);
    }


    /**
     * <!-- available() -->
     * <p>Return the minumum # of bytes available to be read from the first
     * message response.
     * </p>
     * <br>
     * @return  int
     */
    public  int available()
    {
        int                                    returnValue = 0;
        HttpClientProtocol.UBMessageResponse   ubResponse = null;

        try
        {
            // Add all the response data sizes (to this point in time, more may
            // be in the pipeline).
            //
            int         tmp = 0;
            Enumeration responses = m_ubMsgResponses.elements();
            while (responses.hasMoreElements())
            {
                ubResponse = (HttpClientProtocol.UBMessageResponse)responses.nextElement();
                tmp += ubResponse.m_httpInputStream.available();
            }

            returnValue = tmp; /* 20041109-012 : need to send value to caller */
        }
        catch (Exception e)
        {
        }

        return(returnValue);
    }

    /**
     * <!-- sendStopMessage() -->
     * <p>This will send an out-of-band STOP message to the AppServer via the
     * AIA.
     * </p>
     * <br>
     * @exception   IOException
     * @exception   NetworkProtocolException
     */
    public void sendStopMessage(HttpClientMsgOutputStream outMsg)
        throws IOException, NetworkProtocolException
    {
        // If the connection has been closed, we cannot perform the operation.
        //
        if (null == m_aiaConnectionId)
        {
            throw new NetworkProtocolException(NetworkProtocolException.NETWORK_PROTOCOL_ERROR,
                                               m_protocolTypeName,
                                               "No " + m_protocolTypeName + " connection exists");
        }

        // First create a new HTTPClient object to use.
        //
        Long        tmpContext = new Long(System.currentTimeMillis());

        HTTPConnection  stopConnection = null;
        try
        {
            if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                m_loggingObj.logBasic(  m_debugLogIndex,
                                      "Creating new Http connection to send stop message.");

            stopConnection = newHttpConnection(tmpContext);
        }
        catch (NetworkProtocolException e)
        {
            if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                m_loggingObj.logBasic(  m_debugLogIndex,
                                      "Error creating new http stop connection: " +
                                      e.toString());
            throw e;
        }

        try
        {
            // Send the POST operation, skip any response data.  It's not
            // important.  Just the headers.
            //

            // if (HTTP_STATE_IDLE != m_httpState)
            // {
                SendStopThread  stopObj = new SendStopThread(this,
                                                             stopConnection,
                                                             outMsg,
                                                             m_aiaStopMsgId--);

                Thread  stopThread = new Thread(stopObj);

                if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                    m_loggingObj.logBasic(  m_debugLogIndex,
                                            "Starting Http stop thread operation.");

                stopThread.start();
                stopThread.join();

                if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                    m_loggingObj.logBasic(  m_debugLogIndex,
                                           "Http stop thread operation finished.");

                stopThread = null;
                stopObj = null;
            // }

            if (null != m_msgInputStream)
            {
                // forward the stop operation to the msg input stream so that
                // it can emulate Abnormal EOF if necessary.  Let it figure out
                // whether it needs to do so or not.
                //
                m_msgInputStream.setStop();
            }
        }
        catch (Exception e3)
        {
            // Convert the exception to a generic protocol handler exception
            // and then throw it back to the caller.
            //
            m_loggingObj.logStackTrace("",
                                       e3);
            NetworkProtocolException npe = new NetworkProtocolException(NetworkProtocolException.NETWORK_PROTOCOL_ERROR,
                                               m_protocolTypeName,
                                               e3.toString());
            m_loggingObj.logStackTrace("",
                                       npe);
            throw npe;
        }
        finally
        {
            if (null != stopConnection)
            {
                try
                {
                    closeHttpConnection(stopConnection, tmpContext);
                }
                catch (Exception e)
                {
                if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                    m_loggingObj.logBasic(  m_debugLogIndex,
                                          "Error closing http connection: " +
                                           e.toString());
                }
            }
        }

    }

    /**
     * <!-- releaseMsgInputStream() -->
     * <p>This will release the HttpClientMsgInputStream object created by this
     * object to handle the broker message reading operation.
     * This is called by the HttpClientMsgInputStream objects when they are
     * being closed.  It should not be used by other callers.
     * </p>
     */
    public void releaseMsgInputStream()
    {
        if (null != m_msgInputStream)
        {
            // Decrement the reference count.
            if (0 < m_msgInputStreamRefCount)
            {
                m_msgInputStreamRefCount--;
            }
            if (0 == m_msgInputStreamRefCount)
            {
                // Release control.
                m_msgInputStream = null;
            }
        }
    }

    /**
     * <!-- releaseMsgOutputStream() -->
     * <p>This will release the HttpClientMsgInputStream object created by this
     * object to handle the broker message reading operation.
     * This is called by the HttpClientMsgOutputStream objects when they are
     * being closed.  It should not be used by other callers.
     * </p>
     */
    public void releaseMsgOutputStream()
    {
        if (null != m_msgOutputStream)
        {
            // Decrement the reference count.
            if (0 < m_msgOutputStreamRefCount)
            {
                m_msgOutputStreamRefCount--;
            }
            if (0 == m_msgOutputStreamRefCount)
            {
                // Release control.
                m_msgOutputStream = null;
            }
        }
    }

    /*
     * PROTECTED (SUPER) METHODS:
     */

    /*
     * <!-- evaluateResponseCode() -->
     * <p>Evaluate the HTTP response code passed to this method.  If it is
     * an error type, then create the right exception and let the upper layers
     * decide what to do with it.
     * </p>
     * <br>
     * @param response is the UBMessageResponse object to evaluate
     * <br>
     * @return boolean TRUE if an error needs to be thrown or false if not
     * error is needed.
     */
    protected boolean evaluateResponseCode(UBMessageResponse response)
    {
        boolean                         returnValue = false;
        int                             exceptionMsgFormat = NetworkProtocolException.BASE_MSG;
        Vector                          errorDetail = new Vector();

        // The protocol name is always first.
        //
        errorDetail.addElement(new String(m_protocolTypeName));

        // Depending upon whether we are in the middle of a message exchange
        // or just starting one, adjust the default message format.
        //
        if (null == m_aiaConnectionId)
        {
            exceptionMsgFormat = NetworkProtocolException.PROTOCOL_CONNECTION_FAILED;
        }
        else
        {
            exceptionMsgFormat = NetworkProtocolException.NETWORK_PROTOCOL_ERROR;
        }

        switch (response.m_sendMsgResponseCode)
        {
            case 100:
                // Allow continues to come through, they are used like a
                // heartbeat by some servers.
                //
                // errorDetail = "Invalid HTTP information: Continue";
                break;
            case 101:
                errorDetail.addElement(new String("Invalid HTTP information (101): Switch protocols"));
                break;
            case 200:
                // This is a good guy!
                break;
            case 201:
                errorDetail.addElement(new String("Invalid HTTP response (201): Resource Created"));
                break;
            case 202:
                errorDetail.addElement(new String("Invalid HTTP response (202): Request Accepted"));
                break;
            case 203:
                errorDetail.addElement(new String("Invalid HTTP response (203): Non-Authoritive Information"));
                break;
            case 204:
                errorDetail.addElement(new String("Invalid HTTP response (204): No content"));
                break;
            case 205:
                errorDetail.addElement(new String("Invalid HTTP response (205): Reset content"));
                break;
            case 206:
                errorDetail.addElement(new String("Invalid HTTP response (206): Partial content"));
                break;
            case 300:
                errorDetail.addElement(new String("Invalid HTTP Redirection (300): Multiple choice"));
                break;
            case 301:
                errorDetail.addElement(new String("Invalid HTTP redirection (301): Moved permanently"));
                break;
            case 302:
                errorDetail.addElement(new String("Invalid HTTP redirection (302): Moved temporarily"));
                break;
            case 303:
                errorDetail.addElement(new String("Invalid HTTP redirection (303): See other"));
                break;
            case 304:
                errorDetail.addElement(new String("Invalid HTTP redirection (304): Not modified"));
                break;
            case 305:
                errorDetail.addElement(new String("Invalid HTTP redirection (305): Use proxy"));
                break;
            case 400:
                errorDetail.addElement(new String("HTTP client error (400): Bad request"));
                break;
            case 401:
                errorDetail.addElement(new String("client"));
                errorDetail.addElement(new String("HTTP server returned Unauthorized (401)"));
                exceptionMsgFormat = NetworkProtocolException.AUTHENTICATION_REJECTED;
                break;
            case 402:
                errorDetail.addElement(new String("HTTP client error (402): Payment required"));
                break;
            case 403:
                errorDetail.addElement(new String("HTTP client error (403): Forbidden"));
                break;
            case 404:
                errorDetail.addElement(new String("HTTP client error (404): Not found"));
                break;
            case 405:
                errorDetail.addElement(new String("HTTP client error (405): Method not allowed"));
                break;
            case 406:
                errorDetail.addElement(new String("HTTP client error (406): Not acceptable"));
                break;
            case 407:
                errorDetail.addElement(new String("HTTP proxy server returned authentication required (407)"));
                exceptionMsgFormat = NetworkProtocolException.PROXY_AUTHENTICATION_FAILED;
                break;
            case 408:
                errorDetail.addElement(new String(m_connectURL.getHost() +
                                         Integer.toString(m_connectURL.getPort())));
                exceptionMsgFormat = NetworkProtocolException.NETWORK_PROTOCOL_TIMEOUT;
                break;
            case 409:
                errorDetail.addElement(new String("HTTP client error (409): Conflict"));
                break;
            case 410:
                errorDetail.addElement(new String("HTTP client error (410): Gone"));
                break;
            case 411:
                errorDetail.addElement(new String("HTTP client error (411): Length required"));
                break;
            case 412:
                errorDetail.addElement(new String("HTTP client error (412): Precondition failed"));
                break;
            case 413:
                errorDetail.addElement(new String("HTTP client error (413): Request entity too large"));
                break;
            case 414:
                errorDetail.addElement(new String("HTTP client error (414): Request-URI too long"));
                break;
            case 415:
                errorDetail.addElement(new String("HTTP client error (415): Unsupported media type"));
                break;
            case 500:
                errorDetail.addElement(new String(m_connectURL.getHost() +
                                         Integer.toString(m_connectURL.getPort())));
                errorDetail.addElement(new String("HTTP server error (500): Internal error"));
                exceptionMsgFormat = NetworkProtocolException.NETWORK_CONNECTION_ABORTED;
                break;
            case 501:
                errorDetail.addElement(new String(m_connectURL.getHost() +
                                         Integer.toString(m_connectURL.getPort())));
                errorDetail.addElement(new String("HTTP server error (501): Not implemented"));
                exceptionMsgFormat = NetworkProtocolException.NETWORK_CONNECTION_ABORTED;
                break;
            case 502:
                errorDetail.addElement(new String(m_connectURL.getHost() +
                                         Integer.toString(m_connectURL.getPort())));
                errorDetail.addElement(new String("HTTP server error (502): Bad gateway"));
                exceptionMsgFormat = NetworkProtocolException.NETWORK_CONNECTION_ABORTED;
                break;
            case 503:
                errorDetail.addElement(new String(m_connectURL.getHost() +
                                         Integer.toString(m_connectURL.getPort())));
                errorDetail.addElement(new String("HTTP server error (503): Service unavailable"));
                exceptionMsgFormat = NetworkProtocolException.NETWORK_CONNECTION_ABORTED;
                break;
            case 504:
                errorDetail.addElement(new String(m_connectURL.getHost() +
                                         Integer.toString(m_connectURL.getPort())));
                errorDetail.addElement(new String("HTTP server error (504): Gateway timeout"));
                exceptionMsgFormat = NetworkProtocolException.NETWORK_CONNECTION_ABORTED;
                break;
            case 505:
                errorDetail.addElement(new String("HTTP server error (505): HTTP version not supported"));
                break;
            default:
                errorDetail.addElement(new String("HTTP status response code: " +
                                         Integer.toString(response.m_sendMsgResponseCode)));
                break;
        }

        // If something of note comes up, create and format the exception
        // information and return it.
        // (Remember, there is always one element in the Vector. )
        if (1 < errorDetail.size())
        {
            response.m_sendMsgExceptionMsgCode = exceptionMsgFormat;
            response.m_sendMsgExceptionErrorDetail = new Object[errorDetail.size()];
            errorDetail.copyInto(response.m_sendMsgExceptionErrorDetail);
            returnValue = true;
        }
        else
        {
            errorDetail = null;
        }

        return(returnValue);
    }

    /*
     * <!-- setupBasicAuthentication() -->
     * <p>Setup any HTTP Basic authentication for the user. The order of
     * priority is:
     * <br>
     * <ul>
     * <li>Method arguments
     * <li>URL
     * <li>Network properties
     * </ul>
     * </p>
     * @param httpClient is the HTTPConnection object to set Basic authentication
     * for
     * @param context is an Object to use in segmenting the authentication
     * information to a particular context
     */
    protected void setupBasicAuthentication(HTTPConnection  httpClient,
                                            Object          context)
    {
        // Setup the basic authentication just once for the logical connection.
        //
        if (null == m_serverAuthObject)
        {
            // Extract any id and password form the URL to make sure it doesn't go
            // out over the air-waves (so to speak)
            //
            String          tmpRealm = "";
            String          tmpUid = m_userId;
            StringBuffer    tmpPwd = ((null != m_password) ? new StringBuffer(m_password) : null );
            String          tmpDomain = null;
            StringTokenizer parser = null;
            String          credentials = null;

            // No specific user id override, so try the url.
            // We'll not bother with a password if a user-id isn't present.
            //
            if (null == tmpUid)
            {
                try
                {
                    // First, try the userid.  If one exists, then try the
                    // password.  Can't use the password without a user id.
                    //
                    tmpUid = m_connectURL.getUserId();

                    if (null != tmpUid)
                    {
                        // If the password comes back null, an exception is thrown,
                        // so trap it and dismisss it after blanking out the user id
                        // also.  We have to have a full combination before allowing
                        // them to be used.  Note: that a blank password ("") is ok.
                        //
                        tmpPwd = new StringBuffer(m_connectURL.getUserPassword());
                    }
                }
                catch (Exception e)
                {
                    if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                        m_loggingObj.logBasic(  m_debugLogIndex,
                                          "Attempt to use a authentication user-id without a password");
                    // Don't allow the user id to be used later and get us
                    // confused.
                    //
                    tmpUid = null;
                }
            }

            if (null == tmpDomain)
            {
                tmpDomain = m_connectURL.getHost();
            }

            // no user id is present in the URL, see if one is present in the properties
            //
            if (null == tmpUid)
            {
                tmpUid = m_protocolProperties.getProperty(
                    IPoolProps.SESSION_USERID );
                String pass =  m_protocolProperties.getProperty(
                    IPoolProps.SESSION_PASSWORD );
                if (pass!=null) //PSC00307375 - will give NPE if password is not set
                    tmpPwd = new StringBuffer( pass );
            }

            // Add in any basic authentication for the Web site.  We construct it
            // this way with -path- information to see if we can trigger the
            // HTTPClient to send out the authentication information in the header
            // rather than trying to repsond to it in a 401 status response.
            //

            if (null != tmpUid)
            {
                m_serverAuthObject = new AuthorizationInfo(m_connectURL.getHost(),
                                                           m_connectURL.getPort(),
                                                           "Basic",
                                                           tmpRealm,
                                                           Codecs.base64Encode(tmpUid + ":" + tmpPwd));

                m_serverAuthObject.addPath(m_connectURL.getPath());

                if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                    m_loggingObj.logBasic(  m_debugLogIndex,
                                          "Authenticating to HTTP server: " +
                                           tmpRealm + ":" + tmpUid + ":xxxxxxx");

                // Blank out the password storage for security reasons.
                //
                // tmpPwd.replace(0, tmpPwd.length(), " ");
                for (int j = 0; j < tmpPwd.length(); j++)
                {
                    tmpPwd.setCharAt(j, ' ');
                }
            }
        }

        // If server credentials have been set, then set them for the context.
        //
        if (null != m_serverAuthObject)
        {
            AuthorizationInfo.addAuthorization(m_serverAuthObject, context);
        }
    }


    /*
     * <!-- setupProxyServer() -->
     * <p>Setup any proxy server specification, and if supplied any proxy
     * server authentication.
     * </p>
     * @param httpClient is the HTTPConnection object to set Basic authentication
     * for
     * @param context is an Object to use in segmenting the authentication
     * information to a particular context
     * <p>
     * @exception   NetworkProtocolException
     */
    protected void  setupProxyServer(HTTPConnection httpClient,
                                     Object         context) throws NetworkProtocolException
    {
        // Setup the proxy authentication credentials just once the logical
        // connection.
        //
        {
            // Resolve and setup any default proxy information.
            //
            String proxyhost = m_protocolProperties.getProperty(
                IPoolProps.PROXY_HOST );

            if (null != proxyhost)
            {
                try
                {
                    String proxyport = m_protocolProperties.getProperty(
                        IPoolProps.PROXY_PORT );

                    if (null != proxyport)
                    {
                        if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                            m_loggingObj.logBasic(  m_debugLogIndex,
                                                    "Using HTTP Proxy server: " +
                                                    proxyhost + ":" + proxyport );

                        // Verify that the proxy host address is valid and/or
                        // reachable before we use it.  If this doesn't work,
                        // it's not necessary to continue.
                        //
                        try
                        {
                            InetAddress testAddr = InetAddress.getByName(proxyhost);
                        }
                        catch (Exception e)
                        {
                            // log it, report it and throw the proper exception
                            // type.
                            //
                            throw new NetworkProtocolException(
                                NetworkProtocolException.INVALID_PROTOCOL_CONFIGURATION,
                                m_protocolTypeName,
                                "Unknown proxy host " +
                                proxyhost);
                        }

                        httpClient.setCurrentProxy(proxyhost, Integer.parseInt(proxyport));

                        // Resolve and setup any default proxy authentication information.
                        //
                        String proxyId = m_protocolProperties.getProperty(
                            IPoolProps.PROXY_USERID );
                        String  proxyAuthRealm = "";
                        if (null != proxyId)
                        {
                            String proxyPwd = m_protocolProperties.getProperty(
                                IPoolProps.PROXY_PASSWORD );
                            
                            if (null != proxyPwd)
                            {
                                // Build a custom authorization to the proxy.  Since
                                // we want to pre-emtively send the authorization
                                // (that is not wait for the response to ask for it)
                                // we'll use a URL path to obtain a match on
                                // the host, port, and path.
                                //
                                AuthorizationInfo authInfo = new AuthorizationInfo(
                                    proxyhost,
                                    Integer.parseInt(proxyport),
                                    "Basic",
                                    proxyAuthRealm,
                                    Codecs.base64Encode(proxyId + ":" + proxyPwd));
                                authInfo.addPath("/");

                                // Now add the custom built authorization.
                                //
                                if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                                    m_loggingObj.logBasic(
                                        m_debugLogIndex,
                                        "Authenticating to HTTP Proxy server: " +
                                        ((null == proxyAuthRealm) ? "" : proxyAuthRealm + ":") +
                                        proxyId + ":xxxxxxx");

                                if (this == context)
                                {
                                    // Store for later removal
                                    m_proxyAuthObjects.addElement(authInfo);
                                }
                            }
                            else
                            {
                                throw new NetworkProtocolException(
                                    NetworkProtocolException.INVALID_PROTOCOL_CONFIGURATION,
                                    m_protocolTypeName,
                                    "Bad proxy [realm:]uid:pwd format");
                            }
                        }
                    }
                    else
                    {
                        throw new NetworkProtocolException(
                            NetworkProtocolException.INVALID_PROTOCOL_CONFIGURATION,
                            m_protocolTypeName,
                            "Bad proxy host:port format");
                    }
                }
                catch (NetworkProtocolException e)
                {
                    if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                        m_loggingObj.logBasic(  m_debugLogIndex,
                                                "Invalid proxy option: " +
                                                proxyhost + " (" + e.getMessage() + ")");
                    // pass it along after logging the failure.
                    throw e;
                }
            }
        }

        // setup the http connection [context] if any authentication information
        // exists.
        //
        if (!m_proxyAuthObjects.isEmpty())
        {
            AuthorizationInfo   authInfo = null;
            Enumeration         authObjects = m_proxyAuthObjects.elements();
            while (authObjects.hasMoreElements())
            {
                authInfo = (AuthorizationInfo) authObjects.nextElement();
                AuthorizationInfo.addAuthorization(authInfo,
                                                   context);
            }
        }

        // DEBUG::
        //



        // Don't do proxy operations for "localhost"
        //
        try
        {
            HTTPConnection.dontProxyFor( "localhost" );
            HTTPConnection.dontProxyFor( "127.0.0.1" );
        }
        catch (Exception e)
        {
            throw new NetworkProtocolException(NetworkProtocolException.INVALID_PROTOCOL_CONFIGURATION,
                                               m_protocolTypeName,
                                               "Failure removing localhost from proxy list: " +
                                               e.toString());
        }
    }

    /*
     * <!-- setupDefaults() -->
     * <p>Setup any http or protocol defaults.
     * </p>
     * @param httpClient is the HTTPConnection object to set Basic authentication
     * for
     * @param context is an Object to use in segmenting the authentication
     * information to a particular context
     * <p>
     * <br>
     * @exception   NetworkProtocolException
     */
    protected void setupDefaults(HTTPConnection     httpClient,
                                 Object             context) throws NetworkProtocolException
    {
        // Set the default parameters like timeout, etc.
        //
        httpClient.setAllowUserInteraction(false);        // Force off, no options.

        // If a specific timeout has been set, use it
        String httpTimeout = m_protocolProperties.getProperty(
                        IPoolProps.HTTP_TIMEOUT );
        if (null == httpTimeout)
        {
            // Set a default timeout of infinity.
            //
            httpTimeout = HTTP_DEFAULT_TIMEOUT;
        }

        try
        {

            int timeout = Integer.parseInt(httpTimeout);

            // eliminate negative numbers,
            //
            if (0 > timeout)
            {
                timeout = Integer.parseInt(HTTP_DEFAULT_TIMEOUT);
                if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                    m_loggingObj.logBasic(  m_debugLogIndex,
                                          "ignoring negative timeout, setting default http timeout option: " +
                                          HTTP_DEFAULT_TIMEOUT);
            }
            else
            {
                if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                    m_loggingObj.logBasic(  m_debugLogIndex,
                                          "setting http timeout option: " +
                                          httpTimeout);
            }

            if (0 != timeout)
            {
                // convert from seconds to milliseconds if zero (inifinity) isn't
                // specified.
                //
                timeout = timeout * 1000;
            }
            // Now, set the http timeout parameter...
            //
            httpClient.setTimeout(timeout);
        }
        catch (Exception e)
        {
                if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                    m_loggingObj.logBasic(  m_debugLogIndex,
                                  "Invalid http timeout option: " +
                                  httpTimeout);
        }

        // Set default headers to go out with each request...
        //
        NVPair[]    defHeaders = new NVPair[3];
        int         insertIndex = 0;

        defHeaders[insertIndex++] = new NVPair("User-Agent","Progress ASIA Client");
        defHeaders[insertIndex++] = new NVPair("Accept","*/*");
        defHeaders[insertIndex++] = new NVPair("Proxy-Connection","Keep-Alive");

        httpClient.setDefaultHeaders(defHeaders);
    }

    /*
     * <!-- testProtocolConnection() -->
     * <p>Test a connection to the AIA.  We'll use its general Get method
     * to see if we get back a 200 response or not.
     * </p>
     * <br>
     * @exception   IOException
     * @exception   NetworkProtocolException
     */
    protected void testProtocolConnection() throws IOException, NetworkProtocolException
    {
        UBMessageResponse   msgResponse = new UBMessageResponse();

        try
        {
            if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                m_loggingObj.logBasic(  m_debugLogIndex,
                                      "Test Connection message to aia: " + m_connectURL.getPath());

            msgResponse.m_ubMsgResponse = m_httpClient.Get(m_connectURL.getPath());
            msgResponse.m_sendMsgResponseCode = msgResponse.m_ubMsgResponse.getStatusCode();

            // Swallow (ignore) any response data.  We're only interested in the
            // return status.
            //
            try
            {
                msgResponse.m_ubMsgResponse.getData();
            }
            catch (Exception e)
            {
            }

        }
        catch (IOException e1)
        {
            // Pass this type right on through.
            //
            String  ioException = e1.toString();

            if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                m_loggingObj.logBasic(  m_debugLogIndex,
                                  "Detected I/O error on Get (testConnection) message: " +
                                  ioException);
            if (ioException.startsWith("java.net."))
            {
                ioException = ioException.substring(9);
            }
            else if (ioException.startsWith("java.io."))
            {
                ioException = ioException.substring(8);
            }

            NetworkProtocolException npe = new NetworkProtocolException(NetworkProtocolException.PROTOCOL_CONNECTION_FAILED ,
                                               m_protocolTypeName,
                                               ioException);

            throw npe;
        }
        catch (Exception e2)
        {
            // Convert the exception to a generic protocol handler exception
            // and then throw it back to the caller.
            //
            NetworkProtocolException npe = new NetworkProtocolException(NetworkProtocolException.NETWORK_PROTOCOL_ERROR,
                                               m_protocolTypeName,
                                               e2.toString());
            m_loggingObj.logStackTrace("",
                                       npe);
            throw npe;
        }

        // Now evaluate the test response to see if we connected correctly.
        //
        boolean msgError = evaluateResponseCode(msgResponse);

        if (msgError)
        {
            // Make a copy so that the exception points to the right
            // location.
            NetworkProtocolException npe = new NetworkProtocolException(msgResponse.m_sendMsgExceptionMsgCode,
                                                                        msgResponse.m_sendMsgExceptionErrorDetail);
            m_loggingObj.logStackTrace("",
                                       npe);
            throw npe;
        }
        else
        {
           if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
               m_loggingObj.logBasic(  m_debugLogIndex,
                                      "Test Connection message to aia succeeded.");
        }

    }

    /*
     * <!-- newHTTPClient() -->
     * <p>Create a new instance of a HTTPClient object.
     * </p>
     * <br>
     * @param context is an Object that allows multiple instances to have their
     * own connection context
     * <br>
     * @return  HTTPConnection
     * <br>
     * @exception   NetworkProtocolException
     */
    protected HTTPConnection newHTTPClient(Object                 context)
                                           throws NetworkProtocolException
    {
        HTTPConnection      returnValue = null;

        try
        {

            if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                m_loggingObj.logBasic(  m_debugLogIndex,
                                      "Connecting to HTTP server: " +
                                       m_connectURL.toString());

            returnValue = new HTTPConnection(m_connectURL.getProtocol(),
                                             m_connectURL.getHost(),
                                             m_connectURL.getPort());

            // Set the "multi-thread" context to support multiple distinct connection
            // property sets since the application interface is a single pipe.
            //
            returnValue.setContext(context);

            // Disable all cookie handling (for now).  Comment this out and add
            // in the cookie store options for persistent/session settings.
            //
            returnValue.removeModule(Class.forName("HTTPClient.CookieModule"));

        }
        catch (Exception e)
        {
            NetworkProtocolException    npe = new NetworkProtocolException(NetworkProtocolException.PROTOCOL_CONNECTION_FAILED,
                                                                           m_protocolTypeName,
                                                                           e.toString());
            m_loggingObj.logStackTrace("",
                                       npe);
            throw npe;
        }

        return(returnValue);
    }

    /*
     * <!-- newHttpConnection() -->
     * <p>Open and initialize a new HTTPConnection object for this object.
     * It does not establish either a physical or logical connection to the AIA.
     * </p>
     * <br>
     * @param   context     is an Object to use as a context swither for the
     * HTTPConnection object
     * return connection    is a HTTPConnection object
     * <br>
     * @exception   NetworkProtocolException
     */
    public HTTPConnection newHttpConnection(Object context)
        throws NetworkProtocolException
    {
        HTTPConnection      httpClient = null;

        try
        {
            // Create a new connection.
            //
            httpClient = newHTTPClient(context);

            // We support HTTP basic authentication, so see if any is specified
            // in the URL or in the protocol properties...
            //
            setupBasicAuthentication(httpClient, context);

            // We support HTTP proxy servers (and authentication), so see if any
            // were specified in the protocol properties.
            //
            setupProxyServer(httpClient, context);

            // Set defaults...
            //
            setupDefaults(httpClient, context);
        }
        catch (NetworkProtocolException e)
        {
            try
            {
                closeHttpConnection(null, context);
            }
            catch (Exception e1)
            {
                if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                    m_loggingObj.logBasic(  m_debugLogIndex,
                                      "Error closing http connection: " +
                                       e1.toString());
            }
            throw e;
        }

        return(httpClient);
    }

    /**
     * <!-- closeHttpConnection() -->
     * <p>Close a Http connection to the AIA.  This only closes the physical
     * link, not the logical AppServer link.
     * </p>
     * <br>
     * @param httpConnection is the HTTPConnection object to close.  If passed
     * as null, it will use the main connection for this object
     * @param context        is the context associated with the HTTPConnection
     * <br>
     * @exception   Exception
     */
    public    void closeHttpConnection(HTTPConnection httpConnection,
                                       Object context)
        throws Exception
    {
        HTTPConnection      closeConnection = ((null == httpConnection) ? m_httpClient : httpConnection);
        if (null != context)
        {
            // Release any authentication objects for this context
            //
            if (!m_proxyAuthObjects.isEmpty())
            {
                Enumeration authObjs = m_proxyAuthObjects.elements();

                while (authObjs.hasMoreElements())
                {
                    AuthorizationInfo auth = (AuthorizationInfo) authObjs.nextElement();
                    AuthorizationInfo.removeAuthorization(auth, context);
                }
            }
        }

        // Now remove any web server authentication.
        if (null != m_serverAuthObject)
        {
            AuthorizationInfo.removeAuthorization(m_serverAuthObject, context);
        }

        // Insure we shutdown
        if (null != closeConnection)
        {
            try
            {
                // Stop any execution.
                closeConnection.stop();
            }
            catch (Exception e)
            {
                if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                    m_loggingObj.logBasic(  m_debugLogIndex,
                                      "Error stopping http connection: " +
                                       e.toString());
                throw e;
            }
        }
    }

    /*
     * <!-- processHttpRequest() -->
     * <p>Common HTTP Get/Post processing.  Will perform the operation and
     * evaluate the return status code, and read return message body data
     * if directed to.
     * </p>
     * <br>
     * @param connection is a HTTPConnection object to perform the operation
     * on
     * @param outMsg is a HttpClientMsgOutputStream to send the data from
     * @param operationName is a String object holding the logical operation
     * name for logging purposes
     * @param aiaMsgId  is an long that contains a reference message number for
     * aia and HTTPClient tracking purposes.  It will be positive for non-stop
     * messages and negative for stop messages
     * <br>
     * @return  UBMessageResponse
     * <br>
     * @exception   IOException
     * @exception   NetworkProtocolException
     */
    protected UBMessageResponse processHttpRequest(HTTPConnection               connection,
                                                   HttpClientMsgOutputStream    outMsg,
                                                   String                       operationName,
                                                   long                         aiaMsgId)
        throws IOException, NetworkProtocolException
    {

        UBMessageResponse   ubResponse = null;

        // First, send off the request to the http server...
        //
        ubResponse = sendHttpRequest(connection,
                                     outMsg,
                                     operationName,
                                     aiaMsgId);

        getHttpResponse(connection, ubResponse);

        return(ubResponse);

    }


    /*
     * <!-- sendHttpRequest() -->
     * <p>Send a http GET/POST to the HTTP server and queue the response object
     * for later handling.
     * </p>
     * <br>
     * @param connection is a HTTPConnection object to perform the operation
     * on
     * @param outMsg is a HttpClientMsgOutputStream to send the data from
     * @param operationName is a String object holding the logical operation
     * name for logging purposes
     * @param aiaMsgId  is an long that contains a reference message number for
     * aia and HTTPClient tracking purposes.  It will be positive for non-stop
     * messages and negative for stop messages
     * <br>
     * @return  UBMessageResponse
     * <br>
     * @exception   IOException
     * @exception   NetworkProtocolException
     */
    protected UBMessageResponse sendHttpRequest(HTTPConnection               connection,
                                                HttpClientMsgOutputStream    outMsg,
                                                String                       operationName,
                                                long                         aiaMsgId)
        throws IOException, NetworkProtocolException
    {
        UBMessageResponse   ubResponse = new UBMessageResponse();

        ubResponse.m_ubMsgRequestCode = ((null != outMsg) ? outMsg.lastUbMsgRequestCode() : 0);
        ubResponse.m_httpOperationName = operationName;
        ubResponse.m_aiaMsgId = aiaMsgId;

        byte[]              outBytes = ((null != outMsg) ? outMsg.toByteArray() : null);

        String ccid = extractCCID(outBytes);

        try
        {
            if (null == outBytes)
            {
                if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                    m_loggingObj.logBasic(  m_debugLogIndex,
                                          "GET (" + ubResponse.m_httpOperationName +
                                          ") message" + " from aia.");

                m_httpState = HTTP_STATE_GET;
                m_httpThread = Thread.currentThread();

                ubResponse.m_ubMsgResponse = connection.Get(m_connectURL.getPath());

                if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                    m_loggingObj.logBasic(  m_debugLogIndex,
                                          "GET " + ubResponse.m_httpOperationName +
                                          " operation complete.");
            }
            else
            {
                // Keep track of the sequence of messages
                //
                // HTTPClient.NVPair  msgHeaders[] = new HTTPClient.NVPair[2];
                HTTPClient.NVPair  msgHeaders[] = new HTTPClient.NVPair[3];
                msgHeaders[0] = new HTTPClient.NVPair(AIA_MSG_ID,
                                                      Long.toHexString(ubResponse.m_aiaMsgId) );

                // Build the header that contains the AIA's session connection ID.
                // NOTE:  Because of WebClient, the AIA looks for the session id as a
                // Query parameter in the URL (?name=value).  So just comment out the
                // header setting but leave it around in case it changes.
                //

                msgHeaders[1] = new NVPair(CONNECT_ID_IN_HEADER,
                                           ((null == m_aiaConnectionId) ? "0" : m_aiaConnectionId));

                msgHeaders[2] = new NVPair( CCID_MSG_ID, ((null == ccid) ? "0" : ccid));

                if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                    m_loggingObj.logBasic(  m_debugLogIndex,
                                          "POST " + ubResponse.m_httpOperationName +
                                          " message " +
                                          Long.toHexString(ubResponse.m_aiaMsgId) +
                                          " to aia, " +
                                          outBytes.length +
                                          " bytes sent.");

                m_httpState = HTTP_STATE_POST;
                m_httpThread = Thread.currentThread();

                ubResponse.m_ubMsgResponse = connection.Post(m_connectionIdQuery,
                                                             outBytes,
                                                              msgHeaders);

                if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                    m_loggingObj.logBasic(  m_debugLogIndex,
                                          "POST " + ubResponse.m_httpOperationName +
                                          " operation complete.");
            }

            // Show that we're waiting for the response to show up.
            // Then we can evaulate the operation.
            //
            m_httpState = HTTP_STATE_GET_RESPONSE_HEADERS;

        }
        catch (HTTPClient.ModuleException e)
        {
            m_httpState = HTTP_STATE_IDLE;
            m_httpThread = null;

            // Make a copy so that the exception points to the right
            // location.
            NetworkProtocolException npe = new NetworkProtocolException(NetworkProtocolException.BASE_MSG,
                                                                        m_protocolTypeName,
                                                                        e.toString());
            m_loggingObj.logStackTrace("",
                                       npe);
            // Destroy the response, it's not useful.
            //
            ubResponse = null;

            throw npe;
        }
        catch (Exception e)
        {
            m_loggingObj.logStackTrace("General Exception in sendHttpMessage()",
                                       e);

            // Destroy the response, it's not useful.
            //
            ubResponse = null;

            NetworkProtocolException npe = new NetworkProtocolException(NetworkProtocolException.BASE_MSG,
                                                                        m_protocolTypeName,
                                                                        e.toString());
            throw npe;
        }

        return(ubResponse);
    }

    /*
     * <!-- getHttpResponse() -->
     * <p>Get the next queued http response and process it.
     * </p>
     * <br>
     * @param connection is a HTTPConnection object to perform the operation
     * on
     * @param ubResponse is a UBMessageResponse object that holds the context
     * of the http server response
     * <br>
     * @exception   IOException
     * @exception   NetworkProtocolException
     */
    protected void getHttpResponse(HTTPConnection       connection,
                                   UBMessageResponse    ubResponse)
        throws IOException, NetworkProtocolException
    {

        try
        {

            // Get the http message's status code first, so we are just reading
            // the http response headers.
            //
            ubResponse.m_sendMsgResponseCode = ubResponse.m_ubMsgResponse.getStatusCode();

            if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                m_loggingObj.logBasic(  m_debugLogIndex,
                                      "Obtained " + ubResponse.m_httpOperationName +
                                      " response for message " +
                                      ubResponse.m_aiaMsgId + " from aia: " +
                                      Integer.toString(ubResponse.m_sendMsgResponseCode));

            boolean msgError = evaluateResponseCode(ubResponse);
            if (msgError)
            {
                // An error indicates that we're finished handling this response
                // so set the state back to idle so that we can perform the
                // next message operation.
                //
                m_httpState = HTTP_STATE_IDLE;
                m_httpThread = null;

                // Make a copy so that the exception points to the right
                // location.
                NetworkProtocolException npe = new NetworkProtocolException(ubResponse.m_sendMsgExceptionMsgCode,
                                                                            ubResponse.m_sendMsgExceptionErrorDetail);
                m_loggingObj.logStackTrace("",
                                           npe);
                throw npe;
            }

            // Get the response data here.  The HttpClientMsgInputStream will
            // subsequently call getInputStream() on the HTTPResponse to read
            // the data.
            //
            m_httpState = HTTP_STATE_GET_RESPONSE_DATA;

            if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                m_loggingObj.logBasic(  m_debugLogIndex,
                                      "Obtaining " +
                                      ubResponse.m_httpOperationName +
                                      " response data for message " +
                                      ubResponse.m_aiaMsgId + " ...");

            // This will block until "all" the message data has been read.  So
            // if the AIA sends a very large "chunked" message, this could take
            // a LONG time.
            //
            ubResponse.m_ubMsgResponse.getData();

            // Now that we've gotten the data, we can now go back into idle
            // to handle the next message operation.
            //
            m_httpState = HTTP_STATE_IDLE;
            m_httpThread = null;

            // Get the input [ByteArrayInputStream] and record the single
            // instance for all readers to use.  Otherwise we get multiple
            // copies of the data if we call this multiple times.  That would
            // be a very bad thing...
            //
            ubResponse.m_httpInputStream = ubResponse.m_ubMsgResponse.getInputStream();

            if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                m_loggingObj.logBasic(  m_debugLogIndex,
                                      "Obtained " +
                                      ubResponse.m_httpInputStream.available() +
                                      " data bytes from aia for message " +
                                      ubResponse.m_aiaMsgId + " .");

            // dump the returned headers for debugging purposes...
            //
/*****
            if (!m_loggingObj.ignore(Logger.LOGGING_TRACE))
            {
                Enumeration headers = ubResponse.m_ubMsgResponse.listHeaders();
                if (null != headers)
                {
                    while (headers.hasMoreElements())
                    {
                        String  header = (String)headers.nextElement();
                        String  value = ubResponse.m_ubMsgResponse.getHeader(header);
                        m_loggingObj.LogMsgln(m_loggingDest,
                                              Logger.LOGGING_TRACE,
                                              Logger.TIMESTAMP,
                                              "AIA response message " +
                                              ubResponse.m_aiaMsgId +
                                              " header: " + header +
                                              " = '" + value + "'");
                    }
                }
                else
                {
                    m_loggingObj.LogMsgln(m_loggingDest,
                                          Logger.LOGGING_TRACE,
                                          Logger.TIMESTAMP,
                                          "No AIA response headers found in message " +
                                          ubResponse.m_aiaMsgId + " .");
                }
            }
*****/

            // Check for the Connection: stop signal from the server.  It
            // indicates that the persistent connection is being closed.
            //
            String  stopHeader = ubResponse.m_ubMsgResponse.getHeader("Connection");
            if (null != stopHeader)
            {
                if (stopHeader.equalsIgnoreCase("stop"))
                {
                    try
                    {
/*****
                        if (!m_loggingObj.ignore(Logger.LOGGING_TRACE))
                        {
                            m_loggingObj.LogMsgln(m_loggingDest,
                                                  Logger.LOGGING_TRACE,
                                                  Logger.TIMESTAMP,
                                                  "Closing HTTP connection due " +
                                                  "to response message " +
                                                  ubResponse.m_aiaMsgId +
                                                  " Connection header " +
                                                  "containing STOP.");
                        }
*****/
                        closeHttpConnection(connection, connection.getContext());
                    }
                    catch (Exception e)
                    {
                    }
                    finally
                    {
                        connection = null;
                    }
                }
            }

        }
        catch (HTTPClient.ModuleException e)
        {
            m_httpState = HTTP_STATE_IDLE;
            m_httpThread = null;

            // Make a copy so that the exception points to the right
            // location.
            NetworkProtocolException npe = new NetworkProtocolException(NetworkProtocolException.BASE_MSG,
                                                                        m_protocolTypeName,
                                                                        e.toString());
            m_loggingObj.logStackTrace("",
                                       npe);
            throw npe;
        }
        catch (Exception e)
        {
            m_loggingObj.logStackTrace("General Exception in sendHttpMessage()",
                                       e);

            NetworkProtocolException npe = new NetworkProtocolException(NetworkProtocolException.BASE_MSG,
                                                                        m_protocolTypeName,
                                                                        e.toString());
            throw npe;
        }

    }

    /* (non-Javadoc)
     * @see com.progress.ubroker.util.INetworkProtocol#getSSLSubjectName()
     */
    public String getSSLSubjectName()
    {
        // This method does not apply to this protocol.
        return null;
    }

    /*
     * PRIVATE METHODS:
     */

    private String extractCCID(byte[] msgbuf)
    {
        String ret = null;

        if (msgbuf != null)
        {
            try
                {
                    ubMsg ubmsg = ubMsg.newMsg(msgbuf, 
                                               0,
                                               msgbuf.length,
                                               loggingObject());
                    if (ubmsg != null)
                    {
                        ret = ubmsg.getTlvField(ubMsg.TLVTYPE_CLIENT_CONTEXT);
                    }
                }
            catch (Exception ex)
                {
                    ret = null;
                }
        }

        return ret;
    }


}

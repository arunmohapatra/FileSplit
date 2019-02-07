/*
/* <p>Copyright 2000-2009 Progress Software Corporation, All rights reserved.</p>
/* <br>
/* <p>Class:        AppSrvClientProtocol   </p>
/* <br>
/* All rights reserved.  No part of this program or document
/* may be  reproduced in  any form  or by  any means without
/* permission  in  writing  from  Data Language Corporation.
/*
*/

package com.progress.ubroker.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Properties;

import com.progress.common.ehnlog.IAppLogger;
import com.progress.nameserver.client.NameServerClient;
import com.progress.open4gl.dynamicapi.IPoolProps;
import com.progress.ubroker.util.INetworkProtocol;
import com.progress.ubroker.util.IubMsgInputStream;
import com.progress.ubroker.util.IubMsgOutputStream;
import com.progress.ubroker.util.MsgInputStream;
import com.progress.ubroker.util.MsgReader;
import com.progress.ubroker.util.NetworkProtocolException;
import com.progress.ubroker.util.RequestQueue;
import com.progress.ubroker.util.SocketConnectionInfoEx;
import com.progress.ubroker.util.ubConstants;
import com.progress.ubroker.util.ubMsg;

/**
 * <p>The TcpClientProtocol class will handle management of the AppServer protocol
 * over standard TCP/IP sockets.  It will utilize a Progress Name Server if
 * required in order to resolve an AppServer's IP address and port.  After
 * contacting the AppServer, it will handle any reconnection required to
 * reconnect a TCP/IP link to an Application Server Process.
 * </p>
 * <p>The init() method's protocolInitArgs currently does not set protocol
 * specific options.  So the argument may be passed as "null".  If a Properties
 * object is specified, its contents are ignored.
 * </p>
 * <p>The protocol properties used by this protocol handler are:
 * </p>
 * <ul>
 * <li>psc.tcp.nameserver, If set to 'no' it will inhibit the use of a NameServer to
 * obtain the address and port of the AppServer's ubroker.  The url's host and
 * port values are direct addresses of the ubroker.
 * <li>psc.tcp.sotimeout, Is an integer timeout value to set the TCP socket options
 * SOTIMEOUT to.  If not specified, the network stack's default is used.
 * </ul>
 */
public class TcpClientProtocol extends NetworkClientProtocol
    implements INetworkProtocol, ubConstants
{
    /*
     * CLASS Constants
     * private static final <type>  <name> = <value>;
     */
    /*
     * CLASS Properties.
     * public static        <type>  <name> = <value>;
     */

    /*
     * Super Object Properties.
     *  protected       <type>          <name> = <value>;
     */

    /*
     * Object Instance Properties.
     *  private         <type>          <name> = <value>;
     */
    private     Socket                  m_appServerSocket = null;
    private     RequestQueue            m_requestQueue = null;
    private     MsgReader               m_msgReader = null;

    /*
     * Constructors...
     */

    /**
    * <!-- TcpClientProtocol() -->
    * <p>The default class constructor.
    * </p>
    * <br>
    * @param
    * <br>
    * @return   void
    * <br>
    */
    public TcpClientProtocol()
    {
        // Nothing to do right now.
    }

    /*
     * ACCESSOR METHODS:
     */

    /**
     * <!-- getRawSocket() -->
     * <p> Get a reference to the raw Socket object that supports the underlying
     * TCP/IP communications.
     * </p>
     * <br>
     * @return  Socket
     * <br>
     */
    public Socket           rawSocket()
    {
        return(m_appServerSocket);
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
        IubMsgInputStream       msgInputStream = null;

        switch (streamType)
        {
            case INetworkProtocol.MSG_STREAM_UB_BINARY:
                if (null != m_appServerSocket)
                {
                    msgInputStream = new TcpClientMsgInputStream(this,
                                                                 m_appServerSocket.getInputStream(),
                                                                 m_progressServerType);
                }
                break;
            default:
                NetworkProtocolException npe = new NetworkProtocolException(NetworkProtocolException.BASE_MSG,
                                                   "TCP",
                                                   "Unsupported Input Stream type requested.");
                m_loggingObj.logStackTrace("",
                                           npe);
 /////          m_trace.print(npe, 1);
                throw npe;
        }
        return(msgInputStream);
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
        IubMsgOutputStream      msgOutputStream = null;

        switch (streamType)
        {
            case INetworkProtocol.MSG_STREAM_UB_BINARY:
                if (null != m_appServerSocket)
                {
                    msgOutputStream = new TcpClientMsgOutputStream(this,
                                                                   m_appServerSocket.getOutputStream(),
                                                                   m_progressServerType);
                }
                break;
            default:
                NetworkProtocolException npe = new NetworkProtocolException(NetworkProtocolException.BASE_MSG,
                                                   "TCP",
                                                   "Unsupported Output Stream type requested.");
                m_loggingObj.logStackTrace("",
                                           npe);
/////           m_trace.print(npe, 1);
                throw npe;
        }
        return(msgOutputStream);
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
     * specific protocols).  You may enter null to remove a property.
     * <br>
     * @return  void
     * <br>
     * @exception   Exception
     */
    public void setDynamicProtocolProperty(String  propertyName,
                                           String  propertyValue) throws Exception
    {
        // Have the base class do the storage update...
        //
        super.setDynamicProtocolProperty(propertyName, propertyValue);

        if (propertyName.equalsIgnoreCase( IPoolProps.SOCKET_TIMEOUT ))
        {
            // Set the dynamic property if we have a real value.
            if (null != propertyValue &&
                0 < propertyValue.length())
            {
                try
                {
                    int sotimeout = Integer.parseInt(propertyValue);
                    if ((null != m_appServerSocket) && (sotimeout >= 0))
                    {
                        m_appServerSocket.setSoTimeout(sotimeout);
                    }
                }
                catch (Exception e)
                {

                if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                    m_loggingObj.logBasic(m_debugLogIndex,
                                          "Error setting property psc.tcp.sotimeout: " +
                                          e.getMessage());
                }
            }
        }

        if (propertyName.equalsIgnoreCase( IPoolProps.TCP_NODELAY ))
        {
            // Set the dynamic property if we have a real value.
            if (null != propertyValue &&
                0 < propertyValue.length())
            {
                try
                {
                    int nodelay = Integer.parseInt(propertyValue);
                    if (null != m_appServerSocket)
                    {
                        m_appServerSocket.setTcpNoDelay((nodelay != 0));
                    }
                }
                catch (Exception e)
                {

                if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                    m_loggingObj.logBasic(m_debugLogIndex,
                                          "Error setting property tcpNoDelay: " +
                                          e.getMessage());
                }
            }
        }
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
     * PUBLIC METHODS:
     */

    /**
     * <!-- init() -->
     * <p>Initialize [and connect] the network protocol handling class.
     * </p>
     * <br>
     * @param protocolOptions a Properties object with protocol specific initialization
     * options.  The TCP protocol does not support protocol options, this
     * parameter may be null.
     * @param loggingObject a AppLogger object to use in tracing operations, errors
     * and 3rd party exceptions.
     * @param loggingDestingation an int that holds a AppLogger.DEST_XXXXXX value
     * that sets the destination for logging.
     * <br>
     * @return  void
     * <br>
     * @exception   Exception
     * @exception   NetworkProtocolException
     */
    public void             init(Properties             protocolOptions,
                                 IAppLogger             loggingObject,
                                 int                    loggingDestination)
                                 throws Exception, NetworkProtocolException
    {
        super.init(protocolOptions, loggingObject, loggingDestination);

        // Do any one-time TCP protocol init here.
        //
        /* Create the request queue for the message reader */
        if (Integer.parseInt(protocolOptions.getProperty(IPoolProps.DISABLE_READ_THREAD,"1")) == 0 )
        {
            m_requestQueue = new RequestQueue(m_protocolTypeName, 
                                              RequestQueue.NOQUEUELIMIT, 
                                              m_loggingObj);
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
        // Force close any outstanding connections.
        closeConnection(true);
        // Speed up garbage collection by releasing the ref counts on these
        // objects.

        if (m_msgReader != null)
        {
            m_msgReader.close();
            m_msgReader = null;
        }
        if (m_requestQueue != null)
        {
            m_requestQueue.close();
            m_requestQueue = null;
        }

        m_loggingObj = null;
        m_protocolProperties = null;
    }


    /**
     * <!-- resolveConnectionInfo() -->
     * <p> Resolve the AppServer's uBroker IP address and port.
     * Use the Name Server if necessary.
     * </p>
     * <br>
     * @param connectInfo a SocketConnectionInfoEx object that will contain the
     * resolved AppServer IP connection information.
     * <br>
     * @return  void
     * <br>
     * @exception   Exception
     */
    public void resolveConnectionInfo(SocketConnectionInfoEx connectInfo)
        throws Exception
    {
        NameServerClient ns = null;
        NameServerClient.Broker brokerInfo = null;
 
        // Only resolve using name server if not directly connecting.
        if (connectInfo.isDirectConnect())
            return;

        // These two calls will bubble up ConnectException to the client
        //
        ns = new NameServerClient(connectInfo.getHost(), connectInfo.getPort(),"AS");
        brokerInfo = ns.getBroker(connectInfo.getService());

        // Reset connectionInfo with broker info from NameServer
        //
        if (brokerInfo != null)
        {
            connectInfo.setHost(brokerInfo.getHost());
            connectInfo.setPort(brokerInfo.getPort());
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
     * server type we will establish a connection to.
     * @param connectionOptions a Properties object with connection specific
     * initialization options. The TCP network protocol does not support
     * connection specific options.  This parameter may be null.
     * @param userId an Object holding the user credentials that are
     * required by the network protocol to establish a communications channel.
     * The TCP protocol does not use this parameter.
     * @param password a String object holding the password to use in
     * authenticating the client to the network communications channel.
     * The TCP protocol does not use this parameter.
     * <br>
     * @return  void
     * <br>
     * @exception   Exception
     * @exception   NetworkProtocolException
     */
    public void openConnection(SocketConnectionInfoEx   connectInfo,
                               int                      progressServerType,
                               Properties               connectionOptions,
                               Object                   userId,
                               String                   password)
                               throws Exception, NetworkProtocolException
    {
        if (null == m_appServerSocket)
        {
            // A socket connection doesn't exist so create one.
            //
            if (null == connectInfo)
            {
                NullPointerException npe = new NullPointerException("Cannot initialize with a null Socket Connection Information");
                m_loggingObj.logStackTrace("",
                                           npe);
//////          m_trace.print(npe, 1);
                throw npe;
            }
            // Open a raw socket to the end-point.
            try
            {
                if (m_loggingObj.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                    m_loggingObj.logBasic(m_debugLogIndex,
                                          "Creating new Socket connection...");
                long start_time=0, end_time=0;
				int connTimeout = 0;
                String timeout = m_protocolProperties.getProperty(
                        IPoolProps.CONN_TIMEOUT );
				if(timeout !=null)
                 connTimeout = Integer.parseInt(timeout);
                if(connTimeout > 0)
                {
                 m_appServerSocket = new Socket();
                start_time = System.currentTimeMillis();
                m_appServerSocket.connect(new InetSocketAddress(connectInfo.getHost(), connectInfo.getPort()),connTimeout);
                 end_time = System.currentTimeMillis();
                }
                else
                {
                	 m_appServerSocket = new Socket(connectInfo.getHost(), connectInfo.getPort());
                }
                
                if(connTimeout >0)
                {
                	connTimeout = connTimeout-(int)(end_time -start_time);
                if(connTimeout >= 0)
                m_appServerSocket.setSoTimeout(connTimeout);
                else
                	throw new Exception("connect timed out");
                }
                // Set any socket options
              /*  String soTimeout = m_protocolProperties.getProperty(
                                IPoolProps.SOCKET_TIMEOUT );
                if (null != soTimeout)
                {
                    setDynamicProtocolProperty( IPoolProps.SOCKET_TIMEOUT,
                                                       soTimeout);
                } */

                /* create the message reader for this protocol */
                if (m_requestQueue != null)
                {
                    MsgInputStream msgis = new MsgInputStream(m_appServerSocket.getInputStream(),
                                                              MSG_INPUT_STREAM_BUFSIZE,
                                                              m_progressServerType,
                                                              m_loggingObj);
                    m_msgReader = MsgReader.newMsgReader(m_protocolTypeName, 
                                                         m_appServerSocket,
                                                         msgis, 
                                                         m_requestQueue, 
                                                         m_loggingObj);
                }
            }
            catch (Exception e)
            {
                m_appServerSocket = null;
                throw e;
            }

            // Record the type of end-point.
            m_progressServerType = progressServerType;
        }
        else
        {
            NetworkProtocolException npe = new NetworkProtocolException(NetworkProtocolException.NETWORK_PROTOCOL_ERROR,
                                               "TCP",
                                               "A connection is already established");
            m_loggingObj.logStackTrace("",
                                       npe);
/////       m_trace.print(npe, 1);
            throw npe;
        }
    }

    /**
     * <!-- closeConnection() -->
     * <p> Dissolve the network connection to an AppServer's uBroker or
     * application server process.  It will close the raw TCP/IP socket if it
     * is open.
     * </p>
     * @param forceClose is a boolean that indicates to force the message
     * stream references to null even if closing the stream throws an
     * exception.  You should normally pass false for this argument.
     * <br>
     * @return  void
     * <br>
     * @exception   Exception
     * @exception   NetworkProtocolException
     */
    public void closeConnection(boolean forceClose)
        throws Exception, NetworkProtocolException
    {
        if (null != m_appServerSocket)
        {
            // Close the socket,
            try
            {
                if (null != m_appServerSocket)
                {
                    m_appServerSocket.close();
                }
            }
            catch (Exception e)
            {
                if (!forceClose)
                    throw e;
            }
            m_appServerSocket = null;

            if (m_msgReader != null)
            {
                m_msgReader.close();
                m_msgReader = null;
            }

            // Now reset member variables to defaults.
            m_endPointType = INetworkProtocol.END_POINT_CLIENT;
            m_protocolType = INetworkProtocol.PROTOCOL_APPSERVER;
            m_progressServerType = ubMsg.UBTYPE_APPSERVER;
        }
    }

    /* (non-Javadoc)
     * @see com.progress.ubroker.util.INetworkProtocol#getSSLSubjectName()
     */
    public String getSSLSubjectName()
    {
        // This method does not apply to this protocol
        return null;
    }
    
    public MsgReader getReader()
    {
	return (m_msgReader);
    }

    public RequestQueue getQueue()
    {
	return (m_requestQueue);
    }

    /*
     * PROTECTED (SUPER) METHODS:
     */

    /**
     * Sets the underlying socket for this protocol.
     * 
     * @param socket  the new socket
     */
    public void setRawSocket( Socket socket ) 
    {
        this.m_appServerSocket = socket;

        /* create the message reader for this protocol */
        try
        {
            if (m_requestQueue != null)
            {
                m_msgReader = null;  // old reader not valid for this protocol

        	MsgInputStream msgis = new MsgInputStream(m_appServerSocket.getInputStream(),
                                                          MSG_INPUT_STREAM_BUFSIZE,
                                                          m_progressServerType,
                                                          m_loggingObj);
                m_msgReader = MsgReader.newMsgReader(m_protocolTypeName, 
                                                     m_appServerSocket,
                                                     msgis, 
                                                     m_requestQueue, 
                                                     m_loggingObj);
            }
        }
        catch (IOException e)
        {
            m_msgReader = null;
            m_requestQueue = null;
        }
    }

    /*
     * PRIVATE METHODS:
     */

}

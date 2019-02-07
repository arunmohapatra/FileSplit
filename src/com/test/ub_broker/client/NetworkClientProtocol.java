/*
/* <p>Copyright 2000-2004 Progress Software Corportation, All rights reserved.</p>
/* <br>
/* <p>Class:        NetworkClientProtocol  </p>
/* <br>
/* All rights reserved.  No part of this program or document
/* may be  reproduced in  any form  or by  any means without
/* permission  in  writing  from  Data Language Corporation.
/*
*/

package com.progress.ubroker.client;

import java.net.Socket;
import java.util.Properties;

import com.progress.common.ehnlog.AppLogger;
import com.progress.common.ehnlog.IAppLogger;
import com.progress.common.ehnlog.O4glLogContext;
import com.progress.common.ehnlog.RestLogContext;
import com.progress.common.ehnlog.UBrokerLogContext;
import com.progress.common.ehnlog.WsaLogContext;
import com.progress.common.ehnlog.NxGASLogContext;
import com.progress.ubroker.util.INetworkProtocol;
import com.progress.ubroker.util.IubMsgInputStream;
import com.progress.ubroker.util.IubMsgOutputStream;
import com.progress.ubroker.util.NetworkProtocolException;
import com.progress.ubroker.util.SocketConnectionInfoEx;
import com.progress.ubroker.util.ubMsg;

/**
 * Provides socket-based communications for a client connecting to an AppServer
 */
public abstract class NetworkClientProtocol
    implements INetworkProtocol
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
    protected   Properties              m_protocolProperties = null;
    protected   int                     m_endPointType = INetworkProtocol.END_POINT_CLIENT;
    protected   int                     m_protocolType = INetworkProtocol.PROTOCOL_TCP;
    protected   String                  m_protocolTypeName = INetworkProtocol.m_protocolTypeNames[INetworkProtocol.PROTOCOL_TCP];
    protected   String                  m_endPointTypeName = INetworkProtocol.m_endPointTypeNames[END_POINT_CLIENT];
    protected   IAppLogger              m_loggingObj = null;
    protected   int                     m_loggingDest = IAppLogger.DEST_NONE;
    protected   int                     m_progressServerType = ubMsg.UBTYPE_APPSERVER;
//  protected   Tracer                  m_trace = RunTimeProperties.tracer;

    protected   long                    m_debugLogEntries;
    protected   int                     m_debugLogIndex;
    /*
     * Constructors...
     */

    /**
    * <!-- NetworkClientProtocol() -->
    * <p>The default class constructor.
    * </p>
    * <br>
    * @param
    */
    public NetworkClientProtocol()
    {
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
     * @exception   NetworkProtocolException
     */
    public void             init(Properties             protocolOptions,
                                 IAppLogger             loggingObject,
                                 int                    loggingDestination)
                                 throws Exception, NetworkProtocolException
    {
        // setup properties.
        //
        m_protocolProperties = new Properties( protocolOptions );

        //  setup the logging
        //
        m_loggingDest = loggingDestination;

        if (null == loggingObject)
        {
            // Create our own Applogger that is mute.
            m_loggingObj = new AppLogger();
        }
        else
        {
            // reference the caller's applogger.
            m_loggingObj = loggingObject;
        }

        /* initialize log settings based on interface */
        initializeLogging(m_loggingObj);
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
    public abstract void release() throws Exception;

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
    public abstract void resolveConnectionInfo(SocketConnectionInfoEx connectInfo)
        throws Exception;


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
     * initialization options.  See the documentation for the specific protocol
     * class type for a list of property names and their value ranges.
     * @param userId a Object holding the user credentials that are
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
     * @exception   NetworkClientException
     */
    public abstract void openConnection(SocketConnectionInfoEx   connectInfo,
                                        int                      progressServerType,
                                        Properties               connectionOptions,
                                        Object                   userId,
                                        String                   password)
                                        throws Exception, NetworkProtocolException;


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
     * @exception   NetworkProtocolException
     */
    public abstract void closeConnection(boolean forceClose)
        throws Exception, NetworkProtocolException;


    /**
     * <!-- rawSocket() -->
     * <p> Get a reference to the raw Socket object that supports the underlying
     * TCP/IP communications.
     * </p>
     * <br>
     * @return  Socket
     * <br>
     */
    public abstract Socket           rawSocket();


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
    public abstract IubMsgInputStream  getMsgInputStream(int streamType) throws Exception;


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
    public abstract IubMsgOutputStream getMsgOutputStream(int StreamType) throws Exception;


    /**
     * <!-- endPointType() -->
     * <p> Retrieve the end-point type (INetworkProtocol::END_POINT_XXXX) the
     * class represents.
     * </p>
     * <br>
     * @return  int
     * <br>
     */
    public int              endPointType()
    {
        return(m_endPointType);
    }


    /**
     * <!-- protocolType() -->
     * <p> Retrieve the network protocol type (INetworkProtocol::PROTOCOL_XXXX)
     * the class represents.
     * </p>
     * <br>
     * @return  int
     * <br>
     */
    public int              protocolType()
    {
        return(m_protocolType);
    }


    /**
     * <!-- protocolName() -->
     * <p> Retrieve the network protocol type string name the class represents.
     * </p>
     * <br>
     * @return  String
     * <br>
     */
    public String           protocolName()
    {
        return(m_protocolTypeName);
    }


    /**
     * <!-- endPointName() -->
     * <p> Retrieve the end-point type string name the class represents.
     * </p>
     * <br>
     * @return  String
     * <br>
     */
    public String           endPointName()
    {
        return(m_endPointTypeName);
    }


    /**
     * <!-- loggingObject() -->
     * <p> Return the logging sink attached to this object.
     * </p>
     * <br>
     * @return  AppLogger
     * <br>
     */
    public IAppLogger           loggingObject()
    {
        return(m_loggingObj);
    }


    /**
     * <!-- loggingDestination() -->
     * <p>Retrieve the destination for the logging object.
     * </p>
     * <br>
     * @return  int
     */
    public  int loggingDestination()
    {
        return(m_loggingDest);
    }


    /**
     * <!-- progressServerType() -->
     * <p> Returns the type of Progress server this protocol is connecting to.
     * </p>
     * <br>
     * @return  int
     * <br>
     */
    public  int progressServerType()
    {
        return(m_progressServerType);
    }


    /**
     * <!-- getProtocolProperties() -->
     * <p>Get the Properties object that holds the runtime control properties
     * for the network protocol.
     * Note, any property set through the
     * Properties.setProperty() method will not take effect immediately.
     * </p>
     * <br>
     * @return  Properties NOTE: this may return null, depending upon the
     * network protocol handler class implementation.
     * <br>
     * @exception   Exception
     */
    public  Properties getProtocolProperties() throws Exception
    {
        return(m_protocolProperties);
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
        if (null != propertyName &&
            0 < propertyName.length())
        {
            // Do the default update of the properties information.
            //

            if (null == propertyValue)
            {
                // remove the property exists...
                if (m_protocolProperties.containsKey(propertyName))
                {
                    m_protocolProperties.remove(propertyName);
                }
            }
            else
            {
                if (m_protocolProperties.containsKey(propertyName))
                {
                    // Only update if a different value.
                    String prevValue = m_protocolProperties.getProperty(propertyName);
                    if (0 != prevValue.compareTo(propertyValue))
                    {
                        // Set the new value (yes, case changes are valid)
                        // m_protocolProperties.setProperty(propertyName, propertyValue);
                        m_protocolProperties.put(propertyName, propertyValue);
                    }
                }
                else
                {
                    // Add what doesn't exist.
                    // m_protocolProperties.setProperty(propertyName, propertyValue);
                    m_protocolProperties.put(propertyName, propertyValue);
                }
            }
        }
        else
        {
            NullPointerException npe = new NullPointerException("Cannot set an unnamed runtime property");
            m_loggingObj.logStackTrace("",
                                       npe);
////////    m_trace.print(npe, 1);
            throw npe;
        }
    }



    /*
     * PROTECTED (SUPER) METHODS:
     */

    protected void initializeLogging(IAppLogger log)
    {
        String contextName = log.getLogContext().getLogContextName();

        if (contextName.equals("Wsa"))
            {
            m_debugLogEntries    = WsaLogContext.SUB_M_UBROKER;
            m_debugLogIndex      = WsaLogContext.SUB_V_UBROKER;
            }
        else if (contextName.equals("Rest"))
            {
             m_debugLogEntries    = RestLogContext.SUB_M_UBROKER;
             m_debugLogIndex      = RestLogContext.SUB_V_UBROKER;
            }
        else if (contextName.equals("O4gl"))
            {
            m_debugLogEntries    = O4glLogContext.SUB_M_UBROKER;
            m_debugLogIndex      = O4glLogContext.SUB_V_UBROKER;
            }
        else if (contextName.equals("UBroker"))
            {
            m_debugLogEntries    = UBrokerLogContext.SUB_M_UB_DEBUG;
            m_debugLogIndex      = UBrokerLogContext.SUB_V_UB_DEBUG;
            }
        else if (contextName.equals("NxGAS"))
            {
            m_debugLogEntries    = NxGASLogContext.SUB_M_NXGASDEBUG;
            m_debugLogIndex      = NxGASLogContext.SUB_V_NXGASDEBUG;
            }
        else
            {
            m_debugLogEntries    = 0;
            m_debugLogIndex      = 0;
            }
    }

    /*
     * PRIVATE METHODS:
     */

}

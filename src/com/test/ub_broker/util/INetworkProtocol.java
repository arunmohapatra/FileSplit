/*
/* <p>Copyright 2000-2004 Progress Software Corportation, All rights reserved.</p>
/* <br>
/* <p>Class:        INetworkProtocol    </p>
/* <br>
/* All rights reserved.  No part of this program or document
/* may be  reproduced in  any form  or by  any means without
/* permission  in  writing  from  Data Language Corporation.
/*
*/

package com.progress.ubroker.util;

import java.net.Socket;
import java.util.Properties;

import com.progress.common.ehnlog.IAppLogger;

/**
 *
 */
public interface INetworkProtocol
{
    /*
     * CLASS Constants
     */
    /** The network protocol class to create is a client end-point */
    public  static final int            END_POINT_CLIENT = 0;
    /** The network protocol class to create is a server end-point */
    public  static final int            END_POINT_SERVER = 1;
    /** The network protocol class to create is a listener end-point */
    public  static final int            END_POINT_LISTENER = 2;
    /* Unknown end-point type. */
    public  static final int            END_POINT_UNSUPPORTED = 3;

    /* Min int value that used to specify an end-point type. */
    public  static final int            END_POINT_MIN = 0;
    /* Max int value that used to specify an end-point type. */
    public  static final int            END_POINT_MAX = 2;

    /** String names associated with the END_POINT_XXXXXX types. */
    public  static final String         m_endPointTypeNames[] = { "client",
                                                                  "server",
                                                                  "listener",
                                                                  "unsupported" };

    /** The network connection is to a Progress AppServer/WebSpped uBroker */
    public  static final int            PROGRESS_SERVER_BROKER = 0;
    /** The network connection is to a Progress AppServer/WebSpped Server process*/
    public  static final int            PROGRESS_SERVER_SERVER = 1;
    /** The network connection is to a Progress AIA */
    public  static final int            PROGRESS_SERVER_AIA    = 2;

    /** String names associated with the PROGESS_SERVER_XXXXXX types */
    public  static final String         m_progressServerNames[] = { "uBroker",
                                                                    "Server",
                                                                    "AIA" };

    /** Create a basic TCP network protocol handler class */
    public  static final int            PROTOCOL_TCP = 0;
    /** Create an AppServer network protocol handler class */
    public  static final int            PROTOCOL_APPSERVER = 1;
    /** Create an HTTP tunneling protocol handler class */
    public  static final int            PROTOCOL_HTTP_TUNNEL = 2;
    /** Create an HTTP/S tunneling protocol handler class */
    public  static final int            PROTOCOL_HTTPS_TUNNEL = 3;
    /** Create an AppServerDC network protocol handler class */
    public  static final int            PROTOCOL_APPSERVERDC = 4;
    /** Create an AppServer SSL protocol handler class */
    public  static final int            PROTOCOL_APPSERVER_SSL = 5;
    /** Create an AppServerDC SSL protocol handler class */
    public  static final int            PROTOCOL_APPSERVERDC_SSL = 6;
    /** Create an SSL protocol handler class */
    public  static final int            PROTOCOL_INTERNAL = 7;
    /** Create an SSL protocol handler class */
    public  static final int            PROTOCOL_UNSUPPORTED = 8;
    /* Min int value that may be used to specify a protocol type */
    public  static final int            PROTOCOL_MIN = 0;
    /* Max int value that may be used to specify a protocol type */
    public  static final int            PROTOCOL_MAX = 7;

    /** String names associated with the network protocol types. */
    public  static final String[]       m_protocolTypeNames = { "tcp",
                                                                "appserver",
                                                                "http",
                                                                "https",
                                                                "appserverDC",
                                                                "AppServerS",
                                                                "AppServerDCS",
                                                                "internal",
                                                                "unsupported"};

    /* List the message stream types handled. */

    /** Create a XxxxxMsgInputSteam or XxxxxMsgOutputStream to handle the
     *  binary UBroker messages. */
    public  static final int            MSG_STREAM_UB_BINARY = 0;

    public  static final int            MSG_STREAM_MIN = 0;

    public  static final int            MSG_STREAM_MAX = 0;

    public  static final String[]       m_streamTypeNames = {"binary UBroker"};

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
     * @param loggingObject a IAppLogger object to use in tracing operations, errors
     * and 3rd party exceptions.
     * @param loggingDestingation an int that holds a IAppLogger.DEST_XXXXXX value
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
                                 throws Exception, NetworkProtocolException;

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
    public void release() throws Exception;

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
    public void resolveConnectionInfo(SocketConnectionInfoEx connectInfo)
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
     * @param userId an Object holding the user credentials that are
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
     * @exception   NetworkProtocolException
     */
    public void openConnection(SocketConnectionInfoEx   connectInfo,
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
    public void closeConnection(boolean forceClose)
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
    public Socket           rawSocket();

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
    public IubMsgInputStream  getMsgInputStream(int streamType) throws Exception;

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
    public IubMsgOutputStream getMsgOutputStream(int streamType) throws Exception;

    /**
     * <!-- endPointType() -->
     * <p> Retrieve the end-point type (INetworkProtocol::END_POINT_XXXX) the
     * class represents.
     * </p>
     * <br>
     * @return  int
     * <br>
     */
    public int              endPointType();

    /**
     * <!-- protocolType() -->
     * <p> Retrieve the network protocol type (INetworkProtocol::PROTOCOL_XXXX)
     * the class represents.
     * </p>
     * <br>
     * @return  int
     * <br>
     */
    public int              protocolType();

    /**
     * <!-- protocolName() -->
     * <p> Retrieve the network protocol type string name the class represents.
     * </p>
     * <br>
     * @return  String
     * <br>
     */
    public String           protocolName();

    /**
     * <!-- endPointName() -->
     * <p> Retrieve the end-point type string name the class represents.
     * </p>
     * <br>
     * @return  String
     * <br>
     */
    public String           endPointName();

    /**
     * <!-- loggingObject() -->
     * <p> Return the logging sink attached to this object.
     * </p>
     * <br>
     * @return  IAppLogger
     * <br>
     */
    public IAppLogger           loggingObject();

    /**
     * <!-- loggingDestination() -->
     * <p>Retrieve the destination for the logging object.
     * </p>
     * <br>
     * @return  int
     */
    public  int loggingDestination();

    /**
     * <!-- progressServerType() -->
     * <p> Returns the type of Progress server this protocol is connecting to.
     * </p>
     * <br>
     * @return  int
     * <br>
     */
    public  int progressServerType();

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
    public  Properties getProtocolProperties() throws Exception;

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
                                           String propertyValue) throws Exception;

    /**
     * Get the SSL subject name (if it exists for this protocol).
     *
     * @return the subject name as a <code>String</code> value, or
     *         <code>null</code> if this is not an SSL-based protocol
     */
    public String getSSLSubjectName();
}

/*
/* <p>Copyright 2000-2001 Progress Software Corportation, All rights reserved.</p>
/* <br>
/* <p>Class:        NetworkProtocolException    </p>
/* <br>
/* All rights reserved.  No part of this program or document
/* may be  reproduced in  any form  or by  any means without
/* permission  in  writing  from  Data Language Corporation.
/*
*/

package com.progress.ubroker.util;

import  java.lang.*;
import  com.progress.common.exception.*;
import  com.progress.message.jcMsg;

/**
 * This class provides the base Network protocol architecture Exception.  More
 * specific Exception conditions are identified with their own class.  Whenever
 * an unexpected or non-classified error occurs, this class will be used.
 *
 */
public class NetworkProtocolException extends ProException implements jcMsg
{
    private static final String[] m_idMsgs = { "An unexpected %s protocol error occured: %s", // id 0
                                               "The %s %s network functionality is not supported.", // id 1
                                               "Invalid %s protocol configuration: %s", // id 2
                                               "Cannot find the support library for the %s protocol.", // id 3
                                               "The %s protocol connection failed: %s", // id 4
                                               "%s server authentication failed: %s", // id 5
                                               "%s client authentication failed: %s", // id 6
                                               "The %s network protocol connection to %s timed out.", // id 7
                                               "The %s connection to %s was aborted: %s", // id 8
                                               "The %s %s authenticaiton was rejected: %s", // id 9
                                               "A %s network protocol error occured: %s", // id 10
                                               "A %s option parameter failed: %s", // id 11
                                               "An AIA error was returned thru %s: %s", // id 12
                                               "%s proxy authentication failed: %s", // id 13
                                               ""}; // MAX_ID

    /* BASE_MSG
     * An unexpected network protocol error occured in Progress or a 3rd
     * party package.  This exception is used when a more detailed condition
     * cannot be determined.  The protocol type and error message are
     * supplied.
     *
     * string:     An unexpected %s protocol error occured: %s
     * arguments:  %s <protocol type {TCP | HTTP | HTTP/S | SSL}>
     *             %x <Protocol/3rd party generated exectption string>
     */
    public  static final int      BASE_MSG = 0;
    /* UNSUPPORTED_PROTOCOL
     *   desc:       The application requested the use of a protocol type or client/server
     *               end-point type that is not supported by this Progress component.
     *               The protocol and end-point type are supplied.
     *   string:     The %s %s network functionality is not supported.
     *   arguments:  %s <protocol type {TCP | HTTP | HTTP/S | SSL}>
     *               %s <end-point type {client | server}>
     */
    public  static final int      UNSUPPORTED_PROTOCOL = 1;
    /*
     * INVALID_PROTOCOL_CONFIGURATION
     * desc:       An invalid configuration directive for a network protocol was
     *             found.  Network communications are not established.  The protocol
     *             type and invalid configuration directive are supplied.
     * string:     Invalid %s protocol configuration: %s
     * arguments:  %s <protocol type {TCP | HTTP | HTTP/S | SSL}>
     *             %s <configuration error description>
     *
     */
    public  static final int     INVALID_PROTOCOL_CONFIGURATION = 2;
    /*
     * CANNOT_FIND_PROTOCOL_LIBRARY
     * desc:       The Progress/3rd party network protocol Java package cannot be
     *             found/loaded.  The name of the network protocol is supplied.
     * string:     Cannot find the support library for the %s protocol.
     * arguments:  %s <protocol type {TCP | HTTP | HTTP/S | SSL}>
     *
     */
    public  static final int     CANNOT_FIND_PROTOCOL_LIBRARY = 3;
    /*
     * PROTOCOL_CONNECTION_FAILED
     * desc:       The protocol's connection request was rejected by the network peer.
     *             The protocol type and rejection reason is supplied.
     * string:     The %s protocol connection failed: %s
     * arguments:  %s <protocol type {TCP | HTTP | HTTP/S | SSL}>
     *             %s <connection handshake failure reason>
     *
     */
    public  static final int     PROTOCOL_CONNECTION_FAILED = 4;
    /*
     * SERVER_AUTHENTICATION_FAILED
     * desc:       The client failed while authenticating the server's identity.  The
     *             protcol type and authentication failure reason is supplied.
     * string:     %s server authentication failed: %s
     * arguments:  %s <protocol type {TCP | HTTP | HTTP/S | SSL}>
     *             %s <server authentication failure reason>
     *
     */
    public  static final int     SERVER_AUTHENTICATION_FAILED = 5;
    /*
     * CLIENT_AUTHENTICATION_FAILED
     * desc:       The server failed while authenticating the client's identity.  The
     *             protcol type and authentication failure reason is supplied.
     * string:     %s client authentication failed: %s
     * arguments:  %s <protocol type {TCP | HTTP | HTTP/S | SSL}>
     *             %s <client authentication failure reason>
     *
     */
    public  static final int     CLIENT_AUTHENTICATION_FAILED = 6;
    /*
     * NETWORK_PROTOCOL_TIMEOUT
     * desc:       The amount of time allowed for a response from the network peer
     *             expired.  The connection no longer exists.  The protocol type and
     *             the peer's network address are supplied.
     * string:     The %s network protocol connection to %s timed out.
     * arguments:  %s <protocol type {TCP | HTTP | HTTP/S | SSL}>
     *             %s <peer IP address>
     *
     */
    public  static final int     NETWORK_PROTOCOL_TIMEOUT = 7;
    /*
     * NETWORK_CONNECTION_ABORTED
     * desc:       The network peer requested a disconnect.  The protocol type and
     *             the network address of the peer are supplied.
     * string:     The %s connection to %s was aborted: %s
     * arguments:  %s <protocol type {TCP | HTTP | HTTP/S | SSL}>
     *             %s <peer IP address>
     *             %s <peer's reason for the disconnect, may be blank>
     *
     */
    public  static final int     NETWORK_CONNECTION_ABORTED = 8;
    /*
     * AUTENTICATION_REJECTED
     * desc:       The authentication credentials was rejected by the peer.  The
     *             network protocol connection could not be established.  The protocol
     *             type, end-point type, and rejection reason are supplied.
     * string:     The %s %s authenticaiton was rejected: %s
     * arguments:  %s <protocol type {TCP | HTTP | HTTP/S | SSL}>
     *             %s <the end-point type {client | server}
     *             %s <the authentication rejection reason>
     *
     */
    public  static final int     AUTHENTICATION_REJECTED = 9;
    /*
     * NETWORK_PROTOCOL_ERROR
     * desc:       A error was detected in a network protocol exchange with the peer.
     *             The network connection was closed.  The protocol type and any
     *             error description information is supplied.
     * string:     A fatal %s network protocol error occured: %s
     * arguments:  %s <protocol type {TCP | HTTP | HTTP/S | SSL}>
     *             %s <the protocol error description>
     *
     */
    public  static final int     NETWORK_PROTOCOL_ERROR = 10;
    /*
     * PROTOCOL_OPTION_ERROR
     * desc:       A error was detected in setting a network protocol option.
     * string:     A %s option parameter failed: %s
     * arguments:  %s <protocol type {TCP | HTTP | HTTP/S | SSL}>
     *             %s <the protocol option error description>
     */
    public  static final int     PROTOCOL_OPTION_ERROR = 11;
    /*
     * AIA_REPORTED_ERROR
     * desc:       A error was reported by the AIA when using http/https tunneling.
     * string:     An AIA error was returned thru %s: %s
     * arguments:  %s <protocol type {TCP | HTTP | HTTP/S | SSL}>
     *             %s <the protocol option error description>
     */
    public  static final int     AIA_REPORTED_ERROR = 12;
    /*
     * PROXY_AUTHENTICATION_FAILED
     * desc:       The proxy server failed while authenticating the client's identity.  The
     *             protcol type and authentication failure reason is supplied.
     * string:     %s proxy authentication failed: %s
     * arguments:  %s <protocol type {TCP | HTTP | HTTP/S | SSL}>
     *             %s <client authentication failure reason>
     *
     */
    public  static final int     PROXY_AUTHENTICATION_FAILED = 13;


    protected   static  final long[]         m_jcMsgTable = {jcMSG158,   // 0
                                                             jcMSG159,   // 1
                                                             jcMSG160,   // 2
                                                             jcMSG161,   // 3
                                                             jcMSG162,   // 4
                                                             jcMSG163,   // 5
                                                             jcMSG164,   // 6
                                                             jcMSG165,   // 7
                                                             jcMSG166,   // 8
                                                             jcMSG167,   // 9
                                                             jcMSG168,   // 10
                                                             jcMSG169,   // 11
                                                             jcMSG170,   // 12
                                                             jcMSG171};  // 13
    /* MAX sub-type id */
    private static final long     MAX_ID = 14;

    /* Store the instance sub-type */
    private int             m_exceptionId = 0;
    private Object[]        m_tokens = null;
    private long            m_jcMsgId = 0;

    /*
     * Constructors...
     */

    /**
    * <!-- NetworkProtocolException() -->
    * <p>The default class constructor.
    * </p>
    * <br>
    */
    public NetworkProtocolException()
    {
        super();
    }

    /**
     * <!-- NetworkProtocolException() -->
     * <p> Create a protocol of a specific type.
     * </p>
     * <br>
     * @param   protocolName a String object holding the network protocol name
     * such as TCP, HTTP, etc.
     * <br>
     */
    public NetworkProtocolException(String protocolName, String details)
    {
        // super(BASE_MSG, new Object[]{ protocolName, details });
        super(jcMSG158, new Object[]{ protocolName, details });
        m_exceptionId = BASE_MSG;
        m_jcMsgId = jcMSG158;
        m_tokens = new Object[]{protocolName, details};
    }

    public NetworkProtocolException(long exceptionId, String protocolName, String details)
    {
        // super(m_idMsgs[((MAX_ID > exceptionId) ? exceptionId : 0)],
        super( m_jcMsgTable[((MAX_ID > exceptionId) ? (int) exceptionId : 0)],
              new Object[]{ protocolName, details });
        m_exceptionId = (int) exceptionId;
        m_jcMsgId = m_jcMsgTable[((MAX_ID > exceptionId) ? (int) exceptionId : 0)];
        m_tokens = new Object[]{protocolName, details};
    }

    public NetworkProtocolException(long exceptionId, Object[] details)
    {
        // super(m_idMsgs[((MAX_ID > exceptionId) ? exceptionId : 0)],
        super(m_jcMsgTable[((MAX_ID > exceptionId) ? (int) exceptionId : 0)],
              details);
        m_exceptionId = (int) exceptionId;
        m_jcMsgId = m_jcMsgTable[((MAX_ID > exceptionId) ? (int) exceptionId : 0)];
        m_tokens = details;
    }


    /**
     * <!-- exceptionId() -->
     * <p> Return the particular sub-type of the Network Protocol Exception.
     * </p>
     * <br>
     * @return  long
     */
    public int exceptionId()
    {
        return(m_exceptionId);
    }

    /**
     * <!-- jcMsgId() -->
     * <p> Return the particular jcMsg code for this Network Protocol Exception.
     * </p>
     * <br>
     * @return  long
     */
    public long jcMsgId()
    {
        return(m_jcMsgId);
    }

    /**
     * <!-- messageTokens() -->
     * <p>Returns the Object array that is used in token replacement in the
     * message format string.
     * </p>
     * <br>
     * @return  Object[]
     */
    public  Object[] messageTokens()
    {
        return(m_tokens);
    }

    /**
     * <!-- exceptionMessageFormat() -->
     * <p> Return the (english) formatting string associated with an
     * excetption sub-type id.
     * </p>
     * <br>
     * @param exceptionId an long that specifies the NetworkProtocolException
     * sub-type id.  Example BASE_MSG, UNSUPPORTED_PROTOCOL, etc.
     * <br>
     * @return  String (Note: if the sub-id is not valid an empty string is
     * returned.
     */
    public  String exceptionMessageFormat(long exceptionId)
    {
        return(m_idMsgs[((MAX_ID > exceptionId) ? (int) exceptionId : (int) MAX_ID)]);
    }

}


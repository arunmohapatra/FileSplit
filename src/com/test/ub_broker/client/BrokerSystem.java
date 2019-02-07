/*************************************************************/
/* Copyright (c) 1984-2010 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation. */
/*************************************************************/

/*********************************************************************/
/* Module : BrokerSystem                                             */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.client;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Properties;

import com.progress.auth.ClientPrincipal;
import com.progress.auth.ClientPrincipalException;
import com.progress.common.ehnlog.EsbLogContext;
import com.progress.common.ehnlog.RestLogContext;
import com.progress.common.ehnlog.IAppLogger;
import com.progress.common.ehnlog.O4glLogContext;
import com.progress.common.ehnlog.UBrokerLogContext;
import com.progress.common.ehnlog.WsaLogContext;
import com.progress.common.ehnlog.NxGASLogContext;
import com.progress.common.util.Base64;
import com.progress.message.jcMsg;
import com.progress.open4gl.AppServerIOException;
import com.progress.open4gl.BadURLException;
import com.progress.open4gl.BrokerIOException;
import com.progress.open4gl.ConnectAIAException;
import com.progress.open4gl.ConnectException;
import com.progress.open4gl.ConnectFailedException;
import com.progress.open4gl.ConnectHttpAuthException;
import com.progress.open4gl.ConnectHttpsAuthException;
import com.progress.open4gl.ConnectProtocolException;
import com.progress.open4gl.ConnectProxyAuthException;
import com.progress.open4gl.RunTimeProperties;
import com.progress.open4gl.UnknownHostnameException;
import com.progress.open4gl.broker.Broker;
import com.progress.open4gl.broker.BrokerException;
import com.progress.open4gl.dynamicapi.IPoolProps;
import com.progress.open4gl.dynamicapi.Tracer;
import com.progress.open4gl.javaproxy.Connection;
import com.progress.ubroker.util.INetworkProtocol;
import com.progress.ubroker.util.IubMsgInputStream;
import com.progress.ubroker.util.IubMsgOutputStream;
import com.progress.ubroker.util.Logger;
import com.progress.ubroker.util.MessageCompressor;
import com.progress.ubroker.util.NetworkProtocolException;
import com.progress.ubroker.util.NetworkProtocolFactory;
import com.progress.ubroker.util.SocketConnectionInfoEx;
import com.progress.ubroker.util.ubAppServerMsg;
import com.progress.ubroker.util.ubConstants;
import com.progress.ubroker.util.ubMsg;
import com.progress.ubroker.util.ubMsg.InvalidMsgVersionException;
import com.progress.ubroker.util.ubMsg.InvalidTlvBufferException;
import com.progress.ubroker.util.ubMsg.TlvFieldAlreadyExistsException;
import com.progress.ubroker.util.ubRqInfo;

/*********************************************************************/
/*                                                                   */
/* Class BrokerSystem                                                */
/*                                                                   */
/*********************************************************************/

public class BrokerSystem
    implements ubConstants, Broker, jcMsg
{

/*********************************************************************/
/* Constants                                                         */
/*********************************************************************/

/* stuff from the scaffolding */

public static final byte GOOD_CLOSE = 1;
public static final byte BAD_CLOSE  = 2;
public static final byte DATA       = 3;

// public static final int BUFSIZE = (60*1024);  /* 60K */

/* session states */
static final byte STATE_IDLE               = 0x00;
static final byte STATE_CONNECTED          = 0x01;
static final byte STATE_ALLOCATED          = 0x02;
static final byte STATE_SENDING            = 0x03;
static final byte STATE_RECEIVING          = 0x04;
static final byte STATE_EOF                = 0x05;
static final byte STATE_STOPPING           = 0x06;
static final byte STATE_ERROR              = 0x07;

static final String[] DESC_STATE =
  {
  " STATE_IDLE "
, " STATE_CONNECTED "
, " STATE_ALLOCATED "
, " STATE_SENDING "
, " STATE_RECEIVING "
, " STATE_EOF "
, " STATE_STOPPING "
, " STATE_ERROR "
  };

static final byte ASKSTATE_INIT             = 0x00;
static final byte ASKSTATE_ACTIVITY_TIMEOUT = 0x01;
static final byte ASKSTATE_RESPONSE_TIMEOUT = 0x02;

static final String[] DESC_ASKSTATE =
    {
    "ASKSTATE_INIT"
 ,  "ASKSTATE_ACTIVITY_TIMEOUT"
 ,  "ASKSTATE_RESPONSE_TIMEOUT"
    };

static final String DEF_LOGFILE_BASE = "ubClient.";
static final String DEF_LOGFILE = "BrokerSystem.log";
static final String CONN_MSG = "Connect timed out";
static final boolean CONNECT_TO_BROKER        = true;
static final boolean CONNECT_TO_SERVER        = false;
static final int  SOTIMEOUT = 2000;    /* 2 sec */
static final int  HTTPTIMEOUT = 0;      /* infinity */

static final boolean  TESTFLAG = false;  /* test test test */
static  boolean  conntimeout = false;

static final TlvProps[] TLV_PROPERTIES_RQ = 
{
  new TlvProps(IPoolProps.LOCAL_MAJOR_VERSION,  ubMsg.TLVTYPE_OE_MAJOR_VERS)
, new TlvProps(IPoolProps.LOCAL_MINOR_VERSION,  ubMsg.TLVTYPE_OE_MINOR_VERS)
, new TlvProps(IPoolProps.LOCAL_MAINT_VERSION,  ubMsg.TLVTYPE_OE_MAINT_VERS)
, new TlvProps(IPoolProps.LOCAL_CLIENT_TYPE,    ubMsg.TLVTYPE_CLIENT_TYPE)
, new TlvProps(IPoolProps.CLIENT_CONTEXT_ID,    ubMsg.TLVTYPE_CLIENT_CONTEXT)
, new TlvProps(IPoolProps.CLIENT_PRINCIPAL,     ubMsg.TLVTYPE_CLIENTPRINCIPAL)
, new TlvProps(IPoolProps.NET_BUFFER_SIZE,      ubMsg.TLVTYPE_NETBUFFERSIZE)
};

static final TlvProps[] TLV_PROPERTIES_RSP = 
{
  new TlvProps(IPoolProps.REMOTE_MAJOR_VERSION,  ubMsg.TLVTYPE_OE_MAJOR_VERS)
, new TlvProps(IPoolProps.REMOTE_MINOR_VERSION,  ubMsg.TLVTYPE_OE_MINOR_VERS)
, new TlvProps(IPoolProps.REMOTE_MAINT_VERSION,  ubMsg.TLVTYPE_OE_MAINT_VERS)
, new TlvProps(IPoolProps.REMOTE_CLIENT_TYPE,    ubMsg.TLVTYPE_CLIENT_TYPE)
, new TlvProps(IPoolProps.CLIENT_CONTEXT_ID,     ubMsg.TLVTYPE_CLIENT_CONTEXT)
, new TlvProps(IPoolProps.CLIENT_PRINCIPAL,      ubMsg.TLVTYPE_CLIENTPRINCIPAL)
};

/*********************************************************************/
/* Static Data                                                       */
/*********************************************************************/

/*********************************************************************/
/* Instance Data                                                     */
/*********************************************************************/

int current_state;

private SocketConnectionInfoEx sockInfo = null;

private INetworkProtocol netProtocolHandler = null;

int serverPort;
String connID;

private IubMsgOutputStream  os = null;
private int os_pos;
private byte[] os_buf;
private int os_pos_max;

private IubMsgInputStream is = null;
private ubAppServerMsg imsg;
private int is_pos;

boolean eof;
boolean stop_sent;

int seqnum;

IAppLogger log;
int log_dest;
boolean log_dump_full = false;  // Only dump the first log_dump_max bytes of
                                // read/write data.
int log_dump_max = 200;         // Max message log output size for LOGGING_TRACE.
                                // LOGGING_POLL will dump the full amount.

int conn_info_maxlength = 30000;

// Client tracing support
Tracer trace = RunTimeProperties.tracer;

int serverMode;

short ubProtocolVersion;
String m_requestID;

IPoolProps m_properties;
String m_sslSubjectName;

private    long          m_basicLogEntries;
private    int           m_basicLogIndex;
private    long          m_debugLogEntries;
private    int           m_debugLogIndex;
private    long          m_cmprsLogEntries;
private    int           m_cmprsLogIndex;

private Hashtable m_capabilities;

/* smart connections: store the connect procedure string return value  */
private    String        m_connectionReturnValue;

/* ASK version enabled on this connection */
private    int           m_negotiatedAskVersion;

/* ASK capabilities enabled on this connection */
private    int           m_negotiatedAskCaps;

/* ASK timeouts on this connection */
private    int           m_clientASKActivityTimeout;
private    int           m_clientASKResponseTimeout;
private    int           m_clientASKActivityTimeoutMs;
private    int           m_clientASKResponseTimeoutMs;

private    boolean       m_ASKrqstACKsent;
private    byte          m_ASKstate;
private    String        m_sessionID;
private    long          m_tsLastAccess;

private   int            m_compressionCapabilities;
private   int            m_compressionLevel;
private   int            m_compressionThreshold;

/*********************************************************************/
/* Constructors                                                      */
/*********************************************************************/

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public BrokerSystem(IPoolProps properties, IAppLogger log)
    {
    init(properties, log, IAppLogger.DEST_LOGFILE);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

/*
 * Final cleanup.
 */
protected void finalize() throws Throwable
{
    if (null != netProtocolHandler)
    {
        // close and exit
        //
        if (null != os)
        {
            try
            {
                os.close();
            }
            catch (Exception e)
            {
            }
            os = null;
        }

        if (null != is)
        {
            try
            {
                is.close();
            }
            catch (Exception e)
            {
            }
            is = null;
        }

        try
        {
            netProtocolHandler.closeConnection(true);
            netProtocolHandler.release();
        }
        catch (Exception e)
        {
        }
        netProtocolHandler = null;
    }
}

/*********************************************************************/
/* Public Methods                                                    */
/*********************************************************************/

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void connect(
          String requestID,
          String url,
          String username,
          String password,
          String clientInfo)
    throws BrokerException, ConnectException
{
    int                     ret = 0;
    boolean                 disableNameServer = false;
    String                  nameServer = null;
    SocketConnectionInfoEx  connectionInfo = null;
    Properties              instanceOptions = m_properties.getAsProperties();
    String                  strAskCaps;
    int                     clntAskCaps;
    int                     compressionCaps;


    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
       log.logBasic(m_debugLogIndex,
                    "connect() " +
                    "url= (" + url + ") " +
                    "username= (" + username + ") " +
                    "clientInfo= (" + clientInfo + ")");

    trace.print("BrokerSystem CONNECT: " +
        "url= (" + url + ") " +
        "username= (" + username + ") " +
        "clientInfo= (" + clientInfo + ")", 4);

    // Now generate the socket information (URL)
    //
    try
    {
        connectionInfo = new SocketConnectionInfoEx(url);
        int protocolType = connectionInfo.getProtocolType();
        disableNameServer = connectionInfo.isDirectConnect();

        // If this is the appserver protocol AND we disable use of the
        // NameServer AND the current port is the default NameServer port, then
        // override the port specified in the url with the default AppServer
        // port #.
        //
        if (((INetworkProtocol.PROTOCOL_APPSERVER == protocolType) ||
             (INetworkProtocol.PROTOCOL_APPSERVER_SSL == protocolType)) &&
            disableNameServer &&
            SocketConnectionInfoEx.DEF_PORT_NAMESERVER == connectionInfo.getPort())
        {
            connectionInfo.setPort(SocketConnectionInfoEx.DEF_PORT_APPSERVER);
        }
    }
    catch (MalformedURLException e)
    {
        //throwBrokerException("Bad url format");
        BadURLException beURL = new BadURLException(e.getMessage() + ": " + url);

        log.logStackTrace("", beURL);

        trace.print(beURL, 1);
        throw beURL;
    }

    // If we're not in an idle state, we're not prepared to connect to anybody!
    //
    if (current_state != STATE_IDLE)
    {
        //throwBrokerException("connect:Invalid state= "  +
        //                       DESC_STATE[current_state]);

        InvalidStateException beInvSt = new InvalidStateException("connect",
        DESC_STATE[current_state]);

        log.logStackTrace("", beInvSt);

        trace.print(beInvSt, 1);
        throw beInvSt;
    }

    // Create a Network Protocol Handler object here and initialize it...
    //
    try
    {
        netProtocolHandler = NetworkProtocolFactory.create(INetworkProtocol.END_POINT_CLIENT,
                                                           connectionInfo,
                                                           log,
                                                           log_dest);

        // Now init the handler...
        netProtocolHandler.init(instanceOptions, log, log_dest);

        // then call object to resolve the connection port.  Depending upon
        // the protocol, this may use a NameServer or not.  The HTTP and HTTP/S
        // will not.
        //
        if (!disableNameServer)
        {
            netProtocolHandler.resolveConnectionInfo(connectionInfo);
        }

    }
    catch (MalformedURLException e)
    {
        //throwBrokerException("Bad url format");
        BadURLException beURL = new BadURLException(e.getMessage() + ": " + url);

        log.logStackTrace("", beURL);

        trace.print(beURL, 1);
        throw beURL;
    }
    catch (NetworkProtocolException e1)
    {
        // convert the generic NetworkProtocolException into the OpenClient facility
        BrokerException be = new BrokerException(BrokerException.CODE_PROTOCOL_ERROR,
                                                 e1.getMessage());
        log.logStackTrace("", be);
        trace.print(be, 1);
        throw be;
    }
    catch (Exception e2)
    {
        // General exeption handler.
        BrokerException be = new BrokerException(e2.toString());
        log.logStackTrace("", be);
        trace.print(be, 1);
        throw be;
    }


    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
       log.logBasic(m_debugLogIndex,
                    "connect() " +
                    "host= (" + connectionInfo.getHost() + ") " +
                    "port= (" + connectionInfo.getPort() + ") " +
                    "service= (" + connectionInfo.getService() + ") ");

    trace.print("Connecting to Broker: " +
        "host= (" + connectionInfo.getHost() + ") " +
        "port= (" + connectionInfo.getPort() + ") " +
        "service= (" + connectionInfo.getService() + ") ", 4);


    /* Log ASK message if appropriate */
    strAskCaps = instanceOptions.getProperty(IPoolProps.APPSERVER_KEEPALIVE_CAPS);
    if (askValidateProtocolType(connectionInfo))
        clntAskCaps = parseAskCapabilities(strAskCaps);
    else
        clntAskCaps = IPoolProps.ASK_DISABLED;

    if (clntAskCaps == IPoolProps.ASK_DISABLED)
    {
        trace.print(m_sessionID + "      ASK Disabled.", 2);
    }
    else
    {
        trace.print(m_sessionID + "      Requesting ASK Capabilities= " + strAskCaps, 2);
    }

    // Init the server port.

    serverPort = 0;
    for (ubProtocolVersion = ubMsg.MAX_UBVER;
            ubProtocolVersion >= ubMsg.MIN_UBVER;
               ubProtocolVersion--)
    {
    try
        {
        ret = processConnect(requestID,
                             connectionInfo,
                             username,
                             password,
                             clientInfo,
                             CONNECT_TO_BROKER,
                             clntAskCaps);

 
        if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
            log.logBasic(m_debugLogIndex,
                         "BrokerSystem processConnect(): got ubver= " + 
                         ubProtocolVersion);
        break;
        }
    catch (UnknownHostException e)
        {
        UnknownHostnameException hostexp = new UnknownHostnameException(connectionInfo);
        // Exceptions derived from Open4GLError don not need to be traced
        //trace.print(hostexp.toString(), 1);

        log.logStackTrace("", hostexp);

        throw hostexp;
        }
    catch (IOException e)
        {
        BrokerIOException conioexp = 
               new BrokerIOException(connectionInfo, e.toString());

        // Exceptions derived from Open4GLError don not need to be traced
        //trace.print(conioexp.toString(), 1);

        log.logStackTrace("", conioexp);

        throw conioexp;
        }

    catch (BrokerException be)
        {
        /* if the broker doesn't recognize the client's protocol version      */
        /* the broker will disconnect.  we need to keep trying with decreasing*/
        /* protocol numbers until we get a match or we reach ubMsg.MIN_UBVER  */
        /* if we reach MIN_UBVER, then rethrow the exception                  */

        /* PROBLEM:  when we disconnect, and recall processConnect(), we may  */
        /*           actually hit a different broker (due to load balancing). */
        /*           therefore all brokers which serve a single application   */
        /*           service must be the same protocol version!!!             */

        if (ubProtocolVersion > ubMsg.MIN_UBVER)
            {
            disconnectSocket(); /* we will reconnect this when we retry */
            if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                log.logBasic(m_debugLogIndex,
                             "BrokerSystem : error on ver= " + ubProtocolVersion);
            }
        else throw be;
        }
    }

    /* check to see if we need to reconnect to server */

    if ((ret == ubMsg.UBRSP_OK) && (serverPort != 0))
    {
        disconnectSocket();
        connectionInfo.setPort(serverPort);
        serverMode = SERVERMODE_STATE_AWARE;
        try
        {
            /* here we pass in m_negotiatedAskCaps since this */
            /* reflects the negotiated values we got from the */
            /* broker.  We pass these on to the agent process.*/
            /* The agent can use them "as is", since the      */
            /* negotiation is already done.                   */

            ret = processConnect(requestID,
                                 connectionInfo,
                                 username,
                                 password,
                                 clientInfo,
                                 CONNECT_TO_SERVER,
                                 m_negotiatedAskCaps);
        }
        catch (UnknownHostException e)
        {
            UnknownHostnameException hostexp = new UnknownHostnameException(connectionInfo);
            //trace.print(hostexp.toString(), 1);
            log.logStackTrace("", hostexp);
            throw hostexp;
        }
        catch (IOException e)
        {
            AppServerIOException conioexp = new AppServerIOException(connectionInfo, e.toString());
            //trace.print(conioexp.toString(), 1);

            log.logStackTrace("", conioexp);

            throw conioexp;
        }
    }
    else
        serverMode = SERVERMODE_STATELESS;

    if (ret != ubMsg.UBRSP_OK)
    {
       
        if(conntimeout)
        {
        	conntimeout = false;
        BrokerException timeoutid = new BrokerException(BrokerException.CODE_PROTOCOL_ERROR,
        		CONN_MSG);
        throw timeoutid;
        }
        else
        {
        	 ConnectFailedException beConnFld = new ConnectFailedException(ret, m_connectionReturnValue);
        log.logStackTrace("", beConnFld);
        throw beConnFld;
        }

        //trace.print(beConnFld, 1);Version
        
    }
    else
    {
        if (clntAskCaps != IPoolProps.ASK_DISABLED)
        {
            trace.print(m_sessionID + "      Negotiated ASK Version= " +
                        formatAskVersion(m_negotiatedAskVersion) + 
                        "  Capabilities= " + 
                        formatAskCapabilities(m_negotiatedAskCaps), 1);

            if ((m_negotiatedAskCaps & IPoolProps.ASK_CLIENTASK_ENABLED) != 0)
            {
                trace.print(m_sessionID + "      clientASKActivityTimeout= " + 
                            m_clientASKActivityTimeout +
                            "  clientASKResponseTimeout= " + 
                            m_clientASKResponseTimeout, 1);
            }
        }
        
        negotiateCompressionCapabilities();
        
    }

}

private void negotiateCompressionCapabilities() {
    // if compression is enabled on the client, check if the server
    // can do compresssion.  If the server can do compression, test what version,
    // and hold onto the result so we can compress message bodies

    if (m_properties.getBooleanProperty(IPoolProps.ENABLE_COMPRESSION))
    {
        trace.print(m_sessionID + "      Client compression requested.", 1);
        short TLVTYPE_CMPRCAPABLE = (short)5; // compression capable server
        try
        {
            String compressionVersion = getCapability(TLVTYPE_CMPRCAPABLE);

            if (compressionVersion == null)
            {
                m_compressionCapabilities = IPoolProps.COMPRESSION_DISABLED;

                trace.print(m_sessionID + "      Client compression disabled.  Server compression capability was not set.", 1);
            }
            else
            {
                if (compressionVersion.equals("1"))
                {
                    m_compressionCapabilities = IPoolProps.V10_COMPRESSION;

                    trace.print(m_sessionID + "      Client V10 compression enabled.", 1);
                }
                else if (compressionVersion.equals("2"))
                {
//                        m_compressionCapabilities = PROPS.V9_COMPRESSION;
//                        trace.print(m_sessionID + "      Client V9 compression enabled.", 1);
                    m_compressionCapabilities = IPoolProps.COMPRESSION_DISABLED;
                    trace.print(m_sessionID + "      Client compression disabled.  Compression capability only supports V10 or higher servers.", 1);

                }
            }
        }
        catch (Exception e)
        {
            m_compressionCapabilities = IPoolProps.COMPRESSION_DISABLED;
        }
    }
    else
    {
        m_compressionCapabilities = IPoolProps.COMPRESSION_DISABLED;

        trace.print(m_sessionID + "      Client compression disabled.  Compression not requested.", 1);
    }
}

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

/*
public void connect(String url)
    throws BrokerException, ConnectException
    {
    connect(url, "", "", "");
    }
*/

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void xid(
          String requestID,
          String url,
          String username,
          String password,
          String connectionStr)
    throws BrokerException, ConnectException
{
    int                     ret = 0;
    boolean                 disableNameServer = false;
    String                  nameServer = null;
    SocketConnectionInfoEx  connectionInfo = null;
    Properties              instanceOptions = m_properties.getAsProperties();
    String                  strAskCaps;
    int                     clntAskCaps;

    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
        log.logBasic(m_debugLogIndex,
                     "xid() " +
                     "url= (" + url + ") " +
                     "username= (" + username + ") " +
                     "connectionStr= (" + connectionStr + ")");

    trace.print("BrokerSystem XID: " +
        "url= (" + url + ") " +
        "username= (" + username + ") " +
        "connectionStr= (" + connectionStr + ")", 4);

    // Now generate the socket information (URL)
    //
    try
    {
        connectionInfo = new SocketConnectionInfoEx(url);
        int protocolType = connectionInfo.getProtocolType();
        disableNameServer = connectionInfo.isDirectConnect();
        
        // If this is the appserver protocol AND we disable use of the
        // NameServer AND the current port is the default NameServer port, then
        // override the port specified in the url with the default AppServer
        // port #.
        //
        if (((INetworkProtocol.PROTOCOL_APPSERVER == protocolType) ||
             (INetworkProtocol.PROTOCOL_APPSERVER_SSL == protocolType)) &&
            disableNameServer &&
            SocketConnectionInfoEx.DEF_PORT_NAMESERVER == connectionInfo.getPort())
        {
            connectionInfo.setPort(SocketConnectionInfoEx.DEF_PORT_APPSERVER);
        }
    }
    catch (MalformedURLException e)
    {
        //throwBrokerException("Bad url format");
        BadURLException beURL = new BadURLException(e.getMessage() + ": " + url);

        log.logStackTrace("", beURL);

        trace.print(beURL, 1);
        throw beURL;
    }

    // If we're not in an idle state, we're not prepared to connect to anybody!
    //
    if (current_state != STATE_IDLE)
    {
        //throwBrokerException("connect:Invalid state= "  +
        //                       DESC_STATE[current_state]);

        InvalidStateException beInvSt = new InvalidStateException("xid",
        DESC_STATE[current_state]);

        log.logStackTrace("", beInvSt);

        trace.print(beInvSt, 1);
        throw beInvSt;
    }

    // Create a Network Protocol Handler object here and initialize it...
    //
    try
    {
        netProtocolHandler = NetworkProtocolFactory.create(
                                       INetworkProtocol.END_POINT_CLIENT,
                                       connectionInfo,
                                       log,
                                       log_dest);

        // Now init the handler...
        netProtocolHandler.init(instanceOptions, log, log_dest);

        // then call object to resolve the connection port.  Depending upon
        // the protocol, this may use a NameServer or not.  The HTTP and HTTP/S
        // will not.
        //
        if (!disableNameServer)
        {
            netProtocolHandler.resolveConnectionInfo(connectionInfo);
        }

    }
    catch (MalformedURLException e)
    {
        //throwBrokerException("Bad url format");
        BadURLException beURL = new BadURLException(e.getMessage() + ": " + url);

        log.logStackTrace("", beURL);

        trace.print(beURL, 1);
        throw beURL;
    }
    catch (NetworkProtocolException e1)
    {
        // convert the generic NetworkProtocolException into the OpenClient facility
        BrokerException be = new BrokerException(BrokerException.CODE_PROTOCOL_ERROR,
                                                 e1.getMessage());
        log.logStackTrace("", be);
        trace.print(be, 1);
        throw be;
    }
    catch (Exception e2)
    {
        // General exeption handler.
        BrokerException be = new BrokerException(e2.toString());
        log.logStackTrace("", be);
        trace.print(be, 1);
        throw be;
    }


    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
        log.logBasic(m_debugLogIndex,
                     "xid() " +
                     "host= (" + connectionInfo.getHost() + ") " +
                     "port= (" + connectionInfo.getPort() + ") " +
                     "service= (" + connectionInfo.getService() + ") ");

    trace.print("XID to Broker: " +
        "host= (" + connectionInfo.getHost() + ") " +
        "port= (" + connectionInfo.getPort() + ") " +
        "service= (" + connectionInfo.getService() + ") ", 4);

    /* Log ASK message if appropriate */
    strAskCaps = instanceOptions.getProperty(IPoolProps.APPSERVER_KEEPALIVE_CAPS);
    if (askValidateProtocolType(connectionInfo))
        clntAskCaps = parseAskCapabilities(strAskCaps);
    else
        clntAskCaps = IPoolProps.ASK_DISABLED;

    if (clntAskCaps == IPoolProps.ASK_DISABLED)
    {
        trace.print(m_sessionID + "      ASK Disabled.", 2);
    }
    else
    {
        trace.print(m_sessionID + "      Requesting ASK Capabilities= " + strAskCaps, 2);
    }

    // Init the server port.
    serverPort = 0;
    for (ubProtocolVersion = ubMsg.MAX_UBVER;
            ubProtocolVersion >= ubMsg.UBMSG_PROTOCOL_V1;
               ubProtocolVersion--)
    {
    try
        {
        ret = processXID(requestID,
                         connectionInfo,
                         username,
                         password,
                         connectionStr,
                         clntAskCaps);

       if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
           log.logBasic(m_debugLogIndex,
                        "BrokerSystem xid(): got ubver= " + ubProtocolVersion);
        break;
        }
    catch (UnknownHostException e)
        {
        UnknownHostnameException hostexp = new UnknownHostnameException(connectionInfo);
        // Exceptions derived from Open4GLError don not need to be traced
        //trace.print(hostexp.toString(), 1);

        log.logStackTrace("", hostexp);

        throw hostexp;
        }
    catch (IOException e)
        {
        BrokerIOException conioexp = new BrokerIOException(connectionInfo, e.toString());
        // Exceptions derived from Open4GLError don not need to be traced
        //trace.print(conioexp.toString(), 1);

        log.logStackTrace("", conioexp);

        throw conioexp;
        }
    catch (BrokerException be)
        {
        /* if the broker doesn't recognize the client's protocol version      */
        /* the broker will disconnect.  we need to keep trying with decreasing*/
        /* protocol numbers until we get a match or we reach the minimum      */
        /* if we reach the minimum, then rethrow the exception                */

        /* NOTE: for xid(), the minimum functional level is UBMSG_PROTOCOL_V1 */
        /*       not MIN_UBVER                                                */

        /* PROBLEM:  when we disconnect, and recall processConnect(), we may  */
        /*           actually hit a different broker (due to load balancing). */
        /*           therefor all brokers which serve a single application    */
        /*           service must be the same protocol version!!!             */

        if (ubProtocolVersion > ubMsg.UBMSG_PROTOCOL_V1)
            {
            disconnectSocket(); /* we will reconnect this when we retry */
            if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                log.logBasic(m_debugLogIndex,
                            "BrokerSystem : error on ver= " + ubProtocolVersion);
            }
        else throw be;
        }
    }

    serverMode = SERVERMODE_STATE_FREE;

    if (ret != ubMsg.UBRSP_OK)
    {
    	if(conntimeout)
        {
        	conntimeout = false;
        BrokerException timeoutid = new BrokerException(BrokerException.CODE_PROTOCOL_ERROR,
        		CONN_MSG);
        throw timeoutid;
        }
        ConnectFailedException beConnFld = new ConnectFailedException(ret);

        log.logStackTrace("", beConnFld);

        //trace.print(beConnFld, 1);
        throw beConnFld;
    }
    else
    {
        if (clntAskCaps != IPoolProps.ASK_DISABLED)
        {
            trace.print(m_sessionID + "      Negotiated ASK Version= " +
                        formatAskVersion(m_negotiatedAskVersion) + 
                        "  Capabilities= " + 
                        formatAskCapabilities(m_negotiatedAskCaps), 1);
            if ((m_negotiatedAskCaps & IPoolProps.ASK_CLIENTASK_ENABLED) != 0)
            {
                trace.print(m_sessionID + "      clientASKActivityTimeout= " +
                            m_clientASKActivityTimeout +
                            "  clientASKResponseTimeout= " + 
                            m_clientASKResponseTimeout, 1);
            }
        }
        
        negotiateCompressionCapabilities();
    }

}

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void allocate(String requestID)
    throws BrokerException
    {
    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
        log.logBasic(m_debugLogIndex,
                     "allocate()");
    if (current_state != STATE_CONNECTED)
        {
        InvalidStateException beInvState = new InvalidStateException(
                                                    "allocate",
                                                    DESC_STATE[current_state]);

        log.logStackTrace("", beInvState);

        trace.print(beInvState, 1);
        throw beInvState;
        }

    os_pos = 0;
    is_pos = 0;

    this.m_requestID = requestID;

    current_state = STATE_ALLOCATED;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void disconnect()
    throws BrokerException
{
    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
        log.logBasic(m_debugLogIndex,
                     "disconnect()");

    if ((current_state != STATE_IDLE) && (current_state != STATE_ERROR))
        disconnectPacket("disconnect(" + connID + ")");

    disconnectSocket();
}

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void unconditionalDisconnect()
    {
    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
        log.logBasic(m_debugLogIndex,
                     "unconditionalDisconnect()");

    try
        {
        disconnectSocket();
        }
    catch (BrokerException be)
        {
        log.logStackTrace("", be);

        trace.print(be, 1);
        }
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public boolean isConnected()
    {
    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
        log.logBasic(m_debugLogIndex,
                     "isConnected() : current_state= "+ DESC_STATE[current_state]);
    return ((current_state != STATE_IDLE) && (current_state != STATE_ERROR));
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void write(int b)
    throws BrokerException
    {
    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
        log.logBasic(m_debugLogIndex,
                     "write(" + b + ")");
    byte[] buff = new byte[1];

    buff[0] = (byte) b;
    write(buff, 0, buff.length);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void write(byte[] msgbuf)
    throws BrokerException
    {
    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
        log.logBasic(m_debugLogIndex,
                     "write(msgbuf[])");
    write(msgbuf, 0, msgbuf.length);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void write(byte[] msgbuf, int offset, int len)
    throws BrokerException
    {
    int cnt, n, x;

    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
        log.logBasic(m_debugLogIndex,
                     "write(msgbuf[" + offset + ":" + len + "]) " +
        "buflen= " + msgbuf.length);
    // log.LogDump(log_dest, Logger.LOGGING_TRACE, Logger.NOTIMESTAMP,
    //     "write-data:",
    //     msgbuf, ((log_dump_full) ? offset+len : log_dump_max));
    // Client requested a STOP
    if (current_state == STATE_STOPPING)
        {
        ClientStopException beCliStop = new ClientStopException();

        log.logStackTrace("", beCliStop);

        trace.print(beCliStop, 1);
        throw beCliStop;
        }

    if ((current_state != STATE_ALLOCATED) && (current_state != STATE_SENDING))
        {
        InvalidStateException beInvState =
              new InvalidStateException("write", DESC_STATE[current_state]);

        log.logStackTrace("", beInvState);

        trace.print(beInvState, 1);
        throw beInvState;
        }


    for (cnt = 0; cnt < len; cnt += n)
        {
        /* calculate how much room remains in current buffer */
        x = os_buf.length - os_pos;

        /* determine how much to move */
        n = (x > (len - cnt)) ? (len - cnt) : x;

        System.arraycopy(msgbuf, offset+cnt, os_buf, os_pos, n);

        /* increment output stream pointer */
        os_pos += n;

        if (os_pos == os_buf.length)
            {
            try
                {
                // OE00131492 exceptions are now thrown from writePacket()
                writePacket(os_buf, os_pos);
                }
            catch (BrokerException be)
                {
                throw be;
                }
            finally
                {
                if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                    log.logBasic(m_debugLogIndex,
                                "Setting current state to SENDING");

                current_state = STATE_SENDING;
                os_pos = 0;
                }
            }
        }

    /* If I'm here, then write was successful
       See if server sent a stop                */
    if (checkStop())
        {
        ServerStopException beSrvStop = new ServerStopException();

        log.logStackTrace("", beSrvStop);

        trace.print(beSrvStop.toString(), 1);
        throw beSrvStop;
        }
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void prepareToReceive(int reason)
    throws BrokerException
    {
    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
        log.logBasic(m_debugLogIndex,
                     "prepareToReceive(reason= " + reason + ")");

    if ((current_state != STATE_ALLOCATED) && (current_state != STATE_SENDING))
        {
        InvalidStateException beInvState =
             new InvalidStateException("prepareToReceive",
                                        DESC_STATE[current_state]);

        log.logStackTrace("", beInvState);

        trace.print(beInvState.toString(), 1);
        throw beInvState;
        }

    try
        {
        // OE00131492 exceptions are now thrown from writeLastPacket()
        writeLastPacket(reason, os_buf, os_pos);
        }
    catch (BrokerException be)
        {
        throw be;
        }
    finally
        {
        if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
            log.logBasic(m_debugLogIndex,
                        "Setting current state to RECEIVING");

        current_state = STATE_RECEIVING;

        os_pos = 0;
        }
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private void setMessageBody(ubAppServerMsg msg, byte[] msgBuf, int length)
{
    int compressionLevel = getCompressionLevel();
    int compressionThreshold = getCompressionThreshold();
    boolean compressionEnabled = getCompressionEnabled();

    // completely turn off compression for older protocol
    if (ubProtocolVersion == ubMsg.UBMSG_PROTOCOL_V0) {
        compressionEnabled = false;
    }

    // completely turn off compression for v9 compression clients
    if (getCompressionCapabilities() == IPoolProps.V9_COMPRESSION)
    {
        compressionEnabled = false;
    }


    if (compressionEnabled)
    {
        // make sure compression settings are sane
        if (compressionLevel < IPoolProps._MIN_COMPRESSION_LEVEL)
        {
            compressionLevel = IPoolProps._DEFAULT_COMPRESSION_LEVEL;
        }

        if (compressionLevel > IPoolProps._MAX_COMPRESSION_LEVEL)
        {
            compressionLevel = IPoolProps._MAX_COMPRESSION_LEVEL;
        }

        if (compressionThreshold < IPoolProps._MIN_COMPRESSION_THRESHOLD)
        {
            compressionThreshold = IPoolProps._MIN_COMPRESSION_THRESHOLD;
        }
        
        if (compressionThreshold > IPoolProps._MAX_COMPRESSION_THRESHOLD) {
            compressionThreshold = IPoolProps._MAX_COMPRESSION_THRESHOLD;
        }

        // add compression level to tell server what level to compress messages
        try {
            msg.appendTlvField(ubMsg.TLVTYPE_CMPRLEVEL, String.valueOf(compressionLevel));
        } catch (Exception e)
        {
            log.logError("appendTlvField Exception: " + e.getMessage());
        
        }

        // add compression min size to tell server the minimum threshold
        try {
            msg.appendTlvField(ubMsg.TLVTYPE_CMPRMINSZ, String.valueOf(compressionThreshold));
        } catch (Exception e)
        {
            log.logError("appendTlvField Exception: " + e.getMessage());
        }

        // don't bother compressing if the message is too small
        if (length < compressionThreshold)
        {

            try {
                msg.appendTlvField(ubMsg.TLVTYPE_UNCMPRLEN, String.valueOf(0));
            } catch (Exception e)
            {
                log.logError("appendTlvField Exception: " + e.getMessage());
            }

            compressionEnabled = false;

            if (log.ifLogVerbose(m_cmprsLogEntries,m_cmprsLogIndex))
                log.logVerbose(m_cmprsLogIndex,"SEND-COMPRESSION: Message buffer size = " + String.valueOf(length) + " is below minimum compression threshold " + String.valueOf(compressionThreshold));

        }
    }



    if (compressionEnabled)
    {
        byte[] compressedBuf = null;
        try
        {
            compressedBuf = MessageCompressor.compressBytes(msgBuf, 0, length, compressionLevel);
        }
        catch (Exception e)
        {
            // if there was an error doing compression, then just log the exception and 
            // continue to send as uncompressed message
            compressedBuf = null;
            compressionEnabled = false;

            log.logStackTrace(m_cmprsLogIndex, "SEND-COMPRESSION: Compression skipped due to ZLIB compression error ", e);

        }

        if (compressedBuf == null || compressedBuf.length > length) 
        {
            // If there was no gain to due compression then tell the server to still send
            // compressed response, but just send original message
            try {
                msg.appendTlvField(ubMsg.TLVTYPE_UNCMPRLEN, String.valueOf(0));
            } catch (Exception e)
            {
                log.logError("appendTlvField Exception: " + e.getMessage());
            }
            msg.setMsgbuf(msgBuf, length);
        }
        else
        {                  
            // if compression worked fine then set the uncompressed length
            // to the original message size, and set the message body to the
            // compressed bytes.  For consistency with other clients, use zero padded length as a string
            try {
                msg.appendTlvField(ubMsg.TLVTYPE_UNCMPRLEN, String.valueOf(length));
            } catch (Exception e)
            {
                log.logError("appendTlvField Exception: " + e.getMessage());
            }

            msg.setMsgbuf(compressedBuf, compressedBuf.length);

            // need to reset the message length to the compressed size so the server knows how much to read
            // otherwise it will still reference the uncompressed length
            msg.setMsglen(compressedBuf.length + ubAppServerMsg.CSMSSGHDRLEN);

            float compressionRatio = 100.0F - (((float) compressedBuf.length / length) * 100.0F);
            
            if (log.ifLogVerbose(m_cmprsLogEntries, m_cmprsLogIndex))
                log.logVerbose(m_cmprsLogIndex, "SEND-COMPRESSION: Uncompress header = " + msg.getubhdr().length + ";  " + length + " message bytes compressed to " + compressedBuf.length + " at level " + compressionLevel + ", Gained: " + compressionRatio);

        }
    }
    else
    {
        msg.setMsgbuf(msgBuf, length);

    }
}



/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void deallocate()
    throws BrokerException
    {
    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
        log.logBasic(m_debugLogIndex,
                     "deallocate()");

    /* for error recovery, we need to handle case where we're ending  */
    /* prematurely ... maybe force this layer to read until EOF to    */
    /* clear out broker queue .... or perhaps send a deallocate msg   */

    if (
        (current_state != STATE_EOF)  &&
        (current_state != STATE_ALLOCATED)
       )
        {
        InvalidStateException beInvState =
          new InvalidStateException("deallocate", DESC_STATE[current_state]);

        log.logStackTrace("", beInvState);

        trace.print(beInvState.toString(), 1);
        throw beInvState;
        }

    os_pos = 0;
    is_pos = 0;
    eof = false;

    m_requestID = null;

    current_state = STATE_CONNECTED;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int read()
    throws BrokerException
    {
    int ret;
    byte[] buff = new byte[1];

    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
        log.logBasic(m_debugLogIndex,
                     "read()");

    ret = read(buff, 0, buff.length);
    return ret;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int read(byte[] msgbuf)
    throws BrokerException
    {
    int ret;

    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
        log.logBasic(m_debugLogIndex,
                     "read(msgbuf[], len= " + msgbuf.length + ")");

    ret = read(msgbuf, 0, msgbuf.length);
    return ret;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int read(byte[] msgbuf, int offset, int len)
    throws BrokerException
{
    int cnt, n, x;

    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
        log.logBasic(m_debugLogIndex,
                     "read(msgbuf[], offset= " + offset +
                     ", len= " + len + ") msgbuf.length= " + msgbuf.length );

    /* allow for case when last read is exactly size of the buffer */
    if ( current_state == STATE_EOF)
    {
        stop_sent = false;
        return Broker.EOF;
    }

    if (
        (current_state != STATE_RECEIVING)    &&
        (current_state != STATE_STOPPING)
       )
    {
        InvalidStateException beInvState =
              new InvalidStateException("read", DESC_STATE[current_state]);

        log.logStackTrace("", beInvState);

        trace.print(beInvState, 1);
        throw beInvState;
    }

    for (cnt = 0, n = 0; cnt < len; /* cnt += n */)
    {
        /* calculate how much data remains in current buffer */
        x = imsg.getBuflen() - is_pos;

    /* determine how much to move */
        n = (x > (len - cnt)) ? (len - cnt) : x;

        System.arraycopy(imsg.getMsgbuf(), is_pos, msgbuf, offset+cnt, n);

    /* increment input stream pointer */
        is_pos += n;

    /* increment the number of bytes to be returned */
        cnt += n;

        if (is_pos == imsg.getBuflen())
        {
            if (readPacket() == Broker.EOF)
            {
                    current_state = STATE_EOF;
                    stop_sent = false;
                    break;
            }
        }
    }

    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
        log.logBasic(m_debugLogIndex,
                     "read(msgbuf[" + offset + ":" + cnt + "]) ");

    // log.LogDump(log_dest, Logger.LOGGING_TRACE, Logger.NOTIMESTAMP,
    //     "read-data:",
    //     msgbuf, ((log_dump_full) ? offset + cnt : log_dump_max));

    return cnt;
}

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setStop()
    throws BrokerException
    {
    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
        log.logBasic(m_debugLogIndex,
                     "setStop() from state " + DESC_STATE[current_state]);

    switch (current_state)
        {
        case STATE_RECEIVING :
            {
            current_state = STATE_STOPPING;
            // The http and https protocols aren't interruptable like the
            // TCP socket connections are to peak at sending a stop message
            // to the AppServer.
            if (INetworkProtocol.PROTOCOL_HTTP_TUNNEL == netProtocolHandler.protocolType() ||
                INetworkProtocol.PROTOCOL_HTTPS_TUNNEL == netProtocolHandler.protocolType())
            {
/*
                log.LogMsgln(log_dest, Logger.LOGGING_TRACE, Logger.NOTIMESTAMP,
                    "setStop() initiating setStopPacket()");
*/
                setStopPacket();
            }
            }
            break;
        case STATE_SENDING :
        case STATE_STOPPING :
            current_state = STATE_STOPPING;
            break;

        default:

             log.logError("setStop:Invalid state: " +
                          DESC_STATE[current_state]);

             trace.print("Non-fatal error - BrokerSystem.setStop:Invalid state: " +
                         DESC_STATE[current_state], 1);
             /* Remove exception. Do we care if setStop isn't called at the right time?
             throwBrokerException("setStop:Invalid state= "  +
                         DESC_STATE[current_state]); */
        }

    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public boolean isReceiving()
{
/*    log.LogMsgln(log_dest, Logger.LOGGING_TRACE, Logger.TIMESTAMP,
        "isReceiving returning current state " + (current_state == STATE_RECEIVING));
*/
    boolean returnValue = (current_state == STATE_RECEIVING);
    return (returnValue);
}

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public boolean isStopping()
{
    boolean returnValue = (current_state == STATE_STOPPING);
/*    log.LogMsgln(log_dest, Logger.LOGGING_TRACE, Logger.TIMESTAMP,
        "isStopping returning current state " + (current_state == STATE_STOPPING));
*/
    return (returnValue);
}

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public String getConnectionID()
    throws BrokerException
    {
    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
        log.logBasic(m_debugLogIndex,
                     "getConnectionID()");
    return connID;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public String getRequestID()
    throws BrokerException
    {
    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
        log.logBasic(m_debugLogIndex,
                     "getRequestID()");
    return m_requestID;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public String getSSLSubjectName()
    throws BrokerException
    {
    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
        log.logBasic(m_debugLogIndex,
                     "getSSLSubjectName()");
    return m_sslSubjectName;
    }

/* smart connections: retrieve the string returned from the connect procedure
 * if there was no value returned it will be null */
public String getConnectionReturnValue()
    throws BrokerException
    {
    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
        log.logBasic(m_debugLogIndex,
                     "getConnectionReturnValue()");
    return m_connectionReturnValue;
    }

/* KeepAlive support */
public int getASKVersion()
{
        return (m_negotiatedAskVersion);
}

public boolean getServerASKEnabled()
{
        return (m_negotiatedAskCaps & IPoolProps.ASK_SERVERASK_ENABLED) != 0;
}

public boolean getClientASKEnabled()
{
        return (m_negotiatedAskCaps & IPoolProps.ASK_CLIENTASK_ENABLED) != 0;
}

public int getClientASKActivityTimeout()
{
        return m_clientASKActivityTimeout;
}

public int getClientASKResponseTimeout()
{
        return m_clientASKResponseTimeout;
}

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void  manageASKPingRequest()
    throws BrokerException
{
    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
        log.logBasic(m_debugLogIndex,
                 m_sessionID + " manageASKPingRequest()");

    /* the assumption is that this method is ONLY called from  */
    /* the ASKWatchDog thread.  This should only get through   */
    /* to this layer when we are between requests, so our      */
    /* current state should be STATE_CONNECTED. anything else  */
    /* indicates a BAD problem.                                */

    if (current_state != STATE_CONNECTED)
    {
        InvalidStateException beInvState = 
            new InvalidStateException("manageASKPingRequest",
                                       DESC_STATE[current_state]);
        
        log.logStackTrace("", beInvState);
        
        trace.print(beInvState, 1);
        throw beInvState;
    }
    
    /* check the input stream to see if we've got any pending input */
    /* if we've got input ready, we need to read it.  if it is an   */
    /* ASKPing request message, we must reply to it.  Otherwise, we */
    /* are really messed up, so we throw an exception               */
    try 
    {
      if (is.available() > 0)
      {
        ubAppServerMsg msg;

        msg = (ubAppServerMsg) is.readMsg();
        m_tsLastAccess = System.currentTimeMillis();

        if (msg.getubRq() == ubMsg.UBRQ_ASKPING_RQ)
        {
            trace.print(m_sessionID + " detected ASKPing request", 4);
            askPingPacket(ubMsg.UBRQ_ASKPING_RSP);
        }
        else
        {
            BrokerException be = new BrokerException(
                BrokerException.CODE_PROTOCOL_ERROR,
                "unexpected request received by ASKWatchDog : ubRq = "
                 + msg.getubRq());
            log.logStackTrace("", be);
            trace.print(be, 1);
            throw be;
        }
      }
    }
    catch(ubMsg.MsgFormatException me)
    {
        MessageFormatException beMsgFormat = new MessageFormatException();

        log.logStackTrace("", beMsgFormat);
        log.logError(   me.getMessage() + ":" + me.getDetail());

        trace.print(beMsgFormat.toString() + me.getMessage() + 
                     ":" + me.getDetail(), 1);
        throw beMsgFormat;
    }
    catch (NetworkProtocolException e1)
    {
        // convert the generic NetworkProtocolException
        // into the OpenClient facility
        BrokerException be = new BrokerException(
                                      BrokerException.CODE_PROTOCOL_ERROR,
                                      e1.getMessage());
        log.logStackTrace("",
                          be);
        trace.print(be, 1);
        throw be;
    }
    catch (IOException e)
    {
        // convert the IOException into the OpenClient facility
        BrokerException be = new BrokerException(
                                BrokerException.CODE_GENERAL_ERROR, 
                                e + " [" + e.getMessage() + "]");
        log.logStackTrace("",
                          be);
        trace.print(be, 1);
        throw be;
    }

}

/**********************************************************************/
/* Private Methods                                                    */
/**********************************************************************/

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private void init(IPoolProps properties, IAppLogger log, int dest)
    {
    current_state = STATE_IDLE;
    sockInfo = null;
    serverPort = 0;
    connID = null;
    os = null;
    os_pos = 0;
    
    String netBufSize =	(String) properties.getProperty(IPoolProps.NET_BUFFER_SIZE);
    if (null == netBufSize)
    	os_pos_max = 0;
    else
    	os_pos_max = Integer.parseInt(netBufSize);
    if ((os_pos_max <= ubConstants.MSG_MIN_BUFSIZE) || 
        (os_pos_max > ubConstants.MSG_MAX_BUFSIZE))
    {
        if (log.ifLogBasic(m_basicLogEntries,m_basicLogIndex))
                log.logBasic(m_basicLogIndex,
                             "Invalid network buffer size " + os_pos_max +
                             " specified.  Using default size " +
                              ubConstants.MSG_DEF_BUFSIZE);
        properties.setProperty(IPoolProps.NET_BUFFER_SIZE, 
                                  Integer.toString(ubConstants.MSG_DEF_BUFSIZE));
        os_pos_max = ubConstants.MSG_DEF_BUFSIZE;
    }
    
   if (log.ifLogVerbose(m_basicLogEntries,m_basicLogIndex))
       log.logVerbose(m_basicLogIndex,
                      "network buffer size = " + os_pos_max);
   
    os_buf = new byte[os_pos_max];
    
    imsg = new ubAppServerMsg(ubMsg.MAX_UBVER, ubMsg.DEF_BUFSIZE);
    is = null;
    is_pos = 0;
    seqnum = 0;
    eof = false;
    stop_sent = false;
    serverMode = SERVERMODE_STATELESS;
    this.log = log;
    log_dest = dest;
    ubProtocolVersion = ubMsg.MAX_UBVER;
    m_requestID = null;
    m_properties = properties;
    m_sslSubjectName = null;
    /* smart connections */
    m_connectionReturnValue = null;

    m_capabilities = new Hashtable();

    /* ASK Keepalive support */
    m_negotiatedAskVersion  = IPoolProps.INVALID_ASK_VERSION;
    m_negotiatedAskCaps = IPoolProps.ASK_DISABLED;
    m_clientASKActivityTimeout =
        m_properties.getIntProperty(IPoolProps.CASK_ACTIVITY_TIMEOUT);
    m_clientASKResponseTimeout = 
        m_properties.getIntProperty(IPoolProps.CASK_RESPONSE_TIMEOUT);
    m_clientASKActivityTimeoutMs = m_clientASKActivityTimeout * 1000;
    m_clientASKResponseTimeoutMs = m_clientASKResponseTimeout * 1000;
    m_ASKstate = ASKSTATE_INIT;
    m_ASKrqstACKsent = false;
    m_tsLastAccess = 0;
    m_sessionID = null;
    m_compressionCapabilities = IPoolProps.COMPRESSION_DISABLED;
    m_compressionLevel = m_properties.getIntProperty(IPoolProps.COMPRESSION_LEVEL);
    m_compressionThreshold = m_properties.getIntProperty(IPoolProps.COMPRESSION_THRESHOLD);

    /* initialize log settings based on interface */
    initializeLogging(log);
    
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private int processConnect(
          String requestID,
          Object connectionInfo,
          String username, String password,
          String clientInfo,
          boolean fToBroker,
          int clntAskCaps)
    throws BrokerException, UnknownHostException, IOException, ConnectException
{
    int ret = ubMsg.UBRSP_OK;
 
    if (null == sockInfo)
        sockInfo = (SocketConnectionInfoEx) connectionInfo;

    try
    {
        // Do a sanity check for the presence of the network protocol handler.  If
        // one isn't available, we're dead!
        //
        if (null == netProtocolHandler)
        {
            throw new NullPointerException("Null protocol handler");
        }
//    destSocket = new Socket(sockInfo.getHost(), sockInfo.getPort());
        validateConnectionInfo(username, password, clientInfo);
        netProtocolHandler.openConnection(sockInfo,
                                          SERVERTYPE_APPSERVER,
                                          null,
                                          null,
                                          null);

        // Ok, that worked.  Now get the input and output streams.
        os = netProtocolHandler.getMsgOutputStream(INetworkProtocol.MSG_STREAM_UB_BINARY);
        is = netProtocolHandler.getMsgInputStream(INetworkProtocol.MSG_STREAM_UB_BINARY);

        // By now we have the SSL session, if used.  Get the subject name.
        m_sslSubjectName = netProtocolHandler.getSSLSubjectName();
        
        // reset the AppServer protocol's message sequence number.
        seqnum = 0;

        // Send a connect packet to the AppServer...
        ret = connectPacket(requestID, 
                            username,
                            password,
                            clientInfo,
                            fToBroker,
                            clntAskCaps);

        /* we need to call disconnect here to break socket if we're */
        /* linked but the connection process fails                  */

        // Must be an error, disconnect from the appserver.
        if (ret != ubMsg.UBRSP_OK)
        {
            disconnectSocket();
        }

        // Update the state information.
        eof = false;
        current_state = (ret == ubMsg.UBRSP_OK) ? STATE_CONNECTED : STATE_IDLE;
    }
    catch (NetworkProtocolException e)
    {
        ConnectException    ce = null;
        // Convert the Network protocol exception to an Open Client type.
        switch ( e.exceptionId())
        {
            case NetworkProtocolException.PROXY_AUTHENTICATION_FAILED:
                {
                    ce = new ConnectProxyAuthException(e.jcMsgId(),
                                                       e.messageTokens());
                }
                break;
            case NetworkProtocolException.AIA_REPORTED_ERROR:
                {
                    ce = new ConnectAIAException(e.jcMsgId(),
                                                 e.messageTokens());
                }
                break;
            case NetworkProtocolException.AUTHENTICATION_REJECTED:
                {
                    ce = new ConnectHttpAuthException(e.jcMsgId(),
                                                       e.messageTokens());
                }
                break;
            case NetworkProtocolException.SERVER_AUTHENTICATION_FAILED:
                {
                    ce = new ConnectHttpsAuthException(e.jcMsgId(),
                                                       e.messageTokens());
                }
                break;
            default:
                {
                    ce = new ConnectProtocolException(e.jcMsgId(),
                                                      e.messageTokens());

                }
        }

        log.logStackTrace("NetworkProtocolException translation: ",
                          ce);
        trace.print(ce, 1);
        throw ce;
    }
    catch (Exception e1)
    {
        // convert the generic NetworkProtocolException into the OpenClient facility
        BrokerException be = new BrokerException(BrokerException.CODE_PROTOCOL_ERROR,
                                                 e1.getMessage());
        log.logStackTrace("",
                          be);
        trace.print(be, 1);
        throw be;
    }
    return ret;
}

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private int processXID(
          String requestID,
          Object connectionInfo,
          String username, String password,
          String connectionStr,
          int clntAskCaps)
    throws BrokerException, UnknownHostException, IOException, ConnectException
{
    int ret = ubMsg.UBRSP_OK;

    if (null == sockInfo)
        sockInfo = (SocketConnectionInfoEx) connectionInfo;

    try
    {
        // Do a sanity check for the presence of the network protocol handler.  If
        // one isn't available, we're dead!
        //
        if (null == netProtocolHandler)
        {
            throw new NullPointerException("Null protocol handler");
        }

        validateConnectionInfo(username, password, connectionStr);
        netProtocolHandler.openConnection(sockInfo,
                                          SERVERTYPE_APPSERVER,
                                          null,
                                          null,
                                          null);

        // Ok, that worked.  Now get the input and output streams.
        os = netProtocolHandler.getMsgOutputStream(INetworkProtocol.MSG_STREAM_UB_BINARY);
        is = netProtocolHandler.getMsgInputStream(INetworkProtocol.MSG_STREAM_UB_BINARY);

        // reset the AppServer protocol's message sequence number.
        seqnum = 0;

        // Send a connect packet to the AppServer...
        ret = xidPacket(requestID, username, password, connectionStr, clntAskCaps);

        /* we need to call disconnect here to break socket if we're */
        /* linked but the connection process fails                  */

        // Must be an error, disconnect from the appserver.
        if (ret != ubMsg.UBRSP_OK)
        {
            disconnectSocket();
        }

        // Update the state information.
        eof = false;
        current_state = (ret == ubMsg.UBRSP_OK) ? STATE_CONNECTED : STATE_IDLE;
    }
    catch (NetworkProtocolException e)
    {
        ConnectException    ce = null;
        // Convert the Network protocol exception to an Open Client type.
        switch ( e.exceptionId())
        {
            case NetworkProtocolException.PROXY_AUTHENTICATION_FAILED:
                {
                    ce = new ConnectProxyAuthException(e.jcMsgId(),
                                                       e.messageTokens());
                }
                break;
            case NetworkProtocolException.AIA_REPORTED_ERROR:
                {
                    ce = new ConnectAIAException(e.jcMsgId(),
                                                 e.messageTokens());
                }
                break;
            case NetworkProtocolException.AUTHENTICATION_REJECTED:
                {
                    ce = new ConnectHttpAuthException(e.jcMsgId(),
                                                       e.messageTokens());
                }
                break;
            case NetworkProtocolException.SERVER_AUTHENTICATION_FAILED:
                {
                    ce = new ConnectHttpsAuthException(e.jcMsgId(),
                                                       e.messageTokens());
                }
                break;
            default:
                {
                    ce = new ConnectProtocolException(e.jcMsgId(),
                                                      e.messageTokens());

                }
        }

        log.logStackTrace("NetworkProtocolException translation: ",
                          ce);
        trace.print(ce, 1);
        throw ce;
    }
    catch (Exception e1)
    {
        // convert the generic NetworkProtocolException into the OpenClient facility
        BrokerException be = new BrokerException(BrokerException.CODE_PROTOCOL_ERROR,
                                                 e1.getMessage());
        log.logStackTrace("",
                          be);
        trace.print(be, 1);
        throw be;
    }
    return ret;
}


/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private int connectPacket(
          String requestID,
          String username,
          String password,
          String clientInfo ,
          boolean fToBroker,
          int clntAskCaps)
      throws IOException, BrokerException, NetworkProtocolException, Exception
{
    int ret;
    int     n = 0;
    byte[]  b;
    byte[] username_b = null;
    byte[] password_b = null;
    byte[] clientInfo_b = null;
    byte[] connID_b = null;
    byte[] appService_b = null;
    ubRqInfo rqInfo;

    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
        log.logBasic(m_debugLogIndex,
                     "connectPacket() fToBroker= " + fToBroker);

    /* calculate the message buffer size */
    n += (PROGRESS_WIRECODEPAGE == null)   ? 2 : (PROGRESS_WIRECODEPAGE.length() + 3);

    if (username == null)
    {
        n += 2;
    }
    else
    {
        username_b = ubMsg.newNetByteArray(username);
        n += (username_b.length + 3);
    }
    if (password == null)
    {
        n += 2;
    }
    else
    {
        password_b = ubMsg.newNetByteArray(password);
        n += (password_b.length + 3);
    }
    if (clientInfo == null)
    {
        n += 2;
    }
    else
    {
        clientInfo_b = ubMsg.newNetByteArray(clientInfo);
        n += (clientInfo_b.length + 3);
    }
    if (connID == null)
    {
        n += 2;
    }
    else
    {
        connID_b = ubMsg.newNetByteArray(connID);
        n += (connID_b.length + 3);
    }

    // If we are tunneling through the HTTP or HTTP/S protocol, add in the
    // service name here.
    //
    if (SocketConnectionInfoEx.HTTP_PROTOCOL == sockInfo.getProtocolType() ||
        SocketConnectionInfoEx.HTTPS_PROTOCOL == sockInfo.getProtocolType())
    {
        if (sockInfo.getService() == null)
        {
            n += 2;
        }
        else
        {
            appService_b = ubMsg.newNetByteArray(sockInfo.getService());
            n += (appService_b.length + 3);
        }
    }

    /* allocate the new message of the correct length */
    ubAppServerMsg connMsg = new ubAppServerMsg(
                               ubProtocolVersion,
                               ubAppServerMsg.CSMSSG_CONNECT,
                               ++seqnum, n, n);

    /* set header fields */
    connMsg.setubSrc(ubMsg.UBSRC_CLIENT);
    connMsg.setubRq(ubMsg.UBRQ_CONNECT);

    switch (ubProtocolVersion)
        {
        case ubMsg.UBMSG_PROTOCOL_V0:
            break;

        case ubMsg.UBMSG_PROTOCOL_V1:
        default:
            /* set the tlvbuffer */
            try
                {
                connMsg.appendTlvField(ubMsg.TLVTYPE_RQID, requestID);

                /* add the capability information to tell the AppServer our capabilities */
                for (int i = 0; i < APPSRVCAPINFO_TYPE.length; i++)
                    connMsg.appendTlvField(APPSRVCAPINFO_TYPE[i], APPSRVCAPINFO_VALUE[i]);

                /* add the ASK request if the user has set the option */
                if (clntAskCaps != IPoolProps.ASK_DISABLED)
                    {
                    connMsg.appendTlvField(
                                 ubMsg.TLVTYPE_ASKPING_VER,
                                 String.valueOf(IPoolProps.ASK_VERSION));
                    connMsg.appendTlvField(
                                 ubMsg.TLVTYPE_ASKPING_CAPS,
                                 formatAskCapabilities(clntAskCaps));
                    }

                /* add in the requestInfo data */
                rqInfo = new ubRqInfo();
                propertiesToRqInfo(TLV_PROPERTIES_RQ,
                                   m_properties,
                                   rqInfo);
                connMsg.augmentTlvInfo(rqInfo);
                }
            catch (Exception e)
                {
                log.logError("appendTlvField Exception: " + e.getMessage());
                BrokerException be = new BrokerException(
                                           BrokerException.CODE_PROTOCOL_ERROR,
                                           e.getMessage());
                trace.print(be, 1);
                throw be;
                }
            break;
        }

    /* marshall the data into the message */
    b = connMsg.getMsgbuf();

    n = ubAppServerMsg.setNetString(b, 0, PROGRESS_WIRECODEPAGE);
    n = ubAppServerMsg.setNetString(b, n, username_b);
    n = ubAppServerMsg.setNetString(b, n, password_b);
    n = ubAppServerMsg.setNetString(b, n, clientInfo_b);
    n = ubAppServerMsg.setNetString(b, n, connID_b);
    if (SocketConnectionInfoEx.HTTP_PROTOCOL == sockInfo.getProtocolType() ||
        SocketConnectionInfoEx.HTTPS_PROTOCOL == sockInfo.getProtocolType())
    {
        n = ubAppServerMsg.setNetString(b, n, appService_b);
    }

    connMsg.setBuflen(n);

/*
    log.LogDump(log_dest, Logger.LOGGING_DEBUG, Logger.NOTIMESTAMP,
                        "connectPacket() connMsg msgbuf" ,
                        connMsg.getMsgbuf(), connMsg.getBuflen());
*/

    // TEST TEST TEST
    connMsg.print("connectPacket() connMsg before sending",
                     IAppLogger.LOGGING_BASIC,
                     UBrokerLogContext.SUB_V_UB_DEBUG,
                     log);
    try
    {
        os.writeMsg(connMsg);
        os.flushMsg();
        m_tsLastAccess = System.currentTimeMillis();
        for (connMsg = null; connMsg == null;)
        {
            try
            {
//ASK                connMsg = (ubAppServerMsg)is.readMsg();
                connMsg = this.readMsg();
            }
            catch(java.net.SocketTimeoutException e)
            {
            	
                conntimeout = true;
              //  BrokerException be = new BrokerException(BrokerException.CODE_PROTOCOL_ERROR,CONN_MSG);
            	throw new Exception(CONN_MSG);
            }
            catch(InterruptedIOException ie)
            {
            if (log.ifLogVerbose(m_basicLogEntries,m_basicLogIndex))
                log.logVerbose(m_basicLogIndex,
                               "Interrupted CONNECT readMsg() IO Exception: " + ie.getMessage());
            // BUG 20041006-001  Don't bail out for stop handling interrupt
            //throw ie;
            }
        }
    }
    catch(ubMsg.MsgFormatException me)
    {

        log.logError("CONNECT MsgFormatException:  " + me.getMessage()
             + ":" + me.getDetail());

        trace.print("CONNECT MsgFormatException:  " + me.getMessage()
             + ":" + me.getDetail(), 1);
        return ubMsg.UBRSP_MSGFORMATEXCEPTION;
    }
    catch(IOException e)
    {
        log.logError("CONNECT write IOException:  " + e);
        throw e;    // replicate 9.1C functionality, but log the event.
    }

    /* store any info returned from broker in response */
    ret = connMsg.getubRsp();

    /* smart connections: store the string return value from the connect
     * procedure if there is one */
    m_connectionReturnValue = connMsg.get4GLErrMsg();

    /* if there was a string value, log it */
    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex) &&
       (m_connectionReturnValue != null) )
    {
        log.logBasic(m_debugLogIndex,
                     "connect procedure return value = " + m_connectionReturnValue );
    }

    if ((ret == ubMsg.UBRSP_OK)  &&
        (connMsg.getMsgcode() == ubAppServerMsg.CSMSSG_CONNECT_ACK))
    {
        if(connMsg.get4GLCondCode() != ubAppServerMsg.CS_COND_NONE)
        {

            log.logError(   "CONNECT failure(Open4GLCondCode)= " +
                             connMsg.get4GLCondCode());

            trace.print("CONNECT failure(Open4GLCondCode)= " + connMsg.get4GLCondCode(), 1);
            return ubMsg.UBRSP_CONN_REFUSED;
        }
        if (fToBroker)
        {
            /* interpret msg from server ... snag the connection-id */

            /* find the offset of connection-id      */
            /* format of the buffer is as follows:   */
            /*                                       */
            /*  ofst  len  type  desc                */
            /*  ----  ---  ----  ----                */
            /*    0     1  tiny  condition code      */
            /*    1     2  short error code          */
            /*    3     2  short errmsg string len   */
            /*    5     X  str   error message       */
            /*  5+X     1  tiny  null terminator     */
            /*  6+X     1  tiny  reconnect flag      */
            /*  7+X     4  long  service ID          */
            /* 11+X     4  long  log ID              */
            /* 15+X     2  short connection-id len   */
            /* 17+X     Y  str   connection-id       */
            /* 17+X+Y   1  tiny  null terminator     */
            /*                                       */

            int len = connMsg.getBuflen();
            int errmsglen =
                 (len < 5) ? 0 : ubMsg.getNetShort(connMsg.getMsgbuf(), 3);
            int idx = errmsglen + 14;  /* errmsg len includes null terminator */

            /* Get our connectionID */
            connID = (len > idx) ?
                       ubAppServerMsg.getNetString(connMsg.getMsgbuf(), idx) : null;

            /*
            log.LogMsgln(log_dest, Logger.LOGGING_DEBUG, Logger.NOTIMESTAMP,
                           "connID= " + connID);
            */
            trace.print("Connection ID= " + connID, 4);

            /* snag the serverPort number, if it's there */
            /* serverPort is in upper word of RspExt field */
            serverPort = ((connMsg.getubRspExt() >> 16) & 0xFFFF);


            if (log.ifLogVerbose(m_basicLogEntries,m_basicLogIndex))
                log.logVerbose(m_basicLogIndex,
                               "serverPort= " + serverPort);

            trace.print("CONNECT: Reconnecting to AppServer at port " + serverPort);

            /* fetch and store the ask features enabled by the server */
            negotiateAskCapabilities(connMsg);
        }

        try
        {
          String retStr = connMsg.getTlvField_NoThrow(ubMsg.TLVTYPE_APSVCL_VERS);
          if (retStr != null)
          {
              Short key = new Short(ubMsg.TLVTYPE_APSVCL_VERS);
              m_capabilities.put((Object)key, (Object)retStr);
          }
          
          retStr = connMsg.getTlvField_NoThrow(ubMsg.TLVTYPE_CMPRCAPABLE);
          if (retStr != null)
          {
              Short key = new Short(ubMsg.TLVTYPE_CMPRCAPABLE);
              m_capabilities.put((Object) key, (Object) retStr);
          }
          
        /* extract the rqInfo data */
        rqInfo = new ubRqInfo(connMsg);
        rqInfoToProperties(TLV_PROPERTIES_RSP,
                           m_properties,
                           rqInfo);
        }
        catch (Exception e)
        {
          // empty.
        }

    }
    else
    {
        String  errText = null;

        int len = connMsg.getBuflen();
        int errmsglen =
             (len < 5) ? 0 : ubMsg.getNetShort(connMsg.getMsgbuf(), 3);

        if (0 < errmsglen)
        {
            errText = ubAppServerMsg.getNetString(connMsg.getMsgbuf(), 3);
        }

        String  errMsg = new String("CONNECT failure= " + ret + " (" + errText + ")");

        log.logError(errMsg);
        trace.print("CONNECT: Unable to connect - reason= " + ret, 1);
    }

    return ret;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private int xidPacket(
          String requestID,
          String username,
          String password,
          String connectionStr,
          int clntAskCaps
         )
    throws IOException, BrokerException, NetworkProtocolException, Exception
{
    int ret;
    int     n = 0;
    byte[]  b;
    byte[] username_b = null;
    byte[] password_b = null;
    byte[] connectionStr_b = null;
    byte[] connID_b = null;
    byte[] appService_b = null;
    ubRqInfo rqInfo;
 
    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
        log.logBasic(m_debugLogIndex,
                    "xidPacket()");

    /* calculate the message buffer size */
    n += (PROGRESS_WIRECODEPAGE == null)   ? 2 : (PROGRESS_WIRECODEPAGE.length() + 3);

    if (username == null)
    {
        n += 2;
    }
    else
    {
        username_b = ubMsg.newNetByteArray(username);
        n += (username_b.length + 3);
    }
    if (password == null)
    {
        n += 2;
    }
    else
    {
        password_b = ubMsg.newNetByteArray(password);
        n += (password_b.length + 3);
    }
    if (connectionStr == null)
    {
        n += 2;
    }
    else
    {
        connectionStr_b = ubMsg.newNetByteArray(connectionStr);
        n += (connectionStr_b.length + 3);
    }
    if (connID == null)
    {
        n += 2;
    }
    else
    {
        connID_b = ubMsg.newNetByteArray(connID);
        n += (connID_b.length + 3);
    }

    // If we are tunneling through the HTTP or HTTP/S protocol, add in the
    // service name here.
    //
    if (SocketConnectionInfoEx.HTTP_PROTOCOL == sockInfo.getProtocolType() ||
        SocketConnectionInfoEx.HTTPS_PROTOCOL == sockInfo.getProtocolType())
    {
        if (sockInfo.getService() == null)
        {
            n += 2;
        }
        else
        {
            appService_b = ubMsg.newNetByteArray(sockInfo.getService());
            n += (appService_b.length + 3);
        }
    }

    /* allocate the new message of the correct length */
    ubAppServerMsg xidMsg = new ubAppServerMsg(
                               ubProtocolVersion,
                               // ubAppServerMsg.CSMSSG_CONNECT,
                               ubAppServerMsg.CSMSSG_OPEN4GL,
                               ++seqnum, n, n);

    /* set header fields */
    xidMsg.setubSrc(ubMsg.UBSRC_CLIENT);
    xidMsg.setubRq(ubMsg.UBRQ_XID);

    switch (ubProtocolVersion)
        {
        case ubMsg.UBMSG_PROTOCOL_V0:
            break;

        case ubMsg.UBMSG_PROTOCOL_V1:
        default:
            /* set the tlvbuffer */
            try
                {
                xidMsg.appendTlvField(ubMsg.TLVTYPE_RQID, requestID);
 
                /* add the capability information to tell the AppServer our capabilities */
                for (int i = 0; i < APPSRVCAPINFO_TYPE.length; i++)
                    xidMsg.appendTlvField(APPSRVCAPINFO_TYPE[i], APPSRVCAPINFO_VALUE[i]);

                /* add the ASK request if the user has set the option */
                if (clntAskCaps != IPoolProps.ASK_DISABLED)
                    {
                    xidMsg.appendTlvField(
                                 ubMsg.TLVTYPE_ASKPING_VER,
                                 String.valueOf(IPoolProps.ASK_VERSION));
                    xidMsg.appendTlvField(
                                 ubMsg.TLVTYPE_ASKPING_CAPS,
                                 formatAskCapabilities(clntAskCaps));
                    }

                /* add in the requestInfo data */
                rqInfo = new ubRqInfo();
                propertiesToRqInfo(TLV_PROPERTIES_RQ,
                                   m_properties,
                                   rqInfo);
                xidMsg.augmentTlvInfo(rqInfo);
                }
            catch (Exception e)
                {
                log.logError(  "appendTlvField Exception: " + e.getMessage());
                BrokerException be = new BrokerException(
                                           BrokerException.CODE_PROTOCOL_ERROR,
                                           e.getMessage());
                trace.print(be, 1);
                throw be;
                }
            break;
        }
    

    /* marshall the data into the message */
    b = xidMsg.getMsgbuf();

    n = ubAppServerMsg.setNetString(b, 0, PROGRESS_WIRECODEPAGE);
    n = ubAppServerMsg.setNetString(b, n, username_b);
    n = ubAppServerMsg.setNetString(b, n, password_b);
    n = ubAppServerMsg.setNetString(b, n, connectionStr_b);
    n = ubAppServerMsg.setNetString(b, n, connID_b);
    if (SocketConnectionInfoEx.HTTP_PROTOCOL == sockInfo.getProtocolType() ||
        SocketConnectionInfoEx.HTTPS_PROTOCOL == sockInfo.getProtocolType())
    {
        n = ubAppServerMsg.setNetString(b, n, appService_b);
    }

    xidMsg.setBuflen(n);

/*
    log.LogDump(log_dest, Logger.LOGGING_DEBUG, Logger.NOTIMESTAMP,
                        "xidPacket() xidMsg msgbuf" ,
                        xidMsg.getMsgbuf(), xidMsg.getBuflen());
*/

    try
    {
        os.writeMsg(xidMsg);
        os.flushMsg();
        m_tsLastAccess = System.currentTimeMillis();
        for (xidMsg = null; xidMsg == null;)
        {
            try
            {
//ASK                xidMsg = (ubAppServerMsg)is.readMsg();
                xidMsg = this.readMsg();
            }
            catch(java.net.SocketTimeoutException e)
            {
            	conntimeout = true;
                // convert the generic NetworkProtocolException into the OpenClient facility
            	//BrokerException be = new BrokerException(BrokerException.CODE_PROTOCOL_ERROR,CONN_MSG);
            	throw new Exception(CONN_MSG);
            }
            catch(InterruptedIOException ie)
            {
            if (log.ifLogVerbose(m_basicLogEntries,m_basicLogIndex))
                log.logVerbose(m_basicLogIndex,
                               "Interrupted XID readMsg() IO Exception: " + ie.getMessage());
                // Should we ever give up??
            }
        }
    }
    catch(ubMsg.MsgFormatException me)
    {
        log.logError("XID MsgFormatException:  " + me.getMessage()
                     + ":" + me.getDetail());

        trace.print("XID MsgFormatException:  " + me.getMessage()
             + ":" + me.getDetail(), 1);
        return ubMsg.UBRSP_MSGFORMATEXCEPTION;
    }
    catch(IOException e)
    {
        log.logError("XID write IOException:  " + e);
        throw e;    // replicate 9.1C functionality, but log the event.
    }

    /* store any info returned from broker in response */
    ret = xidMsg.getubRsp();

    if (ret == ubMsg.UBRSP_OK)
        {
        /* interpret msg from server ... snag the connection-id */

        /* find the offset of connection-id      */
        /* format of the buffer is as follows:   */
        /*                                       */
        /*  ofst  len  type  desc                */
        /*  ----  ---  ----  ----                */
        /*    0     1  tiny  condition code      */
        /*    1     2  short error code          */
        /*    3     2  short errmsg string len   */
        /*    5     X  str   error message       */
        /*  5+X     1  tiny  null terminator     */
        /*  6+X     1  tiny  reconnect flag      */
        /*  7+X     4  long  service ID          */
        /* 11+X     4  long  log ID              */
        /* 15+X     2  short connection-id len   */
        /* 17+X     Y  str   connection-id       */
        /* 17+X+Y   1  tiny  null terminator     */

        int len = xidMsg.getBuflen();
        int errmsglen =
                 (len < 5) ? 0 : ubMsg.getNetShort(xidMsg.getMsgbuf(), 3);
        int idx = errmsglen + 14;  /* errmsg len includes null terminator */

        /* Get our connectionID */
        connID = (len > idx) ?
                       ubAppServerMsg.getNetString(xidMsg.getMsgbuf(), idx) : null;

       if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
           log.logBasic(m_debugLogIndex,
                       "connID= " + connID);

        trace.print("Connection ID= " + connID, 4);

        serverPort = 0;

        /* fetch and store the ask features enabled by the server */
        negotiateAskCapabilities(xidMsg);

        try
        {
          String retStr = xidMsg.getTlvField_NoThrow(ubMsg.TLVTYPE_APSVCL_VERS);
          if (retStr != null)
          {
              Short key = new Short(ubMsg.TLVTYPE_APSVCL_VERS);
              m_capabilities.put((Object)key, (Object)retStr);
          }
          
          retStr = xidMsg.getTlvField_NoThrow(ubMsg.TLVTYPE_CMPRCAPABLE);
          if (retStr != null) {
              Short key = new Short(ubMsg.TLVTYPE_CMPRCAPABLE);
              m_capabilities.put((Object)key, (Object)retStr);
          }
          
        /* extract the rqInfo data */
        rqInfo = new ubRqInfo(xidMsg);
        rqInfoToProperties(TLV_PROPERTIES_RSP,
                           m_properties,
                           rqInfo);
        }
        catch (Exception e)
        {
          // empty.
        }

        }
    else
        {
        String  errText = null;

        int len = xidMsg.getBuflen();
        int errmsglen =
             (len < 5) ? 0 : ubMsg.getNetShort(xidMsg.getMsgbuf(), 3);

        if (0 < errmsglen)
        {
            errText = ubAppServerMsg.getNetString(xidMsg.getMsgbuf(), 3);
        }

        String  errMsg = new String("XID failure= " + ret + " (" + errText + ")");

        log.logError(errMsg);
        trace.print("XID: Unable to connect - reason= " + ret, 1);
    }

    return ret;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private boolean writePacket(byte[] msgbuf, int len)
    throws BrokerException
    {
    boolean ret = true;

    ubAppServerMsg msg = new ubAppServerMsg(
                             ubProtocolVersion,
                             ubAppServerMsg.CSMSSG_OPEN4GL, ++seqnum,
                             len, ubMsg.DEF_BUFSIZE);

    ubRqInfo rqInfo;

    msg.setubSrc(ubMsg.UBSRC_CLIENT);
    msg.setubRq(ubMsg.UBRQ_WRITEDATA);

    switch (ubProtocolVersion)
        {
        case ubMsg.UBMSG_PROTOCOL_V0:
            break;

        case ubMsg.UBMSG_PROTOCOL_V1:
        default:
            /* set the tlvbuffer */
            try
                {
                msg.appendTlvField(ubMsg.TLVTYPE_RQID, m_requestID);
                addASKRequest(msg, true);

                /* add in the requestInfo to first packet of data */
                if (current_state == STATE_ALLOCATED)
                    {
                    rqInfo = new ubRqInfo();
                    propertiesToRqInfo(TLV_PROPERTIES_RQ,
                                       m_properties,
                                       rqInfo);
                    msg.augmentTlvInfo(rqInfo);
                    }
                }
            catch (Exception e)
                {
                log.logError(  "appendTlvField Exception: " + e.getMessage());
                BrokerException be = new BrokerException(
                                           BrokerException.CODE_PROTOCOL_ERROR,
                                           e.getMessage());
                trace.print(be, 1);
                current_state = STATE_ERROR;
                throw be;
                }
            break;
        }

    setMessageBody(msg, msgbuf, len);

    try
        {
        os.writeMsg(msg);
        ret = true;
        m_tsLastAccess = System.currentTimeMillis();
        }
    catch(IOException e)
        {
        eof = false;
        os_pos = 0;
        current_state = STATE_ERROR;
        throwCommunicationsException(BrokerException.CODE_SENDDATA_ERROR,
                                     "WRITEDATA IOException", e);
        }
    catch (NetworkProtocolException e1)
        {
        eof = false;
        os_pos = 0;
        current_state = STATE_ERROR;
        throwCommunicationsException(BrokerException.CODE_PROTOCOL_ERROR,
                                     "WRITEDATA NetworkProtocolException", e1);
        }

    eof = false;
    return ret;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private boolean writeLastPacket(int reason, byte[] msgbuf, int len)
    throws BrokerException
    {
    boolean ret = true;
    ubRqInfo rqInfo;

    ubAppServerMsg msg = new ubAppServerMsg(
                             ubProtocolVersion,
                             ubAppServerMsg.CSMSSG_OPEN4GL,
                             ++seqnum,
                             len,
                             ubMsg.DEF_BUFSIZE);

    msg.setubSrc(ubMsg.UBSRC_CLIENT);
    msg.setubRq(ubMsg.UBRQ_WRITEDATALAST);

    switch (ubProtocolVersion)
        {
        case ubMsg.UBMSG_PROTOCOL_V0:
            /* encode max buffer size in upper 16 bits of rqExt */
            /* encode reason code in upper byte of lower word   */
            /* low byte of low word used for flags              */
            msg.setubRqExt(
                    (ubConstants.MSG_DEF_BUFSIZE << 16) |
                    ((reason & 0xFF) << 8)
                    );
            break;

        case ubMsg.UBMSG_PROTOCOL_V1:
        default:
            /* set the tlvbuffer */
            try
                {
                msg.appendTlvField(ubMsg.TLVTYPE_RQID, m_requestID);
                addASKRequest(msg, false);

                /* add in the requestInfo to first packet of data */
                if (current_state == STATE_ALLOCATED)
                    {
                    rqInfo = new ubRqInfo();
                    propertiesToRqInfo(TLV_PROPERTIES_RQ,
                                       m_properties,
                                       rqInfo);
                    msg.augmentTlvInfo(rqInfo);
                    }
                }
                catch (Exception e)
                {
                eof = false;
                os_pos = 0;
                current_state = STATE_ERROR;
                log.logError("appendTlvField Exception: " + e.getMessage());
                BrokerException be = new BrokerException(
                                           BrokerException.CODE_PROTOCOL_ERROR,
                                           e.getMessage());
                trace.print(be, 1);
                throw be;
                }

            /* encode readson code as upper byte of lower word */
            /* low byte of low word used for flags             */
            /* NOTE: upper word is tlv_buffer_len              */
            msg.setubRqExt((reason & 0xFF) << 8 );
            break;
        }

    setMessageBody(msg, msgbuf, len);

    try
        {
        os.writeMsg(msg);
        os.flushMsg();
        ret = true;
        m_tsLastAccess = System.currentTimeMillis();
        }
    catch(IOException e)
        {
        eof = false;
        os_pos = 0;
        current_state = STATE_ERROR;
        throwCommunicationsException(BrokerException.CODE_SENDDATA_ERROR,
                                     "WRITEDATALAST IOException", e);
        }
    catch (NetworkProtocolException e1)
    {
        eof = false;
        os_pos = 0;
        current_state = STATE_ERROR;
        throwCommunicationsException(BrokerException.CODE_PROTOCOL_ERROR,
                                     "WRITEDATA NetworkProtocolException", e1);
    }

    eof = false;
    return ret;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private int readPacket()
    throws BrokerException
{
    ubAppServerMsg msg;
    int rq;
    int retlen = 0;
    ubRqInfo rqInfo;

    if (eof)
        {
        is_pos = 0;
        imsg.setBuflen(0);
        return Broker.EOF;
    }
    try
    {
    Properties              instanceOptions = m_properties.getAsProperties();
    String soTimeout = instanceOptions.getProperty(
            IPoolProps.SOCKET_TIMEOUT);
    if(soTimeout != null)
	netProtocolHandler.setDynamicProtocolProperty( IPoolProps.SOCKET_TIMEOUT, soTimeout);

    }
    catch(Exception e)
    {
    
    }
    //log.LogMsgln(log_dest,
    //             Logger.LOGGING_DEBUG,
    //             Logger.TIMESTAMP,
    //             "readPacket start. ");

    for (imsg = null; imsg == null;)
    {
        try
        {
//ASK            imsg = (ubAppServerMsg)is.readMsg();
            imsg = this.readMsg();
            if (imsg != null)
                retlen = imsg.getBuflen();
        }
        catch(ubMsg.MsgFormatException me)
        {
            MessageFormatException beMsgFormat = new MessageFormatException();

            log.logStackTrace("", beMsgFormat);
            log.logError(   me.getMessage() + ":" + me.getDetail());

            trace.print(beMsgFormat.toString() + me.getMessage() + 
                         ":" + me.getDetail(), 1);
            throw beMsgFormat;
        }
        catch(InterruptedIOException ie)
        {
            // See if server is still there
            newAskEvent();

            // Check if the client wants to STOP
            if (current_state == STATE_STOPPING)
            {

            if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                log.logBasic(m_debugLogIndex,
                             "readPacket() calling setStopPacket()");

                setStopPacket();
            }
        }
        catch(IOException e)
        {
        throwCommunicationsException(BrokerException.CODE_RECVDATA_ERROR,
                                     "READPACKET IOException", e);
        }
        catch (NetworkProtocolException e1)
        {
            // convert the generic NetworkProtocolException
            // into the OpenClient facility
            BrokerException be = new BrokerException(
                                          BrokerException.CODE_PROTOCOL_ERROR,
                                          e1.getMessage());
            log.logStackTrace("",
                              be);
            trace.print(be, 1);
            throw be;
        }
    }

    /* check out the response */

    rq = imsg.getubRq();

    switch (imsg.getubRsp())
    {
    case ubMsg.UBRSP_OK:
        /* extract the rqInfo data */
        try
            {
            rqInfo = new ubRqInfo(imsg);
            rqInfoToProperties(TLV_PROPERTIES_RSP,
                               m_properties,
                               rqInfo);
            }
        catch(ubMsg.MsgFormatException me)
            {
            // we already checked for this above */
            }

        break;
    case ubMsg.UBRSP_ABNORMAL_EOF:
        stop_sent = false;
        current_state = STATE_EOF;
        // Print to trace file
        imsg.print("Server returned Abnormal EOF",
                   IAppLogger.LOGGING_ERRORS,0, log);

        AbnormalEOFException beEOFExcp = new AbnormalEOFException();

        log.logStackTrace("", beEOFExcp);

        trace.print(beEOFExcp, 1);
        throw beEOFExcp;
    case ubMsg.UBRSP_NO_AVAILABLE_SERVERS:
        stop_sent = false;
        current_state = STATE_EOF;

        imsg.print("No available servers",
            IAppLogger.LOGGING_ERRORS,0, log);

        NoAvailableServersException beNoServersExcp =
               new NoAvailableServersException();

        log.logStackTrace("", beNoServersExcp);

        trace.print(beNoServersExcp, 1);
        throw beNoServersExcp;
    default:
        //TODO: Print to trace file
        imsg.print( "READ error:  " + imsg.getubRsp(),
         IAppLogger.LOGGING_ERRORS, 0, log);
        //TODO: Better exception - Does this ever happen?
        throwBrokerException("read() error= "  + imsg.getubRsp());
        break;
    }

    eof = (rq == ubMsg.UBRQ_RSPDATALAST);

    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
        log.logBasic(m_debugLogIndex,
                     "readPacket End: eof = " + eof + " : length = " + retlen);

    is_pos = 0;

    if ((retlen == 0) && !eof)
    {
        // Print to trace file
        imsg.print( "retlen==0 & !eof",
        IAppLogger.LOGGING_BASIC, m_debugLogIndex, log);
    }

    if (current_state == STATE_STOPPING)
    {
    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
        log.logBasic(m_debugLogIndex,
                     "readPacket() calling setStopPacket() after read");

        setStopPacket();
    }

    return ((retlen == 0) && eof) ? Broker.EOF : retlen;
}

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private void setStopPacket()
    throws BrokerException
    {

    if (stop_sent)
        return;

    ubAppServerMsg setStop = new ubAppServerMsg(
                             ubProtocolVersion,
                             ubAppServerMsg.CSMSSG_STOP,
                             ++seqnum,
                             0, ubMsg.DEF_BUFSIZE);

    setStop.setubSrc(ubMsg.UBSRC_CLIENT);
    setStop.setubRq(ubMsg.UBRQ_SETSTOP);
    try
        {
        if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
            log.logBasic(m_debugLogIndex,
                        "SETSTOP writeMsg()");
        os.writeMsg(setStop);
        if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
            log.logBasic(m_debugLogIndex,
                         "SETSTOP flush()");
        os.flushMsg();
        stop_sent = true;
        m_tsLastAccess = System.currentTimeMillis();
        }
    catch(IOException e)
        {
        //TODO: Should we throw anything here?

        log.logError("SETSTOP IOException:  " + e);

        trace.print("SETSTOP: IOException:  " + e, 1);
        }
    catch (NetworkProtocolException e1)
    {
        // convert the generic NetworkProtocolException
        // into the OpenClient facility
        BrokerException be = new BrokerException(
                                     BrokerException.CODE_PROTOCOL_ERROR,
                                     e1.getMessage());
        log.logStackTrace("",
                          be);
        trace.print(be, 1);
        throw be;
    }

    }


/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private ubAppServerMsg readMsg()
    throws BrokerException, 
           ubMsg.MsgFormatException, 
           InterruptedIOException, 
           IOException, 
           NetworkProtocolException
{
    ubAppServerMsg msg = null;

    for (msg = null; msg == null; )
    {
       msg = (ubAppServerMsg) is.readMsg();
       m_tsLastAccess = System.currentTimeMillis();
       m_ASKstate = ASKSTATE_ACTIVITY_TIMEOUT;  // reset clientASK state if anything is read

       if ((msg != null) && (msg.getubRq() == ubMsg.UBRQ_ASKPING_RQ))
       {
           trace.print(m_sessionID + " detected ASKPing request", 4);
           askPingPacket(ubMsg.UBRQ_ASKPING_RSP);
           msg = null;
       }

       if ((msg != null) && (msg.getubRq() == ubMsg.UBRQ_ASKPING_RSP))
       {
           trace.print(m_sessionID + " detected ASKPing reply", 4);
           // nothing to do since time and state have already been updated
           msg = null;
       }
    }
    
    return msg;
}

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private void  askPingPacket(int rqCode)
     throws BrokerException
{
    String logStr;
    ubAppServerMsg askPingMsg = new ubAppServerMsg(
                                  ubProtocolVersion,
                                   ubAppServerMsg.CSMSSG_OPEN4GL,
                                   ++seqnum,
                                   0,
                                   ubMsg.DEF_BUFSIZE);
    
    logStr = (rqCode == ubMsg.UBRQ_ASKPING_RQ)  ? "UBRQ_ASKPING_RQ"  :
             (rqCode == ubMsg.UBRQ_ASKPING_RSP) ? "UBRQ_ASKPING_RSP" :
                                                  "(rqCode="+rqCode+")";
    askPingMsg.setubSrc(ubMsg.UBSRC_CLIENT);
    askPingMsg.setubRq(rqCode);

    try
    {
    if (rqCode == ubMsg.UBRQ_ASKPING_RSP)
            trace.print(m_sessionID + " sending ASKPing response", 4);
    else
            trace.print(m_sessionID + " sending ASKPing request", 4);
        
    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
            log.logBasic(m_debugLogIndex,
                     logStr + " writeMsg()");
        os.writeMsg(askPingMsg);

        if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
            log.logBasic(m_debugLogIndex,
                    logStr + " flush()");
        os.flushMsg();
        m_tsLastAccess = System.currentTimeMillis();
   }
    catch (IOException e)
    {
        // convert the IOException into the OpenClient facility
        BrokerException be = new BrokerException(
                                BrokerException.CODE_GENERAL_ERROR, 
                                e + " [" + e.getMessage() + "]");
        log.logStackTrace("",
                          be);
        trace.print(be, 1);
        throw be;
    }
    catch (NetworkProtocolException e1)
    {
        // convert the generic NetworkProtocolException
        // into the OpenClient facility
        BrokerException be = new BrokerException(
                                BrokerException.CODE_PROTOCOL_ERROR, 
                                e1 + " [" + e1.getMessage() + "]");
        log.logStackTrace("",
                          be);
        trace.print(be, 1);
        throw be;
    }
}



/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private boolean checkStop() throws BrokerException
{
    try
    {
/*        log.LogMsgln(log_dest, Logger.LOGGING_TRACE, Logger.TIMESTAMP,
                "Checking for server STOP");
*/
        if (is.available() > 0)
        {
            ubAppServerMsg imsg = (ubAppServerMsg)is.readMsg();
            m_tsLastAccess = System.currentTimeMillis();
            m_ASKstate = ASKSTATE_ACTIVITY_TIMEOUT;

            /*  20041109-012
                We only get here if the available() method returns that there is data.
                However, if all we got was a UBRQ_SEND_EMPTY_MSG message, then readMsg() will 
                return a null message since that message contains no real data, so we
                should check if we got null and return false.
                Just as a note, the UBRQ_SEND_EMPTY_MSG message was added to fix 20020816-010
                - it places an http message on the wire to force the WebServer to send a 
                message immediately to the client.
            */
            if (imsg == null)
                return false;

            /* while checking for a stop message, we might get  */
            /* an ASKPING request.  If so, then reply to it     */
            if (imsg.getubRq() == ubMsg.UBRQ_ASKPING_RQ)
            {
                if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                    log.logBasic(m_debugLogIndex,
                                 "Server sent an ASKPING request");
                trace.print(m_sessionID + " Server sent an ASKPING request", 1);
                askPingPacket(ubMsg.UBRQ_ASKPING_RSP);

                /* we didn't get a STOP msg, so return false here */
                return false;
            }

            if (imsg.getubRq() == ubMsg.UBRQ_ASKPING_RSP)
            {
                if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                    log.logBasic(m_debugLogIndex,
                                 "Server sent an ASKPING reply");
                trace.print(m_sessionID + " Server sent an ASKPing reply", 1);

                /* we didn't get a STOP msg, so return false here */
                return false;
            }

            if (imsg.getMsgcode() == ubAppServerMsg.CSMSSG_STOP)
            {
                if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                    log.logBasic(m_debugLogIndex,
                            "Server sent a STOP");
                trace.print("Server sent a STOP", 1);
                return true;
            }
            /* there is a BIG HOLE here ... */
            /* if we got data available, but it wasn't an ASKPING rq */
            /* and it wasn't a STOP msg, then what was it???  this   */
            /* REALLY BAD, and we should (log and) report it         */
        }
        else
            return false;
    }
    catch(ubMsg.MsgFormatException me)
    {
        MessageFormatException beMsgFormat = new MessageFormatException();

        log.logStackTrace("", beMsgFormat);
        log.logError(me.getMessage() + ":" + me.getDetail());

        trace.print(beMsgFormat, 1);
        throw beMsgFormat;
    }
    catch(IOException e)
    {
        BrokerSystemCommunicationsException beIOExcp =
                      new BrokerSystemCommunicationsException(e);

        log.logStackTrace("", beIOExcp);

        trace.print(beIOExcp, 1);
        throw beIOExcp;
    }
    catch (NetworkProtocolException e1)
    {
        // convert the generic NetworkProtocolException
        // into the OpenClient facility
        BrokerException be = new BrokerException(
                                     BrokerException.CODE_PROTOCOL_ERROR,
                                     e1.getMessage());
        log.logStackTrace( "",
                          be);
        trace.print(be, 1);
        throw be;
    }
    return false;
}

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private boolean disconnectPacket(String requestID)
    {
    boolean ret;
    byte[]  b;
    int n = 0;
    ubRqInfo rqInfo;

    /* calculate the message buffer size */
    n += (PROGRESS_WIRECODEPAGE == null)   ? 
           2 : (PROGRESS_WIRECODEPAGE.length() + 3);
    /* Dummy connection-id and context */
    n += 4;

    /* allocate the new message of the correct length */
    ubAppServerMsg disconnMsg = new ubAppServerMsg(
                                    ubProtocolVersion,
                                    ubAppServerMsg.CSMSSG_DISCONN,
                                    ++seqnum, n, n);

    /* set header fields */
    disconnMsg.setubSrc(ubAppServerMsg.UBSRC_CLIENT);
    disconnMsg.setubRq(ubMsg.UBRQ_DISCONNECT);

    switch (ubProtocolVersion)
        {
        case ubMsg.UBMSG_PROTOCOL_V0:
            break;

        case ubMsg.UBMSG_PROTOCOL_V1:
        default:
            /* set the tlvbuffer */
            try
                {
                disconnMsg.appendTlvField(ubMsg.TLVTYPE_RQID, requestID);

                /* add in the requestInfo data */
                rqInfo = new ubRqInfo();
                propertiesToRqInfo(TLV_PROPERTIES_RQ,
                                   m_properties,
                                   rqInfo);
                disconnMsg.augmentTlvInfo(rqInfo);
                }
            catch (Exception e)
                {
                log.logError(  "appendTlvField Exception: " + e.getMessage());
                BrokerException be = new BrokerException(
                                           BrokerException.CODE_PROTOCOL_ERROR,
                                           e.getMessage());
                trace.print(be, 1);
                // don't throw this ... the upper layers don't want it
                // throw be;
                }
            break;
        }

    /* marshall the data into the message */
    b = disconnMsg.getMsgbuf();

    n = ubAppServerMsg.setNetString(b, 0, PROGRESS_WIRECODEPAGE);
    n = ubAppServerMsg.setNetString(b, n, (String)null);
    n = ubAppServerMsg.setNetString(b, n, (String)null);
    disconnMsg.setBuflen(n);

    try
        {
        os.writeMsg(disconnMsg);
        os.flushMsg();
        m_tsLastAccess = System.currentTimeMillis();
       }
    catch(IOException e)
        {
        log.logError("DISCONNECT IOException:  " + e);

        trace.print("DISCONNECT: IOException:  " + e, 1);
        return false;
        }
    catch (NetworkProtocolException e1)
    {
        log.logError("DISCONNECT NetworkProtocolException:  " + e1);
        trace.print("DISCONNECT: NetworkProtocolException:  " + e1, 1);
        return false;
    }

    for (disconnMsg = null; disconnMsg ==null;)
        {
        try
            {
//ASK            disconnMsg = (ubAppServerMsg)is.readMsg();
            disconnMsg = this.readMsg();

            /* extract the rqInfo data */
            rqInfo = new ubRqInfo(disconnMsg);
            rqInfoToProperties(TLV_PROPERTIES_RSP,
                               m_properties,
                               rqInfo);
            }
        catch(ubMsg.MsgFormatException me)
            {
            log.logError("DISCONNECT MsgFormatException:  " +
                     me.getMessage() + ":" + me.getDetail());

            trace.print("DISCONNECT: MsgFormatException:  " +
                     me.getMessage() + ":" + me.getDetail(), 1);
            return false;
            }
        catch(InterruptedIOException ie)
            {
            log.logError( "DISCONNECT InterruptedIOException:  " + ie);
            // Do nothing.
            }
        catch(IOException e)
            {
            log.logError("DISCONNECT IOException:  " + e);

            trace.print("DISCONNECT: IOException:  " + e, 1);
            return false;
            }
        catch (NetworkProtocolException e1)
        {
            log.logError( "DISCONNECT NetworkProtocolException:  " + e1);
            trace.print("DISCONNECT: NetworkProtocolException:  " + e1, 1);
            return false;
        }
        catch (BrokerException be)
            {
            }
        }

    ret = (disconnMsg.getMsgcode() == ubAppServerMsg.CSMSSG_DISCONN_ACK);

    connID = null;
    return ret;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private void disconnectSocket()
    throws BrokerException
{
    try
    {
    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
        log.logBasic(m_debugLogIndex,
                     "Disconnecting from the AppServer...");

        if (null != netProtocolHandler)
        {
            // Force the streams to close.
            try
            {
                if (os != null)
                {
                    os.close();
                    os = null;
                }
            }
            catch (Exception e)
            {
            }
            try
            {
                if (is != null)
                {
                    is.close();
                    os = null;
                }
            }
            catch (Exception e)
            {
            }
            // Now force the protocol handler to close the connection.
            try
            {

                netProtocolHandler.closeConnection(false);
            }
            catch (NetworkProtocolException e)
            {
                // convert the generic NetworkProtocolException 
                // into the OpenClient facility
                BrokerException be = new BrokerException(
                                            BrokerException.CODE_PROTOCOL_ERROR,
                                            e.getMessage());
                log.logStackTrace("",
                                  be);
                trace.print(be, 1);
                throw be;
            }
            catch (Exception e1)
            {
                // convert the generic NetworkProtocolException
                // into the OpenClient facility
                BrokerException be = new BrokerException(
                                        BrokerException.CODE_PROTOCOL_ERROR,
                                        e1.getMessage());
                log.logStackTrace("",
                                  be);
                trace.print(be, 1);
                throw be;
            }
        }

        // Reset stream and state information for the next connection try.
        os = null;
        is = null;
        eof = false;
        current_state = STATE_IDLE;
    }
    finally
    {
        // Force the stream and state information to accept another connection
        // attempt.
        os = null;
        is = null;
        eof = false;
        current_state = STATE_IDLE;
        m_requestID = null;
    }
}

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private void throwBrokerException(String exceptionMsg)
    throws BrokerException
    {
    BrokerException be = new BrokerException(exceptionMsg);

    log.logStackTrace("", be);

    throw be;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private void throwBrokerException(int ubrsp, String exceptionMsg)
    throws BrokerException
    {
    BrokerException be =
          new BrokerException(ubrsp, exceptionMsg +
                              " (" + ubMsg.getubRspDesc(ubrsp) + ")" );

    log.logStackTrace("", be);

    throw be;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private void throwCommunicationsException(int exceptionCode,
                                          String logMsg,
                                          Exception ex)
    throws BrokerException
    {
    String msg = logMsg + " : " + ex + " (" + ex.getMessage() +")";
    BrokerException be = new BrokerException(exceptionCode, msg);

    log.logStackTrace(msg, ex);

    trace.print(msg, 1);
    trace.print(ex, 1);

    throw be;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private void initializeLogging(IAppLogger log)
    {
    String contextName = log.getLogContext().getLogContextName();

    if (contextName.equals("Wsa"))
        {
        // do we want to split these into multiple bits???
        m_basicLogEntries    = WsaLogContext.SUB_M_UBROKER;
        m_basicLogIndex      = WsaLogContext.SUB_V_UBROKER;
        m_debugLogEntries    = WsaLogContext.SUB_M_UBROKER;
        m_debugLogIndex      = WsaLogContext.SUB_V_UBROKER;
        }
    else if(contextName.equals("Rest"))
    {
        m_basicLogEntries    = RestLogContext.SUB_M_UBROKER;
        m_basicLogIndex      = RestLogContext.SUB_V_UBROKER;
        m_debugLogEntries    = RestLogContext.SUB_M_UBROKER;
        m_debugLogIndex      = RestLogContext.SUB_V_UBROKER;
    }

    else if (contextName.equals("O4gl"))
        {
        // do we want to split these into multiple bits???
        m_basicLogEntries    = O4glLogContext.SUB_M_UBROKER;
        m_basicLogIndex      = O4glLogContext.SUB_V_UBROKER;
        m_debugLogEntries    = O4glLogContext.SUB_M_UBROKER;
        m_debugLogIndex      = O4glLogContext.SUB_V_UBROKER;
        m_cmprsLogEntries    = O4glLogContext.SUB_M_COMPRESSION;
        m_cmprsLogIndex         = O4glLogContext.SUB_V_COMPRESSION;
        }
    else if (contextName.equals("UBroker"))
        {
        m_basicLogEntries    = UBrokerLogContext.SUB_M_UB_BASIC;
        m_basicLogIndex      = UBrokerLogContext.SUB_V_UB_BASIC;
        m_debugLogEntries    = UBrokerLogContext.SUB_M_UB_DEBUG;
        m_debugLogIndex      = UBrokerLogContext.SUB_V_UB_DEBUG;
        m_cmprsLogEntries    = UBrokerLogContext.SUB_M_UB_COMPRESSION;
        m_cmprsLogIndex         = UBrokerLogContext.SUB_V_UB_COMPRESSION;
        }
    else if (contextName.equals("Esb"))
        {
        m_basicLogEntries    = EsbLogContext.SUB_M_NATIVE;
        m_basicLogIndex      = EsbLogContext.SUB_V_NATIVE;
        m_debugLogEntries    = EsbLogContext.SUB_M_O4GL;
        m_debugLogIndex      = EsbLogContext.SUB_V_O4GL;
        }
    else if (contextName.equals("NxGAS"))
        {
        m_basicLogEntries    = NxGASLogContext.SUB_M_NXGASBASIC;
        m_basicLogIndex      = NxGASLogContext.SUB_V_NXGASBASIC;
        m_debugLogEntries    = NxGASLogContext.SUB_M_NXGASDEBUG;
        m_debugLogIndex      = NxGASLogContext.SUB_V_NXGASDEBUG;
        }
    else
        {
        m_basicLogEntries    = 0;
        m_basicLogIndex      = 0;
        m_debugLogEntries    = 0;
        m_debugLogIndex      = 0;
        m_cmprsLogEntries    = 0;
        m_cmprsLogIndex      = 0;
        }
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public long getTsLastAccess()
    {
    return m_tsLastAccess;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private void negotiateAskCapabilities(ubMsg msg)
        {
        int srvrAskCaps;
        int srvrAskVer;
        String tmp;


        /* fetch the server's ASK version */
        try
            {
            tmp = msg.getTlvField_NoThrow(ubMsg.TLVTYPE_ASKPING_VER);
            srvrAskVer = (tmp == null) ? 
                            IPoolProps.INVALID_ASK_VERSION : Integer.parseInt(tmp); 
            }
        catch (Exception e)
            {
            srvrAskVer = IPoolProps.INVALID_ASK_VERSION;
            }

        /* if server's ASK version is invalid or missing, disable the feature */
        if (srvrAskVer == IPoolProps.INVALID_ASK_VERSION)
            {
            m_negotiatedAskVersion = IPoolProps.INVALID_ASK_VERSION;
            m_negotiatedAskCaps = IPoolProps.ASK_DISABLED;
            return;
            }

        /* store the askVersion sent by the server */
        m_negotiatedAskVersion = srvrAskVer;

        try
            {
            tmp = msg.getTlvField_NoThrow(ubMsg.TLVTYPE_ASKPING_CAPS);
            srvrAskCaps = (tmp == null) ? 
                              IPoolProps.ASK_DISABLED : parseAskCapabilities(tmp); 
            }
        catch (Exception e)
            {
            srvrAskCaps = IPoolProps.ASK_DISABLED;
            }

        /* store whatever capabilities the server has enabled */
        m_negotiatedAskCaps = srvrAskCaps;

        /*if (getClientASKEnabled())
        {
           //m_negotiatedAskCaps = m_negotiatedAskCaps & ~IPoolProps.ASK_CLIENTASK_ENABLED; //Arun
           m_negotiatedAskCaps = m_negotiatedAskCaps & IPoolProps.ASK_CLIENTASK_ENABLED;
           trace.print(m_sessionID + " ClientASK Protocol is not supported - ClientASK disabled.", 2 );
        }*/
        if (getClientASKEnabled() && serverPort == 0) 
        {
            m_negotiatedAskCaps = m_negotiatedAskCaps & IPoolProps.ASK_CLIENTASK_ENABLED;
        }
        else
        {
            m_negotiatedAskCaps = m_negotiatedAskCaps & ~IPoolProps.ASK_CLIENTASK_ENABLED; 
            trace.print(m_sessionID + " ClientASK Protocol is not supported for state-reset & state-aware server connections.", 2 );
        }
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private int parseAskCapabilities(String caps_str)
        {
        int caps = IPoolProps.ASK_DEFAULT;
        String upcase_caps_str;

        if (caps_str == null)
            return caps;

        upcase_caps_str = caps_str.toUpperCase();

        if (upcase_caps_str.indexOf("ALLOWSERVERASK") >= 0)
            caps = caps | IPoolProps.ASK_SERVERASK_ENABLED;

        if (upcase_caps_str.indexOf("ALLOWCLIENTASK") >= 0)
            caps = caps | IPoolProps.ASK_CLIENTASK_ENABLED;

        if (upcase_caps_str.indexOf("DENYSERVERASK") >= 0)
            caps = caps & ~IPoolProps.ASK_SERVERASK_ENABLED;

        if (upcase_caps_str.indexOf("DENYCLIENTASK") >= 0)
            caps = caps & ~IPoolProps.ASK_CLIENTASK_ENABLED;

        return caps;
        }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private String formatAskCapabilities(int caps)
        {
        String caps_str;

        caps_str = (((caps & IPoolProps.ASK_SERVERASK_ENABLED) > 0) ?
                     "allowServerASK" : "denyServerASK")       + 
                   "," +
                   (((caps & IPoolProps.ASK_CLIENTASK_ENABLED) > 0) ?
                     "allowClientASK" : "denyClientASK")       ;

        return caps_str;
        }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private String formatAskVersion(int askVer)
        {
        int majorVer;
        int minorVer;
        String verStr;

        majorVer = (askVer >> 16) & 0x0000FFFF;
        minorVer = askVer & 0x0000FFFF;

        verStr = majorVer + "." + minorVer;

        return verStr;
        }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private boolean askValidateProtocolType(SocketConnectionInfoEx connInfo)
//    throws BrokerException
        {
            int protocolType    = connInfo.getProtocolType();
            String protocolName = connInfo.getProtocol();
            BrokerException be;
            boolean ret = true;

            switch(protocolType)
            {
                case SocketConnectionInfoEx.APPSERVER_PROTOCOL:
                case SocketConnectionInfoEx.APPSERVERDC_PROTOCOL:
                    /* serverASK protocol enabled for 10.2A */
                    /* See method negotateAskCapabilities for other disabled features */
                     break;                                             
                default:
                    be = new BrokerException(
                             BrokerException.CODE_PROTOCOL_ERROR,
                             jcMsg.jcMSG193, 
                             new Object[]{protocolName});
                    trace.print(m_sessionID + " " + be.getLocalizedMessage(), 2);
//                  throw be;
                    ret = false;
            }
            return ret;
        }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/
private void addASKRequest(ubAppServerMsg msg, boolean morePacketsToGo)
    throws
        TlvFieldAlreadyExistsException
        ,InvalidMsgVersionException
        ,InvalidTlvBufferException
    {

    long now = System.currentTimeMillis();
    long then = getTsLastAccess();
    long delta = now - then;

    if (getClientASKEnabled() &&
        (m_ASKrqstACKsent == false) &&
        (delta > m_clientASKActivityTimeoutMs))
        {
        trace.print(m_sessionID + " ASKPING request added to message", 4);
        msg.appendTlvField(ubMsg.TLVTYPE_ASKPING_RQST_ACK, Integer.toString(m_clientASKResponseTimeout));
        m_ASKstate = ASKSTATE_RESPONSE_TIMEOUT;
        }

    m_ASKrqstACKsent = morePacketsToGo;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private void newAskEvent()
    throws BrokerException
    {

    /* if we're doing clientASK protocol check to see if it's time  */
    /* to send out an askPing message                               */
    if (getClientASKEnabled())  // only send ASKPing if clientASK is enabled
        {
        long now = System.currentTimeMillis();
        long then = getTsLastAccess();
        long delta = now - then;

        switch(m_ASKstate)
            {
            case ASKSTATE_ACTIVITY_TIMEOUT:
                if (delta > m_clientASKActivityTimeoutMs)
                    processASKActivityTimeout();
                break;

            case ASKSTATE_RESPONSE_TIMEOUT:
                if (delta > m_clientASKResponseTimeoutMs)
                    processASKResponseTimeout();
                break;

            case ASKSTATE_INIT:
            default:
                break;
            }
        }
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private void processASKActivityTimeout()
    throws BrokerException
    {
    trace.print(m_sessionID + " clientASKActivityTimeout has occurred - Issuing ASKPING request to the server", 4);
    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
        log.logBasic(m_debugLogIndex,
                "No messages have been received from the server within " +
                "the clientASKActivityTimeout period ... issuing an " +
                "ASKPing request to the server.");

    /* we must do this BEFORE we send the message to get     */
    /* the sendClientRsp() method to log the correct message */
    m_ASKstate = ASKSTATE_RESPONSE_TIMEOUT;

    /* form an ASKPing request here and send it to client */
    askPingPacket(ubMsg.UBRQ_ASKPING_RQ);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private void processASKResponseTimeout()
throws BrokerException
    {
    unconditionalDisconnect();
    m_ASKstate = ASKSTATE_ACTIVITY_TIMEOUT;

    trace.print(m_sessionID + " clientASKResponseTimeout has occurred - disconnecting from server", 4);
    if (log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
		{
        log.logBasic(m_debugLogIndex,
                     "No messages were received from the server within the " +
                     "clientASKResponseTimeout interval.  The connection " +
                     " to the server has been terminated.");
             throw new BrokerException();
		}
		else
    throwBrokerException(BrokerException.CODE_GENERAL_ERROR, 
        "clientASKResponseTimeout has occurred, disconnecting from server"); 
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setSessionID(String id)
{
    m_sessionID = id;
}

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public static class InvalidStateException extends BrokerException
{
    public InvalidStateException(String action, String currentState)
    {
        /* "Invalid state for %s<action>: Current state=%s<currentState>" */
        super(SYSTEM_ERROR, jcMSG072, new Object[] {action, currentState});
    }
}
/*
public static class NameServerException extends BrokerException
{
    public NameServerException(Throwable nsException)
    {
        /* "%s<nsmsg>" */
/*        super(SYSTEM_ERROR, jcMSG073, null, nsException);
    }
}*/

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public static class ClientStopException extends BrokerException
{
    public ClientStopException()
    {
        /* "Client requested STOP" */
        super(SERVER_STOP, jcMSG075, null);
    }
}

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public static class ServerStopException extends BrokerException
{
    public ServerStopException()
    {
        /* "Server requested STOP" */
        super(SERVER_STOP, jcMSG076, null);
    }
}

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public static class MessageFormatException extends BrokerException
{
    public MessageFormatException()
    {
        /* "Error marshalling or Unmarshalling AppServer message." */
        super(COMM_ERROR, jcMSG095, null);
        //super("Error marshalling or Unmarshalling AppServer message.");
    }
}

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public static class BrokerSystemCommunicationsException extends BrokerException
{
    public BrokerSystemCommunicationsException(Throwable t)
    {
        /* "Client Communications Failure - %s<Java Exception>" */
        super(COMM_ERROR, jcMSG096, new Object[]{t.toString()});
        //super("Client Communications Failure - %s<Java Exception>");
    }
}

/*
public static class BrokerSystemSendDataException extends BrokerSystemCommunicationsException
{
    public BrokerSystemSendDataException(Throwable t)
    {
        super(t);
    }
}

public static class BrokerSystemRecvDataException extends BrokerSystemCommunicationsException
{
    public BrokerSystemRecvDataException(Throwable t)
    {
        super(t);
    }
}
*/

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public static class AbnormalEOFException extends BrokerException
{
    public AbnormalEOFException()
    {
        /* "AppServer returned AbnormalEOF." */
        //super(COMM_ERROR, jcMSG097, null);
    super(BrokerException.ABNORMAL_EOF, "AppServer returned AbnormalEOF.");
    }
}

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public static class NoAvailableServersException extends BrokerException
{
    public NoAvailableServersException()
    {
    super(BrokerException.NO_AVAILABLE_SERVERS,
      "No servers available to process request");
    }
}

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public String getCapability(short key)
{
    String rval = null;
    try
    {
        Short name = new Short(key);
        rval = (String) m_capabilities.get((Object)name);
    }
    catch (Exception e)
    {
        log.logError( "BrokerSystem.getCapability() lookup failed: " + e.getMessage());
    }
    return rval;
}

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public boolean getCompressionEnabled()
{
    return m_compressionCapabilities != IPoolProps.COMPRESSION_DISABLED;
}

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int getCompressionThreshold()
{
    return m_compressionThreshold;
}

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int getCompressionCapabilities()
{
    return m_compressionCapabilities;
}

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int getCompressionLevel()
{
    return m_compressionLevel;
}

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private void rqInfoToProperties(TlvProps[] tlvProps,
                                IPoolProps props,
                                ubRqInfo rqInfo)
{
    int    i;
    int    n;

    String propName;
    short  tlvType;
    String val;
    ClientPrincipal cp = null;

    for (i = 0, n = tlvProps.length; i < n; i++)
    {
        propName = tlvProps[i].getPropertyName();
        tlvType  = tlvProps[i].getTlvType();
        
        if (rqInfo.exists(tlvType))
        {
        val = rqInfo.getStringField(tlvType);

        switch(tlvType)
        {
        case ubMsg.TLVTYPE_CLIENTPRINCIPAL:
            {
            if (val != null)
                {
                byte[] cpbytes;

                cpbytes = Base64.decode(val);

                if (log.ifLogExtended(m_debugLogEntries,m_debugLogIndex))
                    log.logDump(IAppLogger.LOGGING_EXTENDED,
                                m_debugLogIndex,
                                "rcvd clientPrincipal["+cpbytes.length+"]",
                                cpbytes, cpbytes.length);
                try
                    {
                    if (cpbytes.length > 0)
                        cp = new ClientPrincipal(cpbytes);
                    }
                catch (ClientPrincipalException e)
                    {
                    if (log.ifLogExtended(m_debugLogEntries,m_debugLogIndex))
                        log.logExtended(m_debugLogIndex,
                                        "rqInfoToProperties : ClientPrincipal Exception: " + 
                      			           e + " : " + e.getMessage());
                    cp = null;
                    }
                }

            props.setLocalProperty(propName, cp);
            break;
            }
        default:
            props.setLocalProperty(propName, val);
            break;
        }
        }
    }
}

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private void propertiesToRqInfo(TlvProps[] tlvProps,
                                IPoolProps props,
                                ubRqInfo rqInfo)
{
    int      i;
    int      n;

    String   propName;
    short    tlvType;
    String   val;

    for (i = 0, n = tlvProps.length; i < n; i++)
    {
        propName = tlvProps[i].getPropertyName();
        tlvType  = tlvProps[i].getTlvType();

        val      = props.getStringProperty(propName);

        /*
        if (val == null)
            val = "";
        */

        if (val == null)
            rqInfo.setField((int)tlvType, null);
        else rqInfo.setStringField((int)tlvType, val);
    }
}

/*CR: OE00215955*/
/*Appserver Message String Data field can hold maximum 32767 bytes.*/
/*Number 30000 chosen to be in close approximation to  32767.*/

private void validateConnectionInfo(
        String username,
        String password,
        String clientInfo) throws NetworkProtocolException
{
    int conn_info_length  = 0;

    if (username != null)
        conn_info_length = username.length();
    if (password != null)
        conn_info_length += password.length();
    if (clientInfo != null)
        conn_info_length += clientInfo.length();

    if ( conn_info_length > conn_info_maxlength )
    {
        log.logError("NetworkProtocolException: " + 
                     "Connect message size too long: exceeds " + conn_info_maxlength + " bytes.");
        throw new NetworkProtocolException("AppServer", 
                                           "Connection string length " + conn_info_length + 
                                           " exceeds " + conn_info_maxlength + " bytes.");
    }
}

/*********************************************************************/
/* TlvProps - private inner class                                    */
/*********************************************************************/

private static class TlvProps
{
    private    String                  m_propertyName;
    private    short                   m_tlvType;

    private TlvProps(String propertyName, short tlvType)
    {
        m_propertyName    = propertyName;
        m_tlvType         = tlvType;
    }

    private String getPropertyName()
    {
        return m_propertyName;
    }

    private short  getTlvType()
    {
        return m_tlvType;
    }

}   /* end TlvProps */


}   /* end BrokerSystem */


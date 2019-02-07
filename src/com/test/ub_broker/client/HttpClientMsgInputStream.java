/*
/* <p>Copyright 2000-2004 Progress Software Corportation, All rights reserved.</p>
/* <br>
/* <p>Class:        HttpClientMsgInputStream    </p>
/* <br>
/* All rights reserved.  No part of this program or document
/* may be  reproduced in  any form  or by  any means without
/* permission  in  writing  from  Data Language Corporation.
/*
*/

package com.progress.ubroker.client;

import java.io.EOFException;
import java.io.IOException;
import java.util.Properties;

import com.progress.common.ehnlog.IAppLogger;
import com.progress.common.ehnlog.O4glLogContext;
import com.progress.common.ehnlog.RestLogContext;
import com.progress.common.ehnlog.UBrokerLogContext;
import com.progress.common.ehnlog.WsaLogContext;
import com.progress.message.jcMsg;
import com.progress.ubroker.util.IubMsgInputStream;
import com.progress.ubroker.util.Logger;
import com.progress.ubroker.util.MessageCompressor;
import com.progress.ubroker.util.NetworkProtocolException;
import com.progress.ubroker.util.ubAppServerMsg;
import com.progress.ubroker.util.ubConstants;
import com.progress.ubroker.util.ubMsg;
import com.progress.ubroker.util.ubWebSpeedMsg;
import com.progress.ubroker.util.ubMsg.MsgFormatException;
import com.progress.open4gl.dynamicapi.IPoolProps;

/**
 * <p>
 * The HttpClientMsgInputStream class handles all of the MsgInputStream related
 * operations for tunneling the AppServer protocol through the HTTP protocol.
 * </p>
 * The controlling HTTP protocol handler class is HttpClientProtocol.  It provides
 * this class with the basis for all logging and tracing facilities and the
 * linkage to the actual HTTP network read/write operations.
 * <p>
 * The object extends the ByteArrayInputStream so that the calling object may
 * treat this like a buffered InputStream like object.
 * </p>
 *
 */
public class HttpClientMsgInputStream
    implements ubConstants, IubMsgInputStream
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
    private             HttpClientProtocol  m_parentHandler = null;
    private             IAppLogger          m_log = null;
    private             int                 m_logDest = IAppLogger.DEST_NONE;
    private             int                 m_streamTraceLevel = IAppLogger.LOGGING_EXTENDED;
    private             boolean             m_inMessageStream = false;
    // private             MsgInputStream      m_appServerMsgReader = null;
    // private             ByteArrayInputStream m_bais = null;
    private             HttpClientProtocol.UBMessageResponse m_httpResponse = null;
    private             int                 m_serverType = SERVERTYPE_APPSERVER;
    private             boolean             m_emulateAbnormalEOF = false;

    private             long                m_debugLogEntries;
    private             int                 m_debugLogIndex;
    private             long                m_cmprsLogEntries;
    private             int                m_cmprsLogIndex;

    private             boolean             m_compressionEnabled = false;

    /*
     * Constructors...
     */

    /**
    * <!-- HttpClientMsgInputStream() -->
    * <p>The default class constructor.
    * </p>
    * <br>
    * @param parent is a HttpClientProtocl object that is the protocol handler
    * that is associated with this MsgInputStream.
    * <br>
    */
    public HttpClientMsgInputStream(HttpClientProtocol parent)
    {
        if (null == parent)
        {
            throw new NullPointerException("Null protocol handler");
        }

        m_parentHandler = parent;
        m_log = m_parentHandler.loggingObject();
        m_logDest = m_parentHandler.loggingDestination();

        /* initialize log settings based on interface */
        initializeLogging(m_log);

        m_compressionEnabled = compressionEnabled(parent);
    }

    /*
     * Final cleanup.
     */
    protected void finalize() throws Throwable
    {
        close();
    }

    /*
     * ACCESSOR METHODS:
     */

    /**
     * <!-- setMsgBufferSize() -->
     * <p>The setMsgBufferSize allow the object's caller to override the
     * MsgInputStream's buffer sizing and set it to a specific size.  This
     * method is not supported in this class.
     * </p>
     * <br>
     * @param newBufferSize is an int value, greater than zero, that indicates
     * the new buffer size.  If the internal buffer is already that size or
     * larger, a resizing operation is not performed.  If a resizing opertion
     * is performed, a data copy operation is performed in order to not loose
     * data.
     * <br>
     * @return  void
     * <br>
     * @exception   Exception
     */
    public void         setMsgBufferSize(int newBufferSize) throws Exception
    {
    }

    /**
     * <!-- getMsgBufferSize() -->
     * <p>The getMsgBufferSize returns the size of the MsgInputStream's internal
     * buffer.
     * </p>
     * <br>
     * @return  int
     */
    public int          getMsgBufferSize()
    {
        return(available());
    }

    /**
     * <!-- setLoggingTraceLevel() -->
     * <p>The setLoggingTraceLevel sets the specific MsgInputStream object's
     * Progress tracing level if it different from the parent Protocol's
     * tracing level that the MsgInputStream class inherits.
     * </p>
     * <br>
     * @param newTraceLevel is an int value (1 - 6) that indicates the new
     * log tracing level for this object.
     * <br>
     * @return  void
     * <br>
     * @exception   Exception
     */
    public void         setLoggingTraceLevel(int newTraceLevel) throws Exception
    {
        m_streamTraceLevel = newTraceLevel;
    }

    /**
     * <!-- getLoggingTraceLevel() -->
     * <p>This method will return the current trace level used for log output.
     * </p>
     * <br>
     * @return  int
     */
    public int          getLoggingTraceLevel()
    {
        return(m_streamTraceLevel);
    }

    /*
     * PUBLIC METHODS:
     */
    /**
     * <!-- readMsg() -->
     * <p>The readMsg method obtains a complete AppServer protocol message from
     * the network.
     * </p>
     * <br>
     * @return  ubMsg
     * <br>
     * @exception   IOException
     * @exception   NetworkProtocolException
     * @exception   ubMsg.MsgFormatException
     */
    public ubMsg        readMsg()
        throws IOException , ubMsg.MsgFormatException, NetworkProtocolException
    {
        ubAppServerMsg  returnValue = null;
        boolean readAgain = true;
        int     ubMessageType = 0;

        // See if we need to get the next http message because the input buffer
        // is empty from the last one.
        /*
        m_log.LogMsgln(m_logDest,
               Logger.LOGGING_DEBUG,
               Logger.TIMESTAMP,
               "readMsg: byte input stream available: " +
               ((null == m_bais) ? "no" : "yes" ) + " ; available: " +
               ((null == m_bais) ? "0" : Integer.toString(m_bais.available())));
        */

        /*  20041109-012
            We have to check if the message we get is a UBRQ_SEND_EMPTY_MSG message,
            which contains no real data (it was added to fix 20020816-010).
            It was added to place an http message on the wire to force the WebServer to send
            a message immediately to the client.

            We will loop here until there is real data to be sent (in which case readAgain
            will be set to false. If there is no real message other than the UBRQ_SEND_EMPTY_MSG,
            we will return a null message, which should be ok, since if there was no message
            at all, we would've cause an exception when calling readNextHttpMessage anyway.
        */

        for (;readAgain;)
        {

        // Need to "prime the input stream pump" ?
        //
        if (null == m_httpResponse ||
            0 == m_httpResponse.m_httpInputStream.available())
        {
/*
            m_log.LogMsgln(m_logDest,
               Logger.LOGGING_TRACE,
               Logger.TIMESTAMP,
               "readMsg calling readNextHttpMessage().");
*/
            readNextHttpMessage();
        }
        try
        {
            // Read the ubroker message from the http input stream
            //
            returnValue = (ubAppServerMsg) readUBMsg();


            // If this is a appserver message that is part of a data message
            // stream (for reading HUGE data sets), then set the streaming
            // flag so that we don't close down the input stream too quickly.
            //
            ubMessageType = returnValue.getubRq();

            if (m_log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
            {
                String msgType = "unknown";
                int    msgCode = returnValue.getMsgcode();
                for (int i = 0; i < ubAppServerMsg.CSMSSG_MSGCODES.length; i++)
                {
                    if (msgCode ==  ubAppServerMsg.CSMSSG_MSGCODES[i])
                    {
                        msgType = ubAppServerMsg.DESC_MSGCODE[i];
                        break;
                    }
                }

                m_log.logBasic(  m_debugLogIndex,
                               "Read ubAppServerMsg, apMsg: " +
                               msgType +
                               " ; ubMsg: " +
                               returnValue.getubRqDesc());
            }

            /* if we got a UBRQ_SEND_EMPTY_MSG message, try to see if there something else
               to be read.
            */
            if (ubMsg.UBRQ_SEND_EMPTY_MSG == ubMessageType)
            {
                if (m_log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                    m_log.logBasic(m_debugLogIndex,
                                "Skipping [UBRQ_SEND_EMPTY_MSG] message");

                /* get rid of this stream */
                m_httpResponse.m_httpInputStream.close();
                m_httpResponse.m_httpInputStream = null;
                m_httpResponse.m_ubMsgResponse = null;
                m_httpResponse = null;

                /* check we if have something else to on the pipe */
                if (m_parentHandler.available() > 0)
                    continue;

                if (m_log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                    m_log.logBasic(m_debugLogIndex,
                                "Nothing else on the pipe after [UBRQ_SEND_EMPTY_MSG] message");
            }

            /* there is real data to be read (or there is nothing else to be read), 
               so unset readAgain
            */
            readAgain=false;

            if (ubMsg.UBRQ_RSPDATA == ubMessageType)
            {
                m_inMessageStream = true;
            }

            // Next we have to do some response code analysis.
            // If the ubRsp code is not UBRSP_OK, then the AIA has dropped the
            // connection.  We want to tell the parent to stop operations at
            // this time.
            if (ubMsg.UBRSP_ERROR == returnValue.getubRsp())
            {
            if (m_log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                m_log.logBasic(  m_debugLogIndex,
                               "Error detected in ubMsg respsonse, closing " +
                               m_parentHandler.protocolName() + " connection.");

                // Close the physical link to match what the AIA does.
                m_parentHandler.closeHttpConnection(null, m_parentHandler);

                String ubErrMsg = "N/A";
                try
                {
                    ubErrMsg = returnValue.get4GLErrMsg();
                }
                catch (Exception e)
                {
                }
                NetworkProtocolException npe = new NetworkProtocolException(NetworkProtocolException.AIA_REPORTED_ERROR,
                                                   m_parentHandler.protocolName(),
                                                   ubErrMsg);
                m_log.logStackTrace("",
                                    npe);
                throw npe;
            }

        }
        catch (IOException e3)
        {
        if (m_log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
            m_log.logBasic(  m_debugLogIndex,
                           "IOException reading AppServer message: " +
                           e3.getMessage());
            throw e3;
        }
        catch (ubMsg.MsgFormatException e4)
        {
        if (m_log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
            m_log.logBasic(  m_debugLogIndex,
                           "Invalid AppServer message format: " +
                           e4.getMessage());
            throw e4;
        }
        catch (Exception e5)
        {
            e5.printStackTrace();

            // A stray exception must have gotten past the mesage reader code's
            // handlers.  Catch and report it here before it can propogate and
            // wreak havok.
            //
            NetworkProtocolException npe = new NetworkProtocolException(NetworkProtocolException.BASE_MSG,
                                                                        m_parentHandler.protocolName(),
                                                                        "Reading response: " +
                                                                        e5.toString());
            m_log.logStackTrace("",
                                npe);
            throw npe;
        }

        } // for loop

        /* if all we got was a UBRQ_SEND_EMPTY_MSG, send nothing */
        if (ubMsg.UBRQ_SEND_EMPTY_MSG == ubMessageType)
            returnValue = null;

        return(returnValue);
    }

    /**
     * <!-- available() -->
     * <p>The available method indicates whether any AppServer protocol message
     * data is available for reading.
     * </p>
     * <br>
     * @return  int
     */
    public int          available()
    {
        int     returnValue = 0;

        try
        {
            if (null == m_httpResponse)
            {
                // Have to see if a message is in the input queue waiting to be
                // read.
                returnValue = m_parentHandler.available();
            }
            else
            {
                returnValue = m_httpResponse.m_httpInputStream.available();
                if (0 == returnValue)
                {
                    // Try any unread information.
                    returnValue = m_parentHandler.available();
                }
            }
        }
        catch (Exception e)
        {
        }

        return(returnValue);
    }

    /**
     * <!-- close() -->
     * <p>The close method indicates that the MsgInputStream is no longer needed
     * as the connection to the AppServer is being broken.
     * </p>
     * <br>
     * @return  void
     * <br>
     * @exception   IOException
     */
    public void         close() throws IOException
    {
        if (m_log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
            m_log.logBasic(  m_debugLogIndex,
                           "Closing HttpClientMsgInputStream.");

        // Close any outstanding http response streams.
        //
        if (null != m_httpResponse)
        {
            m_httpResponse.m_httpInputStream.close();
            m_httpResponse.m_httpInputStream = null;
            m_httpResponse.m_ubMsgResponse = null;
            m_httpResponse = null;
        }

        // Call our parent handler to relase control.
        //
        m_parentHandler.releaseMsgInputStream();
    }

    /**
     * <!-- setStop() -->
     * <p>This is called by the parent protocol handler to emulate the Appserver
     * stopping a response data stream.  The parent handler has taken care of
     * sending the message to the AIA, but the data may all be returned and in
     * the input stream's ByteArrayInputStream which may still be read by the
     * application.  The parent handler will only call this when it is IDLE, i.e.
     * the data has been fully returned by the AIA.
     * </p>
     */
    public void setStop()
    {
        // Detect that we are in a stream opeation.  If we are not, we don't
        // have to do anything.
        //
        try
        {
            if (0 < m_httpResponse.m_httpInputStream.available())
            {
                m_emulateAbnormalEOF = true;
            }
        }
        catch (Exception e)
        {
        if (m_log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
            m_log.logBasic(m_debugLogIndex,
                         "setStop() encountered an exception: " + e.toString());
        }
    }

    /*
     * PROTECTED (SUPER) METHODS:
     */

    /**
     * <!-- readNextHttpMessage() -->
     * <p>Get the next HTTP message body from the parent.  Construct a new byte
     * array with the raw data bytes and then assign an input stream to it.
     * </p>
     * <br>
     * @exception   IOException
     * @exception   ubMsg.MsgFormatException
     * @exception   NetworkProtocolException
     */
    protected void readNextHttpMessage()
        throws IOException , ubMsg.MsgFormatException, NetworkProtocolException
    {
        // Close the stream to release resources in preparation for reading
        // the next message.
        //
        if (null != m_httpResponse)
        {
            m_httpResponse.m_httpInputStream.close();
            m_httpResponse.m_httpInputStream = null;
            m_httpResponse.m_ubMsgResponse = null;
            m_httpResponse = null;
        }

        try
        {
            if (m_log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                m_log.logBasic(  m_debugLogIndex,
                               "Getting the next HTTP response message");

            // Get the byte stream that should hold the returned
            // AppServer (ubAppServerMsg) data.
            //
            m_httpResponse = m_parentHandler.getUbResponseMessage();

        }
        catch (EOFException eofe)
        {
            // pass along.
            throw eofe;
        }
        catch (NetworkProtocolException e1)
        {
            // Release resources gracefully before throwing the exception...
            //
            close();
            throw e1;
        }
        catch (Exception e2)
        {
            // A stray exception must have gotten past the handler's
            // handlers.  Catch and report it here before it can propogate and
            // wreak havok.
            //
            NetworkProtocolException npe = new NetworkProtocolException(NetworkProtocolException.BASE_MSG,
                                                                        m_parentHandler.protocolName(),
                                                                        "Getting response message: " +
                                                                        e2.toString());
            m_log.logStackTrace("",
                                npe);

            // Release resources gracefully before throwing the exception...
            //
            close();

            throw npe;
        }

        /*
        if (0 == m_httpInputStream.available())
        {
            // The HttpClientProtocol shouldn't hand us back a blank
            // stream without throwing an exception, but do a double check
            // to not allow anything to fall through.
            //
            NetworkProtocolException npe = new NetworkProtocolException(NetworkProtocolException.BASE_MSG,
                                                                        m_parentHandler.protocolName(),
                                                                        "Empty response stream was detected" );
            m_log.logStackTrace("",
                                npe);
            throw npe;
        }
        */

        String  lengthHeader = null;
        try
        {
            lengthHeader = m_httpResponse.m_ubMsgResponse.getHeader("Content-Length");
        }
        catch (Exception e)
        {
        }

        if (null == lengthHeader)
        {
            lengthHeader = "unknown";
        }
        if (m_log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
            m_log.logBasic(  m_debugLogIndex,
                           "HTTP Input stream now has : " + lengthHeader +
                           " bytes available for reading.");
    }

    /*
     * PRIVATE METHODS:
     */
    /*********************************************************************/
    /*                                                                   */
    /*********************************************************************/

    private synchronized ubMsg readUBMsg()
        throws
            IOException,
            ubMsg.MsgFormatException,
            NetworkProtocolException
    {
        ubMsg msg = null;
        byte msgcode;
        byte[] ubhdr;
        byte[] tlvbuf;
        byte[] srvhdr;
        int buflen;
        int msgtype;


        if (m_log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
            m_log.logBasic(m_debugLogIndex,
                         "readUBMsg()");

        ubhdr = readubhdr();

        tlvbuf = readtlvbuf(ubhdr);

        msgtype = ubMsg.getubType(ubhdr);

        switch ( msgtype )
        {
            case ubMsg.UBTYPE_APPSERVER          :
                /* data servers now using AppServer message format */
                if ( (m_serverType != SERVERTYPE_APPSERVER)      &&
                     (m_serverType != SERVERTYPE_ADAPTER)        &&
                     (m_serverType != SERVERTYPE_ADAPTER_CC)     &&
                     (m_serverType != SERVERTYPE_ADAPTER_SC)     &&
                     (m_serverType != SERVERTYPE_DATASERVER_OD)  &&
                     (m_serverType != SERVERTYPE_DATASERVER_OR) )
                {
                    /* Got a message of incorrect type (%s<msgType>)  */
                    /* for this m_serverType (%s<serverType>)           */

                    m_log.logError(jcMsg.jcMSG101,    /*jbMsg.jbMSG078*/
                                  new Object[] {
                                    ubMsg.DESC_UBTYPE[msgtype],
                                    STRING_SERVER_TYPES[m_serverType]
                                     }
                                );

                    /* we got an message from a the wrong kind of client */
                    throw new ubMsg.WrongServerTypeException("ServerType=(" +
                                             ubMsg.getubType(ubhdr) +
                                             ") not supported for this broker");
                }

                srvhdr = readsrvhdr(ubAppServerMsg.getSrvHdrlen());
                msg = new ubAppServerMsg(ubhdr, tlvbuf, srvhdr);

                buflen = ((ubAppServerMsg)msg).getMsglen()
                              - ubAppServerMsg.CSMSSGHDRLEN;

                break;

            case ubMsg.UBTYPE_WEBSPEED           :
                if (m_serverType != SERVERTYPE_WEBSPEED)
                {
                    /* Got a message of incorrect type (%s<msgType>)  */
                    /* for this m_serverType (%s<serverType>)           */

                    m_log.logError(jcMsg.jcMSG101, /* jbMsg.jbMSG078 */
                                  new Object[] {
                                    ubMsg.DESC_UBTYPE[msgtype],
                                    STRING_SERVER_TYPES[m_serverType]
                                     }
                                );

                    /* we got an message from a the wrong kind of client */
                    throw new ubMsg.WrongServerTypeException("ServerType=(" +
                                             ubMsg.getubType(ubhdr) +
                                             ") not supported for this broker");
                }

                srvhdr = readsrvhdr(ubWebSpeedMsg.getSrvHdrlen());
                msg = new ubWebSpeedMsg(ubhdr, tlvbuf, srvhdr);

                /* size in webspeed header is the same as buflen */
                buflen = ((ubWebSpeedMsg)msg).getwsMsglen();
                break;

            case ubMsg.UBTYPE_ADMIN              :
            case ubMsg.UBTYPE_NAMESERVER         :
            default:
                throw new ubMsg.InvalidServerTypeException("ServerType=(" +
                                                     ubMsg.getubType(ubhdr) +
                                                     ") not supported");
        }

        if (buflen > 0)
        {
            buflen = readMsgbuf(msg, buflen);
        }

        // Look for the emulate abnormal EOF flag.  If it is set, then we
        // force a UBRSP_ABNORMAL_EOF response code to stop the caller from
        // processing any more data because it was stopped.  We have to do
        // this emulation because of the buffering performed by reading in
        // the response data all at one time and then allowing the read operation
        // to proceed at its own pace.
        //
        if (m_emulateAbnormalEOF)
        {
            m_emulateAbnormalEOF = false;
            if (ubMsg.UBRQ_RSPDATA == msg.getubRq())
            {
                msg.setubRsp(ubMsg.UBRSP_ABNORMAL_EOF);
/*                if (!m_log.ignore(Logger.LOGGING_TRACE))
                {
                    m_log.LogMsgln(m_logDest,
                                   m_streamTraceLevel,
                                   Logger.NOTIMESTAMP,
                                   "readUBMsg() is emulating AbnormalEOF");
                }
*/
                // empty the buffer, the rest of the data is meaningless.
                //
                m_httpResponse.m_httpInputStream.skip(
                     m_httpResponse.m_httpInputStream.available());
            }
        }
        return msg;
    }

    /*********************************************************************/
    /* Private methods                                                   */
    /*********************************************************************/

    /*********************************************************************/
    /*                                                                   */
    /*********************************************************************/

    private byte[] readubhdr()
        throws
            IOException
           , ubMsg.InvalidMsgVersionException
           , ubMsg.InvalidHeaderLenException
           , ubMsg.MsgFormatException
           , NetworkProtocolException
    {
        byte[] ubhdr = new byte[ubMsg.UBHDRLEN];

        readstream(ubhdr, 0, 2);

        /* check length and ubVer */
        ubMsg.checkubVer(ubhdr);

        readstream(ubhdr, 2, ubhdr.length-2);

        if (m_log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
            m_log.logDump(IAppLogger.LOGGING_BASIC,
                          m_debugLogIndex,
                          "readubhdr",
                          ubhdr,
                          ubhdr.length);
        return ubhdr;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

    private byte[] readtlvbuf(byte[] ubhdr)
        throws
            IOException
           ,NetworkProtocolException
    {
        int    ubver;
        byte[] tlvbuf;
        int    tlvlen;

        try
            {
            tlvlen = ubMsg.getubTlvBuflen(ubhdr);
            tlvbuf = new byte[tlvlen];

            if (tlvlen > 0)
                {
                readstream(tlvbuf, 0, tlvlen);

            if (m_log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                    m_log.logDump(IAppLogger.LOGGING_BASIC,
                                  m_debugLogIndex,
                                  "readtlvbuf",
                                  tlvbuf,
                                  tlvlen);
                }
            }
        catch(ubMsg.MsgFormatException e)
            {
            /* this is a v0 msg */
            tlvbuf = null;
            }

        return tlvbuf;
    }

    /*********************************************************************/
    /*                                                                   */
    /*********************************************************************/

    private void readsrvhdr(ubMsg msg)
        throws 
            IOException
           ,ubMsg.MsgFormatException
           ,NetworkProtocolException
    {
        readstream(msg.getSrvHeader(), 0, msg.getSrvHeaderlen());

        if (m_log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
            m_log.logDump(IAppLogger.LOGGING_BASIC,
                          m_debugLogIndex,
                          "readsrvhdr",
                          msg.getSrvHeader(),
                          msg.getSrvHeaderlen());
    }

    /*********************************************************************/
    /*                                                                   */
    /*********************************************************************/

    private byte[] readsrvhdr(int srvhdrlen)
    throws IOException
           ,ubMsg.MsgFormatException
           , NetworkProtocolException
    {
        byte[] srvhdr = new byte[srvhdrlen];
        readstream(srvhdr, 0, srvhdrlen);

        if (m_log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
            m_log.logDump(IAppLogger.LOGGING_BASIC,
                          m_debugLogIndex,
                          "readsrvhdr",
                          srvhdr,
                          srvhdrlen);

        return srvhdr;
    }

    /*********************************************************************/
    /*                                                                   */
    /*********************************************************************/

    private int readMsgbuf(ubMsg msg, int buflen)
    throws IOException
           ,ubMsg.MsgFormatException
           , NetworkProtocolException
    {
        byte[] tmpbuf;

        tmpbuf = new byte[buflen];
        
        byte[] uncompressedBuf = null;
        int uncompressedLength = buflen;

        readstream(tmpbuf, 0, buflen);

        if (m_log.ifLogVerbose(m_debugLogEntries,m_debugLogIndex))
        {
           m_log.logDump( IAppLogger.LOGGING_BASIC,
                          m_debugLogIndex,
                         "readMsgbuf[" + buflen + "]",
                          tmpbuf,
                          buflen);
        }
        else if (m_log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
        {
            m_log.logDump(IAppLogger.LOGGING_BASIC,
                          m_debugLogIndex,
                          "readMsgbuf[" + buflen + "]",
                          tmpbuf,
                          (buflen > 64) ? 64 : buflen);
        }

        String uncompressedLengthch = null;
        
        try
        {
            if (m_compressionEnabled)
                uncompressedLengthch = msg.getTlvField_NoThrow(ubMsg.TLVTYPE_UNCMPRLEN);
        }
        catch (Exception e)
        {
            // ignore this since it is expected with AIA when it has a connection problem
        }        

        if (uncompressedLengthch != null)
        {
            try
            {
                uncompressedLength = Integer.parseInt(uncompressedLengthch);
            }
            catch (Exception e)
            {
                m_log.logStackTrace(m_cmprsLogIndex, "COMPRESSION: Exception reading uncompressed length", e);
                throw new MsgFormatException(e.getMessage());
            }

            try
            {
                uncompressedBuf = MessageCompressor.unCompressBytes(tmpbuf, 0, tmpbuf.length);
            }
            catch (Exception e)
            {
                CompressionException ce = new CompressionException("COMPRESSION: Fatal ZLIB compression error occurred");
                m_log.logStackTrace(m_cmprsLogIndex, e.getMessage(), e);
                throw ce;
            }

            if (uncompressedBuf.length != uncompressedLength)
            {
            	if (m_log.ifLogVerbose(m_cmprsLogEntries, m_cmprsLogIndex)) {
            		m_log.logVerbose(m_cmprsLogIndex, "VIOLATION: Decompression at target resolved " + uncompressedBuf.length + " of " + uncompressedLength + " source bytes");
            	}
                uncompressedBuf = null;
            }
        }

        if (uncompressedBuf != null)
        {
        	if (m_log.ifLogVerbose(m_cmprsLogEntries, m_cmprsLogIndex)) {
        		m_log.logVerbose(m_cmprsLogIndex, "COMPRESSION: received compressed message length = " + buflen + "; uncompressed length = " + uncompressedLength);
        	}
            msg.setMsgbuf(uncompressedBuf, uncompressedLength);
        }
        else
        {
            msg.setMsgbuf(tmpbuf, buflen);
        }
		
        return buflen;
    }

    /*********************************************************************/
    /*                                                                   */
    /*********************************************************************/

    private void readstream(byte[] msgbuf, int ofst, int len)
    throws IOException
           , ubMsg.MsgFormatException
           , NetworkProtocolException
    {
        int need, got;

        for (need = len, got = 0; need > 0; need -= got)
        {
            try
            {
                /*
                m_log.LogMsgln(m_logDest,
                Logger.LOGGING_DEBUG,
                Logger.NOTIMESTAMP,
                "before read: inputstream variables : " +
                "need= " + need + " pos= " + pos +
                " count= " + count +
                " marklimit= " + marklimit + " markpos= " + markpos);
                */


                got = m_httpResponse.m_httpInputStream.read(msgbuf, ofst + (len - need), need);
/*
                if (!m_log.ignore(Logger.LOGGING_TRACE))
                {
                    m_log.LogMsgln(m_logDest,
                    Logger.LOGGING_TRACE,
                    Logger.NOTIMESTAMP,
                    "HTTP input stream has " + m_httpResponse.m_httpInputStream.available() +
                    " bytes remaining.");
                }
*/
                /*
                m_log.LogMsgln(m_logDest,
                Logger.LOGGING_DEBUG,
                Logger.NOTIMESTAMP,
                "after read: inputstream variables : " +
                "got= " + got + " pos= " + pos + " count= " + count +
                " marklimit= " + marklimit + " markpos= " + markpos);
                */
            }
            catch (IOException ioe)
            {
            if (m_log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                {
                    m_log.logBasic(m_debugLogIndex,
                                   "read() IOException in readstream : " +
                                   ioe.getMessage() +
                                   " : got= " + got + " need= " + need );
                }

                throw ioe;
            }
            if (got == -1)
            {
                // See if we have more data in the HTTP message response silo.
                // It will throw an EOFException if no data is available.
/*
                m_log.LogMsgln(m_logDest,
                   Logger.LOGGING_TRACE,
                   Logger.TIMESTAMP,
                   "readstream() detected premature EOF");
*/

                // For right now, do not automatically get the next response
                // since we don't split message across multiple response
                // messages.
                //
                // readNextHttpMessage();
            }
        }
    }

    /*********************************************************************/
    /*                                                                   */
    /*********************************************************************/

    private void initializeLogging(IAppLogger log)
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
            m_cmprsLogEntries    = O4glLogContext.SUB_M_COMPRESSION;
            m_cmprsLogIndex      = O4glLogContext.SUB_V_COMPRESSION;
            }
        else if (contextName.equals("UBroker"))
            {
            m_debugLogEntries    = UBrokerLogContext.SUB_M_UB_DEBUG;
            m_debugLogIndex      = UBrokerLogContext.SUB_V_UB_DEBUG;
            m_cmprsLogEntries    = UBrokerLogContext.SUB_M_UB_COMPRESSION;
            m_cmprsLogIndex      = UBrokerLogContext.SUB_V_UB_COMPRESSION;
            }
        else
            {
            m_debugLogEntries    = 0;
            m_debugLogIndex      = 0;
            m_cmprsLogEntries    = 0;
            m_cmprsLogIndex      = 0;
            }
    }

    /*********************************************************************/
    /*                                                                   */
    /*********************************************************************/

    private boolean compressionEnabled(HttpClientProtocol parentProtocol)
    {
        boolean ret = false;
        String  val = null;
        Properties props;

        try
        {
            props = parentProtocol.getProtocolProperties();
        }
        catch (Exception e)
        {
            props = null;
        }

        val = (props == null) ? 
             null : (String)props.getProperty(IPoolProps.ENABLE_COMPRESSION);

        ret = (val != null) && (val.equals("1"));

        return ret;
    }

    /*********************************************************************/
    /*                                                                   */
    /*********************************************************************/

}


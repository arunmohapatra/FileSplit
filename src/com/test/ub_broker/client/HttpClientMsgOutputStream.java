/*
/* <p>Copyright 2000-2001 Progress Software Corportation, All rights reserved.</p>
/* <br>
/* <p>Class:        HttpClientMsgOutputStream   </p>
/* <br>
/* All rights reserved.  No part of this program or document
/* may be  reproduced in  any form  or by  any means without
/* permission  in  writing  from  Data Language Corporation.
/*
*/

package com.progress.ubroker.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.progress.common.ehnlog.IAppLogger;
import com.progress.common.ehnlog.O4glLogContext;
import com.progress.common.ehnlog.RestLogContext;
import com.progress.common.ehnlog.WsaLogContext;
import com.progress.common.ehnlog.UBrokerLogContext;
import com.progress.ubroker.util.IubMsgOutputStream;
import com.progress.ubroker.util.NetworkProtocolException;
import com.progress.ubroker.util.ubAppServerMsg;
import com.progress.ubroker.util.ubMsg;

/**
 * <p>
 * The HttpClientMsgOutputStream class handles all of the MsgOutputStream related
 * operations for tunneling the AppServer protocol through the HTTP protocol.
 * </p>
 * The controlling HTTP protocol handler class is HttpClientProtocol.  It provides
 * this class with the basis for all logging and tracing facilities and the
 * linkage to the actual HTTP network read/write operations.
 * <p>
 * The object extends the ByteArrayOutputStream so that the calling object may
 * treat this like a buffered OutputStream like object.
 * </p>
 *
 */
public class HttpClientMsgOutputStream extends ByteArrayOutputStream
    implements IubMsgOutputStream
{
    /*
     * CLASS Constants
     * private static final <type>  <name> = <value>;
     */
    private static final int            INITIAL_SIZE = 65536;
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
    private             int                 m_msgBufferSize = INITIAL_SIZE;
    private             IAppLogger          m_log;
    private             int                 m_logDest = IAppLogger.DEST_NONE;
    private             int                 m_streamTraceLevel = IAppLogger.LOGGING_EXTENDED;
    private             int                 m_lastUbMsgRequestCode = 0;

    private             long                m_debugLogEntries;
    private             int                 m_debugLogIndex;

    /*
     * Constructors...
     */

    /**
    * <!-- HttpClientMsgOutputStream() -->
    * <p>The default class constructor.
    * </p>
    * <br>
    * @param parent is the HttpClientProtocol object that handles the core
    * HTTP protocol operations.
    */
    public HttpClientMsgOutputStream(HttpClientProtocol parent)
    {
        super(INITIAL_SIZE);

        if (null == parent)
        {
            throw new NullPointerException("Null parent protocol handler");
        }

        m_parentHandler = parent;
        m_log = m_parentHandler.loggingObject();
        m_logDest = m_parentHandler.loggingDestination();

        /* initialize log settings based on interface */
        initializeLogging(m_log);
    }

    /*
     * Final cleanup.
     */
    /*
    protected void finalize() throws Throwable
    {
    }
    */

    /*
     * ACCESSOR METHODS:
     */
    /**
     * <!-- lastUbMsgRequestCode() -->
     * <p>Get the type of the last ubroker message so that the caller can
     * do stateful operations.
     * </p>
     * <br>
     * @return  int (See ubMsg.UBRQ_XXXXXXX)
     */
    public  int lastUbMsgRequestCode()
    {
        return(m_lastUbMsgRequestCode);
    }

    /*
     * PUBLIC METHODS:
     */


    /**
     * <!-- flushMsg() -->
     * <p>The flush method insures that all of the AppServer protocol message
     * is transmitted to the network and not held up in some internal buffer
     * waiting to optomize the network transmission.
     * </p>
     * <br>
     * @return  void
     * <br>
     * @exception   IOException
     */
    public void         flushMsg() throws IOException, NetworkProtocolException
    {
        // Call the parent class flush first to clear any buffers and make
        // sure all the data is ready for transport.
        //
        super.flush();

        if (0 < this.count)
        {
            // Flush the http message the aia for processing.
            // Send the bytes to the parent protocol to handle the sending and
            // receiving of the message throught the HTTP protocol.
            //
            try
            {
                // We have to determine if this is a stop-server message.  If it
                // is, it requires special processing.
                //
                if (ubMsg.UBRQ_SETSTOP == m_lastUbMsgRequestCode)
                {
/*
                    m_log.LogMsgln(m_logDest,
                                   Logger.LOGGING_TRACE,
                                   Logger.NOTIMESTAMP,
                                   "Flushing stop message to Http handler.");
*/
                    m_parentHandler.sendStopMessage(this);
                    m_lastUbMsgRequestCode = 0;
                }
                else
                {
                    m_parentHandler.sendUbMessage(this);
                    m_lastUbMsgRequestCode = 0;
                }
            }
            catch (Exception e)
            {
                NetworkProtocolException    npe = new NetworkProtocolException(NetworkProtocolException.PROTOCOL_CONNECTION_FAILED,
                                                                               m_parentHandler.protocolName(),
                                                                               e.toString());
                m_log.logStackTrace("",
                                    npe);
                throw npe;
            }

        }

        // Empty the buffer for the next message to send.
        //
        this.reset();
    }

    /**
     * <!-- close() -->
     * <p>The close method indicates that the MsgOutputStream is not longer needed
     * as the connection to the AppServer is being broken.
     * </p>
     * <br>
     * @return  void
     * <br>
     * @exception   IOException
     */
    public void         close() throws IOException
    {
        // Just clean out the buffer pointer.
        if (m_log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
            m_log.logBasic(  m_debugLogIndex,
                             "Closing HttpClientMsgOutputStream.");


        // Call our parent handler to relase control.
        //
        m_parentHandler.releaseMsgOutputStream();

       // Close the parent class.
       super.close();
    }

    /**
     * <!-- setMsgBufferSize() -->
     * <p>The setMsgBufferSize allow the object's caller to override the
     * MsgOutputStream's buffer sizing and set it to a specific size.  This may
     * be used to pre-extend the MsgOutputStream buffer.
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
        if (newBufferSize > m_msgBufferSize)
        {
            // extend the buffer.  make sure we copy any old contents.
            //
            m_msgBufferSize = newBufferSize;
            byte[]  newbuff = new byte[m_msgBufferSize];
            if (null != newbuff)
            {
                throw new Exception("Cannot extend the stream buffer");
            }
            // See if we have to copy
            //
            if (0 < this.count)
            {
                System.arraycopy(this.buf, 0, newbuff, 0, (this.count - 1));
            }
            // now replace the old buffer.
            this.buf = newbuff;
        }

    }

    /**
     * <!-- getMsgBufferSize() -->
     * <p>The getMsgBufferSize returns the size of the MsgOutputStream's internal
     * buffer.
     * </p>
     * <br>
     * @return  int
     */
    public int          getMsgBufferSize()
    {
        return m_msgBufferSize;
    }

    public void         setLoggingTraceLevel(int newTraceLevel) throws Exception
    {
        m_streamTraceLevel = newTraceLevel;
    }

    /**
     * <!-- getLoggingTraceLevel() -->
     * <p>The getLoggingTraceLevel gets the specific MsgOutputStream object's
     * Progress tracing level 
     * </p>
     * <br>
     * @return  int
     */
    public int          getLoggingTraceLevel()
    {
        return(m_streamTraceLevel);
    }

    /**
     * <!-- writeMsg() -->
     * <p>The writeMsg method writes a complete AppServer protocol message via
     * the network protocol to the AppServer.
     * </p>
     * <br>
     * @param ubMsg is a ubMsg object that encapsulates the AppServer protocol
     * message to transmit.
     * <br>
     * @return  void
     * <br>
     * @exception   IOException
     * @exception   NetworkProtocolException
     */
    public void         writeMsg(ubMsg msg)
        throws IOException, NetworkProtocolException
    {
        int         totalSize = 0;

        // record the last ubroker message that was sent.
        //
        m_lastUbMsgRequestCode = msg.getubRq();

        // Do some nice logging to help track what is taking place.
        //
        if (m_log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
        {
            String      msgType = "unknown";
            int         msgCode = ((ubAppServerMsg)msg).getMsgcode();


            // ubAppServerMsg doesn't have a description lookup so we have to
            // do the work here.
            //
            for (int i = 0; i <  ubAppServerMsg.CSMSSG_MSGCODES.length; i++)
            {
                if (msgCode ==  ubAppServerMsg.CSMSSG_MSGCODES[i])
                {
                    msgType = ubAppServerMsg.DESC_MSGCODE[i];
                    break;
                }
            }

            m_log.logBasic(    m_debugLogIndex,
                               "Write ubAppServerMsg, apMsg: " +
                               msgType +
                               " ; ubMsg: " +
                               msg.getubRqDesc());
        }

        // Now append to the buffer the various message pieces parts.
        // We don't flush the output here to the HTTP message because we may
        // be streaming Huge amounts of data.  The caller (BrokerSystem) will
        // control when to do the flushing.
        //
        try
        {
            byte[] msgbuf = msg.serializeMsg();

            write(msgbuf);

            /* the format of this data doesn't match with any of the rest of */
            /* the msg tracing, but at least the data is captured            */

            if (m_log.ifLogBasic(m_debugLogEntries,m_debugLogIndex))
                {
                m_log.logDump(IAppLogger.LOGGING_BASIC,
                              m_debugLogIndex,
                              "writeMsg",
                              msgbuf,
                              msgbuf.length);
                }

            flushMsg();
        }
        catch (Exception e)
        {
            throw new IOException(e.getMessage());
        }

    }


    /*
     * PROTECTED (SUPER) METHODS:
     */

    /*
     * PRIVATE METHODS:
     */

    private void initializeLogging(IAppLogger log)
    {
        String contextName = log.getLogContext().getLogContextName();

        if (contextName.equals("Wsa"))
            {
            // do we want to split these into multiple bits???
            m_debugLogEntries    = WsaLogContext.SUB_M_UBROKER;
            m_debugLogIndex      = WsaLogContext.SUB_V_UBROKER;
            }
        else if (contextName.equals("Rest"))
        {
	        // do we want to split these into multiple bits???
	        m_debugLogEntries    = RestLogContext.SUB_M_UBROKER;
	        m_debugLogIndex      = RestLogContext.SUB_V_UBROKER;
        }
        else if (contextName.equals("O4gl"))
            {
            // do we want to split these into multiple bits???
            m_debugLogEntries    = O4glLogContext.SUB_M_UBROKER;
            m_debugLogIndex      = O4glLogContext.SUB_V_UBROKER;
            }
        else if (contextName.equals("UBroker"))
            {
            m_debugLogEntries    = UBrokerLogContext.SUB_M_UB_DEBUG;
            m_debugLogIndex      = UBrokerLogContext.SUB_V_UB_DEBUG;
            }
        else
            {
            m_debugLogEntries    = 0;
            m_debugLogIndex      = 0;
            }
    }


}

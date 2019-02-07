
/*************************************************************/
/* Copyright (c) 1984-2009 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation. */
/*************************************************************/
/*
*/

/*********************************************************************/
/* Module : TcpClientMsgOutputStream                                 */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.client;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.progress.common.ehnlog.IAppLogger;
import com.progress.common.ehnlog.O4glLogContext;
import com.progress.common.ehnlog.RestLogContext;
import com.progress.common.ehnlog.WsaLogContext;
import com.progress.common.ehnlog.UBrokerLogContext;
import com.progress.common.ehnlog.NxGASLogContext;
import com.progress.ubroker.util.IubMsgOutputStream;
import com.progress.ubroker.util.MsgReader;
import com.progress.ubroker.util.NetworkProtocolException;
import com.progress.ubroker.util.ubConstants;
import com.progress.ubroker.util.ubMsg;

/*********************************************************************/
/*                                                                   */
/* Class TcpClientMsgOutputStream                                    */
/*                                                                   */
/*********************************************************************/

public class TcpClientMsgOutputStream
    extends BufferedOutputStream
    implements ubConstants, IubMsgOutputStream
{

    /*********************************************************************/
    /* TcpClientMsgOutputStream Constants                                */
    /*********************************************************************/

    /*********************************************************************/
    /* TcpClientMsgOutputStream Static Data                              */
    /*********************************************************************/

    /*********************************************************************/
    /* TcpClientMsgOutputStream Instance Data                            */
    /*********************************************************************/

    private             IAppLogger          m_log;
    private             int                 m_logDest = IAppLogger.DEST_NONE;
    private             int                 m_streamTraceLevel = IAppLogger.LOGGING_EXTENDED;
    private             int                 m_serverType = SERVERTYPE_APPSERVER;
    private             TcpClientProtocol   m_parentProtocol = null;

    private             int                 m_msgBufferSize = MSG_INPUT_STREAM_BUFSIZE;

    private             long                m_debugLogEntries;
    private             int                 m_debugLogIndex;

    /*********************************************************************/
    /* TcpClientMsgOutputStream Constructors                             */
    /*********************************************************************/

    public TcpClientMsgOutputStream(TcpClientProtocol   parentProtocol,
                                    OutputStream        os,
                                    int                 serverType)
    {
        // super always comes first.  Pre-extend the internal buffer.
        super(os, MSG_INPUT_STREAM_BUFSIZE);

        // Can't work without the controlling parent protocol object.
        if (null == parentProtocol)
        {
            throw new NullPointerException("Cannot initialize with a null protocol object");
        }

        m_parentProtocol = parentProtocol;

        // We have to remember the server type.
        //
        this.m_serverType = serverType;

        // save a reference to the parent's logging object
        //
        m_log = m_parentProtocol.loggingObject();
        m_logDest = m_parentProtocol.loggingDestination();

        /* initialize log settings based on interface */
        initializeLogging(m_log);
    }

    /*********************************************************************/
    /* TcpClientMsgOutputStream accessor methods                         */
    /*********************************************************************/

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

    public int          getMsgBufferSize()
    {
        return(m_msgBufferSize);
    }

    public void         setLoggingTraceLevel(int newTraceLevel) throws Exception
    {
        m_streamTraceLevel = newTraceLevel;
    }

    public int          getLoggingTraceLevel()
    {
        return(m_streamTraceLevel);
    }

    /*********************************************************************/
    /* TcpClientMsgOutputStream public methods                           */
    /*********************************************************************/

    public synchronized void writeMsg(ubMsg msg)
        throws IOException
    {
        byte msgcode;

        if (m_log.ifLogVerbose(m_debugLogEntries,m_debugLogIndex))
           m_log.logVerbose(m_debugLogIndex,
                          "writeMsg(" + msg + ")");

        if (msg.getubhdr() != null)
            writeubhdr(msg);

        switch (msg.getubVer())
            {
            case ubMsg.UBMSG_PROTOCOL_V0:
                break;

            case ubMsg.UBMSG_PROTOCOL_V1:
            default:
                writetlvbuf(msg);
                break;
            }

        if (msg.getSrvHeader() != null)
            writeSrvHeader(msg);

        if (msg.getBuflen() > 0)
            writeMsgbuf(msg);
    }

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
        this.flush();
    }

    /*********************************************************************/
    /* TcpClientMsgOutputStream internal methods                         */
    /*********************************************************************/

    /*********************************************************************/
    /*                                                                   */
    /*********************************************************************/

    private void writeSrvHeader(ubMsg msg)
        throws IOException
    {
        if (m_log.ifLogVerbose(m_debugLogEntries,m_debugLogIndex))
            m_log.logDump(IAppLogger.LOGGING_VERBOSE,
                              m_debugLogIndex,
                              "writeSrvrHeader",
                              msg.getSrvHeader(),
                              msg.getSrvHeaderlen());

        writestream(msg.getSrvHeader(), 0, msg.getSrvHeaderlen());
    }

    /*********************************************************************/
    /*                                                                   */
    /*********************************************************************/

    private void writeubhdr(ubMsg msg)
        throws IOException
    {
        if (m_log.ifLogVerbose(m_debugLogEntries,m_debugLogIndex))
            m_log.logDump(IAppLogger.LOGGING_VERBOSE,
                          m_debugLogIndex,
                          "writeubhdr",
                          msg.getubhdr(),
                          ubMsg.UBHDRLEN);

        writestream(msg.getubhdr(), 0, ubMsg.UBHDRLEN);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

    private void writetlvbuf(ubMsg msg)
        throws IOException
    {
        byte[] tlvbuf = null;

        try
            {
            tlvbuf = msg.getubTlvBuf();

            if (m_log.ifLogVerbose(m_debugLogEntries,m_debugLogIndex))
                m_log.logDump(IAppLogger.LOGGING_VERBOSE,
                              m_debugLogIndex,
                              "writetlvbuf",
                              tlvbuf,
                              tlvbuf.length);

            writestream(tlvbuf, 0, tlvbuf.length);

            }
        catch (ubMsg.MsgFormatException e)
            {
            /* this should only happen if the tvl structure is invalid */
            m_log.logStackTrace("getubTlvBuf() Exception in writetlvbuf : " +
                                   e.getMessage(),
                                e);
            }
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

    private void writeMsgbuf(ubMsg msg)
        throws IOException
    {
        if (m_log.ifLogVerbose(m_debugLogEntries,m_debugLogIndex))
            m_log.logDump(IAppLogger.LOGGING_VERBOSE,
                          m_debugLogIndex,
                          "writeMsgbuf",
                          msg.getMsgbuf(),
                          msg.getBuflen());

        writestream(msg.getMsgbuf(), 0, msg.getBuflen());
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

    private void writestream(byte[] buf, int off, int len)
        throws IOException
    {
	MsgReader m_msgReader = m_parentProtocol.getReader();

        if (m_msgReader != null && m_msgReader.isClosed())
            throw new IOException("Failed to send data to the server");

        write(buf, off, len);
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

/*********************************************************************/
/*                                                                   */
/*********************************************************************/


}  /* end of TcpClientMsgOutputStream */


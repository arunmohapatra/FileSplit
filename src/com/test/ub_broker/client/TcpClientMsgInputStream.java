
/*************************************************************/
/* Copyright (c) 1984-2010 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission in writing from Progress Software Corporation. */
/*************************************************************/

/*********************************************************************/
/* Module : TcpClientMsgInputStream                                  */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.client;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.Properties;

import com.progress.common.ehnlog.IAppLogger;
import com.progress.common.ehnlog.O4glLogContext;
import com.progress.common.ehnlog.RestLogContext;
import com.progress.common.ehnlog.UBrokerLogContext;
import com.progress.common.ehnlog.WsaLogContext;
import com.progress.common.ehnlog.NxGASLogContext;
import com.progress.message.jcMsg;
import com.progress.ubroker.util.IubMsgInputStream;
import com.progress.ubroker.util.Logger;
import com.progress.ubroker.util.MessageCompressor;
import com.progress.ubroker.util.MsgReader;
import com.progress.ubroker.util.NetworkProtocolException;
import com.progress.ubroker.util.Request;
import com.progress.ubroker.util.RequestQueue;
import com.progress.ubroker.util.ubAdminMsg;
import com.progress.ubroker.util.ubAppServerMsg;
import com.progress.ubroker.util.ubConstants;
import com.progress.ubroker.util.ubMsg;
import com.progress.ubroker.util.ubWebSpeedMsg;
import com.progress.ubroker.util.ubMsg.MsgFormatException;
import com.progress.open4gl.dynamicapi.IPoolProps;

/*********************************************************************/
/*                                                                   */
/* Class TcpClientMsgInputStream                                     */
/*                                                                   */
/*********************************************************************/

public class TcpClientMsgInputStream
    extends BufferedInputStream
    implements ubConstants, IubMsgInputStream
{

    /*********************************************************************/
    /* Constants                                                         */
    /*********************************************************************/

    /*********************************************************************/
    /* Static Data                                                       */
    /*********************************************************************/


    /*********************************************************************/
    /* Instance Data                                                     */
    /*********************************************************************/

    private             IAppLogger          m_log;
    private             int                 m_logDest = Logger.DEST_NONE;
    private             int                 m_streamTraceLevel = IAppLogger.LOGGING_EXTENDED;
    private             int                 m_serverType = SERVERTYPE_APPSERVER;
    private             TcpClientProtocol   m_parentProtocol = null;
    private             int                 m_msgBufferSize = MSG_INPUT_STREAM_BUFSIZE;

    private             long                m_debugLogEntries;
    private             int                 m_debugLogIndex;
    private             long                m_cmprsLogEntries;
    private             int                 m_cmprsLogIndex;

    private             boolean             m_compressionEnabled = false;

    /*********************************************************************/
    /* Constructors                                                      */
    /*********************************************************************/

    /*********************************************************************/
    /*                                                                   */
    /*********************************************************************/

    public TcpClientMsgInputStream(TcpClientProtocol    parentProtocol,
                                   InputStream          is,
                                   int                  serverType)
    {
        // super always comes first.  Pre-extend the internal buffer.
        super(is, MSG_INPUT_STREAM_BUFSIZE);

        // Can't work without the controlling parent protocol object.
        if (null == parentProtocol)
        {
            throw new NullPointerException("Cannot initialize with a null protocol object");
        }

        m_parentProtocol = parentProtocol;

        // We have to remember the server type.
        //
        this.m_serverType = serverType;

        // save a reference to the parent's logging sink
        //
        m_log = m_parentProtocol.loggingObject();
        m_logDest = m_parentProtocol.loggingDestination();

        /* initialize log settings based on interface */
        initializeLogging(m_log);

        if (!markSupported())
        {
            m_log.logError("mark() is not supported on input stream.");
        }

        m_compressionEnabled = compressionEnabled(m_parentProtocol);
    }

    /*********************************************************************/
    /* Public methods                                                    */
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
    /*                                                                   */
    /*********************************************************************/
    public int available()
        throws IOException
    {
        RequestQueue m_requestQueue = m_parentProtocol.getQueue();
        if (m_requestQueue == null)
            return super.available();	

        if (m_requestQueue.isEmpty())
            return 0;
        
        // remove any timeout messages (exceptions)
        while (true)
        {
            Request r = (Request) m_requestQueue.findFirst();
            if (r == null)
        	return 0;

            ubMsg m = (ubMsg)r.getMsg();
            if (m == null)
                return 0;
            
            // if this is actually a timeout message then
            // remove it.  
            if (m.getubType() == ubMsg.UBRQ_ADMIN)
            {
                ubAdminMsg adMsg = (ubAdminMsg)m;

                if (adMsg.getadRq() == ubAdminMsg.ADRQ_SOCKET_TIMEOUT)
                    m_requestQueue.dequeueRequest();
                else
                    return 1;
            }
            else
                return 1;
        }
    }

    public synchronized ubMsg readMsg()
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
        RequestQueue m_requestQueue = m_parentProtocol.getQueue();
	MsgReader m_msgReader = m_parentProtocol.getReader();

        if (m_log.ifLogVerbose(m_debugLogEntries,m_debugLogIndex))
            m_log.logVerbose(m_debugLogIndex,
                           "readMsg()");

        /*
         * Read the message from the message queue if it exists.
         *
         * If the queue does not exist, read directly from 
         * the input stream.
         */
        if (m_requestQueue != null)
        {
            /*
             * Don't hang waiting for a non-existing message if
             * the agent has disappeared while processing a request
             */
            if ((m_msgReader == null ||                             // (if no thread or
                (m_msgReader != null && m_msgReader.isClosed())) && //  thread is closed) and
                m_requestQueue.isEmpty())                           // queue is empty
                throw new IOException("Error reading the message");     // will never be anything to read

            ubMsg m = (ubMsg) m_requestQueue.dequeueRequest().getMsg();

           // if this is actually an ADMIN message then
 	   // an exception has occurred.  
 	   if (m.getubType() == ubMsg.UBRQ_ADMIN)
           {
                ubAdminMsg adMsg = (ubAdminMsg)m;
                Object o = adMsg.getadParm();

                switch (adMsg.getadRq())
                {
                    case ubAdminMsg.ADRQ_SOCKET_TIMEOUT:
                        throw new InterruptedIOException("Timeout reading message");

                    case ubAdminMsg.ADRQ_MESSAGE_FORMAT_ERROR:
                        throw new ubMsg.MsgFormatException("Invalid message format");

                    case ubAdminMsg.ADRQ_NETWORK_PROTOCOL_ERROR:
                        throw new NetworkProtocolException();

                    case ubAdminMsg.ADRQ_IOEXCEPTION:
                    default:
                        throw new IOException("Error reading message");
                }
           }
           return m;
        }

        /* remember this spot so we can rewind to here .. in case a timeout  */
        /* occurs anywhere during the read .. when the caller retries, we    */
        /* have to start (re)reading at the beginning of the message         */

        mark(MSG_INPUT_STREAM_BUFSIZE);
/*
        if (!m_log.ignore(Logger.LOGGING_TRACE))
        {
            m_log.LogMsgln(m_logDest,
                           Logger.LOGGING_TRACE,
                           Logger.NOTIMESTAMP,
                           "mark() : markpos=" + markpos);
        }
*/
        ubhdr = readubhdr();

        /* read the tlv buffer */
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
    {
        byte[] ubhdr = new byte[ubMsg.UBHDRLEN];

        readstream(ubhdr, 0, 2);

        /* check length and ubVer */
        ubMsg.checkubVer(ubhdr);

        readstream(ubhdr, 2, ubhdr.length-2);

        if (m_log.ifLogVerbose(m_debugLogEntries,m_debugLogIndex))
            m_log.logDump(IAppLogger.LOGGING_VERBOSE,
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
        throws IOException
    {
        int    ubver;
        byte[] tlvbuf;
        int    tlvlen;

        try
            {
            tlvlen = ubMsg.getubTlvBuflen(ubhdr);
            tlvbuf = new byte[tlvlen];

            readstream(tlvbuf, 0, tlvlen);

            if (m_log.ifLogVerbose(m_debugLogEntries,m_debugLogIndex))
                m_log.logDump(IAppLogger.LOGGING_VERBOSE,
                              m_debugLogIndex,
                              "readtlvbuf",
                              tlvbuf,
                              tlvbuf.length);
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
        throws IOException
    {
        readstream(msg.getSrvHeader(), 0, msg.getSrvHeaderlen());

        if (m_log.ifLogVerbose(m_debugLogEntries,m_debugLogIndex))
            m_log.logDump(IAppLogger.LOGGING_VERBOSE,
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
    {
        byte[] srvhdr = new byte[srvhdrlen];
        readstream(srvhdr, 0, srvhdrlen);

        if (m_log.ifLogVerbose(m_debugLogEntries,m_debugLogIndex))
            m_log.logDump(IAppLogger.LOGGING_VERBOSE,
                          m_debugLogIndex,
                          "readsrvhdr",
                          srvhdr,
                          srvhdrlen);

        return srvhdr;
    }

    /*********************************************************************/
    /*                                                                   */
    /**
     * @throws MsgFormatException *******************************************************************/

    private int readMsgbuf(ubMsg msg, int buflen)
    throws IOException, MsgFormatException
    {
        byte[] tmpbuf;
        tmpbuf = new byte[buflen];
        
        byte[] uncompressedBuf = null;
        int uncompressedLength = buflen;

        readstream(tmpbuf, 0, buflen);

        if (m_log.ifLogExtended(m_debugLogEntries,m_debugLogIndex))
            {
                m_log.logDump(  IAppLogger.LOGGING_EXTENDED,
                                m_debugLogIndex,
                                "readMsgbuf[" + buflen + "]",
                                tmpbuf,
                                buflen);
            }
            else
                if (m_log.ifLogVerbose(m_debugLogEntries,m_debugLogIndex))
                {
                m_log.logDump(  IAppLogger.LOGGING_VERBOSE,
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
       	    //ignore this since it is expected with AIA when it has a connection problem
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


                got = read(msgbuf, ofst + (len - need), need);

                /*
                m_log.LogMsgln(m_logDest,
                Logger.LOGGING_DEBUG,
                Logger.NOTIMESTAMP,
                "after read: inputstream variables : " +
                "got= " + got + " pos= " + pos + " count= " + count +
                " marklimit= " + marklimit + " markpos= " + markpos);
                */
            }
            catch (InterruptedIOException inte)
            {
                /*
                m_log.LogMsgln(m_logDest,
                Logger.LOGGING_DEBUG,
                Logger.NOTIMESTAMP,
                "read() InterruptedIOException in readstream : " +
                inte.getMessage() +
                " : got= " + got + " need= " + need +
                " pos= " + pos + " count= " + count +
                " marklimit= " + marklimit + " markpos= " + markpos);
                */


                /* since we will be returning control to the caller      */
                /* we must reset the stream position so that when/if     */
                /* the readMsg() is retried, the already read portions   */
                /* of the message will be re-read from the correct place */

                try
                {
                    reset();
                    /*
                    m_log.LogMsgln(m_logDest,
                    Logger.LOGGING_DEBUG,
                    Logger.NOTIMESTAMP,
                    "reset() : pos= " + pos);
                    */
                }
                catch (IOException eio)
                {
                if (m_log.ifLogVerbose(m_debugLogEntries,m_debugLogIndex))
                    m_log.logVerbose(m_debugLogIndex,
                                   "IOException on reset() : " +
                                   eio + eio.getMessage());
                }

                throw inte;
            }
            catch (IOException ioe)
            {
               if (m_log.ifLogVerbose(m_debugLogEntries,m_debugLogIndex))
                   m_log.logVerbose(m_debugLogIndex,
                                  "read() IOException in readstream : " +
                                  ioe.getMessage() +
                                  " : got= " + got + " need= " + need );

                throw ioe;
            }
            if (got == -1)
            {
                throw new EOFException();
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
            m_cmprsLogEntries    = 0;
            m_cmprsLogIndex      = 0;
            }
        else if (contextName.equals("O4gl"))
            {
            m_debugLogEntries    = O4glLogContext.SUB_M_UBROKER;
            m_debugLogIndex      = O4glLogContext.SUB_V_UBROKER;
            m_cmprsLogEntries    = O4glLogContext.SUB_M_COMPRESSION;
            m_cmprsLogIndex	 = O4glLogContext.SUB_V_COMPRESSION;
            }
        else if (contextName.equals("UBroker"))
            {
            m_debugLogEntries    = UBrokerLogContext.SUB_M_UB_DEBUG;
            m_debugLogIndex      = UBrokerLogContext.SUB_V_UB_DEBUG;
            m_cmprsLogEntries    = UBrokerLogContext.SUB_M_UB_COMPRESSION;
            m_cmprsLogIndex	 = UBrokerLogContext.SUB_V_UB_COMPRESSION;
            }
        else if (contextName.equals("NxGAS"))
            {
            m_debugLogEntries    = NxGASLogContext.SUB_M_NXGASDEBUG;
            m_debugLogIndex      = NxGASLogContext.SUB_V_NXGASDEBUG;
            m_cmprsLogEntries    = 0;
            m_cmprsLogIndex      = 0;
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

    private boolean compressionEnabled(TcpClientProtocol parentProtocol)
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


}  /* end of TcpClientMsgInputStream */



/*************************************************************/
/* Copyright (c) 1984-2005 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission in writing from Progress Software Corporation. */
/*************************************************************/
/*
 */

/*********************************************************************/
/* Module : UbMsgChannel                                             */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.util;

import java.io.IOException;
import java.io.EOFException;
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;

import com.progress.ubroker.util.ubConstants;
import com.progress.common.ehnlog.IAppLogger;
import com.progress.common.ehnlog.UBrokerLogContext;
import com.progress.message.jcMsg;

import com.progress.ubroker.util.ubMsg;
import com.progress.ubroker.util.ubAppServerMsg;
import com.progress.ubroker.util.ubWebSpeedMsg;

/*********************************************************************/
/*                                                                   */
/* Class UbMsgChannel                                                */
/*                                                                   */
/*********************************************************************/

public class UbMsgChannel
    implements IUbMsgChannel,IubMsgInputStream,IubMsgOutputStream
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

IAppLogger         applog;
int                streamLoggingLevel;
long               entrytype;
int                serverType;
int                index_entry_type;
String             entryTypeName;
String             logEnvID;

SocketChannel      channel;
ByteBuffer         channelBuffer;

private  int       m_msgBufferSize = ubConstants.MSG_INPUT_STREAM_BUFSIZE;

/* these two members are used to read the channel to detect disconnects */
private  byte[]    m_peekBuf = new byte[1];
private  boolean   m_bufferedByte = false;

/*********************************************************************/
/* Constructors                                                      */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public UbMsgChannel(SocketChannel channel,
                    int bufsize,
                    IAppLogger lg,
                    int loggingLevel,
                    long entrytype,
                    int index_entry)
    {
	this.channel = channel;
	channelBuffer = ByteBuffer.allocateDirect(bufsize);
    applog = lg;
    this.streamLoggingLevel = loggingLevel;
    this.entrytype = entrytype;
    this.index_entry_type = index_entry;
    entryTypeName = applog.getLogContext().getEntrytypeName(index_entry_type);
    logEnvID = applog.getExecEnvId();
    }

/*********************************************************************/
/* Public methods                                                    */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int available()
    {
	Object lockObj = channel.blockingLock();
	Selector selector;
	SelectionKey key;
	String op = "";
	int n, ret = 0; 
	
	synchronized(lockObj)
        {
        try
		    {
    	    op = "configureBlocking(false)";
            channel.configureBlocking(false);
            op = "Selector.open()";
	        selector = Selector.open();
	        op = "channel.register(OP_READ)";
	        key = channel.register(selector,SelectionKey.OP_READ);
	    
	        /* check the channel for input */
	        op = "selector.selectNow()";
	        n = selector.selectNow();
	    
	        if ((n > 0) && (key.isReadable()))
	    	    ret = 1;
	        
	        /* close the selector (this should de-register the channel) */
	        op = "selector.close()";
	        selector.close();
		    }
	    catch(IOException e)
		    {
            if ((applog != null) && 
                 applog.ifLogIt(streamLoggingLevel,entrytype,index_entry_type))
                {
                applog.logWriteMessage(IAppLogger.DEST_LOGFILE,
                                       streamLoggingLevel,
                                       logEnvID,
                                       entryTypeName,
                                       op + " error : "  + e);
                }
        
            ret = 0;
		    }
	    finally
	        {
            try
		        {
            	/* make sure that we reset the channel to blocking mode */
                channel.configureBlocking(true);
		        }
	        catch(IOException ioe)
		        {
                if ((applog != null) && 
                     applog.ifLogIt(streamLoggingLevel,entrytype,index_entry_type))
                    {
                    applog.logWriteMessage(IAppLogger.DEST_LOGFILE,
                                           streamLoggingLevel,
                                           logEnvID,
                                           entryTypeName,
                                           "error resetting channel to blocking mode : "  + ioe);
                    }
		        }
	        }
        }
	
	return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized boolean socketIsConnected()
    {
    boolean ret;
    
    /* if we've already buffered a byte, then assume we're connected */
    if (m_bufferedByte)
    	return true;
    
    /* find out if an event is available on the wire */
    ret = (available() != 0);
    
    /* if an event is available, find out if there is any data     */
    /* unfortunately, to do this, we must actually read the wire   */
    /* and hang onto anything we receive.  the expectation is that */
    /* there will not be any data, given that the appserver is     */
    /* *usually* request/response                                  */
    if (ret)
        {
    	try
    	    {
            readChannel(m_peekBuf, 0, 1);
            m_bufferedByte = true;
            ret = true;
    	    }
    	catch (IOException eofe)
    	    {
    		ret = false;
    	    }
        }
    else
        {
        /* if no event is available, then no disconnect has been recvd */
    	/* so we must still be connected.                              */
    	ret = true;
        }
    
    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public void         setMsgBufferSize(int newBufferSize) throws Exception
    {
	/* since the channel is not really a stream, it does not implement
	 * some of the basic stream stuff ... see if we can get away without
	 * this
	 */
	
	/*
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
    */
    
    m_msgBufferSize = newBufferSize;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public int          getMsgBufferSize()
    {
    return(m_msgBufferSize);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public void         setLoggingTraceLevel(int newTraceLevel) throws Exception
    {
    streamLoggingLevel = newTraceLevel;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public int          getLoggingTraceLevel()
    {
    return(streamLoggingLevel);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public void flushMsg()
    {
    }


/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized ubMsg readMsg()
    throws
        IOException
      , ubMsg.MsgFormatException
    {
    ubMsg msg = null;
    byte[] ubhdr;
    byte[] tlvbuf;
    byte[] srvhdr;
    int buflen;
    int msgtype;
    
    /* make the channel available for writing */
    channelBuffer.clear();


    if ((applog != null) && applog.ifLogIt(streamLoggingLevel,entrytype,index_entry_type))
        {
        applog.logWriteMessage(IAppLogger.DEST_LOGFILE,
                               streamLoggingLevel,
                               logEnvID,
                               entryTypeName,
                               "readMsg()");
        }

    /* read the ubhdr */
    ubhdr = readubhdr();

    /* read the tlv buffer */
    tlvbuf = readtlvbuf(ubhdr);

    /* grab the msgtype and read the server header */
    msgtype = ubMsg.getubType(ubhdr);

    switch ( msgtype )
    {
        case ubMsg.UBTYPE_APPSERVER          :
            /* data servers now using AppServer message format */
            if ( (serverType != ubConstants.SERVERTYPE_APPSERVER)      &&
                 (serverType != ubConstants.SERVERTYPE_ADAPTER)        &&
                 (serverType != ubConstants.SERVERTYPE_ADAPTER_CC)     &&
                 (serverType != ubConstants.SERVERTYPE_ADAPTER_SC)     &&
                 (serverType != ubConstants.SERVERTYPE_DATASERVER_OD)  &&
                 (serverType != ubConstants.SERVERTYPE_DATASERVER_OR)  &&
                 (serverType != ubConstants.SERVERTYPE_DATASERVER_MSS)  )
                {
                /* Got a message of incorrect type (%s<msgType>)  */
                /* for this serverType (%s<serverType>)           */

                if (applog != null)
                    {
                        applog.logError(jcMsg.jcMSG101,    /*jbMsg.jbMSG078*/
                                        new Object[] { 
                                            ubMsg.DESC_UBTYPE[msgtype],
                                            ubConstants.STRING_SERVER_TYPES[serverType] 
                                             }
                                        );

                     }

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
            if (serverType != ubConstants.SERVERTYPE_WEBSPEED)
                {
                /* Got a message of incorrect type (%s<msgType>)  */
                /* for this serverType (%s<serverType>)           */

                if (applog != null)
                    {
                    applog.logError(jcMsg.jcMSG101, /* jbMsg.jbMSG078 */
                                    new Object[] { 
                                        ubMsg.DESC_UBTYPE[msgtype],
                                        ubConstants.STRING_SERVER_TYPES[serverType] 
                                    }
                                );
                     }

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
/*                                                                   */
/*********************************************************************/

public synchronized void writeMsg(ubMsg msg)
    throws IOException
    {
	/* write msg to logfile */
    logMsg("writeMsg()", msg);
    
    /* write the msg to the channel */
    channelBuffer.clear();
	channelBuffer = msg.wrapMsg(channelBuffer);
    channelBuffer.flip();
    
    /* because the channel is non-blocking, all data may not be written */
    /* on the first write() ... keep trying until the buffer is empty   */
    while (channelBuffer.hasRemaining())
        channel.write(channelBuffer);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public void close()
    throws IOException
    {
    try
        {
        channel.close();
        }
    catch (IOException se)
        {
        /* Note: this used to go to the log in verbose mode. However,since now we
           have to obey to the log context passed to us, I am going to leave the
           logging level as BASIC to whatever entry type we are tracing now.
        */
        if ((applog != null) && applog.ifLogIt(IAppLogger.LOGGING_BASIC,entrytype,index_entry_type))
           {
           applog.logWriteMessage(IAppLogger.DEST_LOGFILE,
                                  IAppLogger.LOGGING_BASIC,
                                  logEnvID,
                                  entryTypeName,
                                  "Closing channel: " + se.getMessage() );
           }
        }
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

    readChannel(ubhdr, 0, 2);

    /* check length and ubVer */
    ubMsg.checkubVer(ubhdr);

    readChannel(ubhdr, 2, ubhdr.length-2);

    if ((applog != null) && applog.ifLogIt(streamLoggingLevel,entrytype,index_entry_type))
        {
        applog.logDump(streamLoggingLevel,
                       index_entry_type,
                       "readubhdr",
                       ubhdr,
                       ubhdr.length);
        }

    return ubhdr;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private byte[] readtlvbuf(byte[] ubhdr)
    throws
        IOException
    {
    int    ubver;
    byte[] tlvbuf = null;
    int    tlvlen;

    try
        {
        tlvlen = ubMsg.getubTlvBuflen(ubhdr);

        if (tlvlen > 0)
        {
            tlvbuf = new byte[tlvlen];
            readChannel(tlvbuf, 0, tlvlen);
        }


        if ((tlvlen > 0) && (applog != null) && 
             applog.ifLogIt(streamLoggingLevel,entrytype,index_entry_type))
            {
        	applog.logDump(streamLoggingLevel,
                           index_entry_type,
                           "readtlvbuf",
                           tlvbuf,
                           tlvbuf.length);
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

private byte[] readsrvhdr(int srvhdrlen)
    throws IOException
    {
    byte[] srvhdr = new byte[srvhdrlen];
    readChannel(srvhdr, 0, srvhdrlen);

    if ((applog != null) && applog.ifLogIt(streamLoggingLevel,entrytype,index_entry_type))
        {
        applog.logDump(streamLoggingLevel,
                       index_entry_type,
                       "readsrvhdr",
                       srvhdr,
                       srvhdrlen);
        }

    return srvhdr;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private int readMsgbuf(ubMsg msg, int buflen)
    throws IOException
    {
    byte[] tmpbuf;

    tmpbuf = new byte[buflen];

    readChannel(tmpbuf, 0, buflen);

    if ((applog != null) && applog.ifLogIt(streamLoggingLevel,entrytype,index_entry_type))
        {
        applog.logDump(streamLoggingLevel,
                       index_entry_type,
                       "readMsgbuf[" + buflen + "]",
                       tmpbuf,
                       buflen);
        }

    msg.setMsgbuf(tmpbuf,buflen);

    return buflen;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private void readChannel(byte[] msgbuf, int ofst, int len)
    throws IOException
    {
    int got;
    
    /*
    if ((applog != null) && applog.ifLogIt(streamLoggingLevel,entrytype,index_entry_type))
        {
        applog.logWriteMessage(IAppLogger.DEST_LOGFILE,
                               streamLoggingLevel,
                               logEnvID,
                               entryTypeName,
                               "readChannel(" + msgbuf + ", "
                                              + ofst   + ", "
                                              + len    + ") ");
        }
    
    if ((applog != null) && applog.ifLogIt(streamLoggingLevel,entrytype,index_entry_type))
        {
        applog.logWriteMessage(IAppLogger.DEST_LOGFILE,
                               streamLoggingLevel,
                               logEnvID,
                               entryTypeName,
                               "readChannel() before clear() : " +
                                  "capacity= " + channelBuffer.capacity() + "  " +
                                  "limit= "    + channelBuffer.limit()    + "  " +
                                  "position= " + channelBuffer.position() + "  " +
                                  "remaining= " + channelBuffer.remaining());
        }
    */
    
    channelBuffer.clear();
    
    /*
    if ((applog != null) && applog.ifLogIt(streamLoggingLevel,entrytype,index_entry_type))
        {
        applog.logWriteMessage(IAppLogger.DEST_LOGFILE,
                               streamLoggingLevel,
                               logEnvID,
                               entryTypeName,
                               "readChannel() before limit() : " +
                                  "capacity= " + channelBuffer.capacity() + "  " +
                                  "limit= "    + channelBuffer.limit()    + "  " +
                                  "position= " + channelBuffer.position() + "  " +
                                  "remaining= " + channelBuffer.remaining());
        }
    */
    
    /* check to see if we've already got a buffered byte to return first */
    if (m_bufferedByte)
        {
    	msgbuf[ofst++] = m_peekBuf[0];
    	m_bufferedByte = false;
    	
    	/* adjust the length to be read */
    	len--;
    	
    	/* no need to read the channel in this case */
    	if (len == 0)
    	    return;
        }
    
    /* setting the limit forces the channel to only return the  */
    /* desired number of bytes, even if more are available      */
    channelBuffer.limit(len);
    
    /*
    if ((applog != null) && applog.ifLogIt(streamLoggingLevel,entrytype,index_entry_type))
        {
        applog.logWriteMessage(IAppLogger.DEST_LOGFILE,
                               streamLoggingLevel,
                               logEnvID,
                               entryTypeName,
                               "readChannel() after limit() : " +
                                  "capacity= " + channelBuffer.capacity() + "  " +
                                  "limit= "    + channelBuffer.limit()    + "  " +
                                  "position= " + channelBuffer.position() + "  " +
                                  "remaining= " + channelBuffer.remaining());
        }
    */
    
    /* now read until we get the desired number */
    while(channelBuffer.hasRemaining())
        {
        try
            {
            got = channel.read(channelBuffer);
            }
        catch (IOException ioe)
            {
            if ((applog != null) && applog.ifLogIt(streamLoggingLevel,entrytype,index_entry_type))
                {
                applog.logWriteMessage(IAppLogger.DEST_LOGFILE,
                                       streamLoggingLevel,
                                       logEnvID,
                                       entryTypeName,
                                       "read() IOException in readChannel : " +
                                       ioe.getMessage() );
                }

            throw ioe;
            }
        
        /* is this right ??? */
        if (got == -1)
            {
            throw new EOFException();
            }
        }
    
    /* if we get here, then we've read the number of bytes we wanted     */
    /* now move them into the provided byte array so they don't get lost */
    channelBuffer.flip();
    channelBuffer.get(msgbuf, ofst, len);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private void logMsg(String description, ubMsg msg)
    {
    byte[] tlvbuf;
    int tlvlen;
    int buflen = msg.getBuflen();
	
	
    if ((applog != null) && applog.ifLogIt(streamLoggingLevel,entrytype,index_entry_type))
        {
        applog.logWriteMessage(IAppLogger.DEST_LOGFILE,
                               streamLoggingLevel,
                               logEnvID,
                               entryTypeName,
                               description);
        
        applog.logDump(streamLoggingLevel,
                       index_entry_type,
                       "writeubhdr",
                       msg.getubhdr(),
                       ubMsg.UBHDRLEN);

        applog.logDump(streamLoggingLevel,
                       index_entry_type,
                       "writeSrvHeader",
                       msg.getSrvHeader(),
                       msg.getSrvHeaderlen());
        
        try
            {
            tlvbuf = msg.getubTlvBuf();
            if (tlvbuf != null)
             	{
                tlvlen = msg.getubTlvBuflen();
                applog.logDump(streamLoggingLevel,
                               index_entry_type,
                               "writetlvbuf",
                               tlvbuf,
                               tlvlen);
        	    }
            }
        catch (ubMsg.MsgFormatException e)
            {
            /* this could happen if the msg is v0, so ignore */
        	/*
            if (applog != null)
               {
               applog.logStackTrace(index_entry_type,
                            "getubTlvBuf() Exception : " +
                             e.getMessage(),
                            e);
               }
            */
            }
            
        applog.logDump(streamLoggingLevel,
                           index_entry_type,
                           "writeMsgbuf[" + buflen + "]",
                           msg.getMsgbuf(), 
                           buflen);
        }
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private void setBlockingMode(SocketChannel ch, boolean fBlocking)
    throws IOException
    {
    try
		{
		/* make the channel non-blocking */
        ch.configureBlocking(fBlocking);
		}
	catch(IOException e)
		{
        if ((applog != null) && 
             applog.ifLogIt(streamLoggingLevel,entrytype,index_entry_type))
            {
            applog.logWriteMessage(IAppLogger.DEST_LOGFILE,
                                   streamLoggingLevel,
                                   logEnvID,
                                   entryTypeName,
                                   "error setting channel (" + ch + ") to " +
                                   (fBlocking ? "" : "non-") + 
                                   "blocking mode : " + e);
            }
        
		throw e;
		}
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private Selector openSelector()
    throws IOException
    {
	Selector selector;
    try
		{
		/* make the channel non-blocking */
    	selector = Selector.open();
		}
	catch(IOException e)
		{
        if ((applog != null) && 
             applog.ifLogIt(streamLoggingLevel,entrytype,index_entry_type))
            {
            applog.logWriteMessage(IAppLogger.DEST_LOGFILE,
                                   streamLoggingLevel,
                                   logEnvID,
                                   entryTypeName,
                                   "error opening selector " + e);
            }
        
		throw e;
		}
	
	return selector;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/


}  /* end of UbMsgChannel */


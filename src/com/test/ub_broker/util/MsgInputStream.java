
/*************************************************************/
/* Copyright (c) 1984-2008 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission in writing from Progress Software Corporation. */
/*************************************************************/
/*
 */

/*********************************************************************/
/* Module : MsgInputStream                                           */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.util;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

import javax.net.ssl.SSLHandshakeException;

import com.progress.common.ehnlog.IAppLogger;
import com.progress.common.ehnlog.UBrokerLogContext;
import com.progress.message.jbMsg;
import com.progress.message.jcMsg;

/*********************************************************************/
/*                                                                   */
/* Class MsgInputStream                                              */
/*                                                                   */
/*********************************************************************/

public class MsgInputStream
    extends BufferedInputStream
    implements ubConstants
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

IAppLogger applog;
int       stream_trace_level;
long      entrytype;
int       serverType;
int       index_entry_type;
String    entryTypeName;
String    logEnvID;

/*********************************************************************/
/* Constructors                                                      */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public MsgInputStream(InputStream is,
                      int bufsize,
                      int serverType,
                      IAppLogger lg,
                      int stream_trace_level,
                      long entrytype,
                      int index_entry)
    {
    super(is, bufsize);
    this.serverType = serverType;
    applog = lg;
    this.stream_trace_level = stream_trace_level;
    this.entrytype = entrytype;
    this.index_entry_type = index_entry;
    entryTypeName = applog.getLogContext().getEntrytypeName(index_entry_type);
    logEnvID = applog.getExecEnvId();

    if (!markSupported())
        applog.logError("mark() is not supported on input stream.");
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public MsgInputStream(InputStream is, int bufsize, int serverType, IAppLogger lg)
    {
    this(is, bufsize, serverType, lg, IAppLogger.LOGGING_BASIC,
         UBrokerLogContext.SUB_M_UB_DEBUG, UBrokerLogContext.SUB_V_UB_DEBUG);
    }

/*********************************************************************/
/* Public methods                                                    */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized ubMsg readMsg()
    throws
        IOException
      , ubMsg.MsgFormatException
    {
    ubMsg msg = null;
    byte msgcode;
    byte[] ubhdr;
    byte[] tlvbuf;
    byte[] srvhdr;
    int buflen;
    int msgtype;


    // OE00182547
    if ((applog != null) && 
         applog.ifLogIt(IAppLogger.LOGGING_EXTENDED,entrytype,index_entry_type))
        {
        applog.logWriteMessage(IAppLogger.DEST_LOGFILE,
                               IAppLogger.LOGGING_EXTENDED,
                               logEnvID,
                               entryTypeName,
                               "reading message ...");
        }

    /* remember this spot so we can rewind to here .. in case a timeout  */
    /* occurs anywhere during the read .. when the caller retries, we    */
    /* have to start (re)reading at the beginning of the message         */
 
    mark(MSG_INPUT_STREAM_BUFSIZE);

    // OE00182547
    if ((applog != null) && 
         applog.ifLogIt(IAppLogger.LOGGING_EXTENDED,entrytype,index_entry_type))
        {
        applog.logWriteMessage(IAppLogger.DEST_LOGFILE,
                               IAppLogger.LOGGING_EXTENDED,
                               logEnvID,
                               entryTypeName,
                               "mark() : markpos=" + markpos);
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
            if ( (serverType != SERVERTYPE_APPSERVER)      &&
                 (serverType != SERVERTYPE_ADAPTER)        &&
                 (serverType != SERVERTYPE_ADAPTER_CC)     &&
                 (serverType != SERVERTYPE_ADAPTER_SC)     &&
                 (serverType != SERVERTYPE_DATASERVER_OD)  &&
                 (serverType != SERVERTYPE_DATASERVER_OR)  &&
                 (serverType != SERVERTYPE_DATASERVER_MSS)  )
                {
                /* Got a message of incorrect type (%s<msgType>)  */
                /* for this serverType (%s<serverType>)           */

                if (applog != null)
                    {
                        applog.logError(jcMsg.jcMSG101,    /*jbMsg.jbMSG078*/
                                        new Object[] { 
                                            ubMsg.DESC_UBTYPE[msgtype],
                                            STRING_SERVER_TYPES[serverType] 
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
            if (serverType != SERVERTYPE_WEBSPEED)
                {
                /* Got a message of incorrect type (%s<msgType>)  */
                /* for this serverType (%s<serverType>)           */

                if (applog != null)
                    {
                        applog.logError(jcMsg.jcMSG101, /* jbMsg.jbMSG078 */
                                        new Object[] { 
                                            ubMsg.DESC_UBTYPE[msgtype],
                                            STRING_SERVER_TYPES[serverType] 
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

    if (buflen >= 0)
        {
        buflen = readMsgbuf(msg, buflen);
        }
    else 
        {
                /* Got a message of negative buflen (%s<msgType>)  */
                if (applog != null)
                    {
                        applog.logError(jcMsg.jcMSG199, /* jcMsg.jbMSG199 */
                                        new Object[] {
                                            ubMsg.DESC_UBTYPE[msgtype],
                                            STRING_SERVER_TYPES[serverType]
                                            }
                                        );
                     }
                throw new ubMsg.MsgFormatException("Negative value received for buffer length.");
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

    if ((applog != null) && applog.ifLogIt(stream_trace_level,entrytype,index_entry_type))
        {
        // OE00182547
        applog.logWriteMessage(IAppLogger.DEST_LOGFILE,
                               stream_trace_level,
                               logEnvID,
                               entryTypeName,
                               "readMsg()");

        applog.logDump(stream_trace_level,
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
    byte[] tlvbuf = new byte[0];
    int    tlvlen;

    try
        {
        tlvlen = ubMsg.getubTlvBuflen(ubhdr);

        if (tlvlen > 0)
        {
            tlvbuf = new byte[tlvlen];
            readstream(tlvbuf, 0, tlvlen);
        }


        if ((tlvlen > 0) && (applog != null) && 
             applog.ifLogIt(stream_trace_level,entrytype,index_entry_type))
            {
        	applog.logDump(stream_trace_level,
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

private void readsrvhdr(ubMsg msg)
    throws IOException
    {
    readstream(msg.getSrvHeader(), 0, msg.getSrvHeaderlen());

    if ((applog != null) && applog.ifLogIt(stream_trace_level,entrytype,index_entry_type))
        {
        applog.logDump(stream_trace_level,
                       index_entry_type,
                       "readsrvhdr",
                       msg.getSrvHeader(),
                       msg.getSrvHeaderlen());
        }

    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private byte[] readsrvhdr(int srvhdrlen)
    throws IOException
    {
    byte[] srvhdr = new byte[srvhdrlen];
    readstream(srvhdr, 0, srvhdrlen);

    if ((applog != null) && applog.ifLogIt(stream_trace_level,entrytype,index_entry_type))
        {
        applog.logDump(stream_trace_level,
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

    readstream(tmpbuf, 0, buflen);

    if ((applog != null) && applog.ifLogIt(stream_trace_level,entrytype,index_entry_type))
        {
        applog.logDump(stream_trace_level,
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

private void readstream(byte[] msgbuf, int ofst, int len)
    throws IOException
    {
    int need, got;

    for (need = len, got = 0; need > 0; need -= got)
        {
        try
            {
/*
            log.LogMsgln(Logger.DEST_LOGFILE,
                         Logger.LOGGING_DEBUG,
                         Logger.NOTIMESTAMP,
                         "before read: inputstream variables : " +
                         "need= " + need + " pos= " + pos + 
                         " count= " + count +
                         " marklimit= " + marklimit + " markpos= " + markpos);
*/


            got = read(msgbuf, ofst + (len - need), need);

/*
            log.LogMsgln(Logger.DEST_LOGFILE,
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
            log.LogMsgln(Logger.DEST_LOGFILE,
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
                log.LogMsgln(Logger.DEST_LOGFILE,
                             Logger.LOGGING_DEBUG,
                             Logger.NOTIMESTAMP,
                             "reset() : pos= " + pos);
*/
                }
            catch (IOException eio)
                {

                if (applog != null)
                {
                applog.logError("IOException on reset() : " + 
                                eio + eio.getMessage());
                }

                }

            /* OE00182547 */
            if ((applog != null) && 
                applog.ifLogIt(IAppLogger.LOGGING_EXTENDED,entrytype,index_entry_type))
            {
                applog.logWriteMessage(IAppLogger.DEST_LOGFILE,
                                       IAppLogger.LOGGING_EXTENDED,
                                       logEnvID,
                                       entryTypeName,
                                       "readMsg() : " + inte.toString() );
            }



            throw inte;
            }
        catch (SSLHandshakeException exception) 
        	{
            if ((applog != null) && applog.ifLogIt(stream_trace_level,entrytype,index_entry_type))
            {
                applog.logWriteMessage(IAppLogger.DEST_LOGFILE,
                                       stream_trace_level,
                                       logEnvID,
                                       entryTypeName,
                                       "read() SSLHandshakeException in readstream : " +
                                       exception.getMessage() +
                                       " : got= " + got + " need= " + need );
            }
//            	applog.logStackTrace(  jbMsg.jbMSG038,
//                        new Object[] { exception.getMessage() },
//                        exception);


            throw exception;
		}        
        catch (IOException ioe)
            {
            if ((applog != null) && applog.ifLogIt(stream_trace_level,entrytype,index_entry_type))
            {
                applog.logWriteMessage(IAppLogger.DEST_LOGFILE,
                                       stream_trace_level,
                                       logEnvID,
                                       entryTypeName,
                                       "read() IOException in readstream : " +
                                       ioe.getMessage() +
                                       " : got= " + got + " need= " + need );
            }


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


}  /* end of MsgInputStream */


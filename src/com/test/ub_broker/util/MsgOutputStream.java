
/*************************************************************/
/* Copyright (c) 1984-2004 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation. */
/*************************************************************/
/*
 * $Id: MsgOutputStream.java,v 1.5 1998/09/29 10:57:38 lecuyer Exp $
 */

/*********************************************************************/
/* Module : MsgOutputStream                                          */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.util;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

import com.progress.common.ehnlog.IAppLogger;

/*********************************************************************/
/*                                                                   */
/* Class MsgOutputStream                                             */
/*                                                                   */
/*********************************************************************/

public class MsgOutputStream extends BufferedOutputStream
{

/*********************************************************************/
/* MsgOutputStream Constants                                         */
/*********************************************************************/

/*********************************************************************/
/* MsgOutputStream Static Data                                       */
/*********************************************************************/

/*********************************************************************/
/* MsgOutputStream Instance Data                                     */
/*********************************************************************/

IAppLogger applog;
int       stream_trace_opt;
long      entrytype;
int       index_entry_type;
String    entryTypeName;
String    logEnvID;

/*********************************************************************/
/* MsgOutputStream Constructors                                      */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public MsgOutputStream(OutputStream os,
                       int bufsize,
                       IAppLogger lg,
                       int stream_trace_opt,
                       long entrytype,
                       int index_entry)
    {
    super(os, bufsize);
    applog = lg;
    this.stream_trace_opt = stream_trace_opt;
    this.entrytype = entrytype;
    this.index_entry_type = index_entry;
    entryTypeName = applog.getLogContext().getEntrytypeName(index_entry_type);
    logEnvID = applog.getExecEnvId();
    }

/*********************************************************************/
/* MsgOutputStream accessor methods                                  */
/*********************************************************************/

public synchronized void writeMsg(ubMsg msg)
    throws IOException
    {
    byte msgcode;

    if ((applog != null) && applog.ifLogIt(stream_trace_opt,entrytype,index_entry_type))
        {
        applog.logWriteMessage(IAppLogger.DEST_LOGFILE,
                               stream_trace_opt,
                               logEnvID,
                               entryTypeName,
                               "writeMsg()");
        }

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

/*********************************************************************/
/* MsgOutputStream internal methods                                   */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private void writeSrvHeader(ubMsg msg)
    throws IOException
    {

    if ((applog != null) && applog.ifLogIt(stream_trace_opt,entrytype,index_entry_type))
        {
        applog.logDump(stream_trace_opt,
                       index_entry_type,
                       "writeSrvHeader",
                       msg.getSrvHeader(),
                       msg.getSrvHeaderlen());
		}

    write(msg.getSrvHeader(), 0, msg.getSrvHeaderlen());
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private void writeubhdr(ubMsg msg)
    throws IOException
    {

    if ((applog != null) && applog.ifLogIt(stream_trace_opt,entrytype,index_entry_type))
        {
        applog.logDump(stream_trace_opt,
                       index_entry_type,
                       "writeubhdr",
                       msg.getubhdr(),
                       ubMsg.UBHDRLEN);
		}

    write(msg.getubhdr(), 0, ubMsg.UBHDRLEN);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private void writetlvbuf(ubMsg msg)
    throws IOException
    {
    byte[] tlvbuf = null;
    short tlvlen = 0;

    try
        {
        tlvbuf = msg.getubTlvBuf();
        tlvlen = msg.getubTlvBuflen();


        if ((applog != null) && applog.ifLogIt(stream_trace_opt,entrytype,index_entry_type))
           {
            applog.logDump(stream_trace_opt,
                           index_entry_type,
                           "writetlvbuf",
                           tlvbuf,
                           tlvlen);
		   }

        if (tlvlen > 0)
            write(tlvbuf, 0, tlvlen);
        }
    catch (ubMsg.MsgFormatException e)
        {

        /* this should only happen if the tvl structure is invalid */
        if (applog != null)
           {
           applog.logStackTrace(index_entry_type,
                                "getubTlvBuf() Exception in writetlvbuf : " +
                                 e.getMessage(),
                                e);
           }

        }
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private void writeMsgbuf(ubMsg msg)
    throws IOException
    {
    int buflen = msg.getBuflen();

    if ((applog != null) && applog.ifLogIt(stream_trace_opt,entrytype,index_entry_type))
        {
            applog.logDump(stream_trace_opt,
                           index_entry_type,
                           "writeMsgbuf[" + buflen + "]",
                           msg.getMsgbuf(), 
                           buflen);
        }

    write(msg.getMsgbuf(), 0, buflen);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public void close()
    throws IOException
    {
    try
        {
        super.close();
        }
    catch (SocketException se)
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
                                  "Closing output stream: " + se.getMessage() );
           }
        }
    }
/*********************************************************************/
/*                                                                   */
/*********************************************************************/


}  /* end of MsgOutputStream */


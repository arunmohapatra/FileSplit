
/*************************************************************/
/* Copyright (c) 1984-2004 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation. */
/*************************************************************/
/*
 * $Id: ubMsgTrace.java,v 1.16 1999/10/28 14:27:11 lecuyer Exp $
 */

/*********************************************************************/
/* Module : ubMsgTrace                                               */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.util;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.progress.common.ehnlog.IAppLogger;

/*********************************************************************/
/*                                                                   */
/* Class ubMsgTrace                                                  */
/*                                                                   */
/*********************************************************************/

public class ubMsgTrace
    implements ubConstants
{

/*********************************************************************/
/* embedded classes                                                  */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

    // public static class TraceRec
    private class TraceRec
        {

        String id;
        ubMsg msg;
        long  tsMsg;
        String src;
        String dest;

        public TraceRec(ubMsg msg, String id, String src, String dest)
            {
            this.id = id;
            this.msg = msg;
            this.src = src;
            this.dest = dest;
            tsMsg = System.currentTimeMillis();
            }



        public void print(IAppLogger log, int indexEntryType, int fmt)
            {

            String tsStr = fmttimestamp(tsMsg);

            String seqnumStr = (msg instanceof ubAppServerMsg) ?
                fmt4.format(((ubAppServerMsg) msg).getSeqnum()) : "";

            switch (fmt)
                {
                case FMT_VERBOSE:
                    msg.print("[" + tsStr + "] {" + id + "}" +
                              " " + msg.getubRqDesc() +
                              " " + seqnumStr + 
                              " " + src + " ---> " + dest,
                              IAppLogger.LOGGING_BASIC,
                              indexEntryType,
                              log);
                    break;

                case FMT_BASIC:
                    log.logBasic(  indexEntryType,
                                   "[" + tsStr + "] {" + id + "}" + 
                                   " " + msg.getubRqDesc() +
                                   " " + seqnumStr + 
                                   " " + src + " --->" +
                                   " " + dest
                                  );
                default:
                } 

            }
        }
/*********************************************************************/
/* Constants                                                         */
/*********************************************************************/

public static final int     FMT_TERSE              = 0;
public static final int     FMT_BASIC              = 0;
public static final int     FMT_VERBOSE            = 1;

public static final int     DEF_LIMIT              = 20;

/*********************************************************************/
/* Static Data                                                       */
/*********************************************************************/

private static DecimalFormat fmt2;
private static DecimalFormat fmt3;
private static DecimalFormat fmt4;
private static SimpleDateFormat tf;
private static DateFormat df;
private static NumberFormat nf;

/*********************************************************************/
/* Static initializer block                                          */
/*********************************************************************/

static
    {
    fmt2 = new DecimalFormat("00");
    fmt3 = new DecimalFormat("000");
    fmt4 = new DecimalFormat("0000");

    /* set up time format */
    tf   = new SimpleDateFormat("HH:mm:ss:SSS");
    tf.setTimeZone(TimeZone.getDefault());

    /* set up date format */
    df   = DateFormat.getDateInstance(DateFormat.MEDIUM,
                                      Locale.getDefault());
    nf = df.getNumberFormat();
    nf.setMinimumIntegerDigits(2);
    nf.setMaximumIntegerDigits(2);
    df.setNumberFormat(nf);
    }

/*********************************************************************/
/* Instance Data                                                     */
/*********************************************************************/

TraceRec[] trace;
int        limit;
int        next;

/*********************************************************************/
/* Constructors                                                      */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public ubMsgTrace(int limit)
    {
    this.limit = limit;
    trace = new TraceRec[limit];

    for (int i = 0; i < limit; i++)
        {
        trace[i] = null;
        }

    next = 0;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public ubMsgTrace()
    {
    this(DEF_LIMIT);
    }

/*********************************************************************/
/* Public Methods                                                    */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public void addMsg(ubMsg msg, String id, String src, String dest)
    {
    TraceRec rec = new TraceRec(msg, id, src, dest);
    trace[next] = rec;    
    next = advance_cursor(next);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public void print(IAppLogger log, int fmt, int indexEntryType, String msg)
    {
    int i;
    int cursor;

    log.logBasic(indexEntryType,msg);

    for (cursor = next, i = 0; i < limit; i++)
        {
        if (trace[cursor] != null)
            trace[cursor].print(log, indexEntryType, fmt);
        cursor = advance_cursor(cursor);
        }
    }

/**********************************************************************/
/* private methods                                                    */
/**********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private String fmttimestamp(long ts)
    {
    Date now = new Date(ts);

    String ret;

/*
    ret = (now == null) ?  "               "  :
                            df.format(now) + " " + tf.format(now);
*/

    ret = (now == null) ?  "               "  : tf.format(now);
    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private int advance_cursor(int cursor)
    {
    cursor = (cursor + 1) % limit;
    return cursor;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/


}  /* end of ubMsgTrace */


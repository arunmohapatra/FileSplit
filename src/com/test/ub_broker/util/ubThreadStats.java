
/*************************************************************/
/* Copyright (c) 1984-1996 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation. */
/*************************************************************/
/*
 * $Id: ubThreadStats.java,v 1.10 1999/08/17 11:35:10 lecuyer Exp $
 */

/*********************************************************************/
/* Module : ubThreadStats                                            */
/*                                                                   */
/*                                                                   */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.util;

import java.text.DecimalFormat;
import java.text.DateFormat;
import java.util.TimeZone;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.text.NumberFormat;
import java.net.InetAddress;

/*********************************************************************/
/*                                                                   */
/* Class ubThreadStats                                               */
/*                                                                   */
/*********************************************************************/

public class ubThreadStats
{

/*********************************************************************/
/* Constants                                                         */
/*********************************************************************/

public static final byte CONNSTATE_INIT             = 0x00;
public static final byte CONNSTATE_CONNECTING       = 0x01;
public static final byte CONNSTATE_CONNECTED        = 0x02;
public static final byte CONNSTATE_SENDING          = 0x03;
public static final byte CONNSTATE_RECEIVING        = 0x04;
public static final byte CONNSTATE_DISCONNECTING    = 0x05;
public static final byte CONNSTATE_DISCONNECTED     = 0x06;

public static final String[] DESC_CONNSTATE =
    {
    "INITIALIZING"
 ,  "CONNECTING"
 ,  "CONNECTED"
 ,  "SENDING"
 ,  "RECEIVING"
 ,  "DISCONNECTING"
 ,  "DISCONNECTED"
    };

/*********************************************************************/
/* Static Data                                                       */
/*********************************************************************/

static DecimalFormat fmt2;
static DecimalFormat fmt3;
static DecimalFormat fmt4;
static DecimalFormat fmt5;
static DecimalFormat fmt6;
static SimpleDateFormat tf;
static DateFormat df;
static NumberFormat nf;

private static Object           connHdlCntrLock;
private static int              connHdlCntr;

/*********************************************************************/
/* Static initializer block                                          */
/*********************************************************************/

static
    {
    fmt2 = new DecimalFormat("00");
    fmt3 = new DecimalFormat("000");
    fmt4 = new DecimalFormat("0000");
    fmt5 = new DecimalFormat("00000");
    fmt6 = new DecimalFormat("000000");

    /* set up time format */
    tf   = new SimpleDateFormat("HH:mm");
    tf.setTimeZone(TimeZone.getDefault());

    /* set up date format */
    df   = DateFormat.getDateInstance(DateFormat.MEDIUM,
                                      Locale.getDefault());
    nf = df.getNumberFormat();
    nf.setMinimumIntegerDigits(2);
    nf.setMaximumIntegerDigits(2);
    df.setNumberFormat(nf);

    connHdlCntrLock = new Object();
    connHdlCntr = 0;
    }

/*********************************************************************/
/* Instance Data                                                     */
/*********************************************************************/


/* usage statistics */

Date                      tsStatsReset;
String                    threadname;
int                       nRqs;
Date                      tsLastStateChg;
long                      tsLastSocketActivity;
int                       nRqMsgs;
int                       nRsps;
int                       nRspMsgs;
int                       nErrors;
Date                      tsLastError;
Date                      tsStartTime;
int                       enqueueWaits;
int                       maxQueueDepth;

int                       maxRqDuration;
long                      totRqDuration;
int                       maxRqWait;
long                      totRqWait;

/* listAppServerConnections fields */
int                       connHdl;
String                    connUserName;
InetAddress               connRmtHost;
int                       connRmtPort;
String                    connID;
int                       connRqs;
int                       connServerPID;
int                       connServerPort;


/*********************************************************************/
/* Static methods                                                    */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public static String getConnStateDesc(int connState)
    {
    String s = DESC_CONNSTATE[connState];
    return s;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private static int newConnHdl()
    {
    int ret;

    synchronized(connHdlCntrLock)
        {
        ret = ++connHdlCntr;
        }

    return ret;
    }

/*********************************************************************/
/* Constructors                                                      */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public ubThreadStats()
    {
    this.threadname = new String("");
    resetStats();
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public ubThreadStats(String threadname)
    {
    this.threadname = new String(threadname);
    resetStats();
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public ubThreadStats(ubThreadStats src)
    {
    this.tsStatsReset = src.tsStatsReset;
    this.threadname = new String(src.getThreadname());
    this.nRqs = src.getnRqs();
    this.tsLastStateChg = src.tsLastStateChg;
    this.tsLastSocketActivity = src.gettsLastSocketActivity();
    this.nRqMsgs = src.getnRqMsgs();
    this.nRsps = src.getnRsps();
    this.nRspMsgs = src.getnRspMsgs();
    this.nErrors = src.getnErrors();
    this.tsLastError = src.tsLastError;
    this.tsStartTime = src.tsStartTime;
    this.maxQueueDepth = src.getMaxQueueDepth();
    this.enqueueWaits = src.getEnqueueWaits();
    this.maxRqDuration = src.maxRqDuration;
    this.totRqDuration = src.totRqDuration;
    this.maxRqWait  = src.maxRqWait;
    this.totRqWait  = src.totRqWait;
    this.connHdl  = src.connHdl;
    this.connRmtHost = src.connRmtHost;
    this.connRmtPort = src.connRmtPort;
    this.connUserName = src.connUserName;
    this.connID = src.connID;
    this.connRqs = src.getConnRqs();
    this.connServerPID = src.connServerPID;
    this.connServerPort = src.connServerPort;
    }

/*********************************************************************/
/* public methods                                                    */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public String getThreadname()
    {
    return new String(threadname);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized Date gettsStatsReset()
    {
    return tsStatsReset;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized void settsStatsReset(Date ts)
    {
    tsStatsReset = ts;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized void settsStatsReset()
    {
    tsStatsReset = new Date();
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int getnRqs()
    {
    return nRqs;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized String getFmtnRqs()
    {
    String s = fmt6.format(nRqs);
    return s;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int incrnRqs()
    {
    return ++nRqs;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized Date gettsLastStateChg()
    {
    return tsLastStateChg;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized String getFmtLastStateChg()
    {
    String s = fmttimestamp(tsLastStateChg);
    return s;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized void settsLastStateChg(Date ts)
    {
    tsLastStateChg = ts;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized void settsLastStateChg()
    {
    tsLastStateChg = new Date();
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized long gettsLastSocketActivity()
    {
    return tsLastSocketActivity;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized void settsLastSocketActivity(long ts)
    {
    tsLastSocketActivity = ts;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized void settsLastSocketActivity()
    {
    tsLastSocketActivity = System.currentTimeMillis();
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int getnRqMsgs()
    {
    return nRqMsgs;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized String getFmtnRqMsgs()
    {
    String s = fmt6.format(nRqMsgs);
    return s;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int incrnRqMsgs()
    {
    return ++nRqMsgs;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int getnRsps()
    {
    return nRsps;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized String getFmtnRsps()
    {
    String s = fmt6.format(nRsps);
    return s;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int incrnRsps()
    {
    return ++nRsps;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int getnRspMsgs()
    {
    return nRspMsgs;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized String getFmtnRspMsgs()
    {
    String s = fmt6.format(nRspMsgs);
    return s;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int incrnRspMsgs()
    {
    return ++nRspMsgs;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int getnErrors()
    {
    return nErrors;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized String getFmtnErrors()
    {
    String s = fmt6.format(nErrors);
    return s;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int incrnErrors()
    {
    return ++nErrors;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized Date gettsLastError()
    {
    return tsLastError;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized String getFmtLastError()
    {
    String s = fmttimestamp(tsLastError);
    return s;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized void settsLastError(Date ts)
    {
    tsLastError = ts;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized void settsLastError()
    {
    tsLastError = new Date();
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized Date gettsStartTime()
    {
    return tsStartTime;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized String getFmtStartTime()
    {
    String s = fmttimestamp(tsStartTime);
    return s;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized void settsStartTime(Date ts)
    {
    tsStartTime = ts;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized void settsStartTime()
    {
    tsStartTime = new Date();
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int getEnqueueWaits()
    {
    return enqueueWaits;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized String getFmtEnqueueWaits()
    {
    String s = fmt6.format(enqueueWaits);
    return s;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int setEnqueueWaits(int val)
    {
    int ret = enqueueWaits;
    enqueueWaits = val;
    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int getMaxQueueDepth()
    {
    return maxQueueDepth;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized String getFmtMaxQueueDepth()
    {
    String s = fmt6.format(maxQueueDepth);
    return s;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int setMaxQueueDepth(int val)
    {
    int ret = maxQueueDepth;
    maxQueueDepth = val;
    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int getMaxRqDuration()
    {
    return maxRqDuration;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized String getFmtMaxRqDuration()
    {
    String s = fmt6.format(maxRqDuration);
    return s;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized long getTotRqDuration()
    {
    return totRqDuration;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized String getFmtTotRqDuration()
    {
    String s = fmt6.format(totRqDuration);
    return s;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized void incTotRqDuration(int rqDuration)
    {
    /* update max, tot stats */

    if (maxRqDuration < rqDuration)
        maxRqDuration = rqDuration;

    /* increment total so later we can compute an average */
    this.totRqDuration += rqDuration;
    }    

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int getMaxRqWait()
    {
    return maxRqWait;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized String getFmtMaxRqWait()
    {
    String s = fmt6.format(maxRqWait);
    return s;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized long getTotRqWait()
    {
    return totRqWait;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized String getFmtTotRqWait()
    {
    String s = fmt6.format(totRqWait);
    return s;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized void incTotRqWait(int rqWait)
    {
    /* update max, tot stats */

    if (maxRqWait < rqWait)
        maxRqWait = rqWait;

    /* increment total so later we can compute an average */
    totRqWait += rqWait;
    }    


/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int getConnHdl()
    {
    return connHdl;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized String getFmtConnHdl()
    {
    String s = fmt6.format(connHdl);
    return s;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int setConnHdl()
    {
    int ret = connHdl;
    connHdl = newConnHdl();
    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized String getConnUserName()
    {
    return connUserName;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized String getFmtConnUserName()
    {
    return connUserName;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized String setConnUserName(String val)
    {
    String ret = connUserName;
    connUserName = val;
    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized String getConnRmtHost()
    {
    return (connRmtHost == null) ? null : connRmtHost.getHostAddress();
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized String getFmtConnRmtHost()
    {
    return (connRmtHost == null) ? null : connRmtHost.getHostAddress();
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized String setConnRmtHost(InetAddress val)
    {
    String ret = (connRmtHost == null) ? null : connRmtHost.getHostAddress();
    connRmtHost = val;
    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int getConnRmtPort()
    {
    return connRmtPort;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized String getFmtConnRmtPort()
    {
    String s = fmt6.format(connRmtPort);
    return s;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int setConnRmtPort(int val)
    {
    int ret = connRmtPort;
    connRmtPort = val;
    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized String getConnID()
    {
    return connID;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized String getFmtConnID()
    {
    return connID;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized String setConnID(String val)
    {
    String ret = connID;
    connID = val;
    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int getConnRqs()
    {
    return connRqs;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized String getFmtConnRqs()
    {
    String s = fmt6.format(connRqs);
    return s;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int setConnRqs(int val)
    {
    int ret = connRqs;
    connRqs = val;
    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int incrConnRqs(int val)
    {
    int ret = connRqs;
    connRqs += val;
    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int getConnServerPID()
    {
    return connServerPID;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized String getFmtConnServerPID()
    {
    String s = fmt6.format(connServerPID);
    return s;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int setConnServerPID(int val)
    {
    int ret = connServerPID;
    connServerPID = val;
    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int getConnServerPort()
    {
    return connServerPort;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized String getFmtConnServerPort()
    {
    String s = fmt6.format(connServerPort);
    return s;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int setConnServerPort(int val)
    {
    int ret = connServerPort;
    connServerPort = val;
    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/*********************************************************************/
/* private methods                                                   */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private String fmttimestamp(Date now)
    {
    String ret;

    ret = (now == null) ?  "               "  :
                            df.format(now) + " " + tf.format(now);

    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private /* synchronized */ void resetStats()
    {
    tsStatsReset = new Date();
    nRqs = 0;
    tsLastStateChg = null;
    tsLastSocketActivity = System.currentTimeMillis();
    nRqMsgs = 0;
    nRsps = 0;
    nRspMsgs = 0;
    nErrors = 0;
    tsLastError = null;
    tsStartTime = null;
    enqueueWaits = 0;
    maxQueueDepth = 0;
    maxRqDuration = 0;
    totRqDuration = 0;
    maxRqWait     = 0;
    totRqWait     = 0;

    connHdl = 0;
    connUserName = null;
    connRmtHost = null;
    connRmtPort = 0;
    connID = null;
    connRqs = 0;
    connServerPID = 0;
    connServerPort = 0;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/



} /* class ubThreadStats */

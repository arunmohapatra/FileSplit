
/*************************************************************/
/* Copyright (c) 1984-2009 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission in writing from Progress Software Corporation. */
/*************************************************************/

/*********************************************************************/
/* Module : Logger                                                   */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.util;

import java.io.*;
import java.util.Date;
import java.util.TimeZone;
import java.text.DecimalFormat;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.text.NumberFormat;

/* common imports */
import com.progress.common.exception.*;
import com.progress.common.ehnlog.AppLogger;

/*********************************************************************/
/*                                                                   */
/* Class Logger                                                      */
/*                                                                   */
/*********************************************************************/

public class Logger
    implements ubConstants
{

/*********************************************************************/
/* Constants                                                         */
/*********************************************************************/

public static final boolean NOTIMESTAMP = false;
public static final boolean TIMESTAMP = true;

public static final int LOGGING_MASK = 0x0F;

public static final int LOGGING_OFF = 0;
public static final int LOGGING_ALL = 0;
public static final int LOGGING_ERRORS = 1;
public static final int LOGGING_TERSE = 2;
public static final int LOGGING_VERBOSE = 3;
public static final int LOGGING_STATS = 4;
public static final int LOGGING_OPT = 4;
public static final int LOGGING_DEBUG = 5;
public static final int LOGGING_TRACE = 6;
public static final int LOGGING_POLL = 7;

/* options for logging at LOGGING_OPT level */

public static final int LOGOPT_MASK                  = 0xFFFFFFF0;

public static final int LOGOPT_NONE                  = 0x00000000;
public static final int LOGOPT_DEBUG                 = 0x00000010;
public static final int LOGOPT_TRACE                 = 0x00000020;
public static final int LOGOPT_CLIENTFSM             = 0x00000040;
public static final int LOGOPT_SERVERFSM             = 0x00000080;
public static final int LOGOPT_TERSE                 = 0x00000100;
public static final int LOGOPT_CLIENTMSGSTREAM       = 0x00000200;
public static final int LOGOPT_SERVERMSGSTREAM       = 0x00000400;
public static final int LOGOPT_XXX                   = 0x00000800;
public static final int LOGOPT_CLIENTMSGQUEUE        = 0x00001000;
public static final int LOGOPT_SERVERMSGQUEUE        = 0x00002000;
public static final int LOGOPT_CLIENTMEMTRACE        = 0x00004000;
public static final int LOGOPT_SERVERMEMTRACE        = 0x00008000;
public static final int LOGOPT_THREADPOOL            = 0x00010000;
public static final int LOGOPT_STATS                 = 0x00020000;

public static final int DEST_NONE = 0;
public static final int DEST_DISPLAY = 1;
public static final int DEST_LOGFILE = 2;
public static final int DEST_BOTH = (DEST_DISPLAY | DEST_LOGFILE);

public static final int TERSE_MSGLEN = 64;

private static final String hexdigits = "0123456789ABCDEF";

public static final long  DEF_COMPONENTSUBSYSTEMID = 0x0000000000000001;
public static final int   DEF_SYSTEMNAMEID         = 0;

public static final String  DEF_COMPID_LIT         = "Logger";
public static final String  DEF_SUBSYS_LIT         = "---";
public static final String  DEF_REQID_LIT          = "";

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

private PrintWriter pw;
private String fid;
private int logging_level;
private int logopt;
private boolean bAppend;
private AppLogger ehnlog;

private long  componentSubSystemID = DEF_COMPONENTSUBSYSTEMID;
private int   systemNameID         = DEF_SYSTEMNAMEID;


/*********************************************************************/
/* Constructors                                                      */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public Logger()
    {
    logging_level = LOGGING_OFF;
    logopt = LOGOPT_NONE;
    pw = null;
    fid = null;
    ehnlog = null;
    bAppend = false;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public Logger(int lvl)
    {
    logging_level = (lvl & LOGGING_MASK);
    logopt = (lvl & LOGOPT_MASK);
/*
    System.out.println("Constructor: lvl= " + logging_level +
                       " logopt= " + logopt);
*/
    pw = null;
    this.fid = null;
    ehnlog = null;
    bAppend = false;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public Logger(String fid, int lvl)
    throws IOException
    {
    logging_level = (lvl & LOGGING_MASK);
    logopt = (lvl & LOGOPT_MASK);
/*
    System.out.println("Constructor: lvl= " + logging_level +
                       " logopt= " + logopt);
*/
    if ((lvl > LOGGING_OFF) && (fid != null))
        {
        pw = new PrintWriter(new BufferedWriter(new FileWriter(fid)));
        this.fid = new String(fid);
        logMsgln(DEST_LOGFILE, TIMESTAMP, true, fid + " opened.");
        }
    else
        {
        pw = null;
        this.fid = null;
        }
    ehnlog = null;
    bAppend = false;
    if (fid != null)
        this.fid = new String(fid);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public Logger(String fid, int lvl, boolean fAppend)
    throws IOException
    {
    logging_level = (lvl & LOGGING_MASK);
    logopt = (lvl & LOGOPT_MASK);

/*
    System.out.println("Constructor: lvl= " + logging_level +
                       " logopt= " + logopt);
*/

    if ((lvl > LOGGING_OFF) && (fid != null))
        {
        pw = new PrintWriter(new BufferedWriter(new FileWriter(fid, fAppend)));
        this.fid = new String(fid);
        if (fAppend)
            logMsgln(DEST_LOGFILE,
                     NOTIMESTAMP,
                     true, 
                     "===================================" +
                     "===================================");
        logMsgln(DEST_LOGFILE,
                 TIMESTAMP,
                 true, 
                 fid + " opened.");
        }
    else
        {
        pw = null;
        this.fid = null;
        }
    ehnlog = null;

    this.bAppend = fAppend;
    if (fid != null)
        this.fid = new String(fid);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public Logger(AppLogger ehnlog)
    {
    this.ehnlog = ehnlog;

    /* SUB_M_UBROKER  =      0x0000000200000000L */
    componentSubSystemID = 0x0000000200000000L;
    /* SUB_V_UBROKER  = 33 */
    systemNameID = 33;

    logging_level = ehnlog.getLoggingLevel();

    /* compensate for funky way that this is handled in Logger : FIX THIS */
    logopt = (int) ehnlog.getLogEntries() << 4;

    pw = null;
    this.fid = null;
    bAppend = false;
    }


/*********************************************************************/
/* Public Methods                                                    */
/*********************************************************************/

/**********************************************************************/
/* Name: LogMsg                                                       */
/*       Outputs a message to the Logfile                             */
/**********************************************************************/

public void LogMsg(int dest, int lvl, boolean fTimestamp, String msg)
    {
    int msglvl = (lvl & LOGGING_MASK);
    
/*
    System.out.println("LogMsg: lvl= " + lvl + 
                       " msglvl= " + msglvl +
                       " logopt= " + logopt +
                       " opt= " + (lvl & logopt) );
*/

    if (logging_level >= msglvl )
        {
        if ( (msglvl != LOGGING_OPT) || ((lvl & logopt) > 0))
            logMsg(dest, fTimestamp, msg);
        }
    }

/**********************************************************************/
/* Name: LogMsgln                                                     */
/*       Outputs a message to the Logfile                             */
/**********************************************************************/

public void LogMsgln(int dest, int lvl, boolean fTimestamp, String msg)
    {
    int msglvl = (lvl & LOGGING_MASK);

/*
    System.out.println("LogMsgln: lvl= " + lvl + 
                       " msglvl= " + msglvl +
                       " logopt= " + logopt +
                       " opt= " + (lvl & logopt) );
*/

    if (logging_level >= msglvl )
        {
        if ( (msglvl != LOGGING_OPT) || ((lvl & logopt) > 0))
            logMsgln(dest, fTimestamp, true, msg);
        }
    }

/**********************************************************************/
/* Name: LogMsgN                                                      */
/*       Outputs a message to the Logfile                             */
/**********************************************************************/

/* the first call to this method MUST be proceeded by a call to  */
/* initialize the promsgs subsystem.                             */

public void LogMsgN(int dest, int lvl, boolean fTimestamp,
                    long msgid, Object[] parms)
    {
    int msglvl = (lvl & LOGGING_MASK);

/*
    System.out.println("LogMsgN: lvl= " + lvl + 
                       " msglvl= " + msglvl +
                       " logopt= " + logopt +
                       " opt= " + (lvl & logopt) );
*/

    if (logging_level >= msglvl )
        {
        if ( (msglvl != LOGGING_OPT) || ((lvl & logopt) > 0))
            logMsgN(dest, fTimestamp, true, msgid, parms);
        }
    }


/**********************************************************************/
/* Name : LogDump                                                     */
/*        Dumps memory area to Logfile in hex                         */
/**********************************************************************/

public void LogDump(int dest,
                    int lvl,
                    boolean fTimestamp,
                    String msg,
            byte[] pbData,
                    int cbData)
    {
    int msglvl = (lvl & LOGGING_MASK);

/*
    System.out.println("LogDump: lvl= " + lvl + 
                       " msglvl= " + msglvl +
                       " logopt= " + logopt +
                       " opt= " + (lvl & logopt) );
*/

    if (logging_level >= msglvl )
        {
        if ( (msglvl != LOGGING_OPT) || ((lvl & logopt) > 0))
            logDump(dest, fTimestamp, msg, pbData, cbData);
        }
    }

/**********************************************************************/
/* Name: LogStackTrace                                                */
/*       Outputs a stack trace to the logfile                         */
/**********************************************************************/

public void LogStackTrace(int dest, int lvl, boolean fTimestamp,
                          String msg, Throwable e)
    {
    int msglvl = (lvl & LOGGING_MASK);

/*
    System.out.println("LogStackTrace: lvl= " + lvl + 
                       " msglvl= " + msglvl +
                       " logopt= " + logopt +
                       " opt= " + (lvl & logopt) );
*/

    if (logging_level >= msglvl )
        {
        if ( (msglvl != LOGGING_OPT) || ((lvl & logopt) > 0))
            logStackTrace(dest, fTimestamp, msg, e);
        }
    }

/**********************************************************************/
/* Name: LogStackTrace                                                */
/*       Outputs a stack trace to the logfile                         */
/**********************************************************************/

public void LogStackTraceN(int dest, int lvl, boolean fTimestamp,
                           long msgid, Object[] parms, Throwable e)
    {
    int msglvl = (lvl & LOGGING_MASK);

/*
    System.out.println("LogStackTraceN: lvl= " + lvl + 
                       " msglvl= " + msglvl +
                       " logopt= " + logopt +
                       " opt= " + (lvl & logopt) );
*/

    if (logging_level >= msglvl )
        {
        if ( (msglvl != LOGGING_OPT) || ((lvl & logopt) > 0))
            logStackTraceN(dest, fTimestamp, msgid, parms, e);
        }
    }

/**********************************************************************/
/* Name: setLoggingLevel */
/*       Set a new logging level */
/**********************************************************************/
    public void setLoggingLevel(int newLevel)
    {
        int         setLevel = newLevel;
        if (setLevel < LOGGING_OFF)
        {
            setLevel = LOGGING_OFF;
        }
        if (setLevel > LOGGING_POLL)
        {
            setLevel = LOGGING_POLL;
        }    
        logging_level = setLevel;    

        try
        {
            if ((setLevel > LOGGING_OFF) && (fid != null) && (pw == null))
            {
                pw = new PrintWriter(new BufferedWriter(new FileWriter(fid, bAppend)));
                if (bAppend)
                    logMsgln(DEST_LOGFILE,
                             NOTIMESTAMP,
                             true, 
                             "===================================" +
                             "===================================");
                logMsgln(DEST_LOGFILE,
                         TIMESTAMP,
                         true, 
                         fid + " opened.");
            }
        }
        catch (IOException e)
        {
        }

        if (setLevel > LOGGING_OFF)
            logMsgln(DEST_LOGFILE,
                     TIMESTAMP,
                     true, 
                     "Logging Level set to = " + setLevel);
    }

/**********************************************************************/
/* Name: ignore                                                       */
/*       Checks to see if a given logging level will generate a entry */
/**********************************************************************/

public boolean ignore(int lvl)
    {
    boolean ret;
    int msglvl = (lvl & LOGGING_MASK);
/*
    System.out.println("ignore: lvl= " + lvl + 
                       " mask= " + (lvl & LOGGING_MASK) );
*/

/*
    ret = ( (logging_level >= msglvl ) && 
            ( (msglvl != LOGGING_OPT) || ((lvl & logopt) > 0)) );
    return (!ret);
*/

    return (logging_level < (lvl & LOGGING_MASK) );
    }


/**********************************************************************/
/* Name: isOptSet                                                     */
/*       Checks to see if a given logging option is set               */
/**********************************************************************/

public boolean isOptSet(int optmask)
    {
    boolean ret;
    ret = ( optmask & logopt ) > 0;
    return (ret);
    }


/**********************************************************************/
/* Name: CloseLogfile                                                 */
/*       Closes the Logfile                                           */
/**********************************************************************/

public synchronized void CloseLogfile()
    {
    if (pw != null)
        {
        logMsgln(DEST_LOGFILE, TIMESTAMP, true, "Log Closed");
        pw.close();
        }
    }

/**********************************************************************/
/* private methods                                                    */
/**********************************************************************/

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private String timestamp(Date now)
    {
    return "(" + df.format(now) + " " + tf.format(now) + ")";
    }

/**********************************************************************/
/* Name: logMsg                                                       */
/**********************************************************************/

private synchronized void logMsg(int dest, boolean fTimestamp, String msg)
    {
    Date now = new Date();

    if (ehnlog == null)
        {
        if ((dest & DEST_DISPLAY) != 0)
            {
            if (fTimestamp)
                System.err.print(timestamp(now) + " " );
            System.err.print(msg);
            }

        if ((pw != null)  && ((dest & DEST_LOGFILE) != 0) )
            {
            if (fTimestamp)
            pw.print (timestamp(now) + " " );

            pw.print(msg);
            if (pw.checkError())
                {
                pw.close();
                pw = null;
                }
            }
        }
    else   /* write in enhanced log format */
        {
        ehnwrite(dest, msg);
        }
    }

/**********************************************************************/
/* Name: logMsgln                                                     */
/**********************************************************************/

private synchronized void logMsgln(int dest,
                                   boolean fTimestamp,
                                   boolean fPrintThreadId,
                                   String msg)
    {
    Date now = new Date();

    if (ehnlog == null)
        {
        if ((dest & DEST_DISPLAY) != 0)
            {
            if (fPrintThreadId)
                System.err.print(Thread.currentThread().getName() + ">");
            if (fTimestamp)
                System.err.print(timestamp(now) + " " );
            System.err.println(msg);
            }

        if ((pw != null) && ((dest & DEST_LOGFILE) != 0))
            {
            if (fPrintThreadId)
                pw.print (Thread.currentThread().getName() + ">");
            if (fTimestamp)
                pw.print(timestamp(now) + " " );

            pw.println(msg);
            if (pw.checkError())
                {
                pw.close();
                pw = null;
                }
            }
        }
    else   /* write in enhanced log format */
        {
        ehnwrite(dest, msg);
        }
    }

/**********************************************************************/
/* Name: logMsgN                                                      */
/**********************************************************************/

private synchronized void logMsgN(int dest,
                                  boolean fTimestamp,
                                  boolean fPrintThreadId,
                                  long msgid,
                                  Object[] parms)
    {
    String msg;
    
/*
        logMsgln(dest, fTimestamp, fPrintThreadId, "msgid= 0x" +
                  Long.toHexString(msgid) );
        for (int i = 0; i < parms.length; i++)
            logMsgln(dest, fTimestamp, fPrintThreadId,
                     "parm[" + i + "]= (" + parms[i].toString() + ")");
*/
    /* first, decode the message string */

    msg = ExceptionMessageAdapter.getMessage(msgid, parms);

    /* now, do something with it ! */

    logMsgln(dest, fTimestamp, fPrintThreadId, msg);
    }


/**********************************************************************/
/* Name : logDump                                                 */
/**********************************************************************/

private synchronized void logDump(int dest,
                                  boolean fTimestamp,
                                  String msg,
                                  byte[] pbData,
                                  int cbData)
    {
    int         i;
    int         n;
    char[]      tmp;
    StringBuffer    txt;
    StringBuffer    ln;

    if (ehnlog == null)
        {
        logMsgln(dest, fTimestamp, true, msg);

        if (pbData == null)
            {
            logMsgln(dest, NOTIMESTAMP, /* false */ true, "<<NULL pointer>>");
            return;
            }

        txt = new StringBuffer(17);
        ln  = new StringBuffer(256);
        tmp = new char[3];
        tmp[0] = ' ';

        for (i=0; i < cbData; i++)
            {
            if ((n = (i % 16)) == 0)
            {
            if (txt.length() > 0)
                logMsgln(dest, NOTIMESTAMP, /* false */ true,
                            ln.toString() + " :" + txt.toString());
            txt.setLength(0);
            ln.setLength(0);
            ln.append(fmt4.format(i) + ":");
            }

            tmp[1] = hexdigits.charAt((pbData[i] >> 4) & 0x0F);
            tmp[2] = hexdigits.charAt(pbData[i] & 0x0F);

            ln.append(tmp);
            txt.append(((pbData[i] >= 0x20) && (pbData[i] < 0x7F))
                            ? (char)pbData[i] : '.');
            }

        if ((n = txt.length()) > 0)
            {
            for (; n < 16; n++)
            ln.append("   ");
            logMsgln(dest, NOTIMESTAMP, /* false */ true,
                        ln.toString() + " :" + txt.toString());
            }
        else logMsgln(dest, NOTIMESTAMP, false, "");
        }
    else
        {
        /* MAJOR HACK ALERT !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  */
        /* the following code ASSUMES that the only time a Logger object  */
        /* is instantiated using an EhnLog object is for the WSA project  */
        /* this is/was true at the time the code was written.  However    */
        /* this may become an invalid assumption in the future, so BEWARE */

        String compID;
        String subsysID;
        String reqID;

            compID = ehnlog.getExecEnvId();

            /* m_subsystemNames.set(SUB_V_UBROKER , "uBroker-Client  "); */
            subsysID = "uBroker-Client  ";
            reqID = ehnlog.getLogContext().getMsgHdr();

        ehnlog.ehnLogDump(
             dest,
             logging_level,
             compID,
             subsysID,
             reqID + msg,
             pbData,
             cbData);
        }
    }

/**********************************************************************/
/* Name: logStackTrace                                                */
/**********************************************************************/

private synchronized void logStackTrace(int dest, boolean fTimestamp,
                                         String msg, Throwable e)
    {
    Date now = new Date();

    if ((dest & DEST_DISPLAY) != 0)
        {
        System.err.print(Thread.currentThread().getName() + ">");
        if (fTimestamp)
            System.err.print(timestamp(now) + " " );
        System.err.println(msg);
        e.printStackTrace();
        }

    if ((pw != null) && ((dest & DEST_LOGFILE) != 0))
        {
        pw.print (Thread.currentThread().getName() + ">");
        if (fTimestamp)
            pw.print(timestamp(now) + " " );

        pw.println(msg);
        e.printStackTrace(pw);
        if (pw.checkError())
            {
            pw.close();
            pw = null;
            }
        }
    }

/**********************************************************************/
/* Name: logStackTraceN                                               */
/**********************************************************************/

private synchronized void logStackTraceN(int dest, boolean fTimestamp,
                                         long msgid, Object[] parms,
                                         Throwable e)
    {
    String msg;
    
    /* first, decode the message string */

    msg = ExceptionMessageAdapter.getMessage(msgid, parms);

    /* now, do something with it ! */

    logStackTrace(dest, fTimestamp, msg, e);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private void ehnwrite(int dest, String msg)
    {

        switch(logging_level)
            {
            case LOGGING_OFF:
                break;
            case LOGGING_ERRORS:
                ehnlog.logError(msg);
                break;
            case LOGGING_TERSE:
                ehnlog.logBasic(systemNameID, msg);
                break;
            case LOGGING_VERBOSE:
                ehnlog.logVerbose(systemNameID, msg);
                break;
            case LOGGING_DEBUG:
            case LOGGING_TRACE:
            case LOGGING_POLL:
                ehnlog.logExtended(systemNameID, msg);
                break;
            default:
                break;
            }
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/


}  /* end of Logger */


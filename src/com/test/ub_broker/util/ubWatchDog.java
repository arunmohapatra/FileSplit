
/*************************************************************/
/* Copyright (c) 1984-2009 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation. */
/*************************************************************/

/*********************************************************************/
/* Module : ubWatchDog                                               */
/*                                                                   */
/*                                                                   */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.util;

/* common imports */
import com.progress.common.ehnlog.IAppLogger;
import com.progress.message.jbMsg;

/*********************************************************************/
/*                                                                   */
/* Class ubWatchDog                                                  */
/*                                                                   */
/*********************************************************************/

public class ubWatchDog
    extends Thread
{

/*********************************************************************/
/* Constants                                                         */
/*********************************************************************/

public static final int DEF_PRIORITY           = Thread.NORM_PRIORITY + 1;
public static final int DEF_INTERVAL           = 60000;  /* one minute */

/*********************************************************************/
/* Static Data                                                       */
/*********************************************************************/

/*********************************************************************/
/* Instance Data                                                     */
/*********************************************************************/

String     threadname;
IWatchable watcher;
long       interval;
int        priority;
Logger     log;
IAppLogger applog;

/*********************************************************************/
/* Constructors                                                      */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* TODO: remove this constructor when the Logger class is removed */

public ubWatchDog(String     threadname,
                  IWatchable watcher,
                  long       interval,
                  int        priority,
                  Logger     log)
    {
    this.threadname = new String(threadname);
    this.watcher    = watcher;
    this.interval   = interval;
    this.priority   = priority;
    this.log        = log;
    this.applog     = null;

    /* set threadname */
    setName(threadname); 

    /* make this a daemon thread */
    setDaemon(true);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public ubWatchDog(String     threadname,
                  IWatchable watcher,
                  long       interval,
                  int        priority,
                  IAppLogger log)
    {
    this.threadname = new String(threadname);
    this.watcher    = watcher;
    this.interval   = interval;
    this.priority   = priority;
    this.applog     = log;
    this.log        = null;

    /* set threadname */
    setName(threadname); 

    /* make this a daemon thread */
    setDaemon(true);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public ubWatchDog()
    {
    this("", null, DEF_INTERVAL, DEF_PRIORITY, new Logger());
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public ubWatchDog(String threadname, IWatchable watcher, long interval)
    {
    this(threadname, watcher, interval, DEF_PRIORITY, new Logger());
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/*********************************************************************/
/* public methods                                                    */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public void run()
    {
    Thread current = Thread.currentThread();

    current.setPriority(priority);
    while (interval > 0)
        {
        try
            {
            Thread.sleep(interval);
            watcher.watchEvent();
            }
        catch (Exception e)
            {
            /* exit if watchdog is being turned off */
            if (interval == 0)
                return;

            /* "sleeping error " + e + " : " + e.getMessage() */
            if (log != null)
            {
            log.LogMsgN(
                Logger.DEST_LOGFILE,
                Logger.LOGGING_ERRORS,
                Logger.TIMESTAMP,
                jbMsg.jbMSG033,
                new Object[] { e.toString() ,
                               e.getMessage() }
                );
            }
            else if (applog != null)
            { 
            applog.logError(jbMsg.jbMSG033,
                            new Object[] { e.toString() ,
                            e.getMessage() }  );
            }

            return;
            }
        }
    }

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

public synchronized long getInterval()
    {
    return interval;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized void setInterval(long interval)
    {
    this.interval = interval;
    if (interval == 0)
	interrupt();
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



} /* class ubWatchDog */

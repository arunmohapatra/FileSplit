
/*************************************************************/
/* Copyright (c) 1984-2002 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission in writing from Progress Software Corporation  */
/*************************************************************/

/*********************************************************************/
/* Module : WatchDog                                                 */
/*                                                                   */
/*                                                                   */
/*                                                                   */
/*********************************************************************/

package com.progress.common.util;

/* common imports */
import com.progress.common.ehnlog.AppLogger;
import com.progress.common.ehnlog.IAppLogger;

/*********************************************************************/
/*                                                                   */
/* Class WatchDog                                                    */
/*                                                                   */
/*********************************************************************/

public class WatchDog
    extends Thread
{

/*********************************************************************/
/* Constants                                                         */
/*********************************************************************/

public static final int DEF_PRIORITY           = Thread.NORM_PRIORITY + 1;

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
IAppLogger log;

/*********************************************************************/
/* Constructors                                                      */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public WatchDog(String     threadname,
                IWatchable watcher,
                long       interval,
                int        priority,
                IAppLogger log)
    {
    this.threadname = new String(threadname);
    this.watcher    = watcher;
    this.interval   = interval;
    this.priority   = priority;
    this.log        = log;

    /* set threadname */
    setName(threadname); 

    /* make this a daemon thread */
    setDaemon(true);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public WatchDog(String     threadname,
                IWatchable watcher,
                long       interval)
    {
    this(threadname, watcher, interval, DEF_PRIORITY, new AppLogger());
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public WatchDog(String     threadname,
                IWatchable watcher,
                long       interval,
                IAppLogger log)
    {
    this(threadname, watcher, interval, DEF_PRIORITY, log);
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

    log.ehnLogWrite(
             IAppLogger.DEST_LOGFILE,
             IAppLogger.LOGGING_EXTENDED,
             "watchdog",
             "start",
             "watchdog thread " + threadname + " started." );

    while (interval > 0)
        {
        try
            {
            Thread.sleep(interval);
            watcher.watchEvent();
            }
        catch (Exception e)
            {
            log.ehnLogWrite(
                     IAppLogger.DEST_LOGFILE,
                     IAppLogger.LOGGING_EXTENDED,
                     "watchdog",
                     "end",
                     "watchdog thread " + threadname + 
                     " exception= " + e  );

            /* force us out of the loop */
            interval = 0;
            }
        }

    log.ehnLogWrite(
             IAppLogger.DEST_LOGFILE,
             IAppLogger.LOGGING_EXTENDED,
             "watchdog",
             "end",
             "watchdog thread " + threadname + " done." );
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



} /* class WatchDog */


/*************************************************************/
/* Copyright (c) 1984-2004 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation. */
/*************************************************************/
/*
 * $Id: Queue.java,v 1.11 1999/01/08 14:48:36 lecuyer Exp $
 */

/*********************************************************************/
/* Module : Queue                                                    */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.util;

/* common imports */
import com.progress.common.ehnlog.IAppLogger;
import com.progress.common.ehnlog.UBrokerLogContext;
import com.progress.common.exception.ProException;
import com.progress.message.jbMsg;

/*********************************************************************/
/*                                                                   */
/* Class Queue                                                       */
/*                                                                   */
/*********************************************************************/

public class Queue
    extends List
{

/*********************************************************************/
/* embedded classes                                                  */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

    public static class QueueException extends ProException
    {
        public QueueException(String detail)
        {
            super("Queue", new Object[] { detail } );
        };


        public String getDetail()
        {
            return (String)getArgument(0);
        }
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

    public static class QueueClosedException extends QueueException
    {
        public QueueClosedException(String detail)
        {
            super("QueueClosed[" + detail + "]");
        };


        public String getDetail()
        {
            return (String)getArgument(0);
        }
    }

/*********************************************************************/
/* Constants                                                         */
/*********************************************************************/

public static final int NOQUEUELIMIT = 0;    /* unlimited */
public static final int DEF_QUEUELIMIT = NOQUEUELIMIT;

/* queue states */
static final byte STATE_READY   = 0x00;
static final byte STATE_CLOSING = 0x01;
static final byte STATE_CLOSED  = 0x02;

static final String[] DESC_STATE =
  {
  " STATE_READY "
, " STATE_CLOSING "
, " STATE_CLOSED "
  };

/*********************************************************************/
/* Static Data                                                       */
/*********************************************************************/

/*********************************************************************/
/* Instance Data                                                     */
/*********************************************************************/

byte queueState;
int maxQueueDepth;
int enqueueWaits;
int currentQueueDepth;
int queueLimit;

/*********************************************************************/
/* Constructors                                                      */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* TODO REMOVE THIS LATER  - used in the javafrom4gl layer */

public Queue(String queueName, int queueLimit, Logger log)
    {
    super(queueName, log);
    maxQueueDepth = 0;
    currentQueueDepth = 0;
    enqueueWaits = 0;
    this.queueLimit = queueLimit;
    queueState = STATE_READY;
    }

public Queue(String queueName, int queueLimit, IAppLogger log)
    {
    super(queueName, log);
    maxQueueDepth = 0;
    currentQueueDepth = 0;
    enqueueWaits = 0;
    this.queueLimit = queueLimit;
    queueState = STATE_READY;
    }


/*********************************************************************/
/* Public Methods                                                    */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public /* synchronized */ void enqueue( Object o )
    throws QueueException
    {
    synchronized(this)
        {
        enqueueObject(o, false);
        }
    yieldControl();
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized void enqueuePriority_old( Object o )
    throws QueueException
    {
    enqueueObject(o, true);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public /* synchronized */ void enqueuePriority( Object o )
    throws QueueException
    {
    synchronized(this)
        {
        enqueueObject(o, true);
        }

    yieldControl();
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized Object dequeue()
    {
    Object obj;
    boolean wasFull = isFull();

    if ((applog != null) && 
        applog.ifLogVerbose(UBrokerLogContext.SUB_M_UB_DEBUG,
                           UBrokerLogContext.SUB_V_UB_DEBUG))
        applog.logVerbose(
            UBrokerLogContext.SUB_V_UB_DEBUG,
            "start dequeue() from (" +
            super.getListName() + ")  " + System.currentTimeMillis() + " depth= " + currentQueueDepth
            );

    while (isEmpty())
        {
        try
            {
/*
            if ((applog != null) && 
                applog.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,
                                  UBrokerLogContext.SUB_V_UB_DEBUG))
                applog.logBasic(
                    UBrokerLogContext.SUB_V_UB_DEBUG,
                    "dequeue() from (" +
                    super.getListName() + ") : start wait() : wasFull= " +
                    wasFull + "  isEmpty() = " + isEmpty());
*/

            wait();

/*

            if ((applog != null) && 
                applog.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,
                                  UBrokerLogContext.SUB_V_UB_DEBUG))
                applog.logBasic(
                    UBrokerLogContext.SUB_V_UB_DEBUG,
                    "dequeue() from (" +
                    super.getListName() + ") : end wait() : wasFull= " +
                    wasFull + "  isFull() = " + isFull() + 
                    "  isEmpty() = " + isEmpty());

            if (wasFull != isFull())
                {
                if ((applog != null) && 
                    applog.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,
                                      UBrokerLogContext.SUB_V_UB_DEBUG))
                    applog.logBasic(
                        UBrokerLogContext.SUB_V_UB_DEBUG,
                        "dequeue() from (" +
                        super.getListName() + ") : end wait() : wasFull= " +
                        wasFull + "  isFull() = " + isFull() + 
                        " : ERROR = wasFull != isFull()");
                }
*/

            /* check to see if the queue was filled since the last time we ran */
            wasFull = isFull();
            }
        catch (InterruptedException e)
            {
            if (log != null)
                {
                log.LogMsgln(Logger.DEST_LOGFILE,
                             Logger.LOGGING_DEBUG, Logger.NOTIMESTAMP,
                             "dequeue " + super.getListName() +
                             " InterruptedExeception " + e.getMessage());
                 }

/*
            if ((applog != null) && 
                applog.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,
                                  UBrokerLogContext.SUB_V_UB_DEBUG))
                {
                applog.logBasic(UBrokerLogContext.SUB_V_UB_DEBUG,
                                "dequeue() from (" + super.getListName() +
                                ") InterruptedExeception " + e.getMessage());
                }
*/

            if (applog != null)
                {
                applog.logError("dequeue() from (" + super.getListName() +
                                ") InterruptedExeception " + e.getMessage());
                }

            /* we should check to see if we're being stopped !!! */
            }
        }

    try
        {
        obj = removeFromFront();
        }
    catch (EmptyListException el)
        {
            /* this shouldn't be possible since */
            /* we're checking the list before   */
            /* attempting to remove the first   */
            /* object, but what the heck ...    */

        /* dequeue %s<queueName> : 
           EmptyListException %s<EmptyListExceptionMsg>.  */

        if (log != null)
            {
            log.LogMsgN(Logger.DEST_LOGFILE,
                        Logger.LOGGING_ERRORS,
                        Logger.TIMESTAMP,
                        jbMsg.jbMSG079,
                        new Object[] { super.getListName(),
                                       el.getMessage()       }
                       );
             }

        if (applog != null)
            {
            applog.logError(jbMsg.jbMSG079,
                            new Object[] { super.getListName(),
                                           el.getMessage()       }
                           );
            }

        obj = null;
        currentQueueDepth = 0;
        }

    currentQueueDepth--;

    if ((applog != null) && 
        applog.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,
                          UBrokerLogContext.SUB_V_UB_DEBUG))
        applog.logBasic(UBrokerLogContext.SUB_V_UB_DEBUG,
                     "dequeue " + System.currentTimeMillis() + "  " + currentQueueDepth);

    if (wasFull)
        {
/*
        if ((applog != null) && 
            applog.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,
                              UBrokerLogContext.SUB_V_UB_DEBUG))
            applog.logBasic(UBrokerLogContext.SUB_V_UB_DEBUG,
                                "dequeue(" + obj + ") from (" +
                                super.getListName() + 
                                ") : notifyAll() : wasFull= " +
                                wasFull);
*/

        notifyAll();
        }

    if ((applog != null) && 
        applog.ifLogVerbose(UBrokerLogContext.SUB_M_UB_DEBUG,
                          UBrokerLogContext.SUB_V_UB_DEBUG))
        applog.logVerbose(UBrokerLogContext.SUB_V_UB_DEBUG,
                            "end   dequeue(" + obj + ") from (" +
                            super.getListName() + ")  " + System.currentTimeMillis() + " depth= " +
                            currentQueueDepth);

    updateQueueState();

    return obj;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized Object poll(long timeout)
    {
    Object obj;
    boolean wasFull = isFull();

    if ((applog != null) && 
        applog.ifLogVerbose(UBrokerLogContext.SUB_M_UB_DEBUG,
                          UBrokerLogContext.SUB_V_UB_DEBUG))
        applog.logVerbose(
            UBrokerLogContext.SUB_V_UB_DEBUG,
            "start poll(" + timeout + ") from (" +
            super.getListName() + ")  " + System.currentTimeMillis() + " depth= " + currentQueueDepth);

    if (isEmpty())
        {
        try
            {
/*
            if ((applog != null) && 
                applog.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,
                                  UBrokerLogContext.SUB_V_UB_DEBUG))
                applog.logBasic(
                    UBrokerLogContext.SUB_V_UB_DEBUG,
                    "poll(" + timeout + ") from (" +
                    super.getListName() + ") : start wait() : wasFull= " +
                    wasFull + "  isEmpty() = " + isEmpty());
*/

            wait(timeout);

/*
            if ((applog != null) && 
                applog.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,
                                  UBrokerLogContext.SUB_V_UB_DEBUG))
                applog.logBasic(
                    UBrokerLogContext.SUB_V_UB_DEBUG,
                    "poll(" + timeout + ") from (" +
                    super.getListName() + ") : end wait() : wasFull= " +
                    wasFull + "  isFull() = " + isFull() + 
                    "  isEmpty() = " + isEmpty());

            if (wasFull != isFull())
                {
                if ((applog != null) && 
                    applog.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,
                                      UBrokerLogContext.SUB_V_UB_DEBUG))
                    applog.logBasic(
                        UBrokerLogContext.SUB_V_UB_DEBUG,
                        "poll(" + timeout + ") from (" +
                        super.getListName() + ") : end wait() : wasFull= " +
                        wasFull + "  isFull() = " + isFull() + 
                        " : ERROR = wasFull != isFull()");
                }
*/

            /* check to see if the queue was filled since the last time we ran */
            wasFull = isFull();
            }
        catch (InterruptedException e)
            {
/*
            if (log != null)
                {
                log.LogMsgln(Logger.DEST_LOGFILE,
                             Logger.LOGGING_DEBUG, Logger.NOTIMESTAMP,
                             "poll " + getListName() +
                            " InterruptedExeception " + e.getMessage());
                }

            if ((applog != null) && 
                applog.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,
                                  UBrokerLogContext.SUB_V_UB_DEBUG))
                {
                applog.logBasic(UBrokerLogContext.SUB_V_UB_DEBUG,
                                "poll(" + timeout + ") from (" + 
                                 super.getListName() +
                                ") InterruptedExeception " + e.getMessage());
                }
*/

            if (applog != null)
                {
                applog.logError( "poll(" + timeout + ") from (" + 
                                 super.getListName() +
                                ") InterruptedExeception " + e.getMessage());
                }

            /* we should check to see if we're being stopped !!! */
            }
        }

    if (isEmpty())
        {
        updateQueueState();
        return null;
        }

    try
        {
        obj = removeFromFront();
        }
    catch (EmptyListException el)
        {
            /* this shouldn't be possible since */
            /* we're checking the list before   */
            /* attempting to remove the first   */
            /* object, but what the heck ...    */
/*
        if (log != null)
            log.LogMsgln(Logger.DEST_LOGFILE,
                         Logger.LOGGING_DEBUG, Logger.NOTIMESTAMP,
                         "dequeue " + super.getListName() +
                         " EmptyListException " + el.getMessage());

        if ((applog != null) && applog.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,UBrokerLogContext.SUB_V_UB_DEBUG))
            applog.logBasic(UBrokerLogContext.SUB_V_UB_DEBUG,
                            "dequeue " + super.getListName() +
                            " EmptyListException " + el.getMessage());
*/
        if (applog != null)
            applog.logError( "dequeue " + super.getListName() +
                            " EmptyListException " + el.getMessage());
        obj = null;
        currentQueueDepth = 0;
        }

    currentQueueDepth--;

    if ((applog != null) && 
        applog.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,
                          UBrokerLogContext.SUB_V_UB_DEBUG))
        applog.logBasic(UBrokerLogContext.SUB_V_UB_DEBUG,
                     "poll " + System.currentTimeMillis() + "  " + currentQueueDepth);

    if (wasFull)
        {
/*
        if ((applog != null) && 
            applog.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,
                              UBrokerLogContext.SUB_V_UB_DEBUG))
            applog.logBasic(UBrokerLogContext.SUB_V_UB_DEBUG,
                                "poll(" + timeout + ") from (" +
                                super.getListName() + 
                                ") : notifyAll() : wasFull= " +
                                wasFull);
*/

        notifyAll();
        }

/*
    if ((log != null) && !log.ignore(Logger.LOGGING_DEBUG))
        {
        log.LogMsgln(Logger.DEST_LOGFILE,
                     Logger.LOGGING_DEBUG, Logger.NOTIMESTAMP,
                    "poll() currentQueueDepth=" + currentQueueDepth);
        }
*/
 
    if ((applog != null) && 
        applog.ifLogVerbose(UBrokerLogContext.SUB_M_UB_DEBUG,
                          UBrokerLogContext.SUB_V_UB_DEBUG))
        applog.logVerbose(UBrokerLogContext.SUB_V_UB_DEBUG,
                            "end   poll(" + timeout + ") from (" +
                            super.getListName() + ")  " + System.currentTimeMillis() + " depth= " +
                            currentQueueDepth);

    updateQueueState();

    return obj;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized Object removeFromQueue(Object deleteItem)
    {
    Object ret;
    try
        {
        ret = removeFromList(deleteItem);
        if (ret != null)
            {
            currentQueueDepth--;
            
        if ((applog != null) && 
            applog.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,
                              UBrokerLogContext.SUB_V_UB_DEBUG))
            applog.logBasic(UBrokerLogContext.SUB_V_UB_DEBUG,
                         "remove " + System.currentTimeMillis() + "  " + currentQueueDepth);

            }
        else
            {
            if (applog != null)
                applog.logError("removeFromQueue " + super.getListName() +
                                " returned null");
            }
        }
    catch (EmptyListException e)
        {
        if (applog != null)
            applog.logError("removeFromQueue " + super.getListName() +
                                " EmptyListException " + e);
        ret = null;
        }

    updateQueueState();

    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized boolean isEmpty()
    {
    return super.isEmpty();
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized boolean isFull()
    {
    return (queueLimit != NOQUEUELIMIT) && (currentQueueDepth >= queueLimit);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* TODO REMOVE THIS METHOD LATER - when contructor with Logger is removed */

public void print(Logger log, int dest, int lvl)
    {
    if (log.ignore(lvl))
        return;

    super.print(log, dest, lvl);

    log.LogMsgln(dest, lvl, Logger.NOTIMESTAMP,
                 " maxQueueDepth=" + maxQueueDepth);

    log.LogMsgln(dest, lvl, Logger.NOTIMESTAMP,
                 " enqueueWaits=" + enqueueWaits);

    log.LogMsgln(dest, lvl, Logger.NOTIMESTAMP,
                 " currentQueueDepth=" + currentQueueDepth);

    log.LogMsgln(dest, lvl, Logger.NOTIMESTAMP,
                 " queueLimit=" + queueLimit);

    }


public void print(IAppLogger log, int lvl, int indexEntryType)
    {

    super.print(log, lvl, indexEntryType);

    log.logWithThisLevel(lvl, indexEntryType,
                 " maxQueueDepth=" + maxQueueDepth);

    log.logWithThisLevel(lvl, indexEntryType,
                 " enqueueWaits=" + enqueueWaits);

    log.logWithThisLevel(lvl, indexEntryType,
                 " currentQueueDepth=" + currentQueueDepth);

    log.logWithThisLevel(lvl, indexEntryType,
                 " queueLimit=" + queueLimit);

    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized void setQueueLimit(int limit)
    {
    queueLimit = limit;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int getQueueLimit()
    {
    return queueLimit;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int getQueueDepth()
    {
    return currentQueueDepth;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int resetMaxQueueDepth()
    {
    int ret = maxQueueDepth;
    maxQueueDepth = 0;
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

public synchronized int resetEnqueueWaits()
    {
    int ret = enqueueWaits;
    enqueueWaits = 0;
    return ret;
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

public synchronized void close()
    {
    queueState = (isEmpty()) ? STATE_CLOSED : STATE_CLOSING;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/*********************************************************************/
/* Private Methods                                                   */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* this method MUST only be called from a synchronized public method */

private void enqueueObject( Object o , boolean fPriority)
    throws QueueException
    {
    boolean wasEmpty = isEmpty();

    if ((applog != null) && 
         applog.ifLogVerbose(UBrokerLogContext.SUB_M_UB_DEBUG,
                            UBrokerLogContext.SUB_V_UB_DEBUG))
        applog.logVerbose(UBrokerLogContext.SUB_V_UB_DEBUG,
                            "start enqueueObject (" + o + ") to (" +
                            super.getListName() + ")  " + System.currentTimeMillis() + " depth= " + currentQueueDepth );

    if (queueState != STATE_READY)
        {
        if (log != null)
            log.LogMsgln(Logger.DEST_LOGFILE,
                         Logger.LOGGING_DEBUG, Logger.NOTIMESTAMP,
                         "enqueueObject (" + o + ") to (" +
                         super.getListName() + ") failed : queue closed.");

        if ((applog != null) && 
             applog.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,
                               UBrokerLogContext.SUB_V_UB_DEBUG))
            applog.logBasic(UBrokerLogContext.SUB_V_UB_DEBUG,
                            "enqueueObject (" + o + ") to (" +
                            super.getListName() + ") failed : queue closed.");

        throw new QueueClosedException("enqueue failed : " + o);
        }

    if (isFull())
        enqueueWaits++;

    while (isFull())
        {
        try
            {
/*
            if ((applog != null) && 
                 applog.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,
                                   UBrokerLogContext.SUB_V_UB_DEBUG))
                applog.logBasic(UBrokerLogContext.SUB_V_UB_DEBUG,
                        "enqueueObject (" + o + ") to (" +
                        super.getListName() + ") : start wait() : isFull() = " +
                        isFull());
*/

            wait();

/*
            if ((applog != null) && 
                 applog.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,
                                   UBrokerLogContext.SUB_V_UB_DEBUG))
                applog.logBasic(UBrokerLogContext.SUB_V_UB_DEBUG,
                        "enqueueObject (" + o + ") to (" +
                        super.getListName() + ") : end wait() : isFull() = " +
                            isFull() + "  wasEmpty= " + wasEmpty);

            if (wasEmpty != isEmpty())
                {
                if ((applog != null) && 
                    applog.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,
                                      UBrokerLogContext.SUB_V_UB_DEBUG))
                    applog.logBasic(
                        UBrokerLogContext.SUB_V_UB_DEBUG,
                        "enqueueObject(" + o + ") to (" +
                        super.getListName() + ") : end wait() : wasEmpty= " +
                        wasEmpty + "  isEmpty() = " + isEmpty() + 
                        " : ERROR - wasEmpty != isEmpty()");
                }
*/

            /* check to see if the queue may have been emptied since the last time we ran */
            wasEmpty = isEmpty();
            }
        catch (InterruptedException e)
            {
/*
            if (log != null)
                {
                log.LogMsgln(Logger.DEST_LOGFILE,
                             Logger.LOGGING_DEBUG, Logger.NOTIMESTAMP,
                             "enqueueObject(" + o + ") to (" +
                              super.getListName() +
                             ")  InterruptedExeception " + e.getMessage());
                 }

            if ((applog != null) && 
                applog.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,
                                  UBrokerLogContext.SUB_V_UB_DEBUG))
                applog.logBasic(UBrokerLogContext.SUB_V_UB_DEBUG,
                             "enqueueObject(" + o + ") to (" +
                              super.getListName() +
                             ")  InterruptedExeception " + e.getMessage());
*/


            if (applog != null)
                applog.logError("enqueueObject(" + o + ") to (" +
                              super.getListName() +
                             ")  InterruptedExeception " + e.getMessage());

            /* we should check to see if we're being stopped !!! */
            }
        }

    if (fPriority)
        insertAtFront( o );
    else insertAtBack( o );

    currentQueueDepth++;

/*
    if ((log != null) && !log.ignore(Logger.LOGGING_DEBUG))
        {
        log.LogMsgln(Logger.DEST_LOGFILE,
                     Logger.LOGGING_DEBUG, Logger.NOTIMESTAMP,
                     "enqueueObject() currentQueueDepth=" + currentQueueDepth);
        }
*/

    if ((applog != null) && 
        applog.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,
                          UBrokerLogContext.SUB_V_UB_DEBUG))
        applog.logBasic(UBrokerLogContext.SUB_V_UB_DEBUG,
                     "enqueue " + System.currentTimeMillis() + "  " + currentQueueDepth);

    if (currentQueueDepth > maxQueueDepth)
        maxQueueDepth = currentQueueDepth;

    if (wasEmpty)
        {
/*
        if ((applog != null) &&
            applog.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,
                              UBrokerLogContext.SUB_V_UB_DEBUG))
            applog.logBasic(UBrokerLogContext.SUB_V_UB_DEBUG,
                            "enqueueObject (" + o + ") to (" +
                            super.getListName() +
                            ") : notifyAll() : wasEmpty= " + wasEmpty);
*/
        notifyAll();
        }

    if ((applog != null) && 
        applog.ifLogVerbose(UBrokerLogContext.SUB_M_UB_DEBUG,
                          UBrokerLogContext.SUB_V_UB_DEBUG))
        applog.logVerbose(UBrokerLogContext.SUB_V_UB_DEBUG,
                            "end   enqueueObject (" + o + ") to (" +
                            super.getListName() + ")  " + System.currentTimeMillis() + " depth= " +
                            currentQueueDepth);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private boolean yieldControl()
    {
    boolean ret = true;

    Thread.yield();

    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private void updateQueueState()
    {
    if ((queueState == STATE_CLOSING) && isEmpty())
        queueState = STATE_CLOSED;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

}  /* end of Queue */




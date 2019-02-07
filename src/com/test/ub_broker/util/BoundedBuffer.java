
/*************************************************************/
/* Copyright (c) 1984-1996 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation. */
/*************************************************************/
/*
 * $Id: BoundedBuffer.java,v 1.1 1998/06/23 15:40:37 lecuyer Exp $
 */

/*********************************************************************/
/* Module : BoundedBuffer                                            */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.util;

/*********************************************************************/
/*                                                                   */
/* Class BoundedBuffer                                               */
/*                                                                   */
/*********************************************************************/

public final class BoundedBuffer
    implements IBoundedBuffer 
{
/*********************************************************************/
/* Constants                                                         */
/*********************************************************************/

public static final int DEF_QUEUELIMIT = 256;

/*********************************************************************/
/* Static Data                                                       */
/*********************************************************************/

/*********************************************************************/
/* Instance Data                                                     */
/*********************************************************************/

private Object[]  array_;      // the elements
private int putPtr_ = 0;       // circular indices
private int takePtr_ = 0;     
private int emptySlots_;       // slot counts
private int usedSlots_ = 0;   
private int waitingPuts_ = 0;  // counts of waiting threads
private int waitingTakes_ = 0;
private Object putMonitor_ = new Object(); // waiting threads
private Object takeMonitor_ = new Object(); 

/*********************************************************************/
/* Constructors                                                      */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public BoundedBuffer(int capacity) 
   throws IllegalArgumentException
    {
    if (capacity <= 0)
        throw new IllegalArgumentException();
    array_ = new Object[capacity];
    emptySlots_ = capacity;
    }

/*********************************************************************/
/* Public Methods                                                    */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

  public int count() { return usedSlots_; }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

  public int capacity() { return array_.length; }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public void put(Object x)
    { 
    synchronized(putMonitor_)
        { // specialized exchange code
        while (emptySlots_ <= 0)
            {
            ++waitingPuts_;
            try
                {
                putMonitor_.wait();
                } 
            catch(InterruptedException ex)
                {
                }; 
            --waitingPuts_;
            }
        --emptySlots_;
        array_[putPtr_] = x;
        putPtr_ = (putPtr_ + 1) % array_.length;
        }

    synchronized(takeMonitor_)
        { // directly notify
        ++usedSlots_;
        if (waitingTakes_ > 0) 
            takeMonitor_.notify();
        }

    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public Object take()
    { // symmetric to put 
    Object old = null; // return value

    synchronized(takeMonitor_)
        { 
        while (usedSlots_ <= 0)
            {
            ++waitingTakes_;
            try
                {
                takeMonitor_.wait();
                } 
            catch(InterruptedException ex)
                {
                }; 
            --waitingTakes_;
            }
        --usedSlots_;
        old = array_[takePtr_];
        array_[takePtr_] = null;
        takePtr_ = (takePtr_ + 1) % array_.length;
        }

    synchronized(putMonitor_)
        {
        ++emptySlots_;
        if (waitingPuts_ > 0)
            putMonitor_.notify();
        }

    return old;
  }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

}    /* BoundedBuffer */


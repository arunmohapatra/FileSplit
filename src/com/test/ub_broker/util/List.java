
/*************************************************************/
/* Copyright (c) 1984-2004 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation. */
/*************************************************************/
/*
 * $Id: List.java,v 1.7 1999/12/07 16:50:39 lecuyer Exp $
 */

/*********************************************************************/
/* Module : List                                                     */
/*                                                                   */
/* List and ListNode definition                                      */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.util;

import com.progress.common.ehnlog.IAppLogger;
import com.progress.common.exception.ProException;

/*********************************************************************/
/*                                                                   */
/* Class ListNode                                                    */
/*                                                                   */
/*********************************************************************/

class ListNode
{
/*********************************************************************/
/* Instance Data                                                     */
/*********************************************************************/

Object data;
ListNode next;

/*********************************************************************/
/* Constructors                                                      */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* Constructor: Create a ListNode that refers to Object o. */
ListNode( Object o )
    {
    this( o, null );
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* Constructor: Create a ListNode that refers to Object o and */
/* to the next ListNode in the List. */

ListNode( Object o, ListNode nextNode )
    {
    data = o;         /* this node refers to Object o */
    next = nextNode;  /* set next to refer to next */
    }


/*********************************************************************/
/* public methods                                                    */
/*********************************************************************/

/* Return the Object in this node */
Object getObject()
    {
    return data;
    }

/* Return the next node */
ListNode getnext()
    {
    return next;
    }

} /* end of ListNode class */


/*********************************************************************/
/*                                                                   */
/* Class List                                                        */
/*                                                                   */
/*********************************************************************/

public class List
{
/*********************************************************************/
/* embedded classes                                                  */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

    public static class EmptyListException extends ProException
    {
        public EmptyListException(String detail)
        {
            super("EmptyListException", new Object[] { detail } );
        };


        public String getDetail()
        {
            return (String)getArgument(0);
        }
    }

/*********************************************************************/
/* Instance Data                                                     */
/*********************************************************************/

private ListNode firstNode;
private ListNode lastNode;
private String name;  /* String like "list" used in printing */
Logger  log; /* allow subclasses to access this */
IAppLogger  applog; /* allow subclasses to access this */

private int listsize;


/*********************************************************************/
/* Constructors                                                      */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/


/*********************************************************************/
/* NOTE: This was only left here because it is used by the javafrom4gl
   classes. Once the work is done on that layer, this constructor should
   be removed
   DO NOT USE THIS CONSTRUCTOR !!!!!!
***********************************************************************/
public List(String s, Logger log)
    {
    name = s;
    firstNode = lastNode = null;
    this.log = log;
    this.applog = null;
    listsize = 0;
    }


/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public List(String s, IAppLogger log)
    {
    name = s;
    firstNode = lastNode = null;
    this.applog = log;
    this.log = null;
    listsize = 0;
    }

/*********************************************************************/
/* Public methods                                                    */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* get the list name */
public String toString()
    {
    /* return new String(name); */
    return name;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* Insert an Object at the front of the List              */
/* If List is empty, firstNode and lastNode refer to      */
/* same Object. Otherwise, firstNode refers to new node.  */

public synchronized void insertAtFront( Object insertItem )
    {
    if ( isEmpty() )
        firstNode = lastNode = new ListNode( insertItem );
    else firstNode = new ListNode( insertItem, firstNode );

    /* increment the list size */
    listsize++;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/


/* Insert an Object at the end of the List                */
/* If List is empty, firstNode and lastNode refer to      */
/* same Object. Otherwise, lastNode's next instance       */
/* variable refers to new node.                           */

public synchronized void insertAtBack( Object insertItem )
    {
    if ( isEmpty() )
        firstNode = lastNode = new ListNode( insertItem );
    else lastNode = lastNode.next = new ListNode( insertItem );

    /* increment the list size */
    listsize++;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/


/* Delete an Object in the list                           */

public synchronized Object removeFromList(Object deleteItem)
          throws EmptyListException
    {
    ListNode n;
    Object ret;

    if (isEmpty())
        throw new EmptyListException( name );

    if ( deleteItem == null)
        return null;

    if (firstNode.data == deleteItem)
        return removeFromFront();

    /* find a reference to previous node */
    for (n = firstNode;
            (n != lastNode) && (n.next.data != deleteItem);
	        n = n.next)
	;

    /* if we get to the last node it's not here */
    if (n == lastNode)
	return null;


    /* return the data for the node being removed */
    /* this should be the same as the target!     */

    ret = n.next.data;

    /* check to see if we're removing the last entry */
    if (n.next == lastNode)
        lastNode = n;

    /* remove it from the list */
    n.next = n.next.next;

    /* decrement the list size */
    listsize--;

    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* Remove the first node from the List. */

public synchronized Object removeFromFront()
          throws EmptyListException
    {
    Object removeItem = null;

    if ( isEmpty() )
        throw new EmptyListException( name );

    removeItem = firstNode.data;  /* retrieve the data */

    /* reset the firstNode and lastNode references */
    if ( firstNode.equals( lastNode ) )
        firstNode = lastNode = null;
    else firstNode = firstNode.next;

    /* decrement the list size */
    listsize--;

    return removeItem;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* Remove the last node from the List. */
public synchronized Object removeFromBack()
          throws EmptyListException
    {
    Object removeItem = null;

    if ( isEmpty() )
        throw new EmptyListException( name );

    removeItem = lastNode.data;  // retrieve the data

    /* reset the firstNode and lastNode references */
    if ( firstNode.equals( lastNode ) )
        firstNode = lastNode = null;
    else
        {
        ListNode current = firstNode;

        while ( current.next != lastNode )
            current = current.next;

        lastNode = current;
        current.next = null;
        }

    /* decrement the list size */
    listsize--;

    return removeItem;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* Return true if the List is empty */
public boolean isEmpty()
    {
    return firstNode == null;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized Object findFirst()
    {
    return isEmpty() ? null : firstNode.data;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized Object findNext(Object target)
    {
    ListNode n;

    if (isEmpty() || (target == null))
        return null;

    for (n = firstNode;
            (n != null) && (n.data != target);
	        n = n.next)
	;

    if (n == null)
        return null;

    n = n.next;

    return (n == null) ? null : n.data;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized Object findPrev(Object target)
    {
    ListNode n;

    if (isEmpty() || (target == null))
        return null;

    for (n = firstNode;
            (n != lastNode) && (n.next.data != target);
	        n = n.next)
	;

    return (n == lastNode) ? null : n.data;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized Object findLast()
    {
    return isEmpty() ? null : lastNode.data;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* this method searches the list to find the specified object */
/* NOTE:  "equality" in this case means that the references   */
/*        are equal (i.e. point to the same object)           */

public boolean inList(Object target)
    {
    ListNode node;

    if (isEmpty() || (target == null))
        return false;

    for (node = firstNode;
            (node != null) && (node.data != target);
	        node = node.next)
	;

    return (node != null);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* Output the List contents */
public synchronized void print(Logger log, int dest, int lvl)
    {
    if ( isEmpty() )
        {
        log.LogMsgln(dest, lvl, Logger.NOTIMESTAMP,
                     "List " + name + " is empty");
        return;
        }

    log.LogMsgln(dest, lvl, Logger.NOTIMESTAMP,
                 "List " + name + " is :");

    ListNode current = firstNode;

    while ( current != null )
        {
        log.LogMsgln(dest, lvl, Logger.NOTIMESTAMP,
                     current.data.toString() + " : " );

        current = current.next;
        }
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* Output the List contents */
public synchronized void print(IAppLogger log, int lvl, int indexEntryType)
    {
    if ( isEmpty() )
        {
        log.logWithThisLevel(lvl, indexEntryType,
                     "List " + name + " is empty");
        return;
        }

    log.logWithThisLevel(lvl, indexEntryType,
                         "List " + name + " is :");

    ListNode current = firstNode;

    while ( current != null )
        {
        log.logWithThisLevel(lvl, indexEntryType,
                             current.data.toString() + " : " );

        current = current.next;
        }
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* get the list name */
public String getListName()
    {
    /* return new String(name); */
    return name;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* get the list size */
public int size()
    {
    return listsize;
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

} /* end of List class */


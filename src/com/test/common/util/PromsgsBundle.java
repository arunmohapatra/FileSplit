/*************************************************************/
/* Copyright (c) 1984-1998 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation. */
/*************************************************************/
/*
 * $Id: PromsgsBundle.java,v 1.1 1998/07/17 15:59:14 wilner Exp $
 */

/*********************************************************************/
/* Class : PromsgsBundle.java                                                  */
/*                                                                   */
/*                                                                   */
/*                                                                   */
/*********************************************************************/
package com.progress.common.util;

import com.progress.common.message.IProMessage;
import com.progress.common.message.IProMessage.ProMessageException;

import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.Vector;


public class PromsgsBundle implements IProMessage
{

    public PromsgsBundle()
    {
        // Instantiate a resource bundle vector list.  We assume that there will be no more than 3, but
        // if there are we will let the vector take care of it.
        rbList = new Vector(3);
        
    }
    
    // Get a message.  The message id must be a valid one from an interface file
    // in com.progress.message.xxMsg or it won't work.
    // 
    // 19991216-055 protect rbList - use synchronized access
    //
    public synchronized String getMessage(long messageId) throws ProMessageException
    {

        String         facility = getFacility(messageId);
        String         fullName = "com.progress.message." + facility;
        int            id       = getId(messageId);
        
        int            i;
        RbEntry        r = null;
        ResourceBundle b;
        String         ret;

        
        // Go through the list of known facilities and figure out if this is a 
        // known facility.

        for (i = 0; i<rbList.size(); i++)
        {
            r = (RbEntry)rbList.elementAt(i);
            
            if (facility.equals(r.getFacility()))
            {
                break;
            }
        }

        try
        {
        
            if (i == rbList.size())
            {
                // If we when through the complete list of facilities and didn't find it
                // then we haven't open this bundle yet so open it here.

                // Get the resource bundle name. By convention they are stored in
                // com.progress.message.xxBundle.  If the location changes then
                // this code needs to change.

                // Note the resource bundle list contains the unqualified resource bundle
                // name while the resource bundle needs to be open using the fully qualified
                // resource bundle  name.


                b = ResourceBundle.getBundle(fullName);

                rbList.addElement(new RbEntry(facility,b));
            }
            else
            {
                b = r.getBundle();
            }
        
            ret = b.getString(String.valueOf(id));
        }
        catch (MissingResourceException e)
        {
            throw new ProMessageException("Unknown Message ID = " + 
                                          fullName + " " +
                                          id);
        }

        return ret;
    }

    // Returns the facility portion of the messageID.

    private String getFacility(long messageId)
    {
        char[] facility = new char[2];
        String ret;
        StringBuffer buf = new StringBuffer();
        
        // Note the facility code is assumed to be the 2 high-order
        // bytes in the message id obtained from xxmsg.  This facility
        // code will be used to find the right resource bundle.

        facility[0] = (char)(byte)(messageId >>> 56);
        facility[1] = (char)(byte)(messageId >>> 48);

        // By convention the bundle name is xxBundle.  That is what java
        // expects. If the name or this code changes then most likely this
        // code will no longer work.
 
        buf.append(facility);
        buf.append("Bundle");
        

        return buf.toString();
    }

    // Returns the message id portion to the messageID

    private int getId(long messageId)
    {
        return (int)(messageId);
    }

    
    // Manages the list of already opened resource bundles

    private class RbEntry
    {
        public RbEntry(String f,ResourceBundle b)
        {
            facility = f;
            bundle   = b;
        }

        public String getFacility()
        {
            return facility;
        }
        
        public ResourceBundle getBundle()
        {
            return bundle;
        }
        
        private String         facility;
        private ResourceBundle bundle;
        
        
    }
    

    private Vector rbList;
    
}














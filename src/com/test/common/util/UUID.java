//**************************************************************
//  Copyright (c) 1997 - 1998 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************
//
//
// UUID
//
//

/**
*	This class contains method to generate a globally unique id.
*   This id is unique across time and space within a single computer
*   network.  The implementation is currently based on the RMI VMID class.
*
*
*	@author		Ken Wilner
*	@version	1.0
*	@since		SkyWalker Beta 1
*   @see java.rmi.dgc.VMID
*/

package com.progress.common.util;

import java.rmi.dgc.VMID;
import java.lang.Thread;

/**
* Constructs a UUID Object.
*/

public final class UUID
{
   public UUID()
   {
       v = new VMID();
   }

    /**
     * Compare this UUID to another, and return true if they are the
     * same identifier.
     * @param obj the UUID to compare to this UUID.
     * @return true if the UUID's are equal; Otherwise, returns false.
     */

    public boolean equals(Object obj)
    {
       return v.equals(obj);
    }

    /**
      * Compute the hashcode for this UUID.
      * @return the hashcode value
      */

    public int hashCode()
    {
        return v.hashCode();
    }

    /**
     * Returns the string representation of this UUID.
     * @return the string representation of the UUID.
     */

    public String toString()
    {
        return v.toString();
    }

    /**
     * When run as an application, this class writes the string representation
     * of a UUID to System.out. This application takes no statup arguments.
     */



    public static void main(String[] args)
    {
        System.out.println(new UUID());

    }

    /** The RMI VMID used to generate the UUID. */

    private VMID v;
}

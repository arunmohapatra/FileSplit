
/*************************************************************/
/* Copyright (c) 1984-1996 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission in writing from Progress Software Corporation. */
/*************************************************************/

/*********************************************************************/
/* Module : CompactUUID                                              */
/*                                                                   */
/*                                                                   */
/*                                                                   */
/*********************************************************************/

/**
*   This class generates a globally unique id.
*   Thie id uses the standard java.util.uuid class and
*   then encodes it using the Base64 class and truncates it to 22 bytes.     
*   This produces a compact globally unique id suitable for use as
*   a request id or context id.
*/

package com.progress.common.util;

import java.util.UUID;

import com.progress.common.util.Base64;

public class CompactUUID
{
/*********************************************************************/
/* Constants                                                         */
/*********************************************************************/

/*********************************************************************/
/* Instance Data                                                     */
/*********************************************************************/

   private UUID m_uuid;

/*********************************************************************/
/* Constructors                                                      */
/*********************************************************************/

public CompactUUID()
    {
    m_uuid = UUID.randomUUID();
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public String toString()
    {
    byte[] uuid;
    String ret;

    uuid = this.toByteArray();
    ret = Base64.encode(uuid);

    return (ret.length() > 22) ? ret.substring(0,22) : ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public byte[] toByteArray()
    {
    long longOne = m_uuid.getMostSignificantBits();
    long longTwo = m_uuid.getLeastSignificantBits();
    byte[] ret;

    ret = new byte[]
        {
        (byte)(longOne >>> 56),
        (byte)(longOne >>> 48),
        (byte)(longOne >>> 40),
        (byte)(longOne >>> 32),   
        (byte)(longOne >>> 24),
        (byte)(longOne >>> 16),
        (byte)(longOne >>> 8),
        (byte) longOne,
        (byte)(longTwo >>> 56),
        (byte)(longTwo >>> 48),
        (byte)(longTwo >>> 40),
        (byte)(longTwo >>> 32),   
        (byte)(longTwo >>> 24),
        (byte)(longTwo >>> 16),
        (byte)(longTwo >>> 8),
        (byte) longTwo
        };

    return ret;
    }

/*********************************************************************/
/* main                                                              */
/*********************************************************************/

public static void main(String[] args)
    {
    System.out.println(new CompactUUID());
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

}


/*************************************************************/
/* Copyright (c) 1984-1996 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation. */
/*************************************************************/

/*********************************************************************/
/* Module : ubRqInfo                                                 */
/*                                                                   */
/*                                                                   */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.util;

import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Properties;

import com.progress.common.ehnlog.IAppLogger;
import com.progress.ubroker.util.ubMsg;

/*********************************************************************/
/*                                                                   */
/* Class ubRqInfo                                                    */
/*                                                                   */
/*********************************************************************/

public class ubRqInfo
{

/*********************************************************************/
/* Constants                                                         */
/*********************************************************************/

private static final int[] RQINFO_TYPES =
    {
    ubMsg.TLVTYPE_RQID
  , ubMsg.TLVTYPE_CLIENT_CONTEXT
  , ubMsg.TLVTYPE_OE_MAJOR_VERS
  , ubMsg.TLVTYPE_OE_MINOR_VERS
  , ubMsg.TLVTYPE_OE_MAINT_VERS
  , ubMsg.TLVTYPE_CLIENT_TYPE
  , ubMsg.TLVTYPE_APSVCL_VERS
  , ubMsg.TLVTYPE_PROCEDURE_NAME
  , ubMsg.TLVTYPE_CLIENTPRINCIPAL
//, ubMsg.TLVTYPE_ABLSESSIONID
  , ubMsg.TLVTYPE_SESSIONTYPE
  , ubMsg.TLVTYPE_NETBUFFERSIZE
  , ubMsg.TLVTYPE_BROKERSESSIONID
  , ubMsg.TLVTYPE_PASAGENTID
  , ubMsg.TLVTYPE_PASTHREADID
  , ubMsg.TLVTYPE_PASSESSIONID
  , ubMsg.TLVTYPE_ADAPTERTYPE
    };

private static final Integer[] RQINFO_INTEGER_TYPES =
    {
    new Integer(ubMsg.TLVTYPE_RQID)
  , new Integer(ubMsg.TLVTYPE_CLIENT_CONTEXT)
  , new Integer(ubMsg.TLVTYPE_OE_MAJOR_VERS)
  , new Integer(ubMsg.TLVTYPE_OE_MINOR_VERS)
  , new Integer(ubMsg.TLVTYPE_OE_MAINT_VERS)
  , new Integer(ubMsg.TLVTYPE_CLIENT_TYPE)
  , new Integer(ubMsg.TLVTYPE_APSVCL_VERS)
  , new Integer(ubMsg.TLVTYPE_PROCEDURE_NAME)
  , new Integer(ubMsg.TLVTYPE_CLIENTPRINCIPAL)
//, new Integer(ubMsg.TLVTYPE_ABLSESSIONID)
  , new Integer(ubMsg.TLVTYPE_SESSIONTYPE)
  , new Integer(ubMsg.TLVTYPE_NETBUFFERSIZE)
  , new Integer(ubMsg.TLVTYPE_BROKERSESSIONID)
  , new Integer(ubMsg.TLVTYPE_PASAGENTID)
  , new Integer(ubMsg.TLVTYPE_PASTHREADID)
  , new Integer(ubMsg.TLVTYPE_PASSESSIONID)
  , new Integer(ubMsg.TLVTYPE_ADAPTERTYPE)
    };


/*********************************************************************/
/* Static Data                                                       */
/*********************************************************************/


/*********************************************************************/
/* Static initializer block                                          */
/*********************************************************************/

static
    {
    }

/*********************************************************************/
/* Instance Data                                                     */
/*********************************************************************/


/* private HashMap<Integer,byte[]> map; */
private HashMap map;

/*********************************************************************/
/* Static methods                                                    */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public static boolean isDefined(int typ)
    {
    boolean ret = false;

    for (int i = 0; i < RQINFO_TYPES.length; i++)
        {
        if (typ == RQINFO_TYPES[i])
            {
            ret = true;
            break;
            }
        }

    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public static int[] rqInfoTypes()
    {
    return RQINFO_TYPES;
    }

/*********************************************************************/
/* Constructors                                                      */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public ubRqInfo()
    {
    /* map = new HashMap<Integer,byte[]>(); */
    map = new HashMap();
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public ubRqInfo(ubMsg msg)
    throws ubMsg.MsgFormatException
    {
    this();
    msg.extractRqInfo(this);
    }


/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public ubRqInfo(Properties props)
    throws ubMsg.MsgFormatException
    {
    this();

    for (int idx = 0; idx < RQINFO_INTEGER_TYPES.length; idx++)
        {
            Integer typ;
            byte[]  val;

            typ = RQINFO_INTEGER_TYPES[idx];
            val = (byte[])props.get(typ);
            if (val != null)
                setField(typ, val);
        }
    }


/*********************************************************************/
/* public methods                                                    */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public boolean exists(int typ)
    {
    boolean ret = false;

    ret = map.containsKey(new Integer(typ));

    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public boolean exists(Integer typ)
    {
    boolean ret = false;

    ret = map.containsKey(typ);

    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized byte[] getField(int typ)
    {
    byte[] val;
    /* val = map.get(new Integer(typ)); */
    val = (byte[])map.get(new Integer(typ));

    return val;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized byte[] getField(Integer typ)
    {
    byte[] val;
    /* val = map.get(typ); */
    val = (byte[])map.get(typ);

    return val;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized String getStringField(int typ)
    {
    byte[] val;
    val = (byte[])map.get(new Integer(typ));

    return (val == null) ?
               "" : 
               ubMsg.newNetString(val, 0, val.length-1); // drop null terminator
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized void setField(int typ, byte[] val)
    {
    map.put(new Integer(typ), val);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized void setStringField(int typ, String strVal)
    {
    byte[] tmp;
    byte[] val;

    /* convert the string to a byte array and append a NULL */
    tmp = ubMsg.newNetByteArray(strVal);
    val = new byte[tmp.length + 1];

    System.arraycopy(tmp, 0, val, 0, tmp.length);

    /* add a null terminator */
    val[tmp.length] = 0;
    map.put(new Integer(typ), val);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized void setField(Integer typ, byte[] val)
    {
    map.put(typ, val);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* add all fields from the source to this object.  */
/* overwrite any fields already in this object     */

public synchronized void setFields(ubRqInfo src)
    {
    byte[] val;
    Integer typ;

    /* for (Iterator<Integer> itr = src.map.keySet().iterator(); itr.hasNext(); ) */
    for (Iterator itr = src.map.keySet().iterator(); itr.hasNext(); )
        {
        /* typ = itr.next(); */
        typ = (Integer)itr.next();
        val = src.getField(typ);
        setField(typ, val);
        }
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* add fields in the source object that do not exist in this object.  */
/* DO NOT overwrite any fields already in this object                 */

public synchronized void addFields(ubRqInfo src)
    {
    byte[] val;
    Integer typ;

    /* for (Iterator<Integer> itr = src.map.keySet().iterator(); itr.hasNext(); ) */
    for (Iterator itr = src.map.keySet().iterator(); itr.hasNext(); )
        {
        /* typ = itr.next(); */
        typ = (Integer)itr.next();
        if (!this.exists(typ))
            {
            val = src.getField(typ);
            setField(typ, val);
            }
        }
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized byte[] serialize()
    {
    byte[] tlvbuf;
    int buflen;
    
    buflen = serializedLength(map.keySet());
    tlvbuf = new byte[buflen];
    serialize(tlvbuf, 0, buflen);

    return tlvbuf;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int serialize(ubMsg msg)
    {
    byte[] tlvbuf;
    int buflen;
    
    tlvbuf = serialize();
    buflen = tlvbuf.length;

    try
        {
        msg.setubTlvBuf(tlvbuf);
        }
    catch (ubMsg.MsgFormatException mfe)
        {
        /* I don't know if there's anything meaningful we can do here */
        buflen = -1;
        }

    return buflen;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int serializeXXX(byte[] tlvbuf, int ofst, int buflen)
    {
    byte[] val;
    int len;
    int fldlen;
    int i, idx;
    Integer typ;

    len = 0;
    idx = ofst;
    /* for (Iterator<Integer> itr = map.keySet().iterator(); itr.hasNext(); ) */
    for (Iterator itr = map.keySet().iterator(); itr.hasNext(); )
        {
        /* typ = itr.next(); */
        typ = (Integer)itr.next();
        val = getField(typ);
        fldlen = (val == null) ? 2 : val.length + 3;
        if (idx+fldlen+2 <= tlvbuf.length)
            {
            /* store the field type */
            ubMsg.setNetShort(tlvbuf, idx, (short)typ.intValue());
            /* store the length and value with NULL terminator */
            len = ubMsg.setTlvString(tlvbuf, idx + 2, val);
            idx += len + 2;
            }
        else return -1;
        }

    return idx;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* this routine assumes that the buffer provided is big enough */
/* caller needs to calculated the space requirements!!!        */
public synchronized int serialize(byte[] tlvbuf, int ofst, int buflen)
    {
    byte[] val;
    int len;
    int fldlen;
    int i, idx;
    Integer typ;

    len = 0;
    idx = ofst;
    /* for (Iterator<Integer> itr = map.keySet().iterator(); itr.hasNext(); ) */
    for (Iterator itr = map.keySet().iterator(); itr.hasNext(); )
        {
        /* typ = itr.next(); */
        typ = (Integer)itr.next();
        val = getField(typ);
        fldlen = (val == null) ? 0 : val.length;

        /* store the field type */
        ubMsg.setNetShort(tlvbuf, idx, (short)typ.intValue());
        idx += 2;

        /* store the field length */
        ubMsg.setNetShort(tlvbuf, idx, (short)fldlen);
        idx += 2;

        /* store the length and value with NULL terminator */
        if (val != null)
            System.arraycopy(val, 0, tlvbuf, idx, fldlen);
        idx += fldlen;
        }

    return idx;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized void print(String title,
                               IAppLogger log,
                               int logginglvl,
                               long logEntries,
                               int idxEntryType)
    {
    int ret;
    Integer typ;
    byte[] buf;

    if (log.ifLogIt(logginglvl, logEntries, idxEntryType))
        {
        log.logWithThisLevel(logginglvl, idxEntryType, title);
        /* for (Iterator<Integer> itr = map.keySet().iterator(); itr.hasNext(); ) */
        for (Iterator itr = map.keySet().iterator(); itr.hasNext(); )
            {
            /* typ = itr.next(); */
            typ = (Integer)itr.next();
            buf = getField(typ);
            log.logDump(logginglvl,idxEntryType, 
                            "key= " + typ.toString(), buf, buf.length
                            );
            }
        }
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* public synchronized Iterator<Integer> keys() */
public synchronized Iterator keys()
    {
    return map.keySet().iterator();
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

/* private int serializedLength(Set<Integer> keys) */
private int serializedLength(Set keys)
    {
    int i;
    int buflen;
    int fldlen;
    byte[] val;
    Integer typ;

    buflen = 0;
    /* for (Iterator<Integer> itr = keys.iterator(); itr.hasNext(); ) */
    for (Iterator itr = keys.iterator(); itr.hasNext(); )
        {
        typ = (Integer)itr.next();
        val = getField(typ);
        fldlen = (val == null) ? 0 : val.length;
        buflen += (2 +       // type
                   2 +       // length
                   fldlen);  // value
        }

    return buflen;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/



} /* class ubRqInfo */

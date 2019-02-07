//**************************************************************
//  Copyright (c) 2009 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************
/*
 * Filename:    TlvItem.java
 * Author  :    David Cleary
 *
 */

package com.progress.ubroker.util;

public class TlvItem 
{
    private short m_tlvType = 0;
    private String m_tlvData = null;
    private boolean m_persist = false;
    
    public TlvItem()
    {
    }
    
    public TlvItem(short tlvType, String tlvData, boolean persistent)
    {
        m_tlvType = tlvType;
        m_tlvData = tlvData;
        m_persist = persistent;
    }

    public short getTlvType() 
    {
        return m_tlvType;
    }

    public void setTlvType(short type) 
    {
        m_tlvType = type;
    }

    public String getTlvData() 
    {
        return m_tlvData;
    }

    public void setTlvData(String data) 
    {
        m_tlvData = data;
    }

    public boolean isPersistent() 
    {
        return m_persist;
    }

    public void setPersistent(boolean persist) 
    {
        m_persist = persist;
    }
    
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("TlvType = [");
     // buf.append(ubMsg.DESC_TLVTYPES[(m_tlvType-1)]);
        buf.append(ubMsg.getTlvDesc(m_tlvType));
        buf.append("] TlvData = [");
        buf.append(m_tlvData);
        buf.append("] Persistent = [");
        buf.append(m_persist);
        buf.append("]");
        
        return buf.toString();
    }

}

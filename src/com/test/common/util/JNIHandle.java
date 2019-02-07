//**************************************************************
//  Copyright (c) 1984-1998 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************
//
//  JNIHandle.java
//
//  This class can be used to hold a JNI address / pointer value inside of
//  Java code.  C-code functions typically allocate a context block of some
//  sort which needs to be "remembered".  Later this context block needs to
//  be freed.  This class allows a way to deal with this situation.
//
//
//  History:
//
//      02/26/98    B. Sadler   Created class.
//

package com.progress.common.util;

public class JNIHandle
{
    private long address;

    public JNIHandle() { setAddr(0); }
    public JNIHandle(long addr) { setAddr(addr); }

    public long getAddr() { return address; }
    public int getIntAddr() { return (int)address; }
    public void setAddr(long addr) { address = addr; }

}

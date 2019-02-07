//**************************************************************
//  Copyright (c) 1984-1999 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************
//
//  @(#)IUBLogfileInfo.java  1.0   9/27/99
//
//  The class provides the information for log file viewing feature, which is accessed 
//  commonly by the Progress Explorer client code and the adminServer plugin code.
//   
//
//  @author: Edith Tan, 9/27/99
//
//  History:
//
//
package com.progress.ubroker.util;

import java.rmi.*;
public interface IUBLogfileInfo extends java.rmi.Remote
{
  /* These three constants have been moved to MMCMsgBundle.java for translation purposes.
  public static String SRVR_LOG_DISP_NAME   = "Server Log";
  public static String BRKR_LOG_DISP_NAME   = "Broker Log";
  public static String LOG_DISP_NAME        = "Log File";
  */
    
  public static final String UB_LOG_DEFAULT_FILEVIEWER_CLASSNAME    
      = "com.progress.vj.ubroker.UBMMCServerLog";
  public static final String UB_LOG_FILEVIEWER_CLASSNAME            
      = "com.progress.vj.ubroker.UBMMCLog";
  public static final String UB_LOG_BRKR_FILEVIEWER_CLASSNAME       
      = "com.progress.vj.ubroker.UBMMCBrokerLog";

}



//**************************************************************
//  Copyright (c) 1997 - 1998 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************
//
// @(#)UBPreferenceProperties.java  2.0 11/2/99
//
// @author: Edith Tan, 10/15/98
//
// History:
//
//  11/02/99  est  toolWidth and toolHeight are no longer needed.
//
//  11/1/98   est  Added indication that default value is used
//
//  10/27/98  est  Added support for admSrvrRegisteredRetry and
//                 admSrvrRegisteredRetryInterval;
//
package com.progress.ubroker.util;

import com.progress.ubroker.tools.PropMgrPlugin;
import com.progress.ubroker.util.IPropConst;
import com.progress.ubroker.util.PropMgrUtils;

public class UBPreferenceProperties implements IPropConst
{
  //public int         m_toolWidth;
  //public int         m_toolHeight;
  public int         m_toolConnectSvcRetry;
  public int         m_toolGetSvcStatusRetry;
  public int         m_toolPingSvcRetry;
  public int         m_toolShutdownSvcConfirmRetry;
  public int         m_toolConnectSvcRetryInterval;
  public int         m_toolShutdownSvcConfirmRetryInterval;
  public int         m_admSrvrRegisteredRetry;
  public int         m_admSrvrRegisteredRetryInterval;
  public boolean     m_useDefault = false;
  
  public UBPreferenceProperties(PropMgrUtils pmuObject)
  {
    getAllPreferences(pmuObject);
  }


  public void refetchAll(PropMgrUtils pmuObject)
  {
    getAllPreferences(pmuObject);
  }

  public int getAdmSrvrRegisteredRetry()
  {
    return m_admSrvrRegisteredRetry;
  }

  public int getAdmSrvrRegisteredIntervalRetry()
  {
    return m_admSrvrRegisteredRetryInterval;
  }

  private void getAllPreferences(PropMgrUtils pmuObject)
  {
    m_useDefault = false;
    try
    {
      m_toolGetSvcStatusRetry = pmuObject.getPreferenceIntProperty(TOOL_GET_SVC_STATUS_RETRY);
    } 
    catch (Exception e) 
    {
      m_useDefault = true;
      m_toolGetSvcStatusRetry = TOOL_GET_STATUS_RETRY_DEF;
    }    
	  try
    {
      m_toolPingSvcRetry = pmuObject.getPreferenceIntProperty(TOOL_PING_SVC_RETRY);
    } 
    catch (Exception e) 
    {
      m_useDefault = true;
      m_toolPingSvcRetry = TOOL_PING_RETRY_DEF;
    }
    try
    {
      m_toolShutdownSvcConfirmRetry = pmuObject.getPreferenceIntProperty(TOOL_SHUTDOWN_SVC_CONFIRM_RETRY);
    } 
    catch (Exception e) 
    {
      m_useDefault = true;
      m_toolShutdownSvcConfirmRetry = TOOL_SHUTDOWN_SVC_CONFIRM_RETRY_DEF;
    }
    try
    {
      m_toolShutdownSvcConfirmRetryInterval = pmuObject.getPreferenceIntProperty(TOOL_SHUTDOWN_SVC_CONFIRM_RETRY_INTERVAL);
    } 
    catch (Exception e) 
    {
      m_useDefault = true;
      m_toolShutdownSvcConfirmRetryInterval = TOOL_SHUTDOWN_SVC_CONFIRM_RETRY_INTERVAL_DEF;
    }
    try
    {
      m_toolConnectSvcRetryInterval = pmuObject.getPreferenceIntProperty(TOOL_CONNECT_SVC_RETRY_INTERVAL);
    } 
    catch (Exception e) 
    {
      m_useDefault = true;
      m_toolConnectSvcRetryInterval = TOOL_CONNECT_SVC_RETRY_INTERVAL_DEF;
    }
    try
    {
      m_toolConnectSvcRetry = pmuObject.getPreferenceIntProperty(TOOL_CONNECT_SVC_RETRY);
    } 
    catch (Exception e) 
    {
      m_useDefault = true;
      m_toolConnectSvcRetry = TOOL_CONNECT_SVC_RETRY_DEF;
    }
    /*
    try
    {
      m_toolWidth = pmuObject.getPreferenceIntProperty(TOOL_WIDTH);
    } 
    catch (Exception e) 
    {
      m_useDefault = true;
      m_toolWidth = TOOL_WIDTH_DEF;
    }
    try
    {
      m_toolHeight = pmuObject.getPreferenceIntProperty(TOOL_HEIGHT);
    } 
    catch (Exception e) 
    {
      m_useDefault = true;
      m_toolHeight = TOOL_HEIGHT_DEF;
    }
    */
    try
    {
      m_admSrvrRegisteredRetry = pmuObject.getPreferenceIntProperty(ADMSRVR_REGISTER_RETRY);
    } 
    catch (Exception e) 
    {
      m_useDefault = true;
      m_admSrvrRegisteredRetry = ADMSRVR_REGISTER_RETRY_DEF;
    }
    
    try
    {
      m_admSrvrRegisteredRetryInterval = pmuObject.getPreferenceIntProperty(ADMSRVR_REGISTER_RETRY_INTERVAL);
    } 
    catch (Exception e) 
    {
      m_useDefault = true;
      m_admSrvrRegisteredRetryInterval = ADMSRVR_REGISTER_RETRY_INTERVAL_DEF;
    }
  }

}



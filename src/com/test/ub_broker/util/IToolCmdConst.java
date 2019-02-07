//*************************************************************
//  Copyright (c) 1984-1998 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************
//
//
//  @(#)IToolCmdConst.java  2.1  12/24/99
//
//  This class listed available remote commands that a tool instance
//  can instruct its remote plugin to perform on its behalf.
//
//  @author: Edith Tan,  12/15/98
//
//  History:
//
//    11/24/99  est  Change command PUT_PREF_PROP to PREF_PROP_CHANGED.
//
//    07/21/99  est  Added VALIDATE_ONE_PROPERTY and NS_INST_REFERENCES
//
//    04/29/99  est  Change GET_GUI_SCHEMA_FN to GET_GUI_SCHEMA_PROP_FN.
//
//    04/22/99  est  Change FETCH_PSS to FETCH_RPM.
//   
//    04/05/99  est  Added support for schema loading and fetching.
//
//    12/28/98  est  Added command to validate and save configuration. 
//
//***********************************************************************************
// NOTE: Please do the following for future enhancement of this class:
// This class is for releases up to 91a.  This class serves as a base class. 
// Any future enhancement for this class must be done in its extended class.
// This basically means that if you need to add new data members to this class, you 
// need to create a new class that extends this base class, and add all the new 
// definitions to the newly extended class. 
//
// There are two companion classes: ToolRemoteCmdStatus.java and ToolRemoteCmdDescriptor.java
// that may need to be extended as well.  The same rule applies to these two classes.
//***********************************************************************************
//
package com.progress.ubroker.util;

import java.util.*;
import java.util.Enumeration;
import java.io.*;
import java.rmi.*;

public interface IToolCmdConst
{
  public final int TOOL_CMD_VERSION = 3;
  //
  //  commands common to all tool instances
  //
  public final int COMMON_CMD_MIN   = 1;
  public final int GET_SVC_CONFIG   = COMMON_CMD_MIN;
  public final int SAVE_SVC_CONFIG  = GET_SVC_CONFIG + 1;
  public final int REPLACE_PROP     = SAVE_SVC_CONFIG + 1;
  public final int GET_PREF_PROP    = REPLACE_PROP + 1;
  public final int PREF_PROP_CHANGED= GET_PREF_PROP +1;
  public final int GET_NS_NAMES     = PREF_PROP_CHANGED + 1;
  public final int GET_AR_NAMES     = GET_NS_NAMES + 1;
  public final int GET_SSL_ALIAS_NAMES = GET_AR_NAMES + 1;
  public final int VAL_SAV_SVC_CFG  = GET_SSL_ALIAS_NAMES + 1;
  public final int LOAD_GUI_SCHEMA  = VAL_SAV_SVC_CFG + 1;
  public final int FETCH_RPM        = LOAD_GUI_SCHEMA + 1; // a.k.a remote PropMgr reference
  public final int GET_GUI_SCHEMA_PROP_FN  = FETCH_RPM + 1;
  public final int GET_PROPERTY_VALUE = GET_GUI_SCHEMA_PROP_FN + 1;
  public final int VALIDATE_ONE_PROPERTY = GET_PROPERTY_VALUE + 1;
  public final int NS_INST_REFERENCES = VALIDATE_ONE_PROPERTY + 1;
  public final int COMMON_CMD_MAX     = 50;

  //
  // commands specific to various flavors of GUI TOOLs 
  //
  public final int GUITOOL_CMD_MIN      = 100; 
  public final int START_SVC            = GUITOOL_CMD_MIN;
  public final int STOP_SVC             = START_SVC + 1;
  public final int PING_SVC             = STOP_SVC + 1;
  public final int ADD_NEW_GUI_TOOL     = PING_SVC + 1;
  public final int REMOVE_GUI_TOOL      = ADD_NEW_GUI_TOOL + 1;
  public final int GET_UB_SUM_STAT_LBL  = REMOVE_GUI_TOOL + 1;
  public final int GET_UB_SUM_STAT_DATA = GET_UB_SUM_STAT_LBL + 1;
  public final int GET_UB_STAT_LBL      = GET_UB_SUM_STAT_DATA + 1;
  public final int GET_UB_STAT_DATA     = GET_UB_STAT_LBL + 1;
  public final int TRIM_UBSRVR_BY       = GET_UB_STAT_DATA + 1;
  public final int ADD_UBSRVR           = TRIM_UBSRVR_BY + 1;
  public final int GET_NS_SUM_STAT_DATA = ADD_UBSRVR + 1;
  public final int GET_NS_STAT_DATA     = GET_NS_SUM_STAT_DATA + 1;
  public final int GET_NS_LOCATION      = GET_NS_STAT_DATA + 1;
  public final int GUITOOL_CMD_MAX      = 200;

  public final int MAX_CMD = GUITOOL_CMD_MAX;


  //
  // command execution status code
  //
  public final int CMD_NOTEXEC = 0;
  public final int CMD_SUCCESS = 1;
  public final int CMD_FAILED  = -1;
  public final int CMD_INVALID = -2;
  public final int CMD_MISSING_ARG = -3;
  
  //
  // filenames array index
  //
  public final int SCHEMA_FN_IDX = 0;
  public final int PROP_FN_IDX = 1;
}
 
// END OF FILE
 

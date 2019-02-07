//**************************************************************
//  Copyright (c) 1984-1998 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************
//
//
//  @(#)ToolRemoteCmdDescriptor.java  2.1  12/23/99
//
//  The descriptor serves as a remote serializable object that describes the
//  detail of a remote command that the tool client side wants the AdminServer
//  plugin to perform on its behalf.  This serves all tool instances: ubroker,
//  nameserver and messangers.
//
//  @author: Edith Tan,  12/15/98
//
//  History:
//
//    12/23/99  est  Added support for getting nameServer location property and 
//                   adding a new nameServer.
//
//    11/24/99  est  Added support for handling command PREF_PROP_CHANGED.
//
//    08/17/99  est  Fetch arglist for VALIDATE_ONE_PROPERTY correctly.
//
//    07/27/99  est  Added support for VALIDATE_ONE_PROPERTY and NS_INST_REFERENCES.
//
//    4/22/99   est  Added support for making packets of new commands to support
//                   gui schema infrastructure.
//
//    12/28/98  est  Added makeValidateSaveCfgPkt() and fetchValidateSaveCfgArg().
//
//***********************************************************************************
// NOTE: Please do the following for future enhancement of this class:
// This class is for releases up to 91a.  This class serves as a base class. 
// Any future enhancement for this class must be done in its extended class.
// This basically means that if you need to add new data members or new methods
// to this class, you need to create a new class that extends this base class, 
// and add all the new definitions to the newly extended class. 
//
// There are two companion classes: IToolCmdConst.java and ToolRemoteCmdStatus.java
// that may need to be extended as well.  The same rule applies to these two classes.
//***********************************************************************************
//
package com.progress.ubroker.util;
import com.progress.ubroker.util.IToolCmdConst;
import com.progress.common.property.IPropertyManagerRemote;

import java.util.*;
import java.util.Enumeration;
import java.io.*;
import java.rmi.*;

public class ToolRemoteCmdDescriptor implements Serializable, IToolCmdConst
{
  public static  int  m_version = TOOL_CMD_VERSION;
  public int     m_command = 0;
 
  public String  m_toolInstancePropSpec = null;
  public Vector  m_cmdArgsList = null;
  public byte[]  m_fileContent = null;

  public ToolRemoteCmdDescriptor()
  {
    resetAllVars();
  }


  //
  //  The getter methods
  //
  public int getCommand()
  {
    return (m_command);
  }

  public String getPropSpec()
  {
    return(m_toolInstancePropSpec);
  }

  public Enumeration getArgsList()
  {
    return (m_cmdArgsList.elements());
  }

  public int getVersion()
  {
    return (m_version);
  }
  
  //
  //  The set value helper methods
  //
  public void setCommand(int cmdValue)
  {
     m_command = cmdValue;
  }

  public void setPropSpec(String instPropSpec)
  {
    m_toolInstancePropSpec = instPropSpec;
  }

  public void addArgsList(Object argObj)
  {
    m_cmdArgsList.addElement(argObj);
  }

  public void addArgsList(Object[] argList)
  {
    for (int i = 0; i < argList.length; i++)
    {
      addArgsList(argList[i]);
    }
  }

  public void resetArgList()
  {
    if (m_cmdArgsList == null || m_cmdArgsList.size() > 0)
      m_cmdArgsList = new Vector();
  }

  //
  // packet descriptor for commands that are common to all tool instances.
  //  
  public void makeGetSvcCfgPkt(String svcGrpPath)
  {
    fillDescriptorContent(GET_SVC_CONFIG, svcGrpPath);
  }


  public void makeSaveSvcCfgPkt(char[] propertiesStream)
  {
    fillDescriptorContent(SAVE_SVC_CONFIG, null, (Object)propertiesStream);
  }

  public void makeValidateSaveCfgPkt(PropertiesSaveDescriptor propSavObj)
  {
    fillDescriptorContent(VAL_SAV_SVC_CFG, null, (Object)propSavObj);
  }

  public void makeStartSvcPkt(String fullPropGrpSpec)
  {
    fillDescriptorContent(START_SVC, fullPropGrpSpec);
  }


  public void makeStopSvcPkt(String fullPropGrpSpec)
  {
    fillDescriptorContent(STOP_SVC, fullPropGrpSpec);
  }


  public void makePingSvcPkt(String fullPropGrpSpec)
  {
    fillDescriptorContent(PING_SVC, fullPropGrpSpec);
  }

  public void makeGetNSNamesPkt()
  {
    fillDescriptorContent(GET_NS_NAMES, null);
  }

  public void makeGetAdminRoleNames()
  {
    fillDescriptorContent(GET_AR_NAMES, null);
  }

  public void makeGetSSLAliasNames()
  {
    fillDescriptorContent(GET_SSL_ALIAS_NAMES, null);
  }

  public void makeReplacePropertiesPkt(String fullPropPath, char[] propertiesStream)
  {
    fillDescriptorContent(REPLACE_PROP, fullPropPath, (Object) propertiesStream);
  }

  public void makeGetPrefPropPkt()
  {
    fillDescriptorContent(GET_PREF_PROP, null);
  }

  public void makePrefPropChangedPkt()
  {
    fillDescriptorContent(PREF_PROP_CHANGED, null);
  }
  
  
  public void makeLoadGUISchemaPkt()
  {
    fillDescriptorContent(LOAD_GUI_SCHEMA, null);
  }
  
  public void makeGetGUISchemaPropFnPkt()
  {
    fillDescriptorContent(GET_GUI_SCHEMA_PROP_FN, null);
  } 
  
  public void makeFetchRPMPkt()
  {
//    System.out.println("ToolRemoteCmdDescriptor: makeFetchRPMPkt()");
    fillDescriptorContent(FETCH_RPM, null);
  }
  

  //
  // command descriptors specific for ubroker instances
  //
  public void makeAddNewGuiToolPkt(String newSvcName)
  {
    fillDescriptorContent(ADD_NEW_GUI_TOOL, "", (Object)newSvcName);
  }

  public void makeAddNewGuiToolPkt(String newSvcName, String newArg1)
  {
    fillDescriptorContent(ADD_NEW_GUI_TOOL, "", new Object[]{(Object)newSvcName,
                          (Object)newArg1});
  }

  public void makeAddNewGuiToolPkt(String newSvcName,
                                   String newArg1,
                                   String newArg2)
  {
    fillDescriptorContent(ADD_NEW_GUI_TOOL, "", new Object[]{(Object)newSvcName,
                          (Object)newArg1, (Object)newArg2});
  }

  public void makeRemoveGuiToolPkt( String svcName, String svcGroupPath)
  {
    fillDescriptorContent(REMOVE_GUI_TOOL, svcGroupPath, (Object)svcName);
  }
  
  public void makeGetSummaryStatusLblPkt(String fullPropGroupSpec)
  { 
    fillDescriptorContent(GET_UB_SUM_STAT_LBL, fullPropGroupSpec);
  }

  public void makeGetSummaryStatusDataPkt(String fullPropGroupSpec)
  {
    fillDescriptorContent(GET_UB_SUM_STAT_DATA, fullPropGroupSpec);
  }

  public void makeGetDetailStatusLblPkt(String fullPropGroupSpec)
  {
    fillDescriptorContent(GET_UB_STAT_LBL, fullPropGroupSpec);
  }

  public void makeGetDetailStatusDataPkt(String fullPropGroupSpec)
  {
    fillDescriptorContent(GET_UB_STAT_DATA, fullPropGroupSpec);
  }

  public void makeTrimSrvrByPkt(int numServerToTrim, String fullPropGroupSpec)
  {
    fillDescriptorContent(TRIM_UBSRVR_BY, fullPropGroupSpec, (Object)(new Integer(numServerToTrim)) );
  }

  public void makeAddNewSrvrsPkt(int numNewServers, String fullPropGroupSpec)
  {
    fillDescriptorContent(ADD_UBSRVR, fullPropGroupSpec, (Object)(new Integer(numNewServers)) );
  }
  
  public void makeValidOnePropPkt(String fullPropGroupSpec, String propertyName, String userInputValue)
  {
    Object[] argList = new Object[2];
    argList[0] = (Object)propertyName;
    argList[1] = (Object)userInputValue;
//    System.out.println("makeValidateOnePropPkt: validating " + fullPropGroupSpec + propertyName +
//                       userInputValue);
    fillDescriptorContent(VALIDATE_ONE_PROPERTY, fullPropGroupSpec, argList);    
  }
  
  public void makeNSInstRefPkt(String fullPropGroupSpec, String nsInstName)
  {
    fillDescriptorContent(NS_INST_REFERENCES, fullPropGroupSpec,
                         (Object)nsInstName);    
  }
  
  //
  // command descriptors that are name server specific
  //
  public void makeGetNSDetailStatPkt(String fullPropGroupSpec)
  {
    fillDescriptorContent(GET_NS_STAT_DATA, fullPropGroupSpec);
  }

  public void makeGetNSSummaryStatusPkt(String fullPropGroupSpec)
  {
    fillDescriptorContent(GET_NS_SUM_STAT_DATA, fullPropGroupSpec);
  }
  
  public void makeGetNSLocPropPkt(String nsInstName, String fullPropGroupSpec)
  {
    fillDescriptorContent(GET_NS_LOCATION, fullPropGroupSpec,
                         (Object)nsInstName);
  }


  //
  // command argument getter methods
  //
  public char[] fetchSaveSvcCfgArg()
  {
    return( (char[])(get1stCmdArg()));
  }

  public PropertiesSaveDescriptor fetchValidateSaveCfgArg()
  {
    return((PropertiesSaveDescriptor)get1stCmdArg());
  }

  public char[] fetchReplacePropertiesArg()
  {
    return( (char[])(get1stCmdArg()) );
  }

  public int fetchPingSvcArg()
  {
    if (m_cmdArgsList.size() == 0) 
      return 0;
    else 
      return(((Integer)(get1stCmdArg())).intValue());
  }


  public String fetchAddNewGuiToolArg()
  {
    return( (String)(get1stCmdArg()));
  }

  public String fetchAddNewGuiToolArg2()
  {
    Enumeration argList = getArgsList();
    String newName = (String)(argList.nextElement());
    String addNewArgStr = (String)(argList.nextElement());
    return( addNewArgStr);
  }

  public String fetchAddNewGuiToolArg3()
  {
    Enumeration argList = getArgsList();
    String newName = (String)(argList.nextElement());
    String addNewArgStr = (String)(argList.nextElement());
    String addNewArgStr2 = (String)(argList.nextElement());
    return( addNewArgStr2);
  }
  
  public String fetchRemoveGuiToolArg( )
  {
    return( (String)(get1stCmdArg()));
  }
  
  public String fetchSchemaGroupArg()
  {
    String schemaGroup = (String)(get1stCmdArg());
 //System.out.println("schema group = " + schemaGroup);
    return( (String)schemaGroup);
  }

  public int fetchTrimSrvrByArg()
  {
    return( ((Integer)(get1stCmdArg())).intValue());
  }

  public int fetchAddNewSrvrsArg()
  {
    return( ((Integer)(get1stCmdArg())).intValue());
  }

  
  public String fetchValOnePropNameArg()
  {
    return( (String)(get1stCmdArg()));
  }
  
  public String fetchValOnePropValArgStr()
  {
    Enumeration argList = getArgsList();
    String propName = (String)(argList.nextElement());
    String valueString = (String)(argList.nextElement());
    return( valueString);
  }
  
  public int fetchValidOnePropValArgInt()
  {
    return( ((Integer)(get1stCmdArg())).intValue());    
  }
  
  
  public String fetchNSInstRefArg()
  {
    return( (String)(get1stCmdArg()));
  }

  public String fetchGetNSLocPropArg()
  {
    return( (String)(get1stCmdArg()));
  }
  
  //
  // other utility methods
  //
  public void  fillDescriptorContent(int cmd, String propFullSpec)
  {
    resetAllVars();
    setCommand(cmd);
    setPropSpec(propFullSpec);
  }

  public void  fillDescriptorContent(int cmd, String propFullSpec, Object arg)
  {
    resetAllVars();
    setCommand(cmd);
    setPropSpec(propFullSpec);
    addArgsList(arg);
  }

  public void  fillDescriptorContent(int cmd, String propFullSpec, Object[] arglist)
  {
    resetAllVars();
    setCommand(cmd);
    setPropSpec(propFullSpec);
    addArgsList(arglist);
  }


  private void resetAllVars()
  {
    m_command = 0;
    m_toolInstancePropSpec = null;
    resetArgList();
  }


  private Object get1stCmdArg()
  {
    Enumeration argList = getArgsList();
    return( (Object)(argList.nextElement()) );
  }

}






 
// END OF FILE
 

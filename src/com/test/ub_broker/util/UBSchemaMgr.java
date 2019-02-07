//*************************************************************
//  Copyright (c) 1984-1999 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************
//
//
// @(#)UBSchemaMgr.java    2.1     9/8/99
//
// Manages meta schema and gui meta schema access for all
// ubroker/nameserver/messenger instances.  All methods in this class are class
// methods.  Within a jvm, the gui schema file should only be loaded once anyway,
// each instance can query its property valueset as needed in instance level.
//
// @author: Edith Tan,  4/2/99
//
// History:
//
//   09/08/99  est  Redirect all loggings to UBToolsMsg.
//
//   04/22/99  est  Simplify the process of constructing a PVS. 
//
package com.progress.ubroker.util;

import com.progress.common.property.IPropertyManagerRemote;
import com.progress.common.guiproperties.PropertyValueSet;
import com.progress.common.guiproperties.GUIMetaSchema;
import com.progress.common.guiproperties.PropertyCategory;
import com.progress.ubroker.tools.IYodaRMI;
import com.progress.ubroker.tools.UBToolsMsg;
import com.progress.ubroker.util.ToolRemoteCmdDescriptor;
import com.progress.ubroker.util.ToolRemoteCmdStatus;
import java.rmi.*;
import java.io.*;

public class UBSchemaMgr implements Serializable
{
  public static boolean     m_schemaLoaded = false;
  static String             m_schemaFileSpec = null;
  static GUIMetaSchema      m_gmsObj = null;
  
  //
  //  Construct this with default schema filename, Nay... maybe we don't want to do this.
  //
  public UBSchemaMgr()
  {
/*
    m_schemaFileSpec = DEF_UBROKER_SCHEMA_FILENAME;
    try
    {
      UBSchemaMgr.load(m_schemaFileSpec);
    }
    catch(Exception e)
    {
    }
*/
  }   

  //
  //  construct this with a specific filename
  //
  public UBSchemaMgr( String schemaFileFullSpec)
  {
    //
    //  for temporary usage only.  Eventually we will ask the remote object for the 
    //  file stream of the schema file.
    //
    m_schemaFileSpec = schemaFileFullSpec;
    try
    {
      UBSchemaMgr.load(m_schemaFileSpec);
    }
    catch(Exception e)
    {
    }
  }   
 
  //
  // We load the schema file only if we haven't done it before.  As it is now, this 
  // only knows how to deal with a single schema file, and it does not know how to handle
  // the loading of the multiple schema file for ubroker property file.  
  //
  public static synchronized boolean load(String schemaFilename)
  { 
    try
    {
      if (!m_schemaLoaded)
      {
        m_schemaFileSpec = schemaFilename;
        m_gmsObj = GUIMetaSchema.instantiate(m_schemaFileSpec);
        m_schemaLoaded = true;
        UBToolsMsg.logMsg("schema file: " + schemaFilename + " is loaded.");
      }  
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return (m_schemaLoaded);
  }
  
 
  //
  // Get the property value set for a property group(i.e. serialized 
  // PropertyTransferObject). We currently don't cache the value set for any group.  
  // So potentially, the vaule set for the same group can be fetched repeatedly.
  //
  public static PropertyValueSet getPVS(String groupFullSpec, IPropertyManagerRemote rpm) 
  {
    int groupSpecStop = groupFullSpec.lastIndexOf((int)'.');
    String instanceSpec = groupFullSpec.substring(groupSpecStop+1);
    String groupSpec = groupFullSpec.substring(0,groupSpecStop);
    return(getPVS(groupSpec, instanceSpec, rpm));
                  
                  
  }
  public static PropertyValueSet getPVS(String groupSpec, String instanceSpec,
                                        IPropertyManagerRemote rpm) 
  {
    PropertyValueSet pvs = null;
    
    if (m_schemaLoaded)
    {
      //System.out.println("in UBSchemaMgr.getPVS (" + groupSpec + "," + instanceSpec + ")");
      try
      {
        pvs = new PropertyValueSet(m_gmsObj, rpm, groupSpec, instanceSpec);
        return(pvs);
      }
      catch (Exception exp) 
      {
        exp.printStackTrace();
      }
    }
    return null;
  }

  //
  // 
  //
  public static PropertyValueSet loadPVS(IYodaRMI yodaRMIGlue, 
                                         IPropertyManagerRemote rpm,
                                         String groupFullSpec) 
    throws RemoteException
  {
    PropertyValueSet       pvs = null;
    //System.out.println("in UBSchemaMgr.loadPVS (" + groupFullSpec + ")");
    try
    {     
      if (m_schemaLoaded && rpm != null)
      {
        try
        {
          pvs = getPVS(groupFullSpec, rpm);
         return(pvs);
        }
        catch (Exception e) 
        {
          e.printStackTrace();
        }
      }
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
    return (null);
  }
  
  public static PropertyValueSet loadPVS(IYodaRMI yodaRMIGlue, IPropertyManagerRemote rpm,
                                         String groupSpec, 
                                         String instanceSpec, String pvsGroupSpec) 
    throws RemoteException
  {
    PropertyValueSet       pvs = null;
    try
    {
      if (m_schemaLoaded && rpm != null)
      {
        try
        {
          pvs = getPVS(groupSpec, instanceSpec, rpm);
          return(pvs);
        }
        catch (Exception e) 
        {
          e.printStackTrace();
        }
      }
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
    return (null);
  }
  
  
  
  //
  // Find the presentation category for the specified catagory name.
  //
  public static PropertyCategory findPresentationCategory(String categoryName)
  {
    UBToolsMsg.logMsg("Finding presentation category: " + categoryName);
    try
    {
      return(m_gmsObj.findCategory(categoryName));
    }
    catch (Exception e)
    {
      e.printStackTrace();   
    }
    return (null);
  }
/*  
  public static IPropertyManagerRemote getRPM(IYodaRMI yodaRMIGlue)
    throws RemoteException
  { 
    if (m_rpm != null)
      return m_rpm;
    try
    {
      ToolRemoteCmdDescriptor cmdPkt = new ToolRemoteCmdDescriptor(); 
      cmdPkt.makeFetchRPMPkt();
      ToolRemoteCmdStatus cmdStatusObj = yodaRMIGlue.doRemoteToolCmd(cmdPkt);    
      m_rpm = cmdStatusObj.fetchRPMStatus();
      return m_rpm;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return null;
  }
*/   
}

 
// END OF FILE
 

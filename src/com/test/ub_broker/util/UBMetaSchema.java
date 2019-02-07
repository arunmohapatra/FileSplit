//*************************************************************
//  Copyright (c) 1984-1999 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************
//
//
// @(#)UBMetaSchema.java       9/28/00
//
// Manages the access to meta schema for all ubroker/nameserver/messenger instances.
// It mainly serves as a transparent layer for fetching the PropertyTransferObject of a
// property group.
//
// @author: Edith Tan,  4/2/99
//
// History:
//
//   9/28/00 est  Added static method cleanupSchemaObject(), specifically
//                for facilitating the adminserver warmStart process due to
//                external update to the property files.
//
//   9/8/99  est  Code cleanup
//
package com.progress.ubroker.util;

import com.progress.common.property.PropertyManager;
import com.progress.common.property.MetaSchema;
import com.progress.common.property.PropertyTransferObject;
import com.progress.ubroker.util.*;

import java.rmi.Remote;
import java.rmi.RemoteException;

// Class Obsolete:  The (Ubroker) PropertyManager(s) are capable of instantiating and
// loading schema's, so there's no need to do this sperately.
public class UBMetaSchema
{
  private static final String MODULE_NAME = "UBMetaSchema";
  static MetaSchema m_metaSchema = null;  // one and only within the life time of AdminServer
  String            m_schemaFileSpec = null;
  PropertyManager   m_pm = null;

  private static synchronized void createMetaSchema(PropertyManager pm, String schemaFileSpec)
  {
    try
    {
      if (m_metaSchema == null)
      {
        m_metaSchema = new MetaSchema(pm, schemaFileSpec);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static synchronized void cleanupSchemaObject()
  {
    m_metaSchema = null;
  }

  //
  //  Construct this with default schema filename
  //
  public UBMetaSchema(PropertyManager pm)
  {
    this(pm, IPropFilename.DEF_UBROKER_SCHEMA_FILENAME );
  }

  //
  //  construct this with a specific filename
  //
  public UBMetaSchema(PropertyManager pm, String schemaFileFullSpec )
  {
    m_schemaFileSpec = schemaFileFullSpec;
    m_pm = pm;
    makeMetaSchema();
  }



  //
  // get a property subset(PSS) for a group from the meta schema
  //
  public PropertyTransferObject getPSS(String groupFullSpec) throws RemoteException
  {
    if (m_pm == null)
      return (null);
    try
    {
      PropertyTransferObject pto  = new PropertyTransferObject( m_pm, groupFullSpec );
      return (pto);
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  //
  // Provide a way to refresh the property manager cached internally
  //
  public void refreshPM(PropertyManager newPM)
  {
    m_pm = newPM;
  }


  private void makeMetaSchema()
  {
    //
    // Try loading the meta schema and the gui meta schema from the schema file.
    //
    try
    {
      UBMetaSchema.createMetaSchema(m_pm, m_schemaFileSpec);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

}


// END OF FILE


//**************************************************************
//  Copyright (c) 1984-1998 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************
//
//
//  @(#)PropertiesSaveDescriptor.java  2.1  9/15/99
//
//  This serializable class contains the various attributes for the properties
//  to be saved for a specific instance of ubroker or nameserver.  It also indicated 
//  whether any property is supposed to be deleted, or the environment subgroup has things deleted. 
////
//  @author: Edith Tan,  11/11/98
//
//  History:
//
//    9/15/99   est  Remove reference to PropListInfo data member and methods for accessing
//                   the data member.
//
//    2/11/99   est  When envlist is null, mark the environment property for removal.
//
//    1/4/99    est  Guard aginst a null envlist.
//
//    11/20/98  est  Remove the bagage of a full property collection stream.  Keep track of 
//                   individual changes to the instance rather.
//
package com.progress.ubroker.util;

import com.progress.ubroker.util.IPropConst;
import java.util.*;
import java.util.Enumeration;
import java.io.*;
import java.rmi.*;


public class PropertiesSaveDescriptor implements Serializable, IPropConst
{
  Vector       m_propList = null;          // A list of property names that changed values
  Vector       m_valueList = null;         // their new changed values
  Vector       m_propertiesToRemove = null;// properties to be deleted
  String       m_envSubGrp = null;         // name of the env. sub group
  String[]     m_appSvcList = null;        // a list of appservice names
  String       m_propGroupName = null;     // full property group path for the instance
  String       m_svcName = null;           // just the service name without property group
  boolean      m_delProperty = false;      // whether there is property to be removed
  boolean      m_addNew = false;           // whether this defines a new instance 
    

  public PropertiesSaveDescriptor()
  {
    m_propList = new Vector();
    m_valueList = new Vector();
    m_propertiesToRemove = new Vector();
  }

  public void setEnvSubGrpName (String envSubGrpName)
  {
    m_envSubGrp = envSubGrpName;
  }

  public String getEnvSubGrpName ()
  {
//System.out.println("getting EnvSubGrpName: " + m_envSubGrp);
    return(m_envSubGrp);
  }


  public void setPropGroupName(String propGroupName)
  {
    m_propGroupName = propGroupName;
  }

  public String getPropGroupName()
  {
//System.out.println("getting m_propGroupName: " + m_propGroupName);
    return(m_propGroupName);
  }
  
  
  public void addPropertyToRemove(String propertyName)
  {
//System.out.println("adding property to remove: " + propertyName);
    m_propertiesToRemove.addElement(propertyName);
    m_delProperty = true;
  }

  public Vector getPropList()
  {
    return m_propList;
  }

  public void setPropList(Vector propList)
  {
    m_propList = propList; 
  }

  public String[] getPropListStrings()
  {
    return(vectorToStrings(m_propList));
  }

  public void  setValueList(Vector valueList)
  {
    m_valueList = valueList;
  }

  public Vector getValueList()
  {
    return m_valueList;
  }


  public String[] getValueListStrings()
  {
    return(vectorToStrings(m_valueList));
  }

  public String[] getAppSvcList()
  {
    return( m_appSvcList );
  }

  public void setAppSvcList(String[] appSvcList)
  {
    m_appSvcList = appSvcList;
  }


  public void setAddNew()
  {
    m_addNew = true;
  }
    
  public boolean isNewInstance()
  {
    return(m_addNew);
  }

  public String[] getPropertiesToRemove()
  {
    return(vectorToStrings(m_propertiesToRemove));
  }



  public void setSvcName(String svcName)
  {
    m_svcName = svcName;
  }


  public String getSvcName()
  {
    return (m_svcName);
  }

  public boolean removeProperties()
  {
    return (m_delProperty);
  }

  private String[] vectorToStrings(Vector vectorObj)
  {
    if (vectorObj != null)
    {
      String[] retList = new String[vectorObj.size()];
      vectorObj.copyInto(retList);
      return (retList);
    }
    return null;
  }
}






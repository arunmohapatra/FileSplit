//**************************************************************
//  Copyright (c) 1984-1998 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************
//
//
//  @(#)PropGroupDescriptor.java  0.1  7/8/98
//
//  A descriptor for the property group in ubroker.properties
//
//  @author: Edith Tan,  7/8/98
//
//  History:
//
package com.progress.ubroker.util;

import com.progress.ubroker.util.*;

//
// For a fully-qualified property group path, the descriptor provides
// two pieces of information:
//   - a service type string that is relevant to the property group
//   - the full spec of the property group.  
// This descriptor is used by findUBPersonStrForSvcName(), a method
// in PropMgrUtils, as a returned object to the caller.  Essentially,
// for a given service name string, e.g. NS1, this class would contain
// two data members:
//   svcTypeStr = NS
//   fullPropSec = NameServer.NS1
//
public class PropGroupDescriptor
{
  private String svcTypeStr;
  private String fullPropSpec;

  public PropGroupDescriptor(String fullPropGroupPath)
  {
    svcTypeStr = PropMgrUtils.getSvcTypeStr(fullPropGroupPath);
    fullPropSpec = fullPropGroupPath;
  }

  public String getSvcTypeStr()
  {
    return(svcTypeStr);
  }

  public String getfullPropSpec()
  {
    return(fullPropSpec);
  }
}







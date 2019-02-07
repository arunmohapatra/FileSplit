//**************************************************************
//  Copyright (c) 1984-1997 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************
//
//
//  @(#)IPUMessages.java  0.2   10/27/98
//
//  The cousin file of PUMessages.  We just remap the numeric message code 
//  to more meaningful names.  It's a bit wasteful, but hopefully, it does not 
//  add too much overhead and at the same time it makes the PropMgrUtils class 
//  more maintainable.
//
//  @author: Edith Tan,  2/23/98
//
//  History:
//
//   10/27/98  est  Use the correct copy of puMsg.
//
//    3/12/98  est  Added REMOVE_GROUP_ERROR.
//
//    2/26/98  est  Added SAVE_ALL_ERROR.
//
package com.progress.ubroker.util;

import com.progress.message.puMsg;

public interface IPUMessages extends puMsg
{

  public final static long   CANT_LOAD_PROPERTIES_REMOTE = puMSG001;
         /* Cannot load configuration of a remote %s service process: %s. 
            (8251)                                                            */

  public final static long   ENUM_GRP_PROP_ERROR         = puMSG002;
         /* Enumerate property group error: %s. (8252)                        */

  public final static long   CANT_LOAD_PROP_FILE         = puMSG003;
         /* Cannot load property file: %s. (8253)                             */

  public final static long   CANT_FIND_PROP_GRP          = puMSG004;
         /* Cannot find property group %s. (8254)                             */

  public final static long   CANT_GET_PROP_COLLECTION    = puMSG005;
         /* Cannot get property collection for %s. (8255)                     */

  public final static long   CANT_PUT_PROPERTY           = puMSG006;
         /* Cannot update property %s to new value: %s. (8256)                */

  public final static long   CANT_GET_PROPERTY_VALUE     = puMSG007;
         /* Cannot get the value of property %s. (8257)                       */

  public final static long   CANT_PUT_PROPERTY_VALUE     = puMSG008;
         /* Cannot update the value of property %s to %s. (8258)              */

  public final static long   CANT_PUT_PROPERTIES         = puMSG009;
         /* Cannot update the property collection for %s (8259)               */

  public final static long   SAVE_GROUP_ERROR            = puMSG010;
         /* Cannot save property group %s to property file, %s. (8260)        */

  public final static long   SAVE_GROUP_REMOTE_ERROR     = puMSG011;
         /* Cannot save property group %s for remote connection. (8261)       */

  public final static long   CANT_GET_PARENT_GROUPS      = puMSG012;
         /* Cannot get parent groups. (8262)                                  */

  public final static long   SAVE_ALL_ERROR              = puMSG013;
         /* Cannot save all properties: %s. (8263)                            */

  public final static long   REMOVE_GROUP_ERROR          = puMSG014;
         /* Cannot remove properties for group: %s. (8264)                    */

} 


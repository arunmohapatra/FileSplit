//**************************************************************
//  Copyright (c) 1984-2015 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************
//
//
//  @(#)PropFilename.java  2.1   6/21/99
//
//  Determines the full path of the ubroker property filename.
//
//  @author: Edith Tan,  2/17/98
//
//  History:
//
//    6/16/99  est  Schema file is located at com.progress.schema
//
//    4/2/99   est  Added support for schema filename.
//
//    8/5/98   est  Property file now resides in sub directory, properties,
//                  under the installation directory.
//
//    4/24/98  est  If a file spec is used to construct the filename
//                  then the fullpath for the property file would use
//                  the user specified file spec, and not to assume the
//                  name of the property file with default file name
//                  at all.
//    9/4/07   dmh  This entire file is somewhat senseless.  Removed everything
//                  except for method "getFullPath".  Rewrote method so that
//                  it is a static that simply returns the location of the
//                  ubroker.properties file.  This method is called ONLY by
//                  the ubroker clients.
//
package com.progress.ubroker.util;

import java.io.File;

import com.progress.chimera.adminserver.IAdminServerConst;

public class PropFilename implements IPropFilename
{
    static final String DEFAULT_PROPERTY_DIR = "properties";

    public static String getFullPath()
    {
        String installDir = ( System.getProperty("UbrokerPropsDir") != null ) ?
                     System.getProperty("UbrokerPropsDir") :
                     IAdminServerConst.INSTALL_DIR;

        String propFileName = installDir +
        			File.separator +
                     DEFAULT_PROPERTY_DIR +
                     File.separator +
                     DEF_UBROKER_PROP_FILENAME;
        return propFileName;
    }

    public static String getSchemaFilename()
    {
        return DEF_UBROKER_SCHEMA_FILENAME;
    }
}


// END OF FILE



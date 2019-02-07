/***************************************************************
/   Copyright (c) 1998-2014 by Progress Software Corporation
/   All rights reserved.  No part of this program or document
/   may be  reproduced in  any form  or by  any means without
/   permission in writing from Progress Software Corporation.
/ *************************************************************
/ 
/   SonicAppService.java
/ 
/   Contains utility methods that are specific  to the Sonic 
/   Application Service name.
/ *************************************************************/

package com.progress.common.util;

public class SonicAppService {
    private static char[] invalidChars = 
        {'$', 
         '@', 
         '{', 
         '}', 
         '/', 
         '\\', // backslash
         '\"', // double quote
         ',', 
         ' ',
         '#',
         '%',
         '&',
         '+',
         '>',
         '<',
         '~',
         ':'};
    // Validate an AppService Name.  Basically, this means making sure there 
    // are no "invalid" characters - i.e., characters that will cause us troubles!
    public static boolean validateName(String name)
    {
        int ix;
        for (ix = 0; ix < invalidChars.length; ix++)
        {
            if (name.indexOf(invalidChars[ix]) != -1)
                return false;
        }
        return true;
    }

    
}

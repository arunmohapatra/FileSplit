/***************************************************************
/   Copyright (c) 1998-2013 by Progress Software Corporation
/   All rights reserved.  No part of this program or document
/   may be  reproduced in  any form  or by  any means without
/   permission in writing from Progress Software Corporation.
/ *************************************************************
/ 
/   AppService.java
/ 
/   Contains utility methods that pertain to the Application
/	Service name.
/ *************************************************************/

package com.progress.common.util;

public class AppService
{
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
		 '.',
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
 


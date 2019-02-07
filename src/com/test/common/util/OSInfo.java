// **************************************************************
// Copyright (c) 2015 by Progress Software Corporation
// All rights reserved. No part of this program or document
// may be reproduced in any form or by any means without
// permission in writing from Progress Software Corporation.
// *************************************************************

package com.progress.common.util;

/**
 * utility class to hold basic info on OS name. Basically just constants for
 * which OS this is running on. Convenience instead of calling
 * System.getProperty("os.name").equalsIgnoreCase(...).
 * 
 * @author mbaker
 * @since 11.6
 *
 */
public interface OSInfo {

	/**
	 * name of operating system. Taken from {@link System}
	 * getProperty("os.name")
	 */
	public String	OS_NAME		= System.getProperty("os.name");
	/**
	 * true if this is running on windows
	 */
	public boolean	IS_WINDOWS	= OS_NAME.startsWith("Windows");
	/**
	 * true if this is running on SunOS
	 */
	public boolean	IS_SUNOS	= OS_NAME.equalsIgnoreCase("SunOS");
	/**
	 * true if this is running on HPUX
	 */
	public boolean	IS_HPUX		= OS_NAME.equalsIgnoreCase("HP-UX");
	/**
	 * true if this is running on Linux
	 */
	public boolean	IS_LINUX	= OS_NAME.equalsIgnoreCase("Linux");
	
	/**
	 * true if this is running on AIX
	 */
	public boolean IS_AIX 	= OS_NAME.equalsIgnoreCase("AIX");

}

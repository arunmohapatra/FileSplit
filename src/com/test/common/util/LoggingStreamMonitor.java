//**************************************************************
//  Copyright (c) 2012 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************

package com.progress.common.util;

import com.progress.common.log.ProLog;

/**
 * a simple stream monitor that redirects output to the adminserver log file
 * @author mbaker
 *
 */
public class LoggingStreamMonitor implements IStreamMonitor {

	private String subsys;
	private int level;

	public LoggingStreamMonitor(String subsys, int level) {
		this.subsys = subsys;
		this.level = level;
	}

	@Override
	public void dataReceived(String line) {
		if (line != null && line.length() > 0) {
			ProLog.logd(subsys, level, line);
		}

	}

}

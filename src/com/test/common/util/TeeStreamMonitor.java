//**************************************************************
//  Copyright (c) 2013-2014 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************

package com.progress.common.util;

/**
 * Simple class to split the data to separate stream monitors.  Used
 * to send stdout data to both a log file and to be consumed as output
 * from a process.
 * 
 * 
 * @author mbaker
 * @since 11.4
 *
 */
public class TeeStreamMonitor implements IStreamMonitor {

	private final IStreamMonitor monitor;
	private final IStreamMonitor monitor2;
	
	
	public TeeStreamMonitor(IStreamMonitor monitor, IStreamMonitor monitor2) {
		this.monitor = monitor;
		this.monitor2 = monitor2;
	}
	
	@Override
	public void dataReceived(String line) {
		monitor.dataReceived(line);
		monitor2.dataReceived(line);
	}

}

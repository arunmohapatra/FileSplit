//**************************************************************
//  Copyright (c) 2013 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************

package com.progress.common.util;

import java.io.PrintStream;

/**
 * Simple utility class that accepts any print stream as the destination for the monitor, and
 * sends any data recevied out to the print stream.
 * 
 * This was initially used in adminservertype to assist with debugging the output from the adminserver which
 * is normally started as a daemon.
 * 
 * @author mbaker
 *
 */
public class PrintStreamMonitor implements IStreamMonitor {

	private final PrintStream out;
	
	public PrintStreamMonitor(PrintStream out) {
		this.out = out;
	}
	
	@Override
	public void dataReceived(String line) {
		out.println(line);
	}

}

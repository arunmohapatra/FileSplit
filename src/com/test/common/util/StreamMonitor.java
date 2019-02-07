//**************************************************************
//  Copyright (c) 2012 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************

package com.progress.common.util;

import java.util.ArrayList;
import java.util.List;

/**
 * the list tream monitor handles data from a threaded stream reader
 * and stores it in a list.
 * 
 * @author mbaker
 *
 */
public class StreamMonitor implements IStreamMonitor {

	private List<String> list;
	
	public StreamMonitor() {
		this.list = new ArrayList<String>();
	}
	
	public StreamMonitor (List<String> list) {
		this.list = list;
	}

	@Override
	public void dataReceived(String line) {
		list.add(line);
	}
	
	public List<String> getData() {
		return list;
	}

}

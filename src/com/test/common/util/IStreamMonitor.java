//**************************************************************
//  Copyright (c) 2012 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************


package com.progress.common.util;

/**
 * a stream monitor is used with the threadedstreamreader class
 * to handle data read from the stream.  The stream monitor determines
 * what happens to the data after it is read from the stream.
 * 
 * @author mbaker
 *
 */
public interface IStreamMonitor {

	public void dataReceived(String line);
}

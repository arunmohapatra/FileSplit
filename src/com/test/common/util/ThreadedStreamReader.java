// **************************************************************
// Copyright (c) 2007-2012 by Progress Software Corporation
// All rights reserved. No part of this program or document
// may be reproduced in any form or by any means without
// permission in writing from Progress Software Corporation.
// *************************************************************
//
// ThreadedStreamReader.java
//
// This class is used to read streams from a process
// so that the execution of the process does not block.
//
// A copy of this class is also found in the source code of OpenEdge Architect.
//
// Usage:
// - Create Monitor for stdout and stderr
// - Create instance of ThreadedStreamReader for stdout and stderr using
// process.getInputStream() and process.getErrorStream() respectively.
// - Call setBlockingIO(true) on both threads
// - Start threads
// - Call process.waitFor()
// - Call join() methods on both threads
//
//
// History:
//
// 07/06/2007 Edsel Garcia Created class.
//
package com.progress.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ThreadedStreamReader extends Thread {
	private static final int		DEFAULT_READ_SLEEP	= 50;

	private final InputStream		inputStream;

	private boolean					isFinished			= false;

	private boolean					blockingIO			= false;

	private final IStreamMonitor	monitor;

	private long					readSleep			= DEFAULT_READ_SLEEP;

	public ThreadedStreamReader(InputStream inputStream, IStreamMonitor monitor, long readSleep) {
		this.inputStream = inputStream;
		this.monitor = monitor;
		this.readSleep = readSleep;
	}

	public ThreadedStreamReader(InputStream inputStream, IStreamMonitor monitor) {
		this(inputStream, monitor, DEFAULT_READ_SLEEP);
	}

	/**
	 * Indicates that the reader should finish. The isFinished flag is set which
	 * causes the reader to stop.
	 * 
	 * @throws Exception
	 */
	public void finish() throws Exception {
		synchronized (this) {
			isFinished = true;
			this.notifyAll();
		}
	}
	
	public synchronized boolean isFinished() {
		return isFinished;
	}

	public void run() {
		BufferedReader r = null;
		try {
			r = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			while (!isFinished()) {
				if (blockingIO || r.ready()) {
					line = r.readLine();
					if (line == null)
						break;
					monitor.dataReceived(line);
				} else
					synchronized (this) {
						if (!isFinished) {
							try {
								this.wait(readSleep);
							} catch (InterruptedException e) {
								// //ignore it
							}
						}

					}
			}
		} catch (IOException e) {
			// ignore it
		} finally {
			if (r != null) {
				try {
					r.close();
				} catch (IOException e) {
					// ignore it
				}
			}
			synchronized(this) {
				isFinished = true;
			}
		}
	}

	public void setBlockingIO(boolean flag) {
		blockingIO = flag;
	}
}

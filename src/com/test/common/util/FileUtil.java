//**************************************************************
//  Copyright (c) 2013 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************

package com.progress.common.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import com.progress.common.log.ProLog;

/**
 * File utility class that makes a few things such as copying files convenient.
 * 
 * @author mbaker
 *
 */
public class FileUtil {

	/**
	 * copy all bytes from one file to another.
	 * @param fromFileName
	 * @param toFileName
	 */
	public static boolean copyFile(String fromFileName, String toFileName) {
		boolean isSuccess = false;
		RandomAccessFile in = null;
		RandomAccessFile out = null;
		try {
			File fromFile = new File(fromFileName);
			File toFile = new File(toFileName);
			in = new RandomAccessFile(fromFile, "r");
			out = new RandomAccessFile(toFile, "rw");
			FileChannel inChannel = in.getChannel();
			FileChannel outChannel = out.getChannel();
			long transferred = inChannel.transferTo(0, fromFile.length(), outChannel);
			
			if (transferred == fromFile.length()) {
				isSuccess = true;
			}
		} catch (FileNotFoundException ex) {
			ProLog.logd("SystemPluginProxy", 4, "Cannot copy " + fromFileName + " to " + toFileName + ";");
			ProLog.logd("SystemPluginProxy", 4, fromFileName + " not found: " + ex.getMessage());
		} catch (IOException e) {
			ProLog.logd("SystemPluginProxy", 4, "Cannot copy " + fromFileName + " to " + toFileName + ";");
			ProLog.logd("SystemPluginProxy", 4, "Error: " + e.getMessage());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// ignore it
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// ignore it
				}
			}
		}
		return isSuccess;
	}

}

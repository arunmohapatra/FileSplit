/* ***********************************************************/
/* Copyright (c) 2006 by Progress Software Corporation       */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission in writing from Progress Software Corporation. */
/*************************************************************/

package com.progress.common.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author core
 * 
 *         this class implements buffered random access file.
 * 
 *         code used was from
 * 
 *         http://www.javaworld.com/javatips/jw-javatip26_p.html
 * 
 *         NOTE: this only buffers the read() method. It does not buffer any
 *         writes.
 * 
 */
public class BufferedRandomAccessFile extends RandomAccessFile {

	// allow this to be configurable in case someone wants to increase it
	private static final int DEFAULT_BUFFER_SIZE = Integer.getInteger("psc.adminserver.filereader.buffersize", 1024 * 64);
	
	// larger read buffers perform better up to a certain point
	int buf_size = DEFAULT_BUFFER_SIZE;

	byte buffer[];

	int buf_end = 0;

	int buf_pos = 0;

	long real_pos = 0;

	public BufferedRandomAccessFile(File file, String mode) throws IOException {
		super(file, mode);
		invalidate();
		buffer = new byte[buf_size];
	}

	public BufferedRandomAccessFile(File file, String mode, int bufsize)
			throws IOException {
		super(file, mode);
		invalidate();
		buf_size = bufsize;
		buffer = new byte[buf_size];
	}

	public BufferedRandomAccessFile(String filename, String mode, int bufsize)
			throws IOException {
		super(filename, mode);
		invalidate();
		buf_size = bufsize;
		buffer = new byte[buf_size];
	}

	public final int read() throws IOException {
		if (buf_pos >= buf_end) {
			if (fillBuffer() < 0)
				return -1;
		}
		if (buf_end == 0) {
			return -1;
		} else {
			return buffer[buf_pos++] & 0xFF;
		}
	}

	private int fillBuffer() throws IOException {
		int n = super.read(buffer, 0, buf_size);
		if (n >= 0) {
			real_pos += n;
			buf_end = n;
			buf_pos = 0;
		}
		return n;
	}

	private void invalidate() throws IOException {
		buf_end = 0;
		buf_pos = 0;
		real_pos = super.getFilePointer();
	}

	public int read(byte b[], int off, int len) throws IOException {
		int leftover = buf_end - buf_pos;
		if (len <= leftover) {
			System.arraycopy(buffer, buf_pos, b, off, len);
			buf_pos += len;
			return len;
		}
		for (int i = 0; i < len; i++) {
			int c = this.read();
			if (c != -1)
				b[off + i] = (byte) c;
			else {
				return i;
			}
		}
		return len;
	}

	public int read(byte b[]) throws IOException {
		return read(b, 0, b.length);
	}

	public long getFilePointer() throws IOException {
		long l = real_pos;
		return (l - buf_end + buf_pos);
	}

	public void seek(long pos) throws IOException {
		int n = (int) (real_pos - pos);
		if (n >= 0 && n <= buf_end) {
			buf_pos = buf_end - n;
		} else {
			if (n >= 0 && n <= buf_size && ((real_pos - buf_size) >= 0)) {
				super.seek(real_pos - buf_size);
				invalidate();
				fillBuffer();
				buf_pos = buf_size - n;
			} else {
				super.seek(pos);
				invalidate();
			}
		}
	}

	public void close() throws IOException {
		buffer = null;
		super.close();
	}
}

/*************************************************************/
/* Copyright (c) 2010 by Progress Software Corporation       */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation. */
/*************************************************************/

package com.progress.ubroker.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class MessageCompressor
{

	/**
	 * Compress an array of bytes down to a smaller array.  The smallest number of bytes that     
	 * can be compressed is 256 (due to limits in algorithm).  The compression level indicates    
     * how much work is done to compress the given bytes.  A default of 1 is usually sufficient   
     * to gain a significant compression level depending on the data in the array.  Valid values  
     * for the compression level are 1 through 9 inclusive.  The higher the compression level the 
     * longer it may take to compress the data                                                    
	 * @param msgBuf
	 * @param offset
	 * @param length
	 * @param compressionLevel
	 * @return
	 * @throws IOException
	 */
	public static byte[] compressBytes(byte[] msgBuf, int offset, int length, int compressionLevel) throws IOException {
    	ByteArrayOutputStream outStream = new ByteArrayOutputStream(length);
    	DeflaterOutputStream zStream = new DeflaterOutputStream(outStream, new Deflater(compressionLevel));
        zStream.write(msgBuf, offset, length);
        zStream.finish();
        zStream.close();
        return outStream.toByteArray();

    }


    /**
     * Decompress a chunk of bytes from the given message buffer, starting at the given offset,
     * and for the given length.  This will return a byte array that represents the uncompressed
     * bytes.
     * @param msgBuf
     * @param offset
     * @param length
     * @return
     * @throws IOException
     */
    public static byte[] unCompressBytes(byte[] msgBuf, int offset, int length) throws IOException
    {
        ByteArrayInputStream inStream = new ByteArrayInputStream(msgBuf, offset, length);
        InflaterInputStream zInStream = new InflaterInputStream(inStream);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(length);
        copyStream(zInStream, outStream);
        return outStream.toByteArray();
    }
    
    static void copyStream(InputStream input, OutputStream output) throws IOException
    {
        byte[] buffer = new byte[2000];
        int len;
        while ((len = input.read(buffer, 0, 2000)) > 0)
        {
            output.write(buffer, 0, len);
        }
        output.flush();
    }    
}

package com.split.file.simple;

import java.util.LinkedHashMap;
import java.util.Map;

public class Tester {


    public static void main(String[] args){

        String fileName = "D:\\Personal\\c++_Br_3.0.4_EAST.zip";

        /*
          Chunk size of : 256 bytes = 1024 * 256
          Chunk size of : 512 bytes = 1024* 512
          Chunk Size of : 1 MB = 1024 * 1024
        */
        Splitting splitting = new Splitting(fileName,1024*512);

        LinkedHashMap<String, byte[]> chunks = splitting.split();

       for(Map.Entry chunk : chunks.entrySet()){
            String chunkNo = (String)chunk.getKey();
            byte[] bytes = (byte[])chunk.getValue();
            System.out.println(chunkNo+"  "+bytes.length);
        }

        Merging merging = new Merging();
        merging.mergeAndCreateFile(chunks,"C:\\work\\test.zip");
    }
}

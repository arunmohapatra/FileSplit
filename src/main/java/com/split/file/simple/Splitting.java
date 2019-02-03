package com.split.file.simple;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedHashMap;

public class Splitting {

    private String fileName;
    private int chunkSize = 1024;

    public Splitting(String filename){
        this.fileName = filename;
    }

    public Splitting(String filename,int chunkSize){
        this.fileName = filename;
        this.chunkSize = chunkSize;
    }


    LinkedHashMap<String, byte[]> split(){
        int chunkNo = 1;
        LinkedHashMap<String,byte[]> chunks = new LinkedHashMap<>();
        try( FileInputStream fileInputStream = new FileInputStream(new File(this.fileName))){
            byte[] buffer = new byte[this.chunkSize];
            int read = 0;
            while( (read = fileInputStream.read(buffer) )> 0){
                String chunkNoStr = new Integer(chunkNo).toString();
                chunks.put(chunkNoStr, Arrays.copyOf(buffer,read));
                Arrays.fill(buffer,(byte)0); /* reset buffer */
                chunkNo++;
            }
        }
        catch(IOException exp){

        }
        return chunks;
    }
}

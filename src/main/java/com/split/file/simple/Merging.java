package com.split.file.simple;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class Merging {

    public Merging(){

    }

    /*Merge the chunks */
    void mergeAndCreateFile(LinkedHashMap<String,byte[]> chunks, String fileName){

        try (FileOutputStream fileOutputStream
                     = new FileOutputStream(new File(fileName))){
            for(Map.Entry chunk : chunks.entrySet()){
                 byte[] bytes = (byte[])chunk.getValue();
                fileOutputStream.write(bytes);
            }
        }
        catch(IOException exp){

        }
    }


}

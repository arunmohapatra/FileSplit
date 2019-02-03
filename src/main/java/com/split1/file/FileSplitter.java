/* File Split */
package com.split1.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.Scanner;

public class FileSplitter {


    private File f;

    public FileSplitter(File f) {
        if (f == null)
            throw new IllegalArgumentException("File must be not null!");
        this.f = f;
        System.out.println("File Length (KB): " + f.length() / 1024.0);
    }

    public boolean split(long size) {
        if (size <= 0)
            return false;

        try {
            int parts = ((int) (f.length() / size));
            long flength = 0;
            if (f.length() % size > 0)
                parts++;

            File[] fparts = new File[parts];

            FileInputStream fis = new FileInputStream(f);
            FileOutputStream fos = null;

            for (int i = 0; i < fparts.length; i++) {
                fparts[i] = new File(f.getPath() + ".part." + i);
                fos = new FileOutputStream(fparts[i]);

                int read = 0;
                long total = 0;
                byte[] buff = new byte[1024];
                int origbuff = buff.length;
                while (total < size) {
                    read = fis.read(buff);
                    if (read != -1) {
                        buff = FileEncoder.invertBuffer(buff, 0, read);
                        total += read;
                        flength += read;
                        fos.write(buff, 0, read);
                    }
                    if (i == fparts.length - 1 && read < origbuff)
                        break;
                }

                fos.flush();
                fos.close();
                fos = null;
            }

            fis.close();
            // f.delete();
            f = fparts[0];

            System.out.println("Length Readed (KB): " + flength / 1024.0);
            return true;
        } catch (Exception ex) {
            System.out.println(ex);
            System.out.println(ex.getLocalizedMessage());
            System.out.println(ex.getStackTrace()[0].getLineNumber());
            ex.printStackTrace();
            return false;
        }
    }

    public boolean split(int parts) {
        if (parts <= 0)
            return false;

        return this.split(f.length() / parts);
    }

    public boolean unsplit() {
        try {
            LinkedList<File> list = new LinkedList<File>();
            boolean exists = true;
            File temp = null;
            File dest = new File(f.getPath().substring(0,
                    f.getPath().lastIndexOf(".part")));
            FileInputStream fis = null;
            FileOutputStream fos = new FileOutputStream(dest);
            int part = 0;
            long flength = 0;
            String name = null;
            while (exists) {
                name = f.getPath();
                name = name.substring(0, name.lastIndexOf(".") + 1) + part;
                temp = new File(name);

                exists = temp.exists();
                if (!exists)
                    break;

                fis = new FileInputStream(temp);
                byte[] buff = new byte[1024];

                int read = 0;
                while ((read = fis.read(buff)) > 0) {
                    buff = FileEncoder.invertBuffer(buff, 0, read);
                    fos.write(buff, 0, read);
                    if (read > 0)
                        flength += read;
                }
                fis.close();
                fis = null;
                temp.delete();
                part++;
            }

            fos.flush();
            fos.close();
            f = dest;
            System.out.println("Length Writed: " + flength / 1024.0);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        System.out.print("Split? [true/false]: ");
        boolean split = Boolean.parseBoolean(scan.next());

        if (split) {
            File file = new File("C:/work/java.exe");
            FileSplitter splitter = new FileSplitter(file);
            System.out.println("splitter.split(3): " + splitter.split(3));
        } else {
            File file = new File(
                    "C:/work/java.exe.part.0");
            FileSplitter splitter = new FileSplitter(file);
            System.out.println("splitter.unsplit(): " + splitter.unsplit());
        }
    }

}


